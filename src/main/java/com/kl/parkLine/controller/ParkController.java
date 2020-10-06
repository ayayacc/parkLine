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

import com.kl.parkLine.entity.Park;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.RestResult;
import com.kl.parkLine.service.ParkService;
import com.kl.parkLine.util.Const;
import com.kl.parkLine.vo.ParkVo;

@RestController
@RequestMapping(value="/parks")
public class ParkController
{
    @Autowired 
    private ParkService parkService;  
    
    /**
     * 保存停车场信息
     * @param park
     * @return 保存结果
     * @throws BusinessException
     */
    @PostMapping("/save")
    public RestResult save(@RequestBody Park park) throws BusinessException
    {
        RestResult restResult = new RestResult();
        restResult.setRetCode(Const.RET_OK);
        restResult.setErrMsg("");

        //将车辆绑定到当前用户
        parkService.save(park);
        return restResult;
    }
    
    /**
     * 获取停车场明细
     * @param parkId 停车场Id
     * @return 停车场明细
     */
    @GetMapping(value = "/{parkId}")
    @PreAuthorize("hasPermission(#park, 'park')")
    public Park getPark(@PathVariable("parkId") Integer parkId, 
            @PathVariable("parkId") Park park)
    {
        return park;
    }
    
    /**
     * 分页查询停车场信息
     * @param couponDef 查询条件
     * @param pageable 分页条件
     * @param auth 当前登录用户
     * @return 查询结果
     */
    @GetMapping("/find")
    public Page<ParkVo> findParks(Park park, Pageable pageable, Authentication auth)
    {
        return parkService.fuzzyFindPage(park, pageable, auth);
    }
}
