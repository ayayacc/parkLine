package com.kl.parkLine.security;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.kl.parkLine.entity.SmsCode;
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
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException
    {
        //获取登录参数
        SmsAuthenticationToken smsAuthenticationToken = (SmsAuthenticationToken)authentication;
        String mobile = smsAuthenticationToken.getName();
        String validCode = smsAuthenticationToken.getValidCode();
        
        //找到验证码
        SmsCode smsCode = smsCodeService.findLastByMobile(mobile);
        if (null == smsCode)
        {
            throw new SmsAuthenticationException("无效验证码");
        }
        
        //比对验证码有效期和值
        Date now = new Date();
        if (smsCode.getExpierTime().before(now))
        {
            throw new SmsAuthenticationException("无效验证码");
        }
        
        if (!smsCode.getCode().equalsIgnoreCase(validCode))
        {
            throw new SmsAuthenticationException("无效验证码");
        }

        //根据用户手机号查询用户
        User user = userDetailsService.loadUserByMobile(mobile);
        
        //手机用户第一次使用本系统
        if (null == user)
        {
            //自动创建用户
            user = new User();
            //生成userName
            String right = mobile.substring(mobile.length()-4); //取手机尾号后四位
            SimpleDateFormat sdf = new SimpleDateFormat("mmss");
            String userName = String.format("SJ_%s_%s", sdf.format(now), right);
            user.setMobile(mobile);
            user.setName(userName);
            user.setEnable(true);
            userService.save(user);
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
