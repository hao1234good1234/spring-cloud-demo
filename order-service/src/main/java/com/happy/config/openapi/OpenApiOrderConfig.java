package com.happy.config.openapi;

import com.happy.openapi.OpenApiCommon;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiOrderConfig {

    @Bean
    public OpenAPI orderOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("订单服务 API").version("v1.0").description("负责订单创建、支付、查询"))
                .addSecurityItem(OpenApiCommon.defaultSecurity().get(0))
                .components(new Components()
                        .addSecuritySchemes("Authorization", OpenApiCommon.bearerAuth())
                );

    }
}