package com.kl.parkLine.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.wxpay.sdk.WXPayUtil;
import com.kl.parkLine.component.Utils;
import com.kl.parkLine.service.UserService;
import com.kl.parkLine.xml.WxGzhMsg;

@RestController
@RequestMapping(value="/gzh")
public class WxGzhController
{
    private final static Logger logger = LoggerFactory.getLogger(WxGzhController.class);
    
    @Value("${wx.open.token}")
    private String gzhToken;
    
    @Autowired
    private HttpServletRequest request;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private Utils utils;
    
    /**
     * 验证消息的确来自微信服务器
     * @param signature 微信加密签名
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param echostr 随机字符串
     * @return 若确认此次GET请求来自微信服务器，请原样返回echostr参数内容，则接入生效，成为开发者成功，否则接入失败
     * @throws IOException
     */
    @GetMapping("/notify")
    public String validate(@RequestParam(name="signature")String signature,
            @RequestParam(name="timestamp")String timestamp, 
            @RequestParam(name="nonce")String nonce, 
            @RequestParam(name="echostr")String echostr) throws IOException
    {
        //字典排序
        List<String> params = new ArrayList<String>();
        params.add(gzhToken);
        params.add(timestamp);
        params.add(nonce);
        Collections.sort(params);
        String values = "";
        for (String param : params)
        {
            values += param;
        }
        
        //计算签名
        String sign = DigestUtils.sha1Hex(values);
        
        //签名校验
        if (sign.equalsIgnoreCase(signature))
        {
            return echostr;
        }
        else
        {
            return "";
        }
    }
    
    /**
     * 接收微信公众号通知
     */
    @PostMapping("/notify")
    public String gzhNotify() throws IOException
    {
        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader reader = request.getReader();
        while ((line = reader.readLine()) != null)
        {
            sb.append(line);
        }
        String notifyData = sb.toString();
        logger.info(notifyData);
        WxGzhMsg wxGzhRespMsg = null;
        try
        {
            Map<String, String> notifyMap = WXPayUtil.xmlToMap(notifyData);// 转换成map
            
            //转换微信公众号推送通知
            WxGzhMsg wxGzhMsg = WxGzhMsg.builder().event(notifyMap.get("Event"))
                    .toUserName(notifyMap.get("ToUserName"))
                    .fromUserName(notifyMap.get("FromUserName"))
                    .createTime(Integer.getInteger(notifyMap.get("CreateTime")))
                    .msgType(notifyMap.get("MsgType"))
                    .event(notifyMap.get("Event")).build();
            
            //关注/取消关注事件
            if (wxGzhMsg.getEvent().equals("subscribe"))
            {
                wxGzhRespMsg = userService.subscribeGzh(wxGzhMsg);
            }
            else if (wxGzhMsg.getEvent().equals("unsubscribe"))
            {
                userService.unsubscribeGzh(wxGzhMsg);
            }
        }
        catch (Exception e)
        {
            logger.error(String.format("Parse xml failed: %s", e.getMessage()));
        }  
        
        //转换xml回复微信
        String ret = "";
        if (null != wxGzhRespMsg)
        {
            ret = utils.convertToXml(wxGzhRespMsg);
        }
        return ret.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
    }
}
