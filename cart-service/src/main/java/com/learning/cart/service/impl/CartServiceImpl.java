package com.learning.cart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.learning.cart.cache.CartCacheService;
import com.learning.cart.client.CourseClient;
import com.learning.cart.dto.resp.CartItemVO;
import com.learning.common.core.dto.CourseFeignResp;
import com.learning.cart.entity.Cart;
import com.learning.cart.mapper.CartMapper;
import com.learning.cart.service.CartService;
import com.learning.common.core.exception.BizException;
import com.learning.common.core.result.R;
import com.learning.common.core.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final CartCacheService cartCacheService;
    private final CourseClient courseClient;

    @Override
    public void add(Long userId, Long courseId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setCourseId(courseId);
        try {
            cartMapper.insert(cart);
        } catch (DuplicateKeyException e) {
            throw new BizException(ResultCode.CART_DUPLICATE);
        }
        try {
            cartCacheService.addToCart(userId, courseId);
        } catch (Exception e) {
            log.error("购物车缓存写入失败: userId={}, courseId={}", userId, courseId, e);
        }
    }

    @Override
    public List<CartItemVO> list(Long userId) {
        Set<Long> courseIds = cartCacheService.getCartCourseIds(userId);
        if (courseIds.isEmpty()) {
            // Fallback to DB
            List<Cart> cartItems = cartMapper.selectList(
                    new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
            if (cartItems.isEmpty()) {
                return Collections.emptyList();
            }
            // Rebuild cache
            for (Cart item : cartItems) {
                cartCacheService.addToCart(userId, item.getCourseId());
            }
            courseIds = cartItems.stream().map(Cart::getCourseId).collect(Collectors.toSet());
        }

        // Fetch cart records from DB to get cartId for each course
        List<Cart> cartRecords = cartMapper.selectList(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
                        .in(Cart::getCourseId, courseIds));
        Map<Long, Long> courseIdToCartId = cartRecords.stream()
                .collect(Collectors.toMap(Cart::getCourseId, Cart::getId));

        // Batch-fetch course info — one Feign call instead of N
        Map<Long, CourseFeignResp> courseMap = fetchCourseBatch(new ArrayList<>(courseIds));

        List<CartItemVO> result = new ArrayList<>();
        for (Long courseId : courseIds) {
            CourseFeignResp course = courseMap.get(courseId);
            CartItemVO vo = new CartItemVO();
            Long cartId = courseIdToCartId.get(courseId);
            vo.setCartId(cartId != null ? cartId : -1L);
            vo.setCourseId(courseId);
            if (course != null) {
                vo.setCourseTitle(course.getTitle());
                vo.setCoverUrl(course.getCoverUrl());
                vo.setTeacherName(course.getTeacherName());
                vo.setPrice(course.getPrice());
            } else {
                vo.setCourseTitle("课程-" + courseId);
                vo.setTeacherName("未知");
            }
            result.add(vo);
        }
        return result;
    }

    private Map<Long, CourseFeignResp> fetchCourseBatch(List<Long> courseIds) {
        if (courseIds.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            R<List<CourseFeignResp>> response = courseClient.getCourseBatch(courseIds);
            if (response != null && response.getCode() == 200 && response.getData() != null) {
                return response.getData().stream()
                        .collect(Collectors.toMap(CourseFeignResp::getId, c -> c));
            }
        } catch (Exception e) {
            log.error("批量获取课程信息失败: courseIds={}", courseIds, e);
        }
        return Collections.emptyMap();
    }

    @Override
    public void remove(Long userId, Long courseId) {
        cartMapper.delete(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getCourseId, courseId));
        cartCacheService.removeFromCart(userId, courseId);
    }

    @Override
    public void clear(Long userId) {
        cartMapper.delete(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
        cartCacheService.clearCart(userId);
    }
}
