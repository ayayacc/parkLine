package com.kl.parkLine.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel("订单类型")
public enum OrderType implements BaseEnum
{
    @ApiModelProperty("1:停车订单")
    parking(1, "停车订单"),
    
    @ApiModelProperty("2:月票")
    monthlyTicket(2, "月票"),
    
    @ApiModelProperty("3:优惠券激活")
    coupon(3, "优惠券激活"),
    
    @ApiModelProperty("4:钱包充值")
    walletIn(4, "钱包充值");
    
    private Integer value;
    private String text;

}
