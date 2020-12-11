package com.kl.parkLine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.kl.parkLine.entity.SmsCode;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.JwtToken;
import com.kl.parkLine.json.RestResult;
import com.kl.parkLine.json.SmsCheckParam;
import com.kl.parkLine.service.SmsCodeService;
import com.kl.parkLine.vo.SmsCodeVo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value="/sms", produces="application/json;charset=utf-8")
@Api(tags = "短信登录")
public class SmsController
{
    @Autowired
    private SmsCodeService smsCodeService;
    
    @Value("${spring.profiles.active}")
    private String active;
    
    /**
     * 无实际意义，为了生成文档使用
     * @param SmsCodeVo
     * @return
     */
    @PostMapping("/login")
    @ApiOperation(value="短信登录", notes="使用之前获取的短信验证码登录  若用户首次登录，则自动注册，登录成功后返回token，后续通过Authorization消息头标识用户，token有效期为1小时，快过期时，通过New-Token获取新令牌", tags="短信登录")
    public JwtToken smsLogin(@ApiParam @RequestBody SmsCheckParam SmsLoginParam)
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
    @ApiOperation(value="获取短信验证码", notes="验证码2分钟内有效，使用该验证码通过/sms/login登录  \n若用户首次登录，则自动注册")
    @ApiImplicitParam(name="mobile",value="手机号码",required=true)
    public RestResult<SmsCodeVo> getSmsCode(@ApiIgnore @RequestBody JSONObject o)
    {
        try
        {
            String mobile = (String) o.get("mobile");
            SmsCode smsCode = smsCodeService.sendSmsCode(mobile);
            SmsCodeVo codeVo = SmsCodeVo.builder().code(smsCode.getCode()).build();
            if (!active.equalsIgnoreCase("dev"))
            {
                return RestResult.success();  //正式机不返回验证码给前端
            }
            else
            {
                return RestResult.success(codeVo);
            }
        }
        catch (Exception e)
        {
            return RestResult.failed(e.getMessage());
        }
        
    }
}
