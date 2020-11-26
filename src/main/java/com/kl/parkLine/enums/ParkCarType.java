package com.kl.parkLine.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel("停车场车辆名单类型")
public enum ParkCarType implements BaseEnum
{
    @ApiModelProperty("1:白名单")
    white(1, "白名单"),
    
    @ApiModelProperty("2:黑名单")
    black(2, "黑名单");
    
    private Integer value;
    private String text;

}
