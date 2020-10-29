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
    

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("WxPayNotifyParam [openId=").append(openId)
                .append(", isSubscribe=").append(isSubscribe)
                .append(", bankType=").append(bankType)
                .append(", transactionId=").append(transactionId)
                .append(", outTradeNo=").append(outTradeNo).append(", attach=")
                .append(attach).append(", timeEnd=").append(timeEnd)
                .append("]");
        return builder.toString();
    }
}
