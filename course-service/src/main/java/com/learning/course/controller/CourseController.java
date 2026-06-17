package com.learning.course.controller;

import com.learning.common.core.dto.CourseFeignResp;
import com.learning.common.core.exception.BizException;
import com.learning.common.core.page.PageResp;
import com.learning.common.core.result.R;
import com.learning.common.core.result.ResultCode;
import com.learning.common.security.annotation.CurrentUser;
import com.learning.common.security.context.UserContext;
import com.learning.course.cache.CourseCacheService;
import com.learning.course.dto.req.CourseSearchReq;
import com.learning.course.dto.resp.CourseCategoryVO;
import com.learning.course.dto.resp.CourseDetailVO;
import com.learning.course.dto.resp.CourseListItemVO;
import com.learning.course.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final CourseCacheService courseCacheService;

    @GetMapping("/category/tree")
    public R<List<CourseCategoryVO>> getCategoryTree() {
        return R.ok(courseService.getCategoryTree());
    }

    @GetMapping("/list")
    public R<PageResp<CourseListItemVO>> list(@Valid CourseSearchReq req) {
        return R.ok(courseService.searchCourses(req));
    }

    @GetMapping("/detail/{id}")
    public R<CourseDetailVO> detail(@PathVariable("id") Long id) {
        return R.ok(courseService.getCourseDetail(id));
    }

    @GetMapping("/hot")
    public R<List<CourseListItemVO>> hot() {
        return R.ok(courseService.getHotTop10());
    }

    @GetMapping("/batch")
    public R<List<CourseFeignResp>> batch(@RequestParam("ids") List<Long> ids) {
        return R.ok(courseService.getCourseBatch(ids));
    }

    @PostMapping("/cache/refresh")
    public R<String> refreshCache(@CurrentUser UserContext userContext) {
        // 仅管理员可刷新缓存，Feign 内部调用时 role 为 null 放行
        if (!userContext.isAdmin()) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        courseCacheService.refreshAllCaches();
        return R.ok("缓存已刷新：课程详情 + 分类树 + 热门课程");
    }
}
