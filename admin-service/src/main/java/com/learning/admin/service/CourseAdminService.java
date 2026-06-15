package com.learning.admin.service;

import com.learning.admin.dto.req.CourseSaveReq;
import com.learning.admin.dto.req.VideoSaveReq;

public interface CourseAdminService {
    void createCourse(CourseSaveReq req, Integer role);
    void updateCourse(Long id, CourseSaveReq req, Integer role);
    void deleteCourse(Long id, Integer role);
    void updateStatus(Long id, Integer status, Integer role);
    void addVideo(VideoSaveReq req, Integer role);
    void updateVideo(Long id, VideoSaveReq req, Integer role);
    void deleteVideo(Long id, Integer role);
}
