package com.kl.parkLine.controller;

import java.math.BigDecimal;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kl.parkLine.entity.User;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.DecryptionParam;
import com.kl.parkLine.json.DecryptionPhoneNoResult;
import com.kl.parkLine.json.MyInfo;
import com.kl.parkLine.json.RestResult;
import com.kl.parkLine.json.SmsCheckParam;
import com.kl.parkLine.json.MobileBindResult;
import com.kl.parkLine.service.UserService;
import com.kl.parkLine.vo.UserVo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value="/users", produces="application/json;charset=utf-8")
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
        userService.save(user);
        return RestResult.success();
    }
    
    @GetMapping("/my")
    @ApiOperation(value="查询我的汇总信息", notes="查询我的汇总信息")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<MyInfo> myInfo(Authentication auth)
    {
        return RestResult.success(userService.myInfo(auth.getName()));
    }
    
    @GetMapping("/my/walletBalance")
    @ApiOperation(value="查询我的钱包余额", notes="查询我的钱包余额")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<BigDecimal> walletBalance(Authentication auth)
    {
        User user = userService.findByName(auth.getName());
        return RestResult.success(user.getBalance());
    }
    
    @GetMapping("/my/setQuickPay")
    @ApiOperation(value="设置快捷支付", notes="设置快捷支付")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<Object> setQuickPay(Authentication auth, @ApiParam(name="是否开通", required=true) @RequestParam(value="isQuickPay",required=true) Boolean isQuickPay)
    {
        userService.setQuickPay(auth.getName(), isQuickPay);
        return RestResult.success();
    }
    
    @PostMapping("/my/bindMobile")
    @ApiOperation(value="绑定手机号", notes="绑定手机号")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<Object> checkValidCode(Authentication auth, @ApiParam(name="绑定手机号码参数", required=true) @RequestBody(required=true) SmsCheckParam smsCheckParam) throws BusinessException
    {
        userService.checkValidCode(auth.getName(), smsCheckParam);
        return RestResult.success();
    }
    
    @GetMapping("/isMobileBinded")
    @ApiOperation(value="检查用户是否绑定了手机号", notes="检查用户是否绑定了手机号")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<MobileBindResult> isMobileProvided(Authentication auth)
    {
        return RestResult.success(userService.isMobileProvided(auth.getName()));
    }
    
    @PostMapping("/decryption")
    @ApiOperation(value="解密微信获取的敏感信息", notes="检查用户是否绑定了手机号")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<DecryptionPhoneNoResult> decryption(Authentication auth, @ApiParam(name="解密参数", required=true) @RequestBody(required=true) DecryptionParam decryptionParam) throws Exception
    {
        return RestResult.success(userService.decryption(auth.getName(), decryptionParam));
    }
}
