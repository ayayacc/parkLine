package com.kl.parkLine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kl.parkLine.entity.Coupon;
import com.kl.parkLine.entity.CouponDef;
import com.kl.parkLine.entity.Order;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.RestResult;
import com.kl.parkLine.service.CouponService;
import com.kl.parkLine.vo.CouponVo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value="/coupons", produces="application/json;charset=utf-8")
@Api(tags = "优惠券实例")
public class CouponController
{
    @Autowired
    private CouponService couponService;
    
    /**
     * 领取优惠券
     * @param couponId 被领取的优惠券定义
     * @param auth 当前登录用户
     * @return 优惠券领取结果
     * @throws BusinessException
     */
    @ApiOperation(value="领取优惠券", notes="领取某个优惠券定义的实例")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    @GetMapping("/apply/{couponDefId}")
    public RestResult<CouponVo> apply(@ApiParam(name="优惠券定义Id",type="path") @PathVariable("couponDefId") Integer couponDefId, 
            @ApiIgnore @PathVariable("couponDefId") CouponDef couponDef, Authentication auth)
    {
        try
        {
            Coupon coupon = couponService.apply(couponDef, auth.getName());
            CouponVo couponVo = CouponVo.builder().couponId(coupon.getCouponId())
                    .code(coupon.getCode())
                    .discount(coupon.getDiscount())
                    .activePrice(coupon.getActivePrice())
                    .name(coupon.getName())
                    .status(coupon.getStatus())
                    .startDate(coupon.getStartDate())
                    .endDate(coupon.getEndDate())
                    .couponDefCode(couponDef.getCode())
                    .couponDefCouponDefId(couponDef.getCouponDefId())
                    .ownerName(auth.getName())
                    .maxAmt(coupon.getMaxAmt())
                    .build();
            return RestResult.success(couponVo);
        }
        catch (Exception e)
        {
            return RestResult.failed(e.getMessage());
        }
    }
    
    /**
     * 分页查询优惠券列表
     * @param car 查询条件
     * @param pageable 分页条件
     * @param auth 当前登录优惠券
     * @return 优惠券查询结果
     */
    @GetMapping("/find")
    @ApiOperation(value="查询优惠券清单", notes="分页查询优惠券清单")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<Page<CouponVo>> find(@ApiParam(name="查询条件",type="query")CouponVo couponVo, 
            @ApiParam(name="分页信息",type="query") Pageable pageable, Authentication auth)
    {
        return RestResult.success(couponService.fuzzyFindPage(couponVo, pageable, auth.getName()));
    }
    
    /**
     * 查找订单可用的优惠券
     * @param car 查询条件
     * @param pageable 分页条件
     * @param auth 当前登录优惠券
     * @return 优惠券查询结果
     */
    @GetMapping("/available")
    @ApiOperation(value="查询适用于订单的优惠券清单", notes="根据订单Id分页查询适用的优惠券清单")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<Page<CouponVo>> available4Order(@ApiParam(name="查询条件",type="query") @RequestParam("orderId") Integer orderId, 
            @ApiIgnore @RequestParam("orderId") Order order,
            @ApiParam(name="分页信息",type="query") Pageable pageable, Authentication auth)
    {
        if (null == order)
        {
            return RestResult.failed(String.format("无效的订单Id: %d", orderId));
        }
        try
        {
            return RestResult.success(couponService.available4Order(order, pageable));
        }
        catch (BusinessException e)
        {
            return RestResult.failed(e.getMessage());
        }
    }
}
