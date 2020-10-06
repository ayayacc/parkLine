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
     * 获取用户信息
     * @return
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
     * 获取停车场信息
     * @return
     */
    @GetMapping(value = "/{parkId}")
    @PreAuthorize("hasPermission(#park, 'park')")
    public Park getPark(@PathVariable("parkId") Integer parkId, 
            @PathVariable("parkId") Park park)
    {
        return park;
    }
    
    /**
     * 查询停车场信息
     * @return
     */
    @GetMapping("/find")
    public Page<ParkVo> findParks(Park park, Pageable pageable, Authentication auth)
    {
        return parkService.fuzzyFindPage(park, pageable, auth);
    }
}
