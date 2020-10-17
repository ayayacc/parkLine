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

import com.kl.parkLine.entity.User;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.ChargeWalletParam;
import com.kl.parkLine.json.RestResult;
import com.kl.parkLine.service.UserService;
import com.kl.parkLine.vo.UserVo;

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
    @Autowired
    private UserService userService;
    
    /**
     * 根据用户Id查询单个用户信息
     * @param userId 用户Id
     * @param user
     * @return
     */
    @GetMapping(value = "/{userId}")
    @PreAuthorize("hasPermission(#user, 'read')")
    @ApiOperation(value="查询用户", notes="根据用户Id查询单个用户信息")
    @ApiImplicitParams({
        @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header"),
        @ApiImplicitParam(name="userId", value="用户Id", required=true, paramType="path")
    })
    public RestResult<UserVo> getUser(@ApiParam(name="用户Id",type="path") @PathVariable("userId") Integer userId, 
            @ApiIgnore @PathVariable("userId") User user)
    {
        if (null == user)
        {
            return RestResult.failed(String.format("无效的用户Id: %d", userId));
        }
        else
        {
            UserVo userVo = UserVo.builder()
                    .userId(user.getUserId())
                    .name(user.getName())
                    .mobile(user.getMobile())
                    .isEnable(user.isEnabled())
                    .gender(user.getGender())
                    .build();
            return RestResult.success(userVo);
        }
    }
    
    /**
     * 分页查询用户列表
     * @param user 查询条件
     * @param pageable 分页条件
     * @param auth 当前登录用户
     * @return 用户查询结果
     */
    @GetMapping("/find")
    @ApiOperation(value="查询用户清单", notes="分页查询用户清单")
    public RestResult<Page<UserVo>> find(@ApiParam(name="查询条件",type="query")UserVo userVo, 
            @ApiParam(name="分页信息",type="query") Pageable pageable, Authentication auth)
    {
        return RestResult.success(userService.fuzzyFindPage(userVo, pageable, auth.getName()));
    }
    
    /**
     * 新增/编辑用户
     * @param user 用户信息
     * @param remark 修改的备注
     * @return
     * @throws BusinessException
     */
    @PostMapping("/save")
    @ApiOperation(value="新增/编辑用户", notes="普通用户只能编辑自己的信息，管理员可以编辑所有用户，name，Id等系统生成的字段不能修改")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<UserVo> save(@ApiParam(name="用户信息") @RequestBody User user)
    {
        try
        {
            userService.save(user);
            return RestResult.success();
        }
        catch (Exception e)
        {
            return RestResult.failed(e.getMessage());
        }
    }
    
    /**
     * 钱包充值
     */
    @PostMapping("/wallet/charge")
    @ApiOperation(value="钱包充值", notes="用户进行钱包充值操作")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<Object> chargeWallet(@ApiParam(name="充值参数", required=true) @RequestBody ChargeWalletParam walletChargeParam)
    {
        //TODO: 钱包充值
        return null;
    }
}
