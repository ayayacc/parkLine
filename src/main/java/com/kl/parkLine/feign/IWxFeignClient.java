package com.kl.parkLine.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.kl.parkLine.json.WxAccessTokenResult;
import com.kl.parkLine.json.WxCode2SessionResult;
import com.kl.parkLine.json.WxSendMsgResult;
import com.kl.parkLine.json.WxTpltMsg;
import com.kl.parkLine.json.WxUserInfo;

@FeignClient(name="wxFeignClient", url="https://api.weixin.qq.com")
public interface IWxFeignClient
{
    //"https://api.weixin.qq.com/sns/jscode2session?appid={APPID}&secret={APPSECRET}&js_code={JSCODE}&grant_type=authorization_code";
    @GetMapping(value = "/sns/jscode2session?appid={APPID}&secret={APPSECRET}&js_code={JSCODE}&grant_type=authorization_code")
    public WxCode2SessionResult code2Session(@RequestParam("APPID")String appId, 
            @RequestParam("APPSECRET")String appSecret, @RequestParam("JSCODE")String code);
    
    //https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET
    @GetMapping(value = "/cgi-bin/token?grant_type=client_credential&appid={APPID}&secret={APPSECRET}")
    public WxAccessTokenResult getAccessToken(@RequestParam("APPID")String appId, 
            @RequestParam("APPSECRET")String appSecret);
    
    //https://api.weixin.qq.com/cgi-bin/user/info?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN
    @GetMapping(value = "/cgi-bin/user/info?access_token={ACCESS_TOKEN}&openid={OPENID}&lang=zh_CN")
    public WxUserInfo getUserInfo(@RequestParam("ACCESS_TOKEN")String accessToken, 
            @RequestParam("OPENID")String openId);
    
    //https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=ACCESS_TOKEN
    @PostMapping(value = "/cgi-bin/message/template/send?access_token= {ACCESS_TOKEN}")
    public WxSendMsgResult sendTpltMsg(@RequestParam("ACCESS_TOKEN")String accessToken, @RequestBody WxTpltMsg wxTpltMsg);
}