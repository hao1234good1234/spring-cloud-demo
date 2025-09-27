package com.happy.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

public class JwtUtil {
    // ✅ 密钥
    private static String jwtSecret = "mySuperLongAndSecureSecretKeyForDevOnly123456789012345678901234567890";

    // ✅ 单位：秒
    private static int expiration = 3600; // 1小时

    // ✅ 生成 Token
    public static String generateToken(String userName, Collection<String> roles, String userId) {
        return Jwts.builder()
                .setSubject(userName)  // 设置用户名
                .claim("userId", userId) // 👈 添加用户ID
                .claim("roles", roles) // 👈 关键：将角色列表放入 Token 的 claim 中
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000)) // 转毫秒
                .signWith(SignatureAlgorithm.HS256, jwtSecret.getBytes())
                .compact();
    }

    // ✅ 验证 Token
    public static boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(jwtSecret.getBytes())
                    .parseClaimsJws(token); // ✅ 这里会自动验证过期时间
            return true;
        } catch (Exception e) {
            System.out.println("Token 无效: " + e.getMessage());
            return false;
        }
    }

    // ✅ 获取 Claims
    public static Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret.getBytes())
                .parseClaimsJws(token)
                .getBody();
    }

    // ✅ 获取用户名
    public static String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String userName = claims.getSubject();
        return userName;
    }

    // ✅ 获取角色列表
    public static Collection<String> getRolesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Object roles = claims.get("roles");
        if (roles instanceof Collection) {
            return (Collection<String>) roles;
        } else if (roles instanceof String) {
            // 兼容旧格式：逗号分隔的字符串
            return Arrays.asList(((String) roles).split(","));
        }
        return Collections.emptyList();
    }

    // ✅ 获取用户ID
    public static String getUserIdFromToken(String token) {
            // 使用你的 JWT 签名密钥（和生成 Token 时一致）
            Claims claims = getClaimsFromToken(token);
            String userId = claims.get("userId", String.class);
            return userId;
    }

}