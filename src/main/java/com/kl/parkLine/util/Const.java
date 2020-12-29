package com.kl.parkLine.util;

public class Const
{
    //发送验证码的最短间隔(分钟)
    public final static int VALID_CODE_MIN_INTERVAL = 2;
    
    //JWT 信息
    public final static String JWT_CLAIM_USER_NAME = "username";
    public final static String JWT_CLAIM_ROLES = "JWT_CLAIM_ROLES";
    public final static Integer JWT_EXPIRED_TIME_MIN = 60; //60分钟jwt过期
    
    //微信前缀
    public final static String WX_PREFIX = "wxopenid_";
    
    //日志记录
    public final static String LOG_CREATE = "新增";
    
    //微信返回值，成功
    public final static String WX_SUCCESS = "SUCCESS";
    
    //优惠券激活默认有效期(天)
    public final static Integer COUPON_ACTIVE_DAYS = 7;
    
    public final static String SYS_USER = "SYSTEM";
    
    //提前付款出场时间30min
    public final static Integer OUT_LIMIT_TIME_NIN = 30;
    
    public final static  String TIME_STAMP = "TIME_STAMP";
}
