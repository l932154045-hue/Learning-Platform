package com.learning.course.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.learning.common.core.exception.BizException;
import com.learning.common.core.page.PageReq;
import com.learning.common.core.page.PageResp;
import com.learning.common.core.result.ResultCode;
import com.learning.course.cache.CourseCacheService;
import com.learning.course.dto.req.CourseSaveReq;
import com.learning.course.dto.req.VideoSaveReq;
import com.learning.course.dto.resp.CourseListItemVO;
import com.learning.course.entity.ChapterVideo;
import com.learning.course.entity.Course;
import com.learning.course.entity.CourseCategory;
import com.learning.course.mapper.ChapterVideoMapper;
import com.learning.course.mapper.CourseCategoryMapper;
import com.learning.course.mapper.CourseMapper;
import com.learning.course.service.CourseAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseAdminServiceImpl implements CourseAdminService {

    private final CourseMapper courseMapper;
    private final CourseCategoryMapper categoryMapper;
    private final ChapterVideoMapper chapterVideoMapper;
    private final CourseCacheService courseCacheService;

    @Override
    public PageResp<CourseListItemVO> listAllCourses(PageReq req) {
        Page<Course> page = new Page<>(req.getPageNum(), req.getPageSize());
        IPage<Course> iPage = courseMapper.searchAllCourses(page, null, null);

        List<CourseListItemVO> list = iPage.getRecords().stream().map(course -> {
            CourseListItemVO vo = new CourseListItemVO();
            BeanUtils.copyProperties(course, vo);
            CourseCategory cat = categoryMapper.selectById(course.getCategoryId());
            if (cat != null) vo.setCategoryName(cat.getName());
            return vo;
        }).collect(Collectors.toList());

        return PageResp.of(list, iPage.getTotal(), req.getPageNum(), req.getPageSize());
    }

    @Override
    @Transactional
    public Long createCourse(CourseSaveReq req) {
        Course course = new Course();
        BeanUtils.copyProperties(req, course);
        course.setStatus(0); // Draft by default
        course.setSaleCount(0);
        courseMapper.insert(course);
        log.info("管理员创建课程成功: id={}, title={}", course.getId(), req.getTitle());
        return course.getId();
    }

    @Override
    @Transactional
    public void updateCourse(Long id, CourseSaveReq req) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BizException(ResultCode.COURSE_NOT_FOUND);
        }
        BeanUtils.copyProperties(req, course, "id", "saleCount", "status", "createdAt", "updatedAt");
        course.setId(id);
        courseMapper.updateById(course);
        courseCacheService.evict(id);
        log.info("管理员更新课程成功: id={}", id);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BizException(ResultCode.COURSE_NOT_FOUND);
        }
        course.setStatus(-1); // Soft delete
        courseMapper.updateById(course);
        courseCacheService.evict(id);
        log.info("管理员删除课程成功: id={}", id);
    }

    @Override
    @Transactional
    public void updateCourseStatus(Long id, Integer status) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BizException(ResultCode.COURSE_NOT_FOUND);
        }
        course.setStatus(status);
        courseMapper.updateById(course);
        courseCacheService.evict(id);
        log.info("管理员更新课程状态: id={}, status={}", id, status);
    }

    @Override
    @Transactional
    public void addVideo(VideoSaveReq req) {
        // Verify course exists
        Course course = courseMapper.selectById(req.getCourseId());
        if (course == null) {
            throw new BizException(ResultCode.COURSE_NOT_FOUND);
        }
        ChapterVideo video = new ChapterVideo();
        BeanUtils.copyProperties(req, video);
        video.setStatus(1);
        chapterVideoMapper.insert(video);
        courseCacheService.evict(req.getCourseId());
        log.info("管理员添加视频: courseId={}, video={}", req.getCourseId(), req.getVideoTitle());
    }

    @Override
    @Transactional
    public void updateVideo(Long id, VideoSaveReq req) {
        ChapterVideo video = chapterVideoMapper.selectById(id);
        if (video == null) {
            throw new BizException(40016, "视频不存在");
        }
        BeanUtils.copyProperties(req, video, "id", "status");
        video.setId(id);
        chapterVideoMapper.updateById(video);
        courseCacheService.evict(video.getCourseId());
        log.info("管理员更新视频: id={}", id);
    }

    @Override
    @Transactional
    public void deleteVideo(Long id) {
        ChapterVideo video = chapterVideoMapper.selectById(id);
        if (video == null) {
            throw new BizException(40016, "视频不存在");
        }
        chapterVideoMapper.deleteById(id);
        courseCacheService.evict(video.getCourseId());
        log.info("管理员删除视频: id={}", id);
    }

    // Category operations

    @Override
    @Transactional
    public Long createCategory(String name, Long parentId, Integer sortOrder) {
        CourseCategory category = new CourseCategory();
        category.setName(name);
        category.setParentId(parentId != null ? parentId : 0L);
        category.setSortOrder(sortOrder != null ? sortOrder : 0);
        categoryMapper.insert(category);
        courseCacheService.evictCategoryTree();
        log.info("管理员创建分类: id={}, name={}, parentId={}", category.getId(), name, parentId);
        return category.getId();
    }

    @Override
    @Transactional
    public void updateCategory(Long id, String name, Integer sortOrder) {
        CourseCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BizException(40017, "分类不存在");
        }
        if (name != null) {
            category.setName(name);
        }
        if (sortOrder != null) {
            category.setSortOrder(sortOrder);
        }
        categoryMapper.updateById(category);
        courseCacheService.evictCategoryTree();
        log.info("管理员更新分类: id={}, name={}", id, name);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        CourseCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BizException(40017, "分类不存在");
        }
        // Check if category has subcategories
        Long count = categoryMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CourseCategory>()
                        .eq(CourseCategory::getParentId, id));
        if (count != null && count > 0) {
            throw new BizException(40018, "该分类下有子分类，无法删除");
        }
        categoryMapper.deleteById(id);
        courseCacheService.evictCategoryTree();
        log.info("管理员删除分类: id={}", id);
    }

    @Override
    public Long getCourseCount() {
        return courseMapper.selectCount(null);
    }
}
