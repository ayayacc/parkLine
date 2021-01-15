package com.kl.parkLine.feign;

import org.springframework.stereotype.Component;

import com.kl.parkLine.json.QqMapSearchResult;
import com.kl.parkLine.json.WxAccessTokenResult;
import com.kl.parkLine.json.WxCode2SessionResult;

@Component
public class FeignClientFallback implements IWxFeignClient, IMapFeignClient
{

    @Override
    public WxCode2SessionResult code2Session(String appId, String appSecret,
            String code)
    {
        WxCode2SessionResult result = new WxCode2SessionResult();
        result.setErrmsg("code2Session exception");
        return result;
    }

    @Override
    public QqMapSearchResult search(String boundary, String key, String keyword,
            String sig)
    {
        QqMapSearchResult result = new QqMapSearchResult();
        result.setMessage("qq map search exception");
        return result;
    }

    @Override
    public WxAccessTokenResult getAccessToken(String openId, String openSecret)
    {
        WxAccessTokenResult result = new WxAccessTokenResult();
        result.setErrmsg("getAccessToken exception");
        return null;
    }
    
}