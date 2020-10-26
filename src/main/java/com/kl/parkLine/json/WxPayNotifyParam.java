package com.kl.parkLine.json;

import java.util.Date;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WxPayNotifyParam
{
    //微信openId
    private String openId;
    //是否关注公众账号    
    private String isSubscribe;
    //付款银行
    private String bankType;
    //微信支付订单号
    private String transactionId;  
    //商户订单号
    private String outTradeNo;  
    //商家数据包
    private String attach;  
    //支付完成时间
    private Date timeEnd;  
}
