package com.kl.parkLine.feign;

import org.springframework.stereotype.Component;

import com.kl.parkLine.json.QqMapSearchResult;
import com.kl.parkLine.json.WxAccessTokenResult;
import com.kl.parkLine.json.WxCode2SessionResult;
import com.kl.parkLine.json.WxSendMsgResult;
import com.kl.parkLine.json.WxTpltMsg;
import com.kl.parkLine.json.WxUserInfo;

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
        return result;
    }

    @Override
    public WxSendMsgResult sendTpltMsg(String accessToken, WxTpltMsg wxTpltMsg)
    {
        WxSendMsgResult result = new WxSendMsgResult();
        result.setErrmsg("sendWxMessage exception");
        return result;
    }

    @Override
    public WxUserInfo getUserInfo(String accessToken, String openId)
    {
        WxUserInfo result = new WxUserInfo();
        result.setErrmsg("getUserInfo exception");
        return result;
    }
    
}