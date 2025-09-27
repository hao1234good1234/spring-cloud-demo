package com.happy.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.text.SimpleDateFormat;

/**
 *  配置 Jackson ObjectMapper
 */
@Configuration
public class JsonConfig {
    /**
     * 由于 gateway-service 本身是 Spring Cloud Gateway（基于 WebFlux），
     * 它默认不包含 Jackson 的 ObjectMapper 自动配置用于工具类序列化，所以我们需要显式引入并使用。
     * @return
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        return mapper;
    }
}