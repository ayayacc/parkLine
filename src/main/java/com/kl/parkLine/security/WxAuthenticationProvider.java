package com.kl.parkLine.security;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.kl.parkLine.component.Utils;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.feign.IWxFeignClient;
import com.kl.parkLine.json.DecryptionUserResult;
import com.kl.parkLine.json.WxCode2SessionResult;
import com.kl.parkLine.json.WxLoginParam;
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
    
    @Autowired
    private Utils utils;
    
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
        WxLoginParam wxLoginParam = wxAuthenticationToken.getWxLoginParam();
        WxUserInfo wxUserInfo = wxLoginParam.getUserInfo();
        if (!StringUtils.isEmpty(result.getUnionid())) //code2Session返回了Id
        {
            wxUserInfo.setUnionId(result.getUnionid());
        }
        else 
        {
            String sign = DigestUtils.sha1Hex(wxLoginParam.getRawData()+result.getSessionKey());
            if (sign.equalsIgnoreCase(wxLoginParam.getSignature()))
            {
                throw new WxAuthenticationException("密文验证失败");
            }
            try
            {
                String text = utils.decrypt(result.getSessionKey(), wxLoginParam.getIv(), wxLoginParam.getEncryptedData());
                DecryptionUserResult userInfo = JSON.parseObject(text, DecryptionUserResult.class);
                wxUserInfo.setUnionId(userInfo.getUnionId());
            }
            catch (Exception e)
            {
                throw new WxAuthenticationException(e.getMessage());
            }
        }
        if (StringUtils.isEmpty(wxUserInfo.getUnionId()))
        {
            throw new WxAuthenticationException("获取OpenId失败");
        }
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
            throw new WxAuthenticationException("您的帐号已被禁用");
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
