package com.kl.parkLine.util;

public class Const
{
    //信路通返回值
    public final static Integer CLT_RET_CODE_OK = 0; //成功
    public final static Integer CLT_RET_CODE_FAILED = 3; //执行失败
    public final static Integer CLT_RET_CODE_INVALID_PARAM = 4; //无效参数
    
    //发送验证码的最短间隔(分钟)
    public final static int VALID_CODE_MIN_INTERVAL = 2;
    
    //JWT 信息
    public final static String JWT_CLAIM_USER_NAME = "username";
    public final static String JWT_CLAIM_ROLES = "JWT_CLAIM_ROLES";
    
    //rest返回值
    public final static Integer RET_OK = 0;
    public final static Integer RET_LOGIN_TIME_OUT = 1;
    public final static Integer RET_FAILED = 2;
}
