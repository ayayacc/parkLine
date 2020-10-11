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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value="/orders")
@Api(tags = "订单管理")
public class OrderController
{
    @Autowired 
    private OrderService orderService;  
    
    /**
     * 分页查询订单
     * @param order 查询条件
     * @param pageable 分页条件
     * @param auth 当前登录用户
     * @return 订单查询结果
     */
    @GetMapping("/find")
    @ApiOperation(value="分页查询订单，系统管理员使用", notes="若首次添加车牌，则自动创建车辆数据")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public Page<OrderVo> find(@ApiParam(name="查询条件",type="query")Order order, 
            @ApiParam(name="分页信息",type="query") Pageable pageable, 
            Authentication auth)
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
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public Order getOrder(@ApiParam(name="订单Id",type="path") @PathVariable("orderId") Integer orderId, 
            @ApiIgnore @PathVariable("orderId") Order order)
    {
        return order;
    }
}
