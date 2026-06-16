package com.learning.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.learning.common.core.exception.BizException;
import com.learning.common.core.result.ResultCode;
import com.learning.learning.dto.req.ReviewReq;
import com.learning.learning.dto.resp.ReviewVO;
import com.learning.learning.entity.CourseReview;
import com.learning.learning.mapper.CourseReviewMapper;
import com.learning.learning.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final CourseReviewMapper courseReviewMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long userId, ReviewReq req) {
        // Check if already reviewed
        Long count = courseReviewMapper.selectCount(new LambdaQueryWrapper<CourseReview>()
                .eq(CourseReview::getUserId, userId)
                .eq(CourseReview::getCourseId, req.getCourseId()));
        if (count > 0) {
            throw new BizException(ResultCode.REVIEW_EXISTS);
        }

        CourseReview review = new CourseReview();
        review.setUserId(userId);
        review.setCourseId(req.getCourseId());
        review.setRating(req.getRating());
        review.setContent(req.getContent());
        review.setCreatedAt(java.time.LocalDateTime.now());
        courseReviewMapper.insert(review);
        log.info("用户提交评价: userId={}, courseId={}, rating={}", userId, req.getCourseId(), req.getRating());
    }

    @Override
    public ReviewVO getMyReview(Long userId, Long courseId) {
        CourseReview review = courseReviewMapper.selectOne(new LambdaQueryWrapper<CourseReview>()
                .eq(CourseReview::getUserId, userId)
                .eq(CourseReview::getCourseId, courseId));
        if (review == null) {
            return null;
        }
        return toVO(review);
    }

    @Override
    public List<ReviewVO> getCourseReviews(Long courseId) {
        List<CourseReview> reviews = courseReviewMapper.selectList(
                new LambdaQueryWrapper<CourseReview>()
                        .eq(CourseReview::getCourseId, courseId)
                        .orderByDesc(CourseReview::getCreatedAt));
        return reviews.stream().map(this::toVO).collect(Collectors.toList());
    }

    private ReviewVO toVO(CourseReview review) {
        ReviewVO vo = new ReviewVO();
        vo.setId(review.getId());
        vo.setUserId(review.getUserId());
        vo.setNickname("用户" + review.getUserId());
        vo.setRating(review.getRating());
        vo.setContent(review.getContent());
        vo.setCreatedAt(review.getCreatedAt());
        return vo;
    }
}
