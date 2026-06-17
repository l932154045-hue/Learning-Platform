package com.learning.admin.controller;

import com.learning.admin.dto.req.CourseSaveReq;
import com.learning.admin.dto.req.VideoSaveReq;
import com.learning.admin.service.CourseAdminService;
import com.learning.common.core.page.PageReq;
import com.learning.common.core.page.PageResp;
import com.learning.common.core.result.R;
import com.learning.common.security.annotation.CurrentUser;
import com.learning.common.security.context.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/course")
@RequiredArgsConstructor
public class CourseAdminController {

    private final CourseAdminService courseAdminService;

    @GetMapping("/list")
    public R<PageResp<Map<String, Object>>> list(PageReq req,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) String teacherName,
                                                  @RequestParam(required = false) Integer status,
                                                  @CurrentUser UserContext userContext) {
        return R.ok(courseAdminService.listCourses(req, keyword, teacherName, status, userContext.getRole()));
    }

    @PostMapping
    public R<Void> create(@Valid @RequestBody CourseSaveReq req,
                           @CurrentUser UserContext userContext) {
        courseAdminService.createCourse(req, userContext.getRole());
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable("id") Long id,
                           @Valid @RequestBody CourseSaveReq req,
                           @CurrentUser UserContext userContext) {
        courseAdminService.updateCourse(id, req, userContext.getRole());
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable("id") Long id,
                           @CurrentUser UserContext userContext) {
        courseAdminService.deleteCourse(id, userContext.getRole());
        return R.ok();
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable("id") Long id,
                                 @RequestParam Integer status,
                                 @CurrentUser UserContext userContext) {
        courseAdminService.updateStatus(id, status, userContext.getRole());
        return R.ok();
    }

    @PostMapping("/{id}/video")
    public R<Void> addVideo(@Valid @RequestBody VideoSaveReq req,
                             @CurrentUser UserContext userContext) {
        courseAdminService.addVideo(req, userContext.getRole());
        return R.ok();
    }

    @PutMapping("/video/{id}")
    public R<Void> updateVideo(@PathVariable("id") Long id,
                                @Valid @RequestBody VideoSaveReq req,
                                @CurrentUser UserContext userContext) {
        courseAdminService.updateVideo(id, req, userContext.getRole());
        return R.ok();
    }

    @DeleteMapping("/video/{id}")
    public R<Void> deleteVideo(@PathVariable("id") Long id,
                                @CurrentUser UserContext userContext) {
        courseAdminService.deleteVideo(id, userContext.getRole());
        return R.ok();
    }
}
