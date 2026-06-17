package com.learning.learning.controller;

import com.learning.common.core.result.R;
import com.learning.common.security.annotation.CurrentUser;
import com.learning.common.security.context.UserContext;
import com.learning.learning.dto.req.ReviewReq;
import com.learning.learning.dto.resp.ReviewVO;
import com.learning.learning.service.ReviewService;
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
    public R<ReviewVO> getMyReview(@PathVariable("courseId") Long courseId, @CurrentUser UserContext userContext) {
        Long userId = userContext.getUserId();
        return R.ok(reviewService.getMyReview(userId, courseId));
    }

    @PostMapping("/review")
    public R<Void> submitReview(@Valid @RequestBody ReviewReq req, @CurrentUser UserContext userContext) {
        Long userId = userContext.getUserId();
        reviewService.submit(userId, req);
        return R.ok();
    }

    @GetMapping("/course/{id}/reviews")
    public R<List<ReviewVO>> getCourseReviews(@PathVariable("id") Long id) {
        return R.ok(reviewService.getCourseReviews(id));
    }
}
