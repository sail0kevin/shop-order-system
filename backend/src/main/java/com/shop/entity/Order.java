// ================================================
// Order.java —— 订单实体类
// 会用在：下单、订单列表查询
// ================================================

package com.shop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;        // 订单编号
    private Long userId;           // 用户ID
    private BigDecimal totalAmount;// 总金额
    private Integer status;        // 0-待支付 1-已支付 2-已发货 3-已完成
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
