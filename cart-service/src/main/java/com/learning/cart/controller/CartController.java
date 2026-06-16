package com.learning.cart.controller;

import com.learning.cart.dto.req.CartAddReq;
import com.learning.cart.dto.resp.CartItemVO;
import com.learning.cart.service.CartService;
import com.learning.common.core.result.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/list")
    public R<List<CartItemVO>> list(@RequestAttribute("userId") Long userId) {
        return R.ok(cartService.list(userId));
    }

    @PostMapping("/add")
    public R<Void> add(@RequestAttribute("userId") Long userId,
                        @Valid @RequestBody CartAddReq req) {
        cartService.add(userId, req.getCourseId());
        return R.ok();
    }

    @DeleteMapping("/remove/{courseId}")
    public R<Void> remove(@RequestAttribute("userId") Long userId,
                           @PathVariable("courseId") Long courseId) {
        cartService.remove(userId, courseId);
        return R.ok();
    }

    @DeleteMapping("/clear")
    public R<Void> clear(@RequestAttribute("userId") Long userId) {
        cartService.clear(userId);
        return R.ok();
    }
}
