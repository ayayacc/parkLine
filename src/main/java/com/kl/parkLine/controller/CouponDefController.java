package com.kl.parkLine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

@RestController
@RequestMapping(value="/couponDefs")
public class CouponDefController
{
    @Autowired 
    private CouponDefService couponDefService;  
    
    /**
     * 保存优惠券定义
     * @return
     * @throws BusinessException 
     */
    @PostMapping("/save")
    public RestResult save(@RequestBody CouponDef couponDef) throws BusinessException
    {
        RestResult restResult = new RestResult();
        restResult.setRetCode(Const.RET_OK);
        restResult.setErrMsg("");

        //将车辆绑定到当前用户
        couponDefService.save(couponDef);
        return restResult;
    }
    
    /**
     * 查询优惠券定义
     * @return
     */
    @GetMapping(value = "/{userId}")
    @PreAuthorize("hasPermission(#couponDef, 'read')")
    public CouponDef getCouponDef(@PathVariable("userId") Integer userId, 
            @PathVariable("userId") CouponDef couponDef)
    {
        return couponDef;
    }
}
