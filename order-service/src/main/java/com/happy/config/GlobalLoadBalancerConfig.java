package com.happy.config;

import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@LoadBalancerClients({
        @LoadBalancerClient(value = "user-service", configuration = RandomLoadBalancerConfig.class),
        @LoadBalancerClient(value = "product-service", configuration = RandomLoadBalancerConfig.class)
})
public class GlobalLoadBalancerConfig {
    // 空类，仅用于注册

}