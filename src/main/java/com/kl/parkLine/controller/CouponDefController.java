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

import com.kl.parkLine.entity.CouponDef;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.RestResult;
import com.kl.parkLine.service.CouponDefService;
import com.kl.parkLine.vo.CouponDefVo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value="/couponDefs", produces="application/json;charset=utf-8")
@Api(tags = "优惠券定义")
public class CouponDefController
{
    @Autowired 
    private CouponDefService couponDefService;  
    
    /**
     * 保存优惠券定义
     * @param couponDef 被修改的优惠券
     * @param remark 修改的备注
     * @return
     * @throws BusinessException
     */
    @PostMapping("/save")
    @ApiOperation(value="保存优惠券定义", notes="创建/修改一个优惠券定义")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<CouponDefVo> save(@ApiParam(name="优惠券定义") @RequestBody CouponDef couponDef) throws BusinessException
    {
        couponDefService.save(couponDef);
        CouponDefVo couponDefVo = CouponDefVo.builder().code(couponDef.getCode())
                .couponDefId(couponDef.getCouponDefId()).build();
        return RestResult.success(couponDefVo);
    }
    
    /**
     * 查询优惠券定义明细
     * @param couponDefId 优惠券定义Id
     * @param couponDef 优惠券定义对象
     * @return 优惠券定义明细
     */
    @GetMapping(value = "/{couponId}")
    @PreAuthorize("hasPermission(#couponDef, 'read')")
    @ApiOperation(value="查询优惠券定义明细", notes="查看单个优惠券定义明细")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<CouponDefVo> getCouponDef(@ApiParam(name="优惠券定义Id",type="path") @PathVariable("couponDefId") Integer couponDefId, 
            @ApiIgnore @PathVariable("couponDefId") CouponDef couponDef)
    {
        if (null == couponDef)
        {
            return RestResult.failed(String.format("无效的优惠券定义Id: %d", couponDefId));
        }
        else 
        {
            CouponDefVo couponDefVo = CouponDefVo.builder()
                    .name(couponDef.getName())
                    .code(couponDef.getCode())
                    .build();
            return RestResult.success(couponDefVo);
        }
    }
    
    /**
     * 分页查询优惠券定义列表
     * @param couponDef 查询条件
     * @param pageable 分页条件
     * @param auth 当前登录用户
     * @return 优惠券定义查询结果
     */
    @GetMapping("/find")
    @ApiOperation(value="分页查询优惠券定义清单", notes="查询优惠券定义清单")
    public RestResult<Page<CouponDefVo>> find(@ApiParam(name="查询条件",type="query") CouponDefVo couponDefVo, 
            @ApiParam(name="分页信息",type="query") Pageable pageable, Authentication auth)
    {
        return RestResult.success(couponDefService.fuzzyFindPage(couponDefVo, pageable, auth.getName()));
    }
}
