package com.happy.result;


public enum SystemErrorType implements ErrorType {
    // 通用错误
    SYSTEM_ERROR(500, "系统繁忙，请稍后再试"),
    PARAMS_ERROR(400, "参数错误"),
    AUTH_ERROR(401, "认证失败"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    UNAUTHORIZED(401, "未授权访问"),
    INVALID_TOKEN(401, "无效或过期token"),
    MISSING_TOKEN(401, "缺少授权信息"),
    ROUTE_NOT_FOUND(404, "请求路径不存在"),
    USERNAME_NOT_FOUND(401, "用户名不存在"),
    PASSWORD_ERROR(401, "密码错误"),
    USER_NOT_ROLE(403, "用户没有分配任何角色"),
    RATE_LIMIT_EXCEEDED(429, "请求过于频繁，请稍后再试"),
    // ========== 新增或确认以下网关/通用场景错误码 ==========

    GATEWAY_TIMEOUT(504, "网关超时，请稍后重试"),          // 对应 HTTP 504
    BAD_GATEWAY(502, "网关错误"),                        // 对应 HTTP 502
    SERVICE_UNAVAILABLE(503, "服务暂时不可用"),           // 你已有，保留
    CONNECTION_REFUSED(500, "服务连接失败，请检查服务状态") // 新增，替代你的 50003
    ;
    // 可选：如果你希望区分“服务未启动”和“服务超时”，可以保留更细粒度
    // 但建议优先使用标准 HTTP 语义（502, 503, 504）

    private final Integer code;
    private final String message;

    SystemErrorType(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

