package com.kl.parkLine.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.kl.parkLine.json.WxCode2SessionResult;

/**
 * @author chenc
 */
@Component
public class WxCmpt
{

    private final String WX_APP_ID = "wx022cd083171c164f";
    private final String WX_APP_SECRET = "5ff32eb6bfd676e8ca93289e333b54cf";
    
    @Autowired
    private WXMappingJackson2HttpMessageConverter jsonConverter;

    public WxCode2SessionResult code2Session(String jsCode) throws Exception
    {
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid={APPID}&secret={APPSECRET}&js_code={JSCODE}&grant_type=authorization_code";
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(jsonConverter);
        WxCode2SessionResult result = restTemplate.getForObject(url, WxCode2SessionResult.class, WX_APP_ID, WX_APP_SECRET, jsCode);
        if (false == StringUtils.isEmpty(result.getErrmsg()))
        {
            throw new Exception(String.format("Get WeChat jscode2session failed:%d, %s", 
                    result.getErrcode(), result.getErrmsg()));
        }
        return result;
    }
    
}
