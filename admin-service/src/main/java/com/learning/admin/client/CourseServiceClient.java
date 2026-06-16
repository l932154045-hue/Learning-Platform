package com.learning.admin.client;

import com.learning.admin.dto.req.CourseSaveReq;
import com.learning.admin.dto.req.VideoSaveReq;
import com.learning.common.core.page.PageReq;
import com.learning.common.core.page.PageResp;
import com.learning.common.core.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "course-service", path = "/api/course")
public interface CourseServiceClient {

    // Admin course list (all statuses)
    @GetMapping("/internal/list-all")
    R<PageResp<Map<String, Object>>> listAllCourses(@SpringQueryMap PageReq req,
                                                     @RequestParam(required = false) String keyword,
                                                     @RequestParam(required = false) String teacherName,
                                                     @RequestParam(required = false) Integer status);

    // Course CRUD (internal endpoints)
    @PostMapping("/internal")
    R<Long> createCourse(@RequestBody CourseSaveReq req);

    @PutMapping("/internal/{id}")
    R<Void> updateCourse(@PathVariable("id") Long id, @RequestBody CourseSaveReq req);

    @DeleteMapping("/internal/{id}")
    R<Void> deleteCourse(@PathVariable("id") Long id);

    @PutMapping("/internal/{id}/status")
    R<Void> updateStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status);

    // Video CRUD
    @PostMapping("/internal/video")
    R<Void> addVideo(@RequestBody VideoSaveReq req);

    @PutMapping("/internal/video/{id}")
    R<Void> updateVideo(@PathVariable("id") Long id, @RequestBody VideoSaveReq req);

    @DeleteMapping("/internal/video/{id}")
    R<Void> deleteVideo(@PathVariable("id") Long id);

    // Category CRUD
    @PostMapping("/internal/category")
    R<Long> createCategory(@RequestParam("name") String name,
                           @RequestParam("parentId") Long parentId,
                           @RequestParam("sortOrder") Integer sortOrder);

    @PutMapping("/internal/category/{id}")
    R<Void> updateCategory(@PathVariable("id") Long id,
                           @RequestParam("name") String name,
                           @RequestParam("sortOrder") Integer sortOrder);

    @DeleteMapping("/internal/category/{id}")
    R<Void> deleteCategory(@PathVariable("id") Long id);

    // Stats
    @GetMapping("/internal/count")
    R<Long> getCourseCount();
}
