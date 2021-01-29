package com.kl.parkLine.security;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.kl.parkLine.entity.User;
import com.kl.parkLine.feign.IWxFeignClient;
import com.kl.parkLine.json.WxCode2SessionResult;
import com.kl.parkLine.json.WxUserInfo;
import com.kl.parkLine.service.UserService;

@Component
public class WxAuthenticationProvider implements AuthenticationProvider
{
    @Value("${wx.app.id}")
    private String appId;
    
    @Value("${wx.app.secret}")
    private String appSecret;
    
    @Autowired
    private IWxFeignClient wxFeignClient;
    
    @Autowired
    private UserService userService;
    
    @Override
    @Transactional
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException
    {
        String code = authentication.getName();
        WxCode2SessionResult result = wxFeignClient.code2Session(appId, appSecret, code);
        if (!StringUtils.isEmpty(result.getErrmsg()))
        {
            throw new WxAuthenticationException(result.getErrmsg());
        }
        WxAuthenticationToken wxAuthenticationToken = (WxAuthenticationToken)authentication;
        WxUserInfo wxUserInfo = wxAuthenticationToken.getWxUserInfo();
        wxUserInfo.setUnionId(result.getUnionid());
        wxUserInfo.setWxXcxOpenId(result.getOpenid());
        wxUserInfo.setSessionKey(result.getSessionKey());
        User user;
        try
        {
            user = userService.setupUser(wxUserInfo);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new WxAuthenticationException(result.getErrmsg());
        }
        
        if (!user.isEnabled())
        {
            throw new SmsAuthenticationException("您的帐号已被禁用");
        }
        
        WxAuthenticationToken token = new WxAuthenticationToken(user.getUsername(), user.getAuthorities());
        token.setDetails(authentication.getDetails());
        return token;
    }

    @Override
    public boolean supports(Class<?> authentication)
    {
        return (WxAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
