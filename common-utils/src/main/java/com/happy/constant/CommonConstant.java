package com.happy.constant;
// 通用常量
public class CommonConstant {
    // 过滤器优先级顺序统一配置
    // Integer 对象不是编译期常量表达式
    public static final int AuthFilter_ORDER = -90;  //全局认证与授权过滤器
//    public static final int RateLimitFilter_ORDER = -80;  //自定义限流响应过滤器
    public static final int SkyWalkingTraceHeaderFilter_ORDER = -70;  //order-service链路追踪过滤器

}
