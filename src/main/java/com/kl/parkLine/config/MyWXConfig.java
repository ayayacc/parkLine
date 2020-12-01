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
    
    @Value("${wx.app.secret}")
    private String appSecret;
    
    @Override
    public String getAppID()
    {
        return appId;
    }

    @Override
    public String getMchID()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getKey()
    {
        return appSecret;
    }

    @Override
    public InputStream getCertStream()
    {
        // TODO Auto-generated method stub
        return null;
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
