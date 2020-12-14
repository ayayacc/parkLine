package com.kl.parkLine.json;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel("我的信息")
public class MyInfo
{
    @ApiModelProperty("用户Id")
    private String userName;
    
    @ApiModelProperty("月票数量")
    private Integer monthlyTktCnt;
    
    @ApiModelProperty("优惠券数量")
    private Integer couponCnt;
    
    @ApiModelProperty("钱包余额")
    private BigDecimal walletBalance;
    
    @ApiModelProperty("是否开通了快捷支付")
    private Boolean isQuickPay;
}
