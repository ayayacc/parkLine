package com.kl.parkLine.service;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.kl.parkLine.component.Utils;
import com.kl.parkLine.dao.IUserDao;
import com.kl.parkLine.entity.Car;
import com.kl.parkLine.entity.QUser;
import com.kl.parkLine.entity.Role;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.enums.Gender;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.DecryptionParam;
import com.kl.parkLine.json.DecryptionResult;
import com.kl.parkLine.json.MobileBindResult;
import com.kl.parkLine.json.MyInfo;
import com.kl.parkLine.json.SmsCheckParam;
import com.kl.parkLine.json.WxCode2SessionResult;
import com.kl.parkLine.json.WxUserInfo;
import com.kl.parkLine.predicate.UserPredicates;
import com.kl.parkLine.util.Const;
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
@Service
@Transactional(rollbackFor = Exception.class)
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
    
    @Autowired
    private CouponService couponService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private SmsCodeService smsCodeService;
    
    @Autowired
    private Utils utils;
    
    /**
     * 保存用户
     * @param user
     */
    public void save(User user)
    {
        userDao.save(user);
    }
    
    /**
     * 创建新的用户
     * @param userName 用户名
     */
    public User createEndUser(String mobile)
    {
        User user = new User();
        //生成userName
        String right = mobile.substring(mobile.length()-4); //取手机尾号后四位
        SimpleDateFormat sdf = new SimpleDateFormat("mmss");
        String userName = String.format("SJ_%s_%s", sdf.format(new Date()), right);
        user.setName(userName);
        user.setMobile(mobile);
        user.setEnabled(true);
        Set<Role> roles = new HashSet<>();
        Role role = roleService.findOneByCode(RoleCode.END_USER);
        roles.add(role);
        user.setRoles(roles);
        user.setGender(Gender.unkonwn);
        user.setSubscribe("N");
        save(user);
        return user;
    }
    
    /**
     * 设置微信用户
     * @param wxUserInfo 微信登录信息
     * @throws UnsupportedEncodingException 
     */
    public User setupUser(WxCode2SessionResult sessionResult, WxUserInfo wxUserInfo) throws UnsupportedEncodingException
    {
        String userName = Const.WX_PREFIX + sessionResult.getOpenid();
        User user = findByName(userName);
        if (null != user)
        {
            return user;
        }
        else
        {
            user = new User();
        }
        user.setName(userName);
        user.setNickName(Base64.encodeBase64String(wxUserInfo.getNickName().getBytes("utf-8")));
        user.setWxOpenId(sessionResult.getOpenid());
        user.setCountry(wxUserInfo.getCountry());
        user.setProvince(wxUserInfo.getProvince());
        user.setCity(wxUserInfo.getCity());
        user.setWxSessionKey(sessionResult.getSessionKey());
        user.setEnabled(true);
        switch (wxUserInfo.getGender())
        {
            case 1:
                user.setGender(Gender.male);
                break;
            case 2:
                user.setGender(Gender.female);
            default:
                user.setGender(Gender.unkonwn);
                break;
        }
        
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
    public User findByName(String name)
    {
        return userDao.findOneByName(name);
    }
    
    /**
     * 根据用户名称查找
     * @param name 用户名称
     */ 
    public User findWxOpenId(String openId)
    {
        return userDao.findOneByWxOpenId(openId);
    }
    
    /**
     * 根据手机号查找
     * @param name 用户名称
     */ 
    public User findOneByMobile(String mobile)
    {
        return userDao.findOneByMobile(mobile);
    }
    
    /**
     * 为用户添加车辆
     * @param car
     */
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
    public boolean hasPermission(User reqData, User user, String permission) 
    {
        return reqData.getUsername().equals(user.getName());
    }
    
    /**
     * 统计我的各种数量
     * @param userName 用户名
     * @return
     */
    public MyInfo myInfo(String userName)
    {
        MyInfo myInfo = new MyInfo();
        //用户名称
        myInfo.setUserName(userName);
        //找到当前用户
        User user = userDao.findOneByName(userName);
        //查询优惠券数量
        myInfo.setCouponCnt(couponService.countValidByOwner(user));
        //查询月票数量
        myInfo.setMonthlyTktCnt(orderService.countValidMonthlyTktByOwner(user));
        //钱包余额
        myInfo.setWalletBalance(user.getBalance());
        //是否开通了快捷支付
        myInfo.setIsQuickPay(user.getIsQuickPay());
        
        
        return myInfo;
    }
    
    /**
     * 设置快捷支付
     * @param userName 用户名
     * @return
     */
    public void setQuickPay(String userName, Boolean isQuickPay)
    {
        //找到当前用户
        User user = userDao.findOneByName(userName);
        user.setIsQuickPay(isQuickPay);
        save(user);
    }
    
    /**
     * 设置快捷支付
     * @param userName 用户名
     * @return
     */
    public void checkValidCode(String userName, SmsCheckParam smsCheckParam) throws BusinessException
    {
        //找到当前用户
        User user = userDao.findOneByName(userName);
        
        //检查验证码
        smsCodeService.checkValidCode(smsCheckParam.getMobile(), smsCheckParam.getValidCode());
        
        //保存用户手机号
        user.setMobile(smsCheckParam.getMobile());
        save(user);
    }
    
    /**
     * 检查手机号是否提供
     * @param userName 用户名
     * @return
     */
    public MobileBindResult isMobileProvided(String userName)
    {
        //找到当前用户
        User user = userDao.findOneByName(userName);
        
        
        //检查用户是否提供了手机号
        return MobileBindResult.builder()
                .isBinded(!StringUtils.isEmpty(user.getMobile()))
                .mobile(user.getMobile()).build();
    }
    
    /**
     * 解密微信用户手机号
     * @param auth
     * @param decryptionParam
     * @return
     * @throws Exception 
     */
    public DecryptionResult decryption(String userName, DecryptionParam decryptionParam) throws Exception
    {
        User user = findByName(userName);
        String text = utils.decrypt(user.getWxSessionKey(), decryptionParam.getIv(), decryptionParam.getEncryptedData());
        
        return JSON.parseObject(text, DecryptionResult.class);
    }
}
