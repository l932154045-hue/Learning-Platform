package com.learning.learning.controller;

import com.learning.common.core.result.R;
import com.learning.learning.dto.req.ReviewReq;
import com.learning.learning.dto.resp.ReviewVO;
import com.learning.learning.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/review/{courseId}")
    public R<ReviewVO> getMyReview(@PathVariable("courseId") Long courseId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return R.ok(reviewService.getMyReview(userId, courseId));
    }

    @PostMapping("/review")
    public R<Void> submitReview(@Valid @RequestBody ReviewReq req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        reviewService.submit(userId, req);
        return R.ok();
    }

    @GetMapping("/course/{id}/reviews")
    public R<List<ReviewVO>> getCourseReviews(@PathVariable("id") Long id) {
        return R.ok(reviewService.getCourseReviews(id));
    }
}
