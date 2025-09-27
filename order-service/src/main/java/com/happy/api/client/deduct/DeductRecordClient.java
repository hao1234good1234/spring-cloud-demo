package com.happy.api.client.deduct;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign 客户端：查询扣减记录
 */
@FeignClient(
        contextId = "deductRecordClient",
        name = "user-service"
)
public interface DeductRecordClient {
    /**
     * 查询扣减记录
     */
    @GetMapping("/deduct-record/{orderId}")
    Boolean isDeducted(@PathVariable String orderId);
}



