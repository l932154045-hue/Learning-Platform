package com.learning.admin.client;

import com.learning.common.core.page.PageReq;
import com.learning.common.core.page.PageResp;
import com.learning.common.core.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@FeignClient(name = "order-service", path = "/api/order")
public interface OrderServiceClient {

    @GetMapping("/internal/list")
    R<PageResp<Map<String, Object>>> listOrders(@SpringQueryMap PageReq req,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) Integer status);

    @GetMapping("/internal/count")
    R<Long> getOrderCount();

    @GetMapping("/internal/revenue")
    R<BigDecimal> getTotalRevenue();

    @PutMapping("/internal/updateStatus/{id}")
    R<Void> updateStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status);
}
