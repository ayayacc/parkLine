package com.kl.parkLine.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kl.parkLine.entity.User;
import com.kl.parkLine.service.UserService;

@RestController
@RequestMapping(value="/users")
public class UserController
{
    @Autowired 
    private UserService userService;  
    
    /**
     * 获取用户信息
     * @return
     */
    @GetMapping()
    public List<User> users()
    {
        return userService.getUsers();
    }
    
    /**
     * 获取用户信息
     * @return
     */
    @GetMapping(value = "/{userId}")
    @PreAuthorize("hasPermission(#user, 'read')")
    public User getUser(@PathVariable("userId") Integer userId, 
            @PathVariable("userId") User user)
    {
        return user;
    }
}
