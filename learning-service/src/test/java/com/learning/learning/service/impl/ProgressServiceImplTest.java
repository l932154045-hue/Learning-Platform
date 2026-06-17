package com.learning.learning.service.impl;

import com.learning.common.core.exception.BizException;
import com.learning.learning.dto.req.ProgressReportReq;
import com.learning.learning.entity.VideoProgress;
import com.learning.learning.mapper.EnrollmentMapper;
import com.learning.learning.mapper.VideoProgressMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProgressServiceImpl 进度防刷单元测试")
class ProgressServiceImplTest {

    @Mock
    private VideoProgressMapper videoProgressMapper;
    @Mock
    private EnrollmentMapper enrollmentMapper;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;
    @InjectMocks
    private ProgressServiceImpl progressService;

    @Test
    @DisplayName("进度只进不退 — 提交更大进度成功")
    void shouldAcceptLargerProgress() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(eq("progress:1:1"))).thenReturn(30); // cached = 30

        ProgressReportReq req = new ProgressReportReq();
        req.setVideoId(1L);
        req.setCourseId(100L);
        req.setProgressSeconds(60);
        req.setDuration(120);

        VideoProgress existing = new VideoProgress();
        existing.setId(1L);
        when(videoProgressMapper.selectOne(any())).thenReturn(existing);

        assertDoesNotThrow(() -> progressService.report(1L, req));
        verify(valueOperations).set(eq("progress:1:1"), eq(60), any(Duration.class));
    }

    @Test
    @DisplayName("进度只进不退 — 提交更小进度抛异常")
    void shouldRejectSmallerProgress() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(eq("progress:1:1"))).thenReturn(50); // cached = 50

        ProgressReportReq req = new ProgressReportReq();
        req.setVideoId(1L);
        req.setCourseId(100L);
        req.setProgressSeconds(20);
        req.setDuration(120);

        assertThrows(BizException.class, () -> progressService.report(1L, req));
    }

    @Test
    @DisplayName("首次上报进度 — 无缓存记录，插入新记录")
    void shouldAcceptFirstProgress() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(eq("progress:1:1"))).thenReturn(null);

        ProgressReportReq req = new ProgressReportReq();
        req.setVideoId(1L);
        req.setCourseId(100L);
        req.setProgressSeconds(30);
        req.setDuration(120);

        when(videoProgressMapper.selectOne(any())).thenReturn(null);

        assertDoesNotThrow(() -> progressService.report(1L, req));
        verify(valueOperations).set(eq("progress:1:1"), eq(30), any(Duration.class));
    }

    @Test
    @DisplayName("进度达到95%阈值 — 标记为已完成")
    void shouldMarkFinishedWhenAboveThreshold() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(eq("progress:1:1"))).thenReturn(null);

        ProgressReportReq req = new ProgressReportReq();
        req.setVideoId(1L);
        req.setCourseId(100L);
        req.setProgressSeconds(114); // 114 >= 120 * 0.95 = 114
        req.setDuration(120);

        when(videoProgressMapper.selectOne(any())).thenReturn(null);

        assertDoesNotThrow(() -> progressService.report(1L, req));
        verify(valueOperations).set(eq("progress:1:1"), eq(114), any(Duration.class));
    }
}
