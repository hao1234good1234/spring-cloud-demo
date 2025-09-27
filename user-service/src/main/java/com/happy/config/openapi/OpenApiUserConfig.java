package com.happy.config.openapi;

import com.happy.openapi.OpenApiCommon;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiUserConfig {

    @Bean
    public OpenAPI userOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("用户中心服务 API").version("v1.0").description("负责用户注册、登录、资料管理"))
                .addSecurityItem(OpenApiCommon.defaultSecurity().get(0))
                .components(new Components()
                        .addSecuritySchemes("Authorization", OpenApiCommon.bearerAuth())
                );
    }
}