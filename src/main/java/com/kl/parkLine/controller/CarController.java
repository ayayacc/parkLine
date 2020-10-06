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

@RestController
@RequestMapping(value="/cars")
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
    public RestResult add(Authentication authentication, @RequestBody Car car)
    {
        RestResult restResult = new RestResult();
        restResult.setRetCode(Const.RET_OK);
        restResult.setErrMsg("");

        //将车辆绑定到当前用户
        carService.addCar(authentication.getName(), car);
        return restResult;
    }
}
