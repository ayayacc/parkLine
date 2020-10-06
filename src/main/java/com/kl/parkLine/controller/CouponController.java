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
import com.kl.parkLine.service.CouponService;

@RestController
@RequestMapping(value="/coupons")
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
    @GetMapping("/apply/{couponDefId}")
    public Coupon apply(@PathVariable("couponDefId") Integer couponDefId, 
            @PathVariable("couponDefId") CouponDef couponDef, 
            Authentication auth) throws BusinessException
    {
        Coupon coupon = couponService.apply(couponDef, auth);
        return coupon;
    }
}
