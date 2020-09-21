package com.kl.parkLine.feign;

import org.springframework.stereotype.Component;

import com.kl.parkLine.json.WxCode2SessionResult;

@Component
public class WxFeignClientFallback implements IWxFeignClient
{

    @Override
    public WxCode2SessionResult code2Session(String appId, String appSecret,
            String code)
    {
        WxCode2SessionResult result = new WxCode2SessionResult();
        result.setErrmsg("code2Session exception");
        return result;
    }
    
}