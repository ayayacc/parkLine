package com.kl.parkLine.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.kl.parkLine.json.WxAccessTokenResult;
import com.kl.parkLine.json.WxCode2SessionResult;

@FeignClient(name="wxFeignClient", url="https://api.weixin.qq.com")
public interface IWxFeignClient
{
    //"https://api.weixin.qq.com/sns/jscode2session?appid={APPID}&secret={APPSECRET}&js_code={JSCODE}&grant_type=authorization_code";
    @GetMapping(value = "/sns/jscode2session?appid={APPID}&secret={APPSECRET}&js_code={JSCODE}&grant_type=authorization_code")
    public WxCode2SessionResult code2Session(@RequestParam("APPID")String appId, 
            @RequestParam("APPSECRET")String appSecret, @RequestParam("JSCODE")String code);
    
    //https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET
    @GetMapping(value = "/cgi-bin/token?grant_type=client_credential&appid={APPID}&secret={APPSECRET}")
    public WxAccessTokenResult getAccessToken(@RequestParam("APPID")String openId, 
            @RequestParam("APPSECRET")String openSecret);
}