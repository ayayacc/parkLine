package com.kl.parkLine.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel("订单状态")
public enum OrderStatus implements BaseEnum
{
    @ApiModelProperty("1:已入场")
    in(1, "已入场"),
    @ApiModelProperty("2:待支付")
    needToPay(2, "待支付"),
    @ApiModelProperty("3:已支付")
    payed(3, "已支付"),
    @ApiModelProperty("4:开票中")
    invoicing(4, "开票中"),
    @ApiModelProperty("5:已开票")
    invoiced(5, "已开票"),
    @ApiModelProperty("6:已取消")
    canceled(6, "已取消");
    
    private Integer value;
    private String text;

}
