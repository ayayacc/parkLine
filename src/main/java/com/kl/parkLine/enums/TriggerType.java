package com.kl.parkLine.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel("事件触发类型")
public enum TriggerType implements BaseEnum
{
    @ApiModelProperty("1:自动")
    auto(1, "自动"),
    
    @ApiModelProperty("2:手动")
    manual(2, "手动");
    
    private Integer value;
    private String text;

}
