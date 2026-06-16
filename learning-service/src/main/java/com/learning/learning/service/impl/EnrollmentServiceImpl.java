package com.learning.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.learning.common.core.exception.BizException;
import com.learning.common.core.result.ResultCode;
import com.learning.learning.dto.resp.MyCourseVO;
import com.learning.learning.entity.Enrollment;
import com.learning.learning.entity.VideoProgress;
import com.learning.learning.mapper.EnrollmentMapper;
import com.learning.learning.mapper.VideoProgressMapper;
import com.learning.learning.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentMapper enrollmentMapper;
    private final VideoProgressMapper videoProgressMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enroll(Long userId, Long courseId) {
        // Check if already enrolled
        Long count = enrollmentMapper.selectCount(new LambdaQueryWrapper<Enrollment>()
                .eq(Enrollment::getUserId, userId)
                .eq(Enrollment::getCourseId, courseId));
        if (count > 0) {
            throw new BizException(ResultCode.COURSE_ALREADY_PURCHASED);
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setUserId(userId);
        enrollment.setCourseId(courseId);
        enrollment.setStatus(1);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setLastLearnedAt(LocalDateTime.now());
        enrollmentMapper.insert(enrollment);
        log.info("用户选课成功: userId={}, courseId={}", userId, courseId);
    }

    @Override
    public List<MyCourseVO> myCourses(Long userId) {
        List<Enrollment> enrollments = enrollmentMapper.selectList(
                new LambdaQueryWrapper<Enrollment>()
                        .eq(Enrollment::getUserId, userId)
                        .orderByDesc(Enrollment::getEnrolledAt));

        return enrollments.stream().map(e -> {
            MyCourseVO vo = new MyCourseVO();
            vo.setEnrollmentId(e.getId());
            vo.setCourseId(e.getCourseId());
            vo.setCourseTitle("课程-" + e.getCourseId());
            vo.setTeacherName("讲师");
            vo.setEnrolledAt(e.getEnrolledAt());
            vo.setLastLearnedAt(e.getLastLearnedAt());

            // Calculate total progress percentage
            List<VideoProgress> progressList = videoProgressMapper.selectList(
                    new LambdaQueryWrapper<VideoProgress>()
                            .eq(VideoProgress::getUserId, userId)
                            .eq(VideoProgress::getCourseId, e.getCourseId()));
            if (!progressList.isEmpty()) {
                vo.setVideoCount(progressList.size());
                long finishedCount = progressList.stream()
                        .filter(p -> p.getIsFinished() != null && p.getIsFinished() == 1)
                        .count();
                vo.setFinishedVideoCount((int) finishedCount);
                if (vo.getVideoCount() > 0) {
                    vo.setTotalProgress((int) (finishedCount * 100 / vo.getVideoCount()));
                }
            } else {
                vo.setVideoCount(0);
                vo.setFinishedVideoCount(0);
                vo.setTotalProgress(0);
            }
            return vo;
        }).collect(Collectors.toList());
    }
}
