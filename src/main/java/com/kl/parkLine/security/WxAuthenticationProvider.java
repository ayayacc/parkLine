package com.kl.parkLine.security;

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
import com.kl.parkLine.service.UserService;

@Component
public class WxAuthenticationProvider implements AuthenticationProvider
{
    @Value("${wx.app.id}")
    private String appId;
    
    @Value("${wx.app.secret}")
    private String appSecret;
    
    private final String WX_PREFIX = "wxopenid_";
    
    private MyUserDetailsService userDetailsService;
    
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
        String userName = WX_PREFIX + result.getOpenid();
        User user = userDetailsService.loadUserByUsername(userName);
        WxAuthenticationToken wxAuthenticationToken = (WxAuthenticationToken)authentication;
        //微信用户第一次使用本系统
        if (null == user)
        {
            //自动创建用户
            user = userService.createEndUser(userName, wxAuthenticationToken.getWxUserInfo());
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
    
    public void setUserDetailsService(MyUserDetailsService userDetailsService)
    {
        this.userDetailsService = userDetailsService;
    }

}
