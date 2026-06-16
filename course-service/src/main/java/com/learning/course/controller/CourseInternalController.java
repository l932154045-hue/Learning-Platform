package com.learning.course.controller;

import com.learning.common.core.exception.BizException;
import com.learning.common.core.result.R;
import com.learning.common.core.result.ResultCode;
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
                                 @RequestAttribute(value = "role", required = false) Integer role) {
        checkAdmin(role);
        Long id = courseAdminService.createCourse(req);
        return R.ok(id);
    }

    @PutMapping("/{id}")
    public R<Void> updateCourse(@PathVariable Long id,
                                 @Valid @RequestBody CourseSaveReq req,
                                 @RequestAttribute(value = "role", required = false) Integer role) {
        checkAdmin(role);
        courseAdminService.updateCourse(id, req);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> deleteCourse(@PathVariable Long id,
                                 @RequestAttribute(value = "role", required = false) Integer role) {
        checkAdmin(role);
        courseAdminService.deleteCourse(id);
        return R.ok();
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id,
                                 @RequestParam Integer status,
                                 @RequestAttribute(value = "role", required = false) Integer role) {
        checkAdmin(role);
        courseAdminService.updateCourseStatus(id, status);
        return R.ok();
    }

    // ===== Video CRUD =====

    @PostMapping("/video")
    public R<Void> addVideo(@Valid @RequestBody VideoSaveReq req,
                             @RequestAttribute(value = "role", required = false) Integer role) {
        checkAdmin(role);
        courseAdminService.addVideo(req);
        return R.ok();
    }

    @PutMapping("/video/{id}")
    public R<Void> updateVideo(@PathVariable Long id,
                                @Valid @RequestBody VideoSaveReq req,
                                @RequestAttribute(value = "role", required = false) Integer role) {
        checkAdmin(role);
        courseAdminService.updateVideo(id, req);
        return R.ok();
    }

    @DeleteMapping("/video/{id}")
    public R<Void> deleteVideo(@PathVariable Long id,
                                @RequestAttribute(value = "role", required = false) Integer role) {
        checkAdmin(role);
        courseAdminService.deleteVideo(id);
        return R.ok();
    }

    // ===== Category CRUD =====

    @PostMapping("/category")
    public R<Long> createCategory(@RequestParam String name,
                                   @RequestParam(defaultValue = "0") Long parentId,
                                   @RequestParam(defaultValue = "0") Integer sortOrder,
                                   @RequestAttribute(value = "role", required = false) Integer role) {
        checkAdmin(role);
        Long id = courseAdminService.createCategory(name, parentId, sortOrder);
        return R.ok(id);
    }

    @PutMapping("/category/{id}")
    public R<Void> updateCategory(@PathVariable Long id,
                                   @RequestParam String name,
                                   @RequestParam(defaultValue = "0") Integer sortOrder,
                                   @RequestAttribute(value = "role", required = false) Integer role) {
        checkAdmin(role);
        courseAdminService.updateCategory(id, name, sortOrder);
        return R.ok();
    }

    @DeleteMapping("/category/{id}")
    public R<Void> deleteCategory(@PathVariable Long id,
                                   @RequestAttribute(value = "role", required = false) Integer role) {
        checkAdmin(role);
        courseAdminService.deleteCategory(id);
        return R.ok();
    }

    // ===== Stats =====

    @GetMapping("/count")
    public R<Long> getCourseCount() {
        return R.ok(courseAdminService.getCourseCount());
    }

    // ===== Helper =====

    private void checkAdmin(Integer role) {
        if (role != null && role != 1) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
    }
}
