// ================================================
// Product.java —— 商品实体类
// 作用：映射数据库 product 表的每一行数据
// 会用在：商品列表、商品详情、购物车、订单
// ================================================

package com.shop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;           // 商品名称

    private String description;    // 商品描述

    private BigDecimal price;      // 价格（BigDecimal 适合金额，不用 Double 避免精度问题）

    private Integer stock;         // 库存数量

    private String image;          // 图片 URL

    private String category;       // 分类（电子产品 / 服装 / 食品）

    private Integer status;        // 状态：1-上架 0-下架

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
