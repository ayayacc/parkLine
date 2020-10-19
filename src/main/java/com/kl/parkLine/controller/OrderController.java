package com.kl.parkLine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kl.parkLine.entity.Order;
import com.kl.parkLine.json.PayOrderParam;
import com.kl.parkLine.json.RestResult;
import com.kl.parkLine.service.OrderService;
import com.kl.parkLine.vo.OrderVo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value="/orders")
@Api(tags="订单管理")
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
    @ApiOperation(value="分页查询订单", notes="分页批量查询订单")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<Page<OrderVo>> find(@ApiParam(name="查询条件",type="query")OrderVo orderVo, 
            @ApiParam(name="分页信息",type="query") Pageable pageable, Authentication auth)
    {
        return RestResult.success(orderService.fuzzyFindPage(orderVo, pageable, auth.getName()));
    }
    
    /**
     * 查询订单明细
     * @param orderId 订单Id
     * @return 订单明细
     */
    @GetMapping(value = "/{orderId}")
    @PreAuthorize("hasPermission(#order, 'read')")
    @ApiOperation(value="查询订单明细", notes="根据订单Id")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<OrderVo> getOrder(@ApiParam(name="订单Id",type="path") @PathVariable("orderId") Integer orderId, 
            @ApiIgnore @PathVariable("orderId") Order order)
    {
        if (null == order)
        {
            return RestResult.failed(String.format("无效的订单Id: %d", orderId));
        }
        else 
        {
            OrderVo orderVo = OrderVo.builder()
                    .code(order.getCode())
                    .orderId(order.getOrderId())
                    .status(order.getStatus())
                    .parkId(order.getPark().getParkId())
                    .parkName(order.getPark().getName())
                    .carNo(order.getCar().getCarNo())
                    .carId(order.getCar().getCarId())
                    .type(order.getType())
                    .build();
            return RestResult.success(orderVo);
        }
    }
    
    /**
     * 根据车牌号查询等待付款的订单
     */
    @GetMapping("/needToPay")
    @ApiOperation(value="找到等待支付的订单", 
        notes="1.订单拥有者是登录用户;2.订单拥有者为空，但是车辆绑定到登录用户的未支付订单")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<Page<OrderVo>> getNeedToPay(@ApiParam(name="分页信息",type="query") Pageable pageable,
            Authentication auth)
    {
        return RestResult.success(orderService.needToPay(auth.getName(), pageable));
    }
    
    /**
     * 支付订单
     */
    @PostMapping("/pay")
    @ApiOperation(value="支付订单", notes="发起订单支付")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<Object> pay(@ApiParam(name="订单支付参数", required=true) @RequestBody PayOrderParam payParam)
    {
        //TODO: 实现订单支付
        return null;
    }
    
    /**
     * 找到可以开票的订单
     */
    @GetMapping("/invoiceable")
    @ApiOperation(value="找到可以开票的订单", 
        notes="已经付款，未取消，未进入开票申请")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<Page<OrderVo>> invoiceable(@ApiParam(name="分页信息",type="query") Pageable pageable,
            Authentication auth)
    {
        return RestResult.success(orderService.invoiceable(auth.getName(), pageable));
    }
    
}
