package com.kl.parkLine.util;

public class DictCode
{
    //事件类型
    public final static String EVENT_TYPE_CAR_IN = "EVENT_TYPE_CAR_IN";
    public final static String EVENT_TYPE_CAR_OUT = "EVENT_TYPE_CAR_OUT";
    public final static String EVENT_TYPE_CAR_COMPLETE = "EVENT_TYPE_CAR_COMPLETE";
    public final static String EVENT_TYPE_CANCEL = "EVENT_TYPE_CANCEL";
    
    
    //车牌颜色
    public final static String PLATE_COLOR_UNKNOWN = "PLATE_COLOR_UNKNOWN";
    public final static String PLATE_COLOR_BLUE = "PLATE_COLOR_BLUE";
    public final static String PLATE_COLOR_YELLOW = "PLATE_COLOR_YELLOW";
    public final static String PLATE_COLOR_BLACK = "PLATE_COLOR_BLACK";
    public final static String PLATE_COLOR_WHITE = "PLATE_COLOR_WHITE";
    public final static String PLATE_COLOR_GREEN = "PLATE_COLOR_GREEN";
    
    //停车异常
    public final static String PARK_ABNORMAL_ZC = "PARK_ABNORMAL_ZC"; //正常
    public final static String PARK_ABNORMAL_KW = "PARK_ABNORMAL_KW"; //跨位
    public final static String PARK_ABNORMAL_XW = "PARK_ABNORMAL_XW"; //斜位
    public final static String PARK_ABNORMAL_YX = "PARK_ABNORMAL_YX"; //压线
    
    //订单类型:停车订单/月票订单/优惠券激活订单/钱包充值订单/钱包提现订单
    public final static String ORDER_TYPE_PARK = "ORDER_TYPE_PARK"; //停车
    public final static String ORDER_TYPE_MONTHLY_TICKET = "ORDER_TYPE_MONTHLY_TICKET"; //月票订单
    public final static String ORDER_TYPE_COUPON = "ORDER_TYPE_COUPON"; //优惠券激活订单
    public final static String ORDER_TYPE_WALLET_IN = "ORDER_TYPE_WALLET_IN"; //钱包充值订单
    public final static String ORDER_TYPE_WALLET_OUT = "ORDER_TYPE_WALLET_OUT"; //钱包提现订单
    
    //已入场/待支付（已出场）/已支付/开票中/已开票
    public final static String ORDER_STATUS_IN = "ORDER_STATUS_IN"; //已入场
    public final static String ORDER_STATUS_NEED_TO_PAY = "ORDER_STATUS_NEED_TO_PAY"; //待支付
    public final static String ORDER_STATUS_PAYED = "ORDER_STATUS_PAYED"; //已支付
    public final static String ORDER_STATUS_INVOICING = "ORDER_STATUS_INVOICING"; //已开票
    public final static String ORDER_STATUS_INVOICED = "ORDER_STATUS_INVOICED"; //已开票
    public final static String ORDER_STATUS_CANCELED = "ORDER_STATUS_CANCELED"; //已取消
    
    //优惠券有效期
    public final static String COUPON_STATUS_VALID = "COUPON_STATUS_VALID"; //有效
    public final static String COUPON_STATUS_USED = "COUPON_STATUS_USED"; //已使用
    public final static String COUPON_STATUS_INVALID = "COUPON_STATUS_INVALID"; //无效
    public final static String COUPON_STATUS_EXPIRED = "COUPON_STATUS_EXPIRED"; //已过期
}
