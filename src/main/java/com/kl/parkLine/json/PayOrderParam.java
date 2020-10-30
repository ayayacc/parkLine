package com.kl.parkLine.json;

import java.math.BigDecimal;

import com.kl.parkLine.enums.PaymentType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel("订单支付参数")
public class PayOrderParam
{
    @ApiModelProperty("订单Id")
    private Integer orderId;
    
    @ApiModelProperty("优惠券Id")
    private Integer couponId;
    
    @ApiModelProperty("支付方式: wx,qb")
    private PaymentType paymentType;
    
    @ApiModelProperty("付款金额")
    private BigDecimal amt;
}
