package com.happy.openapi;

import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import java.util.List;

public class OpenApiCommon {

    public static SecurityScheme bearerAuth() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name("Authorization")
                .in(SecurityScheme.In.HEADER);
    }

    public static List<SecurityRequirement> defaultSecurity() {
        return List.of(new SecurityRequirement().addList("Authorization"));
    }
}