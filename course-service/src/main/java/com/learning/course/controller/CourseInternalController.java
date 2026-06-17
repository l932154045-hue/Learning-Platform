package com.learning.course.controller;

import com.learning.common.core.exception.BizException;
import com.learning.common.core.result.R;
import com.learning.common.core.result.ResultCode;
import com.learning.common.security.annotation.CurrentUser;
import com.learning.common.security.context.UserContext;
import com.learning.course.dto.req.CourseSaveReq;
import com.learning.course.dto.req.VideoSaveReq;
import com.learning.course.service.CourseAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/course/internal")
@RequiredArgsConstructor
public class CourseInternalController {

    private final CourseAdminService courseAdminService;

    // ===== Course CRUD =====

    @PostMapping
    public R<Long> createCourse(@Valid @RequestBody CourseSaveReq req,
                                 @CurrentUser UserContext userContext) {
        checkAdmin(userContext);
        Long id = courseAdminService.createCourse(req);
        return R.ok(id);
    }

    @PutMapping("/{id}")
    public R<Void> updateCourse(@PathVariable Long id,
                                 @Valid @RequestBody CourseSaveReq req,
                                 @CurrentUser UserContext userContext) {
        checkAdmin(userContext);
        courseAdminService.updateCourse(id, req);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> deleteCourse(@PathVariable Long id,
                                 @CurrentUser UserContext userContext) {
        checkAdmin(userContext);
        courseAdminService.deleteCourse(id);
        return R.ok();
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id,
                                 @RequestParam Integer status,
                                 @CurrentUser UserContext userContext) {
        checkAdmin(userContext);
        courseAdminService.updateCourseStatus(id, status);
        return R.ok();
    }

    // ===== Video CRUD =====

    @PostMapping("/video")
    public R<Void> addVideo(@Valid @RequestBody VideoSaveReq req,
                             @CurrentUser UserContext userContext) {
        checkAdmin(userContext);
        courseAdminService.addVideo(req);
        return R.ok();
    }

    @PutMapping("/video/{id}")
    public R<Void> updateVideo(@PathVariable Long id,
                                @Valid @RequestBody VideoSaveReq req,
                                @CurrentUser UserContext userContext) {
        checkAdmin(userContext);
        courseAdminService.updateVideo(id, req);
        return R.ok();
    }

    @DeleteMapping("/video/{id}")
    public R<Void> deleteVideo(@PathVariable Long id,
                                @CurrentUser UserContext userContext) {
        checkAdmin(userContext);
        courseAdminService.deleteVideo(id);
        return R.ok();
    }

    // ===== Category CRUD =====

    @PostMapping("/category")
    public R<Long> createCategory(@RequestParam String name,
                                   @RequestParam(defaultValue = "0") Long parentId,
                                   @RequestParam(defaultValue = "0") Integer sortOrder,
                                   @CurrentUser UserContext userContext) {
        checkAdmin(userContext);
        Long id = courseAdminService.createCategory(name, parentId, sortOrder);
        return R.ok(id);
    }

    @PutMapping("/category/{id}")
    public R<Void> updateCategory(@PathVariable Long id,
                                   @RequestParam String name,
                                   @RequestParam(defaultValue = "0") Integer sortOrder,
                                   @CurrentUser UserContext userContext) {
        checkAdmin(userContext);
        courseAdminService.updateCategory(id, name, sortOrder);
        return R.ok();
    }

    @DeleteMapping("/category/{id}")
    public R<Void> deleteCategory(@PathVariable Long id,
                                   @CurrentUser UserContext userContext) {
        checkAdmin(userContext);
        courseAdminService.deleteCategory(id);
        return R.ok();
    }

    // ===== Stats =====

    @GetMapping("/list-all")
    public R<com.learning.common.core.page.PageResp<com.learning.course.dto.resp.CourseListItemVO>> listAll(
            com.learning.common.core.page.PageReq req,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String teacherName,
            @RequestParam(required = false) Integer status,
            @CurrentUser UserContext userContext) {
        checkAdmin(userContext);
        return R.ok(courseAdminService.listAllCourses(req, keyword, teacherName, status));
    }

    @GetMapping("/count")
    public R<Long> getCourseCount(@CurrentUser UserContext userContext) {
        checkAdmin(userContext);
        return R.ok(courseAdminService.getCourseCount());
    }

    // ===== Helper =====

    private void checkAdmin(UserContext userContext) {
        if (!userContext.isAdmin()) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
    }
}
