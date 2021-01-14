package com.kl.parkLine.json;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel("现金支付参数")
public class XjPayParam
{
    @ApiModelProperty("订单Id")
    private Integer orderId;
    
    @ApiModelProperty("付款时间")
    private Long paymentTime;
    
    @ApiModelProperty("收款人")
    private String payee;
    
    @ApiModelProperty("公钥")
    private String publicKey;
    
    @ApiModelProperty("备注")
    private String remark;
    
    @ApiModelProperty("签名,md5(orderId=xxx&paymentTime=xxx(timestamp)&payee=xxx&remark=xxx&publicKey=xxx&privateKey=xxx)")
    private String sign;
    
}
