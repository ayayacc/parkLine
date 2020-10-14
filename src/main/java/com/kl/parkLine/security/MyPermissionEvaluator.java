package com.kl.parkLine.security;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.kl.parkLine.entity.User;
import com.kl.parkLine.service.UserService;

@Component("myPermissionEvaluator")
public class MyPermissionEvaluator implements PermissionEvaluator
{
    @Autowired
    private UserService userService;
    
    @Override
    public boolean hasPermission(Authentication auth, 
            Object targetDomainObject, Object permission)
    {
        if (null == targetDomainObject)
        {
            return true;
        }
        
        //根据不同类型的对象的访问请求，使用不同的对象判断
        if (targetDomainObject instanceof User)
        {
            return userService.hasPermission((User)targetDomainObject, auth, permission.toString());
        }
        return true;
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId,
            String targetType, Object permission)
    {
        return false;
    }

}
