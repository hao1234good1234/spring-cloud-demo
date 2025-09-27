package com.happy.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * TCC事务日志表，用于实现Confirm/Cancel阶段的幂等性控制
 * tcc_transaction_log
 */
@Data
public class TccTransactionLog implements Serializable {
    /**
     * 主键，自增
     */
    private Long id;

    /**
     * 全局事务ID（XID），Seata生成
     */
    private String xid;

    /**
     * 分支事务ID，可选，用于追踪
     */
    private Long branchId;

    /**
     * 事务阶段：TRY / CONFIRM / CANCEL
     */
    private String transactionType;

    /**
     * 日志创建时间
     */
    private LocalDateTime createTime;

    private static final long serialVersionUID = 1L;
}