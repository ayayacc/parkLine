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
import com.kl.parkLine.service.OrderService;
import com.kl.parkLine.vo.OrderVo;

@RestController
@RequestMapping(value="/orders")
public class OrderController
{
    @Autowired 
    private OrderService orderService;  
    
    /**
     * 获取我的订单信息,终端用户使用
     * @param couponDef 查询条件
     * @param pageable 分页条件
     * @param auth 当前登录用户
     * @return 订单VO对象
     */
    @GetMapping("/my")
    public Page<OrderVo> myOrders(Order order, Pageable pageable, Authentication auth)
    {
        return orderService.fuzzyFindPage(order, pageable, auth);
    }
    
    /**
     * 分页查询订单，系统管理员使用
     * @param couponDef 查询条件
     * @param pageable 分页条件
     * @param auth 当前登录用户
     * @return 订单查询结果
     */
    @GetMapping("/find")
    public Page<OrderVo> find(Order order, Pageable pageable, Authentication auth)
    {
        return orderService.fuzzyFindPage(order, pageable, auth);
    }
    
    /**
     * 查询订单明细
     * @param orderId 订单Id
     * @return 订单明细
     */
    @GetMapping(value = "/{orderId}")
    @PreAuthorize("hasPermission(#order, 'read')")
    public Order getOrder(@PathVariable("orderId") Integer orderId, 
            @PathVariable("orderId") Order order)
    {
        return order;
    }
}
