package com.kl.parkLine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kl.parkLine.entity.Car;
import com.kl.parkLine.json.RestResult;
import com.kl.parkLine.service.CarService;
import com.kl.parkLine.util.Const;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;


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
     * @param car 车辆信息
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value="添加车辆并且绑定到当前登录用户", notes="若首次添加车牌，则自动创建车辆数据")
    @ApiImplicitParams({
        @ApiImplicitParam(name="Authorization", value="登录令牌",required=true, paramType="header"),
        @ApiImplicitParam(name="carNo",value="车牌号码",required=true)
    })
    public RestResult<Car> add(Authentication authentication, 
            @ApiIgnore @RequestBody Car car)
    {
        RestResult<Car> restResult = new RestResult<Car>();
        restResult.setRetCode(Const.RET_OK);
        restResult.setErrMsg("");

        //将车辆绑定到当前用户
        carService.addCar(authentication.getName(), car);
        return restResult;
    }
}
