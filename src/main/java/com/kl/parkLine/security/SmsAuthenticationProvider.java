package com.kl.parkLine.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.entity.User;
import com.kl.parkLine.service.SmsCodeService;
import com.kl.parkLine.service.UserService;

@Component
public class SmsAuthenticationProvider implements AuthenticationProvider
{
    private MyUserDetailsService userDetailsService;
    
    @Autowired
    private SmsCodeService smsCodeService;
    
    @Autowired
    private UserService userService;
    
    @Override
    @Transactional
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException
    {
        //获取登录参数
        SmsAuthenticationToken smsAuthenticationToken = (SmsAuthenticationToken)authentication;
        String mobile = smsAuthenticationToken.getName();
        String validCode = smsAuthenticationToken.getValidCode();
        
        //校验验证码
        try
        {
            smsCodeService.checkValidCode(mobile, validCode);
        }
        catch (Exception e)
        {
            throw new SmsAuthenticationException(e.getMessage());
        }

        //根据用户手机号查询用户
        User user = userDetailsService.loadUserByMobile(mobile);
        
        //手机用户第一次使用本系统
        if (null == user)
        {
            //自动创建用户
            user = userService.createEndUser(mobile);
        }
        
        //用户被禁用
        if (!user.isEnabled())
        {
            throw new SmsAuthenticationException("您的帐号已被禁用");
        }
        
        SmsAuthenticationToken token = new SmsAuthenticationToken(user.getUsername(), user.getAuthorities());
        token.setDetails(authentication.getDetails());
        return token;
    }

    @Override
    public boolean supports(Class<?> authentication)
    {
        return (SmsAuthenticationToken.class.isAssignableFrom(authentication));
    }
    
    public void setUserDetailsService(MyUserDetailsService userDetailsService)
    {
        this.userDetailsService = userDetailsService;
    }

}
