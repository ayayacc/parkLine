package com.kl.parkLine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kl.parkLine.entity.Car;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.CarParam;
import com.kl.parkLine.json.DrivingLicenseVo;
import com.kl.parkLine.json.RestResult;
import com.kl.parkLine.service.CarService;
import com.kl.parkLine.service.OrderService;
import com.kl.parkLine.vo.CarVo;
import com.kl.parkLine.vo.ParkLocationVo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;


@RestController
@RequestMapping(value="/cars", produces="application/json;charset=utf-8")
@Api(tags = "车辆管理")
public class CarController
{
    @Autowired 
    private CarService carService;  
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 添加车辆，并且绑定到登陆用户
     * @param authentication 当前登录用户
     * @param carNo 车牌号码
     * @return
     */
    @PostMapping("/bind")
    @ApiOperation(value="将车牌绑定到当前登录用户", notes="一个用户可以绑定多个车牌，但是一个车牌只能绑定到一个用户，必须先将车牌解绑后，才能绑定到新的用户")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<CarVo> bind(@ApiParam(name="车牌号码", required=true) @RequestBody(required=true) CarParam bindCarParam, 
            Authentication auth)
    {
        try
        {
            //将车辆绑定到当前用户
            Car car = carService.bind(auth.getName(), bindCarParam);
            return RestResult.success(CarVo.builder().carId(car.getCarId()).build());
        }
        catch (BusinessException e)
        {
            return RestResult.failed(e.getMessage());
        }
    }
    
    @PostMapping("/lock/{carId}")
    @ApiOperation(value="上传行驶证锁定车辆", notes="通过上传行驶证照片，锁定车辆，同时强制解绑未上传行驶证的绑定关系,上传行驶证后，只有本人和管理员可以进行解绑")
    @ApiImplicitParam(name="Authorization", value="登录令牌",required=true, paramType="header")
    public RestResult<DrivingLicenseVo> lock(@ApiParam(name="车辆Id", type="string")@PathVariable Integer carId, 
            @ApiIgnore @PathVariable(name = "carId", required = true) Car car,
            @ApiParam(name="行驶证照片照片") @RequestParam MultipartFile licenseImg, Authentication auth)
    {
        if (null == car)
        {
            return RestResult.failed(String.format("无效的车辆Id: %d", carId));
        }
        
        try
        {
            //上传车辆行驶证,锁定车辆
            return RestResult.success(carService.lock(auth.getName(), car, licenseImg));
        }
        catch (Exception e)
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
    @GetMapping(value = "/unbind/{carId}")
    @ApiOperation(value="将车牌从绑定用户解绑", notes="若车牌号不存在，接口会自动创建车辆但是并不绑定到用户，返回成功；如果车辆当前并未绑定用户，接口直接返回成功")
    @ApiImplicitParam(name="Authorization", value="登录令牌",required=true, paramType="header")
    public RestResult<Car> unbind(@ApiParam(name="车辆Id") @PathVariable(name = "carId", required = true) Integer carId,
            @ApiIgnore @PathVariable(name = "carId", required = true) Car car,
            Authentication auth)
    {
        if (null == car)
        {
            return RestResult.failed(String.format("无效的车辆Id: %d", carId));
        }
        
        try
        {
            //将车辆解绑
            carService.unbind(car);
            return RestResult.success();
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
    public RestResult<Page<CarVo>> find(@ApiParam(name="查询条件",type="query")CarVo carVo, 
            @ApiParam(name="分页信息",type="query") Pageable pageable, Authentication auth)
    {
        return RestResult.success(carService.fuzzyFindPage(carVo, pageable, auth.getName()));
    }
    
    /**
     * 根据车牌号查找停车位置信息
     * @param couponDef 查询条件
     * @param pageable 分页条件
     * @param auth 当前登录用户
     * @return 查询结果
     */
    @GetMapping("/getParkLocation/{carId}")
    @ApiOperation(value="根据车牌号查找停车地点", notes="根据车牌号查找停车地点")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<ParkLocationVo> getParkLocation(@ApiParam(name="车辆Id") @PathVariable(name = "carId", required = true) Integer carId,
            @ApiIgnore @PathVariable(name = "carId", required = true) Car car,
            Authentication auth)
    {
        if (null == car)
        {
            return RestResult.failed(String.format("无效的车辆Id: %d", carId));
        }
        
        try
        {
            //找到停车场信息
            return RestResult.success(orderService.findParkLocationByCar(car));
        }
        catch (Exception e)
        {
            return RestResult.failed(e.getMessage());
        }
    }
}
