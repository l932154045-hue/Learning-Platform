package com.learning.learning.service;

import com.learning.learning.dto.resp.MyCourseVO;

import java.util.List;

public interface EnrollmentService {
    void enroll(Long userId, Long courseId);
    List<MyCourseVO> myCourses(Long userId);
}
