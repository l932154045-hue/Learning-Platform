package com.learning.learning.service;

import com.learning.learning.dto.req.ReviewReq;
import com.learning.learning.dto.resp.ReviewVO;

import java.util.List;

public interface ReviewService {
    void submit(Long userId, ReviewReq req);
    ReviewVO getMyReview(Long userId, Long courseId);
    List<ReviewVO> getCourseReviews(Long courseId);
}
