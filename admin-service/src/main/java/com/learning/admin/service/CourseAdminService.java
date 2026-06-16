package com.learning.admin.service;

import com.learning.admin.dto.req.CourseSaveReq;
import com.learning.admin.dto.req.VideoSaveReq;
import com.learning.common.core.page.PageReq;
import com.learning.common.core.page.PageResp;

import java.util.Map;

public interface CourseAdminService {
    PageResp<Map<String, Object>> listCourses(PageReq req, String keyword, String teacherName, Integer status, Integer role);
    void createCourse(CourseSaveReq req, Integer role);
    void updateCourse(Long id, CourseSaveReq req, Integer role);
    void deleteCourse(Long id, Integer role);
    void updateStatus(Long id, Integer status, Integer role);
    void addVideo(VideoSaveReq req, Integer role);
    void updateVideo(Long id, VideoSaveReq req, Integer role);
    void deleteVideo(Long id, Integer role);
}
