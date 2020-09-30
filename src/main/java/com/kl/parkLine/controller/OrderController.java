package com.kl.parkLine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kl.parkLine.entity.Order;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.service.OrderService;
import com.kl.parkLine.vo.OrderVo;

@RestController
@RequestMapping(value="/orders")
public class OrderController
{
    @Autowired 
    private OrderService orderService;  
    
    /**
     * 获取用户信息
     * @return
     */
    @GetMapping("/my")
    public Page<OrderVo> myOrders(Order order, Pageable pageable, Authentication auth)
    {
        return orderService.fuzzyFindPage(order, pageable, auth);
    }
    
    /**
     * 获取用户信息
     * @return
     */
    @GetMapping(value = "/{userId}")
    @PreAuthorize("hasPermission(#user, 'read')")
    public User getUser(@PathVariable("userId") Integer userId, 
            @PathVariable("userId") User user)
    {
        return user;
    }
}
