package com.kl.parkLine.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.RestResult;
import com.kl.parkLine.service.SmsCodeService;
import com.kl.parkLine.util.Const;

@RestController
@RequestMapping(value="/smsCode")
public class SmsController
{
    @Autowired
    private SmsCodeService smsCodeService;
    
    /**
     * 获取验证码
     * @param o json对象，包含手机号信息
     * @return 验证码
     * @throws BusinessException
     */
    @PostMapping("/get")
    public RestResult getSmsCode(@RequestBody JSONObject o)
    {
        RestResult restResult = new RestResult();
        try
        {
            String mobile = (String) o.get("mobile");
            String code = smsCodeService.sendSmsCode(mobile);
            Map<String, String> ret = new HashMap<String, String>();
            ret.put("code", code);
            restResult.setRetCode(Const.RET_OK);
            restResult.setData(ret);
        }
        catch (Exception e)
        {
            restResult.setRetCode(Const.RET_FAILED);
            restResult.setErrMsg(e.getMessage());
        }
        
        return restResult;
    }
}
