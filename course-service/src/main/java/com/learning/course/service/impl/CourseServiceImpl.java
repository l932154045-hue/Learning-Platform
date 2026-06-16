package com.learning.course.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.learning.common.core.exception.BizException;
import com.learning.common.core.page.PageResp;
import com.learning.common.core.result.ResultCode;
import com.learning.course.cache.CourseCacheService;
import com.learning.course.dto.req.CourseSearchReq;
import com.learning.course.dto.resp.*;
import com.learning.course.entity.ChapterVideo;
import com.learning.course.entity.Course;
import com.learning.course.entity.CourseCategory;
import com.learning.course.mapper.ChapterVideoMapper;
import com.learning.course.mapper.CourseCategoryMapper;
import com.learning.course.mapper.CourseMapper;
import com.learning.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final CourseCategoryMapper categoryMapper;
    private final ChapterVideoMapper chapterVideoMapper;
    private final CourseCacheService courseCacheService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CATEGORY_TREE_KEY = "course:category:tree";
    private static final String HOT_TOP10_KEY = "course:hot:top10";
    private static final Set<String> ALLOWED_SORT = Set.of("price_asc", "price_desc", "newest", "saleCount_desc");

    @Override
    public PageResp<CourseListItemVO> searchCourses(CourseSearchReq req) {
        String sort = ALLOWED_SORT.contains(req.getSort()) ? req.getSort() : "saleCount_desc";

        // Resolve category IDs: if a category is selected, include its children
        List<Long> categoryIds = null;
        if (req.getCategoryId() != null) {
            categoryIds = new ArrayList<>();
            categoryIds.add(req.getCategoryId());
            collectChildCategoryIds(req.getCategoryId(), categoryIds);
        }

        Page<Course> page = new Page<>(req.getPageNum(), req.getPageSize());
        IPage<Course> iPage = courseMapper.searchCourses(page,
                req.getKeyword(), categoryIds,
                req.getPriceMin(), req.getPriceMax(), sort);

        List<CourseListItemVO> list = iPage.getRecords().stream().map(course -> {
            CourseListItemVO vo = new CourseListItemVO();
            BeanUtils.copyProperties(course, vo);
            return vo;
        }).collect(Collectors.toList());

        return PageResp.of(list, iPage.getTotal(), req.getPageNum(), req.getPageSize());
    }

    @Override
    public CourseDetailVO getCourseDetail(Long courseId) {
        return courseCacheService.getCourseDetail(courseId, id -> {
            Course course = courseMapper.selectById(id);
            if (course == null) {
                throw new BizException(ResultCode.COURSE_NOT_FOUND);
            }

            CourseDetailVO vo = new CourseDetailVO();
            BeanUtils.copyProperties(course, vo);

            // Fill category name
            if (course.getCategoryId() != null) {
                CourseCategory category = categoryMapper.selectById(course.getCategoryId());
                if (category != null) {
                    vo.setCategoryName(category.getName());
                }
            }

            // Fill chapter videos
            List<ChapterVideo> videos = chapterVideoMapper.selectByCourseId(id);
            List<ChapterVideoVO> chapterVOS = videos.stream().map(v -> {
                ChapterVideoVO cv = new ChapterVideoVO();
                BeanUtils.copyProperties(v, cv);
                return cv;
            }).collect(Collectors.toList());
            vo.setChapters(chapterVOS);

            return vo;
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<CourseCategoryVO> getCategoryTree() {
        // Try Redis first
        Object cached = redisTemplate.opsForValue().get(CATEGORY_TREE_KEY);
        if (cached != null) {
            return (List<CourseCategoryVO>) cached;
        }

        // Query MySQL and build tree
        List<CourseCategory> allCategories = categoryMapper.selectList(null);
        List<CourseCategoryVO> tree = buildCategoryTree(allCategories, 0L);

        // Cache for 1 hour
        redisTemplate.opsForValue().set(CATEGORY_TREE_KEY, tree, Duration.ofHours(1));
        return tree;
    }

    private List<CourseCategoryVO> buildCategoryTree(List<CourseCategory> allCategories, Long parentId) {
        List<CourseCategoryVO> result = new ArrayList<>();
        for (CourseCategory cat : allCategories) {
            Long catParentId = cat.getParentId() == null ? 0L : cat.getParentId();
            if (catParentId.equals(parentId)) {
                CourseCategoryVO vo = new CourseCategoryVO();
                BeanUtils.copyProperties(cat, vo);
                vo.setParentId(catParentId);
                List<CourseCategoryVO> children = buildCategoryTree(allCategories, cat.getId());
                vo.setChildren(children.isEmpty() ? null : children);
                result.add(vo);
            }
        }
        return result;
    }

    private void collectChildCategoryIds(Long parentId, List<Long> result) {
        List<CourseCategory> children = categoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CourseCategory>()
                        .eq(CourseCategory::getParentId, parentId));
        for (CourseCategory child : children) {
            result.add(child.getId());
            collectChildCategoryIds(child.getId(), result);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<CourseListItemVO> getHotTop10() {
        // ZREVRANGE course:hot:top10 0 9
        Set<Object> hotSet = redisTemplate.opsForZSet().reverseRange(HOT_TOP10_KEY, 0, 9);
        if (hotSet == null || hotSet.isEmpty()) {
            // Fallback: query top 10 by sale_count from MySQL and populate Redis
            List<Course> topCourses = courseMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Course>()
                            .eq(Course::getStatus, 1)
                            .orderByDesc(Course::getSaleCount)
                            .last("LIMIT 10"));
            if (topCourses.isEmpty()) {
                return Collections.emptyList();
            }
            for (Course course : topCourses) {
                CourseListItemVO vo = new CourseListItemVO();
                BeanUtils.copyProperties(course, vo);
                redisTemplate.opsForZSet().add(HOT_TOP10_KEY, vo, course.getSaleCount());
            }
            // Re-fetch from Redis for consistent output
            hotSet = redisTemplate.opsForZSet().reverseRange(HOT_TOP10_KEY, 0, 9);
        }

        if (hotSet == null || hotSet.isEmpty()) {
            return Collections.emptyList();
        }
        List<CourseListItemVO> result = new ArrayList<>();
        for (Object obj : hotSet) {
            if (obj instanceof CourseListItemVO) {
                result.add((CourseListItemVO) obj);
            }
        }
        return result;
    }
}
