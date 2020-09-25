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
import com.kl.parkLine.service.SmsCodeService;

@RestController
@RequestMapping(value="/smsCode")
public class SmsController
{
    @Autowired
    private SmsCodeService smsCodeService;
    
    /**
     * 接收信路通事件推送
     * @param request 请求消息体
     * @param xltEvt 信路通事件对象
     * @return
     * @throws BusinessException 
     */
    @PostMapping("/get")
    public Map<String, String> getSmsCode(@RequestBody JSONObject o) throws BusinessException
    {
        String mobile = (String) o.get("mobile");
        String code = smsCodeService.sendSmsCode(mobile);
        Map<String, String> ret = new HashMap<String, String>();
        ret.put("code", code);
        return ret;
    }
}
