package com.kl.parkLine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kl.parkLine.entity.Coupon;
import com.kl.parkLine.entity.CouponDef;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.RestResult;
import com.kl.parkLine.service.CouponService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value="/coupons")
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
    @ApiImplicitParams({
        @ApiImplicitParam(name="mobile", value="用户手机号", required=true),
        @ApiImplicitParam(name="validCode", value="验证码", required=true)
    })
    @GetMapping("/apply/{couponDefId}")
    public RestResult<Coupon> apply(@ApiParam(name="优惠券定义Id",type="path") @PathVariable("couponDefId") Integer couponDefId, 
            @ApiIgnore @PathVariable("couponDefId") CouponDef couponDef, Authentication auth)
    {
        try
        {
            Coupon coupon = couponService.apply(couponDef, auth.getName());
            return RestResult.success(coupon);
        }
        catch (Exception e)
        {
            return RestResult.failed(e.getMessage());
        }
    }
}
