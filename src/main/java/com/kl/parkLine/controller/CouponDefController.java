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
import com.kl.parkLine.util.Const;
import com.kl.parkLine.vo.CouponDefVo;

@RestController
@RequestMapping(value="/couponDefs")
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
    public RestResult save(@RequestBody CouponDef couponDef, String remark) throws BusinessException
    {
        RestResult restResult = new RestResult();
        restResult.setRetCode(Const.RET_OK);
        restResult.setErrMsg("");

        //保存当前优惠券
        couponDefService.save(couponDef, remark);
        return restResult;
    }
    
    /**
     * 查询优惠券定义明细
     * @param couponDefId 优惠券定义Id
     * @param couponDef 优惠券定义对象
     * @return 优惠券定义明细
     */
    @GetMapping(value = "/{couponId}")
    @PreAuthorize("hasPermission(#couponDef, 'read')")
    public CouponDef getCouponDef(@PathVariable("couponDefId") Integer couponDefId, 
            @PathVariable("couponDefId") CouponDef couponDef)
    {
        return couponDef;
    }
    
    /**
     * 分页查询优惠券定义列表
     * @param couponDef 查询条件
     * @param pageable 分页条件
     * @param auth 当前登录用户
     * @return 优惠券定义查询结果
     */
    @GetMapping("/find")
    public Page<CouponDefVo> find(CouponDef couponDef, Pageable pageable, Authentication auth)
    {
        return couponDefService.fuzzyFindPage(couponDef, pageable, auth);
    }
}
