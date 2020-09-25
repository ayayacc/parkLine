package com.kl.parkLine.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.dao.IUserDao;
import com.kl.parkLine.entity.User;

/**
 * @author chenc
 *
 */
@Service("userService")
public class UserService
{
    @Autowired
    private IUserDao userDao;
    
    /**
     * 保存用户
     * @param user
     */
    @Transactional
    public void save(User user)
    {
        userDao.save(user);
    }
    
    /**
     * 根据名称查找用户
     * @param user
     */
    @Transactional(readOnly = true)
    public List<User> getUsers()
    {
        return userDao.findAll();
    }
    
    /**
     * 根据用户名称查找
     * @param name 用户名称
     */ 
    @Transactional(readOnly = true)
    public User findByName(String name)
    {
        return userDao.findOneByName(name);
    }
    /**
     * 根据用户名称查找
     * @param name 用户名称
     */ 
    @Transactional(readOnly = true)
    public User findOneByMobile(String mobile)
    {
        return userDao.findOneByMobile(mobile);
    }
    
    /**
     * 校验当前登录用户是否可以访问data 数据
     * 普通用户只能访问自己的数据
     * @param data 期待访问的数据
     * @param auth 当前登录的用户
     * @param permission 需要的权限
     * @return 是否有权限
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(User reqData, Authentication auth, String permission) 
    {
        return reqData.getUsername().equals(auth.getPrincipal());
    }
}
