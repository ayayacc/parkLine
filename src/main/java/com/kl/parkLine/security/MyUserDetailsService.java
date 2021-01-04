package com.kl.parkLine.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.entity.User;
import com.kl.parkLine.service.UserService;

@Component("myUserDetailsService")
public class MyUserDetailsService implements UserDetailsService
{
    @Autowired 
    private UserService userService;  
    
    //获取用户名,密码,提供给Spring进行验证
    @Override
    @Transactional(readOnly = true)
    public User loadUserByUsername(String userName) throws UsernameNotFoundException
    {
        return userService.findByName(userName);
    }
    
    @Transactional(readOnly = true)
    public User loadUserByMobile(String mobile) 
    {
        return userService.findOneByMobile(mobile);
    }
    
    @Transactional(readOnly = true)
    public User loadUserByName(String userName) 
    {
        User user = userService.findByName(userName);
        if (null != user)
        {
            user.getAuthorities();
        }
        return user;
    }
}
