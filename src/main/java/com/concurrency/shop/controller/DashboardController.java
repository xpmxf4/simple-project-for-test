package com.concurrency.shop.controller;

import com.concurrency.shop.domain.coupon.CouponRepository;
import com.concurrency.shop.domain.order.OrderRepository;
import com.concurrency.shop.domain.product.ProductRepository;
import com.concurrency.shop.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 대시보드 컨트롤러
 */
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final OrderRepository orderRepository;

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("coupons", couponRepository.findAll());
        model.addAttribute("orders", orderRepository.findAll());

        return "dashboard";
    }
}
