package com.kl.parkLine.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kl.parkLine.entity.User;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value="/users")
@Api(tags = "用户管理")
public class UserController
{
    /**
     * 获取用户信息
     * @return
     */
    @GetMapping(value = "/{userId}")
    @PreAuthorize("hasPermission(#user, 'read')")
    @ApiOperation(value="查询用户", notes="根据用户Id查询单个用户信息")
    @ApiImplicitParams({
        @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header"),
        @ApiImplicitParam(name="userId", value="优惠券用户Id", required=true, paramType="path")
    })
    public User getUser(@ApiParam(name="用户Id",type="path") @PathVariable("userId") Integer userId, 
            @ApiIgnore @PathVariable("userId") User user)
    {
        return user;
    }
}
