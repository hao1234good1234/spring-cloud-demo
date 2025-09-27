//package com.happy.config;
//import feign.RequestInterceptor;
//import feign.RequestTemplate;
//import io.seata.core.context.RootContext;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//// ✅ 配置 FeignClient 拦截器，把seata的at模式创建的全局事务id XID 塞进 HTTP Header
//@Configuration
//public class SeataFeignConfig {
//
//    @Bean
//    public RequestInterceptor seataRequestInterceptor() {
//        return new RequestInterceptor() {
//            @Override
//            public void apply(RequestTemplate template) {
//
//            }
//        };
//    }
//}