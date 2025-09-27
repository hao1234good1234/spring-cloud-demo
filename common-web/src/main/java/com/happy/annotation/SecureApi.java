package com.happy.annotation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Operation 和 SecurityRequirement的组合注解
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Operation
@SecurityRequirement(name = "Authorization")  // ✅ 只为调试方便，❌ 登录接口本身 → 不加（你还没 Token 呢） Knife4j UI 右上角会出现 🔑 按钮，你点一下，输入 Token，之后所有标记了 @SecurityRequirement 的接口自动带 Token 调试
public @interface SecureApi {
    String summary();
    String description() default "";
}