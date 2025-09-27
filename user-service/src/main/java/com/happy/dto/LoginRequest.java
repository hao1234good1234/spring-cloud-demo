package com.happy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
// 这个类用来接收登录请求的 JSON 数据 用来接收前端传来的用户名密码

@Data
public class LoginRequest {
    @Schema(description = "用户名")
    @NotBlank(message = "用户名不能为空")
    private String username;
    @Schema(description = "密码")
    @NotBlank(message = "密码不能为空")
    @Length(min = 6, max = 12, message = "密码长度不能小于6位不能大于12位")
    private String password;
//    @Schema(description = "用户id")
//    @NotBlank(message = "用户id不能为空")
//    private String userid;
//    @Schema(description = "手机号", pattern = "^1[3-9]\\d{9}$")
//    private String phonenumber;

    // createTime / updateTime 等系统字段不加 @Schema，前端不需要关心
}