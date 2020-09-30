package com.kl.parkLine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kl.parkLine.entity.Car;
import com.kl.parkLine.entity.User;
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
     * 获取用户信息
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
    
    /**
     * 获取用户信息
     * @return
     */
    @GetMapping(value = "/{userId}")
    @PreAuthorize("hasPermission(#user, 'read')")
    public User getUser(@PathVariable("userId") Integer userId, 
            @PathVariable("userId") User user)
    {
        return user;
    }
}
