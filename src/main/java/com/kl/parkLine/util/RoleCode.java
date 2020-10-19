package com.kl.parkLine.util;

public class RoleCode
{
    public static String SYS_ADMIN = "ROLE_SYS_ADMIN"; //系统管理员
    
    //公司角色
    public static String BIZ_MARKETING = "ROLE_BIZ_MARKETING"; //业务市场
    public static String BIZ_OPERATE = "ROLE_BIZ_OPERATE"; //业务运营
    public static String BIZ_CUSTOMERE_SERVICE = "ROLE_BIZ_CUSTOMERE_SERVICE"; //客服
    public static String BIZ_FINANCIAL = "ROLE_BIZ_FINANCIAL"; //财务
    public static String BIZ_PATROL = "ROLE_BIZ_PATROL"; //巡检
    
    //停车场角色，必须绑定到停车场
    public static String PARK_ADMIN = "ROLE_PARK_ADMIN"; //停车场管理员
    public static String PARK_GUARD = "ROLE_PARK_GUARD"; //停车场值班人员（巡检或岗亭）
    public static String PARK_FINANCIAL = "ROLE_PARK_FINANCIAL"; //停车场财务
    
    public static String END_USER = "ROLE_END_USER"; //终端用户
    
    
}
