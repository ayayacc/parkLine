package com.kl.parkLine.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel("支付方式")
public enum PaymentType implements BaseEnum
{
    @ApiModelProperty("1:微信")
    wx(1, "微信"),
    @ApiModelProperty("2:钱包")
    qb(2, "钱包");
    
    private Integer value;
    private String text;

}
