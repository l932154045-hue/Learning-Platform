package com.learning.payment.controller;

import com.learning.common.core.result.R;
import com.learning.common.security.annotation.CurrentUser;
import com.learning.common.security.context.UserContext;
import com.learning.payment.dto.resp.PayResultVO;
import com.learning.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/pay/{orderId}")
    public R<PayResultVO> pay(@CurrentUser UserContext userContext,
                               @PathVariable("orderId") Long orderId) {
        return R.ok(paymentService.pay(userContext.getUserId(), orderId));
    }

    @GetMapping("/result/{orderId}")
    public R<PayResultVO> result(@PathVariable("orderId") Long orderId,
                                  @CurrentUser UserContext userContext) {
        return R.ok(paymentService.queryResult(orderId, userContext.getUserId()));
    }
}
