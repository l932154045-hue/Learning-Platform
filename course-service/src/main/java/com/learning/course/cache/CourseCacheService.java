package com.learning.course.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.learning.course.dto.resp.CourseDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseCacheService {
    private final Cache<Long, CourseDetailVO> caffeineCache;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String DETAIL_KEY = "course:detail:";
    private static final String LOCK_KEY = "lock:course:";
    private static final String NULL_MARKER = "NULL_MARKER";
    private static final Random RANDOM = new Random();

    public CourseDetailVO getCourseDetail(Long courseId, Function<Long, CourseDetailVO> loader) {
        // Step 1: Caffeine local cache
        CourseDetailVO cached = caffeineCache.getIfPresent(courseId);
        if (cached != null) {
            return cached;
        }

        // Step 2: Redis
        String redisKey = DETAIL_KEY + courseId;
        Object redisValue = redisTemplate.opsForValue().get(redisKey);
        if (redisValue != null) {
            if (NULL_MARKER.equals(redisValue)) {
                return null;
            }
            CourseDetailVO vo = (CourseDetailVO) redisValue;
            caffeineCache.put(courseId, vo);
            return vo;
        }

        // Step 3: Distributed lock to prevent cache breakdown
        String lockKey = LOCK_KEY + courseId;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(10));
        if (Boolean.FALSE.equals(locked)) {
            // Spin retry up to 3 times
            for (int i = 0; i < 3; i++) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
                redisValue = redisTemplate.opsForValue().get(redisKey);
                if (redisValue != null) {
                    if (!NULL_MARKER.equals(redisValue)) {
                        caffeineCache.put(courseId, (CourseDetailVO) redisValue);
                        return (CourseDetailVO) redisValue;
                    }
                    return null;
                }
            }
        }
        try {
            // Step 4: Query MySQL
            CourseDetailVO vo = loader.apply(courseId);
            if (vo == null) {
                // Prevent cache penetration: cache null value with TTL=2min
                redisTemplate.opsForValue().set(DETAIL_KEY + "null:" + courseId, NULL_MARKER, Duration.ofMinutes(2));
                return null;
            }
            // Step 5: Write cache with random TTL to prevent avalanche
            int randomMin = RANDOM.nextInt(5) + 1;
            redisTemplate.opsForValue().set(redisKey, vo, Duration.ofMinutes(30 + randomMin));
            caffeineCache.put(courseId, vo);
            return vo;
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    public void evict(Long courseId) {
        redisTemplate.delete(DETAIL_KEY + courseId);
        caffeineCache.invalidate(courseId);
    }

    private static final String CATEGORY_TREE_KEY = "course:category:tree";
    private static final String HOT_TOP10_KEY = "course:hot:top10";

    public void evictAllCourseDetail() {
        caffeineCache.invalidateAll();
        var keys = redisTemplate.keys(DETAIL_KEY + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        log.info("已清除所有课程详情缓存");
    }

    public void evictCategoryTree() {
        redisTemplate.delete(CATEGORY_TREE_KEY);
        log.info("已清除分类树缓存");
    }

    public void evictHotTop10() {
        redisTemplate.delete(HOT_TOP10_KEY);
        log.info("已清除热门课程缓存");
    }

    public void refreshAllCaches() {
        evictAllCourseDetail();
        evictCategoryTree();
        evictHotTop10();
        log.info("已刷新全部缓存：课程详情 + 分类树 + 热门课程");
    }
}
