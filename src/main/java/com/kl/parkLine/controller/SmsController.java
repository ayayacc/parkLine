package com.kl.parkLine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.kl.parkLine.entity.SmsCode;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.JwtToken;
import com.kl.parkLine.json.RestResult;
import com.kl.parkLine.json.SmsLoginParam;
import com.kl.parkLine.service.SmsCodeService;
import com.kl.parkLine.util.Const;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value="/sms")
@Api(tags = "短信登录")
public class SmsController
{
    @Autowired
    private SmsCodeService smsCodeService;
    
    /**
     * 无实际意义，为了生成文档使用
     * @param SmsLoginParam
     * @return
     */
    @PostMapping("/login")
    @ApiOperation(value="短信登录", notes="使用之前获取的短信验证码登录  若用户首次登录，则自动注册", tags="短信登录")
    public JwtToken smsLogin(@ApiParam @RequestBody SmsLoginParam SmsLoginParam)
    {
        return null;
    }
    
    /**
     * 获取验证码
     * @param o json对象，包含手机号信息
     * @return 验证码
     * @throws BusinessException
     */
    @PostMapping("/getCode")
    @ApiOperation(value="获取短信验证码", notes="验证码2分钟内有效，使用该验证码通过/smslogin登录  \n若用户首次登录，则自动注册")
    @ApiImplicitParam(name="mobile",value="手机号码",required=true)
    public RestResult<SmsCode> getSmsCode(@ApiIgnore @RequestBody JSONObject o)
    {
        RestResult<SmsCode> restResult = new RestResult<SmsCode>();
        try
        {
            String mobile = (String) o.get("mobile");
            SmsCode code = smsCodeService.sendSmsCode(mobile);
            restResult.setRetCode(Const.RET_OK);
            restResult.setData(code);
        }
        catch (Exception e)
        {
            restResult.setRetCode(Const.RET_FAILED);
            restResult.setErrMsg(e.getMessage());
        }
        
        return restResult;
    }
}
