package com.kl.parkLine.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel("计费类型")
public enum ChargeType implements BaseEnum
{
    @ApiModelProperty("1: 固定计费")
    fixed(1, "1: 固定费率"),
    @ApiModelProperty("2: 阶梯计费")
    step(2, "2: 阶梯计费");
    
    private Integer value;
    private String text;
}
