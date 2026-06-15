package com.learning.cart.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CART_USER_PREFIX = "cart:user:";
    private static final Duration CART_TTL = Duration.ofDays(7);

    public void addToCart(Long userId, Long courseId) {
        String key = CART_USER_PREFIX + userId;
        redisTemplate.opsForSet().add(key, courseId.toString());
        redisTemplate.expire(key, CART_TTL);
    }

    public Set<Long> getCartCourseIds(Long userId) {
        String key = CART_USER_PREFIX + userId;
        Set<Object> members = redisTemplate.opsForSet().members(key);
        if (members == null || members.isEmpty()) {
            return Set.of();
        }
        return members.stream()
                .map(Object::toString)
                .map(Long::valueOf)
                .collect(Collectors.toSet());
    }

    public void removeFromCart(Long userId, Long courseId) {
        String key = CART_USER_PREFIX + userId;
        redisTemplate.opsForSet().remove(key, courseId.toString());
    }

    public void clearCart(Long userId) {
        String key = CART_USER_PREFIX + userId;
        redisTemplate.delete(key);
    }
}
