package com.kl.parkLine.controller;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kl.parkLine.entity.Park;
import com.kl.parkLine.json.BaseEnumJson;
import com.kl.parkLine.json.NearbyParam;
import com.kl.parkLine.json.RestResult;
import com.kl.parkLine.service.ParkService;
import com.kl.parkLine.vo.ParkLocationVo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value="/parks", produces="application/json;charset=utf-8")
@Api(tags = "停车场管理")
public class ParkController
{
    @Autowired 
    private ParkService parkService;  
    
    @Autowired
    private WKTReader wktReader;
    
    @PostMapping("/nearby")
    @ApiOperation(value="附近停车场信息", notes="查询附近停车场")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<List<ParkLocationVo>> nearby(@ApiParam(name="查询条件",type="query") @RequestBody NearbyParam nearbyParam,
            Authentication auth) throws ParseException
    {
        Geometry point = wktReader.read(nearbyParam.getCenterPoint());
        Point centerPoint = point.getInteriorPoint();
        return RestResult.success(parkService.findNearby(centerPoint, nearbyParam.getDistanceKm()));
    }
    
    /**
     * 获取停车场明细
     * @param parkId 停车场Id
     * @return 停车场明细
     */
    @GetMapping(value = "/placeType/{parkId}")
    @ApiOperation(value="查询停车场明细", notes="查看单个停车场明细")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<List<BaseEnumJson>> getPlaceType(@ApiParam(name="停车场Id",type="path") @PathVariable("parkId") Integer parkId, 
            @ApiIgnore @PathVariable("parkId") Park park)
    {
        if (null == park)
        {
            return RestResult.failed(String.format("无效的停车场Id: %d", parkId));
        }
        else
        {
            return RestResult.success(parkService.getPlaceTypes(park));
        }
    }
}
