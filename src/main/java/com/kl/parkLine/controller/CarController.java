package com.kl.parkLine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kl.parkLine.entity.Car;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.RestResult;
import com.kl.parkLine.service.CarService;
import com.kl.parkLine.util.Const;
import com.kl.parkLine.vo.CarVo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;


@RestController
@RequestMapping(value="/cars")
@Api(tags = "车辆管理")
public class CarController
{
    @Autowired 
    private CarService carService;  
    
    /**
     * 添加车辆，并且绑定到登陆用户
     * @param authentication 当前登录用户
     * @param carNo 车牌号码
     * @return
     */
    @PostMapping("/bind")
    @ApiOperation(value="将车牌绑定到当前登录用户", notes="一个用户可以绑定多个车牌，但是一个车牌只能绑定到一个用户，必须先将车牌解绑后，才能绑定到新的用户")
    @ApiImplicitParam(name="Authorization", value="登录令牌",required=true, paramType="header")
    public RestResult<Car> bind(@ApiParam(name="车牌号码") String carNo, Authentication auth)
    {
        try
        {
            //将车辆绑定到当前用户
            carService.bind(auth.getName(), carNo);
            return RestResult.success();
        }
        catch (BusinessException e)
        {
            return RestResult.failed(e.getMessage());
        }
    }
    
    /**
     * 解绑车辆
     * @param authentication 当前登录用户
     * @param carNo 车牌号码
     * @return
     */
    @PostMapping("/unbind")
    @ApiOperation(value="将车牌从绑定用户解绑", notes="若车牌号不存在，接口会自动创建车辆但是并不绑定到用户，返回成功；如果车辆当前并未绑定用户，接口直接返回成功")
    @ApiImplicitParam(name="Authorization", value="登录令牌",required=true, paramType="header")
    public RestResult<Car> unbind(@ApiParam(name="车牌号码") String carNo)
    {
        RestResult<Car> restResult = new RestResult<Car>();
        //将车辆解绑
        carService.unbind(carNo);
        restResult.setRetCode(Const.RET_OK);
        return restResult;
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
    public RestResult<Page<CarVo>> find(@ApiParam(name="查询条件",type="query")CarVo car, 
            @ApiParam(name="分页信息",type="query") Pageable pageable, Authentication auth)
    {
        //TODO:实现功能
        return null;
        /*List<CarVo> carVos = new ArrayList<CarVo>();
        return new PageImpl<>(carVos, pageable, carVos.size());*/
    }
}
