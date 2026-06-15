package com.learning.course.service;

import com.learning.common.core.page.PageResp;
import com.learning.course.dto.req.CourseSearchReq;
import com.learning.course.dto.resp.CourseCategoryVO;
import com.learning.course.dto.resp.CourseDetailVO;
import com.learning.course.dto.resp.CourseListItemVO;

import java.util.List;

public interface CourseService {
    PageResp<CourseListItemVO> searchCourses(CourseSearchReq req);
    CourseDetailVO getCourseDetail(Long courseId);
    List<CourseCategoryVO> getCategoryTree();
    List<CourseListItemVO> getHotTop10();
}
