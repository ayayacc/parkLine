package com.kl.parkLine.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.dao.IUserDao;
import com.kl.parkLine.entity.Car;
import com.kl.parkLine.entity.Role;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.enums.Gender;
import com.kl.parkLine.util.RoleCode;

/**
 * @author chenc
 *
 */
@Service("userService")
public class UserService
{
    @Autowired
    private IUserDao userDao;
    
    @Autowired
    private RoleService roleService;
    
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
     * 创建新的用户
     * @param mobile 手机号码
     */
    @Transactional
    public User createUser(String mobile)
    {
        User user = new User();
        //生成userName
        String right = mobile.substring(mobile.length()-4); //取手机尾号后四位
        SimpleDateFormat sdf = new SimpleDateFormat("mmss");
        String userName = String.format("SJ_%s_%s", sdf.format(new Date()), right);
        user.setMobile(mobile);
        user.setName(userName);
        //TODO: 获取真实性别
        user.setGender(Gender.male);
        user.setEnabled(true);
        Set<Role> roles = new HashSet<>();
        Role role = roleService.findOneByCode(RoleCode.END_USER);
        roles.add(role);
        user.setRoles(roles);
        save(user);
        return user;
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
     * 根据手机号查找
     * @param name 用户名称
     */ 
    @Transactional(readOnly = true)
    public User findOneByMobile(String mobile)
    {
        return userDao.findOneByMobile(mobile);
    }
    
    /**
     * 为用户添加车辆
     * @param car
     */
    @Transactional
    public void addCar(String userName, Car car)
    {
        User user = this.findByName(userName);
        user.getCars().add(car);
        this.save(user);
    }
    
    /**
     * 校验当前登录用户是否可以访问data 数据
     * 停车场人员无权限访问
     * 普通用户只能访问自己的数据
     * 系统运营和管理员可以访问所有用户
     * @param data 期待访问的数据
     * @param auth 当前登录的用户
     * @param permission 需要的权限
     * @return 是否有权限
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(User reqData, Authentication auth, String permission) 
    {
        //TODO: 实现用户访问权限控制
        return reqData.getUsername().equals(auth.getPrincipal());
    }
}
