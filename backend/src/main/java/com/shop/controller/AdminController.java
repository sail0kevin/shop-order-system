package com.shop.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.common.Result;
import com.shop.entity.Order;
import com.shop.entity.User;
import com.shop.mapper.OrderMapper;
import com.shop.mapper.ProductMapper;
import com.shop.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        long userCount = userMapper.selectCount(null);
        long orderCount = orderMapper.selectCount(null);
        long productCount = productMapper.selectCount(null);

        List<Order> allOrders = orderMapper.selectList(null);
        BigDecimal totalSales = allOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalSales", totalSales);
        result.put("orderCount", orderCount);
        result.put("userCount", userCount);
        result.put("productCount", productCount);
        return Result.success(result);
    }

    @GetMapping("/chart")
    public Result<List<Map<String, Object>>> chart() {
        List<Order> allOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, LocalDateTime.now().minusDays(7)));

        Map<String, BigDecimal> dailyMap = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd");
        for (int i = 6; i >= 0; i--) {
            dailyMap.put(LocalDate.now().minusDays(i).format(fmt), BigDecimal.ZERO);
        }

        for (Order order : allOrders) {
            if (order.getCreateTime() != null) {
                String key = order.getCreateTime().toLocalDate().format(fmt);
                dailyMap.merge(key, order.getTotalAmount(), BigDecimal::add);
            }
        }

        List<Map<String, Object>> list = new ArrayList<>();
        dailyMap.forEach((date, amount) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", date);
            item.put("amount", amount);
            list.add(item);
        });
        return Result.success(list);
    }

    @GetMapping("/orders")
    public Result<Page<Order>> orders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Order> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Order::getCreateTime);
        return Result.success(orderMapper.selectPage(pageObj, wrapper));
    }

    @GetMapping("/users")
    public Result<Page<User>> users(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<User> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(User::getCreateTime);
        return Result.success(userMapper.selectPage(pageObj, wrapper));
    }
}
