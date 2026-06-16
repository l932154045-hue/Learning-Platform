package com.learning.course.service;

import com.learning.common.core.page.PageReq;
import com.learning.common.core.page.PageResp;
import com.learning.course.dto.req.CourseSaveReq;
import com.learning.course.dto.req.VideoSaveReq;
import com.learning.course.dto.resp.CourseListItemVO;

public interface CourseAdminService {
    PageResp<CourseListItemVO> listAllCourses(PageReq req);
    Long createCourse(CourseSaveReq req);
    void updateCourse(Long id, CourseSaveReq req);
    void deleteCourse(Long id);
    void updateCourseStatus(Long id, Integer status);
    void addVideo(VideoSaveReq req);
    void updateVideo(Long id, VideoSaveReq req);
    void deleteVideo(Long id);

    // Category operations
    Long createCategory(String name, Long parentId, Integer sortOrder);
    void updateCategory(Long id, String name, Integer sortOrder);
    void deleteCategory(Long id);

    // Stats
    Long getCourseCount();
}
