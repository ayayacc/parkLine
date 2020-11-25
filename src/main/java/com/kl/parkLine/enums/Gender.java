package com.kl.parkLine.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel("性别")
public enum Gender implements BaseEnum
{
    @ApiModelProperty("0:未知")
    unkonwn(0, "未知"),
    @ApiModelProperty("1:男")
    male(1, "男"),
    @ApiModelProperty("2:女")
    female(2, "女");
    
    private Integer value;
    private String text;
}
