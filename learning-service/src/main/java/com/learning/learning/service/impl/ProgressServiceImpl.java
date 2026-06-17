package com.learning.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.learning.common.core.exception.BizException;
import com.learning.common.core.result.ResultCode;
import com.learning.learning.dto.req.ProgressReportReq;
import com.learning.learning.dto.resp.ProgressVO;
import com.learning.learning.entity.Enrollment;
import com.learning.learning.entity.VideoProgress;
import com.learning.learning.mapper.EnrollmentMapper;
import com.learning.learning.mapper.VideoProgressMapper;
import com.learning.learning.service.ProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {

    private final VideoProgressMapper videoProgressMapper;
    private final EnrollmentMapper enrollmentMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PROGRESS_KEY_PREFIX = "progress:";
    private static final int PROGRESS_TTL_HOURS = 24;
    private static final double FINISH_THRESHOLD = 0.95;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void report(Long userId, ProgressReportReq req) {
        String redisKey = PROGRESS_KEY_PREFIX + userId + ":" + req.getVideoId();

        // Anti-brush: progress can only go forward
        Integer cachedProgress = (Integer) redisTemplate.opsForValue().get(redisKey);
        if (cachedProgress != null && req.getProgressSeconds() <= cachedProgress) {
            log.warn("进度防刷拦截: userId={}, videoId={}, cachedProgress={}, reportProgress={}",
                    userId, req.getVideoId(), cachedProgress, req.getProgressSeconds());
            throw new BizException(ResultCode.PROGRESS_BLOCKED);
        }

        // Store new progress in Redis with 24h TTL
        redisTemplate.opsForValue().set(redisKey, req.getProgressSeconds(), Duration.ofHours(PROGRESS_TTL_HOURS));

        // Determine if finished (progress >= 95% of duration)
        int isFinished = req.getProgressSeconds() >= req.getDuration() * FINISH_THRESHOLD ? 1 : 0;

        // Update or insert video progress
        VideoProgress existing = videoProgressMapper.selectOne(
                new LambdaQueryWrapper<VideoProgress>()
                        .eq(VideoProgress::getUserId, userId)
                        .eq(VideoProgress::getVideoId, req.getVideoId()));

        if (existing != null) {
            existing.setProgressSeconds(req.getProgressSeconds());
            existing.setCourseId(req.getCourseId());
            existing.setIsFinished(isFinished);
            videoProgressMapper.updateById(existing);
        } else {
            VideoProgress progress = new VideoProgress();
            progress.setUserId(userId);
            progress.setVideoId(req.getVideoId());
            progress.setCourseId(req.getCourseId());
            progress.setProgressSeconds(req.getProgressSeconds());
            progress.setIsFinished(isFinished);
            videoProgressMapper.insert(progress);
        }

        // Update enrollment last learned time
        Enrollment enrollment = enrollmentMapper.selectOne(
                new LambdaQueryWrapper<Enrollment>()
                        .eq(Enrollment::getUserId, userId)
                        .eq(Enrollment::getCourseId, req.getCourseId()));
        if (enrollment != null) {
            enrollment.setLastLearnedAt(LocalDateTime.now());
            enrollmentMapper.updateById(enrollment);
        }
    }

    @Override
    public List<ProgressVO> getProgress(Long userId, Long courseId) {
        List<VideoProgress> progressList = videoProgressMapper.selectList(
                new LambdaQueryWrapper<VideoProgress>()
                        .eq(VideoProgress::getUserId, userId)
                        .eq(VideoProgress::getCourseId, courseId));

        return progressList.stream().map(p -> {
            ProgressVO vo = new ProgressVO();
            vo.setVideoId(p.getVideoId());
            vo.setCourseId(p.getCourseId());
            vo.setProgressSeconds(p.getProgressSeconds());
            vo.setFinished(p.getIsFinished() != null && p.getIsFinished() == 1);
            vo.setUpdatedAt(p.getUpdatedAt());
            return vo;
        }).collect(Collectors.toList());
    }
}
