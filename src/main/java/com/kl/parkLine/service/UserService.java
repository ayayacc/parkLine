package com.kl.parkLine.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.dao.IUserDao;
import com.kl.parkLine.entity.Car;
import com.kl.parkLine.entity.QUser;
import com.kl.parkLine.entity.Role;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.enums.Gender;
import com.kl.parkLine.predicate.UserPredicates;
import com.kl.parkLine.util.RoleCode;
import com.kl.parkLine.vo.UserVo;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

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
    
    @Autowired
    private UserPredicates userPredicates;
    
    @Autowired
    private JPAQueryFactory jpaQueryFactory;
    
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
        user.setSubscribe("N");
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
     * 根据用户名称查找
     * @param name 用户名称
     */ 
    @Transactional(readOnly = true)
    public User findWxOpenId(String openId)
    {
        return userDao.findOneByWxOpenId(openId);
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
     * 模糊匹配优惠券定义
     * @param user  
     * @param pageable
     * @param auth
     * @return
     */
    @Transactional(readOnly = true)
    public Page<UserVo> fuzzyFindPage(UserVo userVo, Pageable pageable, String userName)
    {
        User user = this.findByName(userName);
        Predicate searchPred = userPredicates.fuzzy(userVo, user);
        
        QUser qUser = QUser.user;
        QueryResults<UserVo> queryResults = jpaQueryFactory
                .select(Projections.bean(UserVo.class, 
                        qUser.userId,
                        qUser.name,
                        qUser.mobile,
                        qUser.nickName))
                .from(qUser)
                .where(searchPred)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();
        return new PageImpl<>(queryResults.getResults(), pageable, queryResults.getTotal());
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
    public boolean hasPermission(User reqData, User user, String permission) 
    {
        //TODO: 实现用户访问权限控制
        return reqData.getUsername().equals(user.getName());
    }
}
