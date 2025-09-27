package com.happy.utils;

import io.seata.rm.tcc.api.BusinessActionContext;

import java.math.BigDecimal;

/**
 * Seata BusinessActionContext 参数安全转换工具
 */
public class ActionContextUtils {

    /**
     * 安全获取字符串（自动 toString()）
     */
    public static String getString(BusinessActionContext ctx, String key) {
        Object value = ctx.getActionContext(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 安全获取 Long
     */
    public static Long getLong(BusinessActionContext ctx, String key) {
        Object value = ctx.getActionContext(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 安全获取 Integer
     */
    public static Integer getInteger(BusinessActionContext ctx, String key) {
        Object value = ctx.getActionContext(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 安全获取 BigDecimal（最常用）
     */
    public static BigDecimal getBigDecimal(BusinessActionContext ctx, String key) {
        Object value = ctx.getActionContext(key);
        if (value == null) return null;
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return new BigDecimal(((Number) value).doubleValue());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 安全获取 Boolean
     */
    public static Boolean getBoolean(BusinessActionContext ctx, String key) {
        Object value = ctx.getActionContext(key);
        if (value == null) return null;
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }

    /**
     * 判断参数是否存在且非空
     */
    public static boolean hasValidParam(BusinessActionContext ctx, String key) {
        return ctx.getActionContext(key) != null;
    }
}