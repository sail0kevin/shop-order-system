package com.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.dto.LoginRequest;
import com.shop.dto.RegisterRequest;
import com.shop.entity.Order;
import com.shop.entity.Product;
import com.shop.entity.User;
import com.shop.service.CartService;
import com.shop.service.OrderService;
import com.shop.service.ProductService;
import com.shop.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 核心业务单元测试
 *
 * @SpringBootTest 启动完整 Spring 上下文
 * @Transactional 测试数据自动回滚，不影响数据库
 */
@SpringBootTest
class ShopApplicationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    // ==================== 用户模块 ====================

    @Test
    @Transactional
    void testRegisterAndLogin() {
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setUsername("testuser");
        registerReq.setPassword("123456");
        registerReq.setNickname("测试用户");
        userService.register(registerReq);

        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername("testuser");
        loginReq.setPassword("123456");
        String token = userService.login(loginReq);
        assertNotNull(token);
        assertTrue(token.length() > 20);
    }

    @Test
    void testLoginFailure() {
        LoginRequest wrongUserReq = new LoginRequest();
        wrongUserReq.setUsername("nonexistent");
        wrongUserReq.setPassword("123456");
        assertThrows(RuntimeException.class, () ->
                userService.login(wrongUserReq));
    }

    // ==================== 商品模块 ====================

    @Test
    void testProductList() {
        Page<Product> page = productService.getProductPage(1, 10, null, null);
        assertNotNull(page);
        assertTrue(page.getTotal() >= 5);
        assertFalse(page.getRecords().isEmpty());

        Page<Product> electronicPage = productService.getProductPage(1, 10, "电子产品", null);
        assertTrue(electronicPage.getRecords().stream()
                .allMatch(p -> "电子产品".equals(p.getCategory())));

        Page<Product> searchPage = productService.getProductPage(1, 10, null, "iPhone");
        assertTrue(searchPage.getRecords().stream()
                .allMatch(p -> p.getName().contains("iPhone")));
    }

    @Test
    void testProductDetail() {
        Product product = productService.getProductDetail(1L);
        assertNotNull(product);
        assertEquals("iPhone 15 Pro", product.getName());
        assertTrue(product.getPrice().compareTo(new BigDecimal("8999.00")) == 0);

        assertThrows(RuntimeException.class, () ->
                productService.getProductDetail(9999L));
    }

    // ==================== 购物车 + 下单 + 支付 ====================

    @Test
    @Transactional
    void testCartAndOrderFlow() {
        Long userId = 1L;

        cartService.addCart(userId, 1L, 1);
        cartService.addCart(userId, 3L, 2);

        List<CartService.CartItemVO> cartItems = cartService.getCartList(userId);
        assertEquals(2, cartItems.size());

        Order order = orderService.createOrder(userId);
        assertNotNull(order);
        assertNotNull(order.getOrderNo());
        assertEquals(0, order.getStatus().intValue());
        assertTrue(order.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);

        Order paidOrder = orderService.payOrder(order.getOrderNo(), userId);
        assertEquals(1, paidOrder.getStatus().intValue());

        assertThrows(RuntimeException.class, () ->
                orderService.payOrder(order.getOrderNo(), userId));

        assertThrows(RuntimeException.class, () ->
                orderService.payOrder(order.getOrderNo(), 9999L));
    }

    @Test
    void testPayValidation() {
        assertThrows(RuntimeException.class, () ->
                orderService.payOrder("NONEXISTENT_ORDER", 1L));
    }
}
