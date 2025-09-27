package com.happy.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;

/**
 * 通用随机负载均衡配置
 * 可用于任意服务：user-service, product-service, payment-service...
 */
@Configuration
public class RandomLoadBalancerConfig {

    private final DiscoveryClient discoveryClient;

    public RandomLoadBalancerConfig(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactorServiceInstanceLoadBalancer randomLoadBalancer(LoadBalancerClientFactory factory) {
        return new ReactorServiceInstanceLoadBalancer() {
            private final Random random = new Random();

            @Override
            public Mono<Response<ServiceInstance>> choose(Request request) {
                // 获取服务名
                Object context = request.getContext();
                String serviceId = null;
                // 从 RequestDataContext -> URL -> host 获取服务名
                if (context instanceof RequestDataContext) {
                    RequestDataContext dataContext = (RequestDataContext) context;
                    serviceId = dataContext.getClientRequest().getUrl().getHost();
                }
                // 判断服务名为空先注释掉，看能不能实现熔断
                if (serviceId == null || serviceId.isEmpty()) {
                    System.out.println("❌ [" + serviceId + "] 为空");
                    return Mono.just(new EmptyResponse());
                }

                List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
                // 判断实例为空先注释掉，看能不能实现熔断
                if (instances == null || instances.isEmpty()) {
                    System.out.println("❌ [" + serviceId + "] 无可用实例");
                    return Mono.just(new EmptyResponse());
                }

                int index = random.nextInt(instances.size());
                ServiceInstance instance = instances.get(index);
                System.out.println("🎯 [" + serviceId + "] 随机选择了: " + instance.getHost() + ":" + instance.getPort());
                return Mono.just(new DefaultResponse(instance));
            }
        };
    }
}