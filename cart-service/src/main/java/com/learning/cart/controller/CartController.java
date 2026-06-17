package com.learning.cart.controller;

import com.learning.cart.dto.req.CartAddReq;
import com.learning.cart.dto.resp.CartItemVO;
import com.learning.cart.service.CartService;
import com.learning.common.core.result.R;
import com.learning.common.security.annotation.CurrentUser;
import com.learning.common.security.context.UserContext;
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
    public R<List<CartItemVO>> list(@CurrentUser UserContext userContext) {
        return R.ok(cartService.list(userContext.getUserId()));
    }

    @PostMapping("/add")
    public R<Void> add(@CurrentUser UserContext userContext,
                        @Valid @RequestBody CartAddReq req) {
        cartService.add(userContext.getUserId(), req.getCourseId());
        return R.ok();
    }

    @DeleteMapping("/remove/{courseId}")
    public R<Void> remove(@CurrentUser UserContext userContext,
                           @PathVariable("courseId") Long courseId) {
        cartService.remove(userContext.getUserId(), courseId);
        return R.ok();
    }

    @DeleteMapping("/clear")
    public R<Void> clear(@CurrentUser UserContext userContext) {
        cartService.clear(userContext.getUserId());
        return R.ok();
    }
}
