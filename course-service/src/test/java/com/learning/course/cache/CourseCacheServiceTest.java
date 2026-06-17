package com.learning.course.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.learning.course.dto.resp.CourseDetailVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseCacheService 单元测试")
class CourseCacheServiceTest {

    @Mock
    private Cache<Long, CourseDetailVO> caffeineCache;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private CourseCacheService courseCacheService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Caffeine 命中直接返回")
    void shouldReturnFromCaffeineWhenHit() {
        CourseDetailVO cached = new CourseDetailVO();
        cached.setId(1L);
        cached.setTitle("Cached Course");
        when(caffeineCache.getIfPresent(1L)).thenReturn(cached);
        Function<Long, CourseDetailVO> loader = id -> {
            throw new RuntimeException("should not be called");
        };
        CourseDetailVO result = courseCacheService.getCourseDetail(1L, loader);
        assertNotNull(result);
        assertEquals("Cached Course", result.getTitle());
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    @DisplayName("Caffeine miss -> Redis hit")
    void shouldFallbackToRedisWhenCaffeineMiss() {
        CourseDetailVO redisCached = new CourseDetailVO();
        redisCached.setId(1L);
        redisCached.setTitle("Redis Course");
        when(caffeineCache.getIfPresent(1L)).thenReturn(null);
        when(valueOperations.get("course:detail:1")).thenReturn(redisCached);
        Function<Long, CourseDetailVO> loader = id -> {
            throw new RuntimeException("should not be called");
        };
        CourseDetailVO result = courseCacheService.getCourseDetail(1L, loader);
        assertNotNull(result);
        assertEquals("Redis Course", result.getTitle());
        verify(caffeineCache).put(eq(1L), any(CourseDetailVO.class));
    }

    @Test
    @DisplayName("Redis 空值标记防穿透")
    void shouldReturnNullForNullMarker() {
        when(caffeineCache.getIfPresent(1L)).thenReturn(null);
        when(valueOperations.get("course:detail:1")).thenReturn("NULL_MARKER");
        Function<Long, CourseDetailVO> loader = id -> {
            throw new RuntimeException("should not be called");
        };
        CourseDetailVO result = courseCacheService.getCourseDetail(1L, loader);
        assertNull(result);
    }

    @Test
    @DisplayName("驱逐单个课程缓存")
    void shouldEvictSingleCourse() {
        courseCacheService.evict(1L);
        verify(redisTemplate).delete("course:detail:1");
        verify(caffeineCache).invalidate(1L);
    }

    @Test
    @DisplayName("驱逐分类树缓存")
    void shouldEvictCategoryTree() {
        courseCacheService.evictCategoryTree();
        verify(redisTemplate).delete("course:category:tree");
        verify(redisTemplate).delete("course:category:list");
    }

    @Test
    @DisplayName("驱逐热门课程")
    void shouldEvictHotTop10() {
        courseCacheService.evictHotTop10();
        verify(redisTemplate).delete("course:hot:top10");
    }
}
