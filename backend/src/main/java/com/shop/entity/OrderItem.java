// ================================================
// OrderItem.java —— 订单明细实体类
// 会用在：创建订单时记录每个商品
// ================================================

package com.shop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_item")
public class OrderItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;          // 所属订单ID
    private Long productId;        // 商品ID
    private String productName;    // 商品名称
    private BigDecimal price;      // 单价
    private Integer quantity;      // 数量
    private BigDecimal subtotal;   // 小计
    private LocalDateTime createTime;
}
