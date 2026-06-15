package com.learning.cart.service;

import com.learning.cart.dto.resp.CartItemVO;

import java.util.List;

public interface CartService {
    List<CartItemVO> list(Long userId);

    void add(Long userId, Long courseId);

    void remove(Long userId, Long courseId);

    void clear(Long userId);
}
