package com.kl.parkLine.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.kl.parkLine.entity.User;
import com.kl.parkLine.service.UserService;

@Component("myUserDetailsService")
public class MyUserDetailsService implements UserDetailsService
{
    @Autowired 
    private UserService userService;  
    
    //获取用户名,密码,提供给Spring进行验证
    @Override
    public User loadUserByUsername(String userName) throws UsernameNotFoundException
    {
        return userService.findByName(userName);
    }
}
