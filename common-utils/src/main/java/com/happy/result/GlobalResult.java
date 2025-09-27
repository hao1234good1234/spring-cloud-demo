package com.happy.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 定义全局返回格式
 * @param <T>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalResult<T> {
    @Schema(description = "状态码")
    private Integer code;       // 状态码（200 成功，其他失败）
    @Schema(description = "描述信息")
    private String message;     // 描述信息
    @Schema(description = "是否成功")
    private Boolean success;    // 是否成功，这个值不是多余的，省去前端要判断code等于啥值时是成功的麻烦，前端会根据这个值来判断是否成功
    @Schema(description = "数据")
    private T data;             // 数据

    // 快捷方法
    public static <T> GlobalResult<T> success(T data) {
        return new GlobalResult<>(200, "操作成功", true, data);
    }

    public static <T> GlobalResult<T> success() {
        return success(null);
    }

    public static <T> GlobalResult<T> error(String message) {
        return new GlobalResult<>(500, message, false, null);
    }

    public static <T> GlobalResult<T> error(Integer code, String message) {
        return new GlobalResult<>(code, message, false, null);
    }

    public static <T> GlobalResult<T> error(SystemErrorType errorType) {
        return new GlobalResult<>(errorType.getCode(), errorType.getMessage(), false, null);
    }
}