package com.kl.parkLine.config;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.github.wxpay.sdk.WXPayConfig;

@Configuration
public class MyWXConfig implements WXPayConfig
{

    @Value("${wx.app.id}")
    private String appId;
    
    @Value("${wx.pay.key}")
    private String wxPayKey;
    
    @Value("${wx.pay.mch.id}")
    private String mchId;
    
    @Override
    public String getAppID()
    {
        return appId;
    }

    @Override
    public String getMchID()
    {
        return mchId;
    }

    @Override
    public String getKey()
    {
        return wxPayKey;
    }

    @Override
    public InputStream getCertStream()
    {
        return getClass().getClassLoader().getResourceAsStream("apiclient_cert.p12");
    }

    @Override
    public int getHttpConnectTimeoutMs()
    {
        return 0;
    }

    @Override
    public int getHttpReadTimeoutMs()
    {
        return 0;
    }

}
