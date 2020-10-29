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
import com.kl.parkLine.vo.ParkVo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value="/parks")
@Api(tags = "停车场管理")
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
    @ApiOperation(value="保存停车场数据", notes="创建/修改停车场")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<Park> eidt(@ApiParam(name="停车场信息") @RequestBody Park park,
            Authentication auth)
    {
        try
        {
            parkService.edit(park, auth.getName());
            return RestResult.success();
        }
        catch (Exception e)
        {
            return RestResult.failed(e.getMessage());
        }
    }
    
    /**
     * 获取停车场明细
     * @param parkId 停车场Id
     * @return 停车场明细
     */
    @GetMapping(value = "/{parkId}")
    @PreAuthorize("hasPermission(#park, 'park')")
    @ApiOperation(value="查询停车场明细", notes="查看单个停车场明细")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<ParkVo> getPark(@ApiParam(name="停车场Id",type="path") @PathVariable("parkId") Integer parkId, 
            @ApiIgnore @PathVariable("parkId") Park park)
    {
        if (null == park)
        {
            return RestResult.failed(String.format("无效的停车场Id: %d", parkId));
        }
        else
        {
            ParkVo parkVo = ParkVo.builder()
                    .parkId(park.getParkId())
                    .code(park.getCode())
                    .name(park.getName())
                    .build();
            return RestResult.success(parkVo);
        }
    }
    
    /**
     * 分页查询停车场信息
     * @param park 查询条件
     * @param pageable 分页条件
     * @param auth 当前登录用户
     * @return 查询结果
     */
    @GetMapping("/find")
    @ApiOperation(value="分页查询停车场信息", notes="分页查询停车场信息")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<Page<ParkVo>> find(@ApiParam(name="查询条件",type="query")ParkVo parkVo, 
            @ApiParam(name="分页信息",type="query") Pageable pageable, Authentication auth)
    {
        return RestResult.success(parkService.fuzzyFindPage(parkVo, pageable, auth.getName()));
    }
}
