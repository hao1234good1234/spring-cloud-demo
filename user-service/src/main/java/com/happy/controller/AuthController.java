package com.happy.controller;

import com.happy.dto.LoginRequest;
import com.happy.result.GlobalResult;
import com.happy.result.SystemErrorType;
import com.happy.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 注意：这个接口不需要网关鉴权，所以路径最好是 /auth 开头
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户注册登录服务", description = "用户注册登录服务核心接口") // ✅ 必须：分组标识
public class AuthController {
    // 模拟数据库：用户名密码
    private static final Map<String, String> USERS = new HashMap<>();

    static {
        USERS.put("zhangsan", "123456"); // 普通用户
        USERS.put("lisi", "123456");      // 普通用户
        USERS.put("admin", "admin");   // 管理员
    }

    // 模拟角色：谁是什么角色
    // ✅ 改为：支持一个用户多个角色
    private static final Map<String, List<String>> ROLES = new HashMap<>();

    static {
        ROLES.put("zhangsan", List.of("USER"));
        ROLES.put("lisi", List.of("OPERATOR"));
        ROLES.put("admin", List.of("ADMIN", "OPERATOR")); // 👈 admin 同时有 ADMIN 和 OPERATOR 角色
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录返回token和角色") // ✅ 必须：简洁标题
    public GlobalResult<?> login(@Validated @RequestBody LoginRequest request) {

        // 1. 检查用户名是否存在
        if (!USERS.containsKey(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户名不存在");
        }

        // 2. 检查密码是否正确
        String correctPassword = USERS.get(request.getUsername());
        if (!correctPassword.equals(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "密码错误");
        }

        // 3. 获取用户角色列表
        List<String> roles = ROLES.getOrDefault(request.getUsername(), List.of());

        // 在 user-service 的 Controller 中
        if (roles.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户无角色");
        }

        // 4. 生成 JWT Token（包含用户名和角色列表）
        // userid本该是从数据库查的，现在直接用用户名代替
        String token = JwtUtil.generateToken(request.getUsername(), roles, request.getUsername()); // ✅ 正确传入 Collection<String>

        // 5. 返回 Token 和角色信息（可选）


        return GlobalResult.success(Map.of(
                "token", token,
                "roles", roles));
    }

    @GetMapping("/register-user")
    @Operation(summary = "注册用户角色返回token") // ✅ 必须：简洁标题
    public GlobalResult<Map<String, String>> registerUser() {
        // ✅ 正确：使用大写角色名，保持一致性
        String token = JwtUtil.generateToken("zhangsan", List.of("USER"), "user_001");
        return GlobalResult.success(Map.of("token", token));
    }

    @GetMapping("/register-operator")
    @Operation(summary = "注册操作员角色返回token") // ✅ 必须：简洁标题
    public GlobalResult<Map<String, String>> registerOperator() {
        // ✅ 正确：使用大写角色名，保持一致性
        String token = JwtUtil.generateToken("lisi", List.of("OPERATOR"), "lisi");
        return GlobalResult.success(Map.of("token", token));
    }

    @GetMapping("/register-admin")
    @Operation(summary = "注册管理员角色返回token") // ✅ 必须：简洁标题
    public GlobalResult<Map<String, String>> registerAdmin() {
        // ✅ 正确：使用大写角色名，保持一致性
        String token = JwtUtil.generateToken("admin", List.of("ADMIN", "OPERATOR"), "lisi");
        return GlobalResult.success(Map.of("token", token));
    }
}