package com.learning.course.controller;

import com.learning.common.core.page.PageResp;
import com.learning.common.core.result.R;
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
}
