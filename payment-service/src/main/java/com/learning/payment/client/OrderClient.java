package com.learning.payment.client;

import com.learning.common.core.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-service")
public interface OrderClient {

    @PutMapping("/api/order/internal/updateStatus/{orderId}")
    R<Void> updateStatus(@PathVariable("orderId") Long orderId,
                         @RequestParam("status") Integer status);

    @GetMapping("/api/order/internal/owner/{orderId}")
    R<Long> getOwnerUserId(@PathVariable("orderId") Long orderId);
}
