package com.learning.payment.client;

import com.learning.common.core.result.R;
import com.learning.payment.dto.resp.OrderSummaryVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-service", path = "/api/order")
public interface OrderClient {

    @GetMapping("/internal/summary/{orderId}")
    R<OrderSummaryVO> getOrderSummary(@PathVariable("orderId") Long orderId);

    @GetMapping("/internal/owner/{orderId}")
    R<Long> getOwnerUserId(@PathVariable("orderId") Long orderId);

    @PutMapping("/internal/updateStatus/{orderId}")
    R<Void> updateStatus(@PathVariable("orderId") Long orderId,
                         @RequestParam("status") Integer status);
}
