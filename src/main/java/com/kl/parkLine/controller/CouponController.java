package com.kl.parkLine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import com.kl.parkLine.vo.CouponVo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
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
                    .amt(coupon.getCouponDef().getAmt())
                    .name(coupon.getCouponDef().getName())
                    .status(coupon.getStatus())
                    .startDate(coupon.getStartDate())
                    .endDate(coupon.getEndDate())
                    .couponDefCode(couponDef.getCode())
                    .couponDefId(couponDef.getCouponDefId())
                    .owner(auth.getName())
                    .minAmt(couponDef.getMinAmt())
                    .build();
            return RestResult.success(couponVo);
        }
        catch (Exception e)
        {
            return RestResult.failed(e.getMessage());
        }
    }
    
    /**
     * 分页查询车辆列表
     * @param car 查询条件
     * @param pageable 分页条件
     * @param auth 当前登录车辆
     * @return 车辆查询结果
     */
    @GetMapping("/find")
    @ApiOperation(value="查询车辆清单", notes="分页查询车辆清单")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<Page<CouponVo>> find(@ApiParam(name="查询条件",type="query")CouponVo couponVo, 
            @ApiParam(name="分页信息",type="query") Pageable pageable, Authentication auth)
    {
        return RestResult.success(couponService.fuzzyFindPage(couponVo, pageable, auth.getName()));
    }
}
