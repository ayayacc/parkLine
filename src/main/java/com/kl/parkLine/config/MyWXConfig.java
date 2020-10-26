package com.kl.parkLine.config;

import java.io.InputStream;

import org.springframework.context.annotation.Configuration;

import com.github.wxpay.sdk.WXPayConfig;

@Configuration
public class MyWXConfig implements WXPayConfig
{

    @Override
    public String getAppID()
    {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getHttpReadTimeoutMs()
    {
        // TODO Auto-generated method stub
        return 0;
    }

}
