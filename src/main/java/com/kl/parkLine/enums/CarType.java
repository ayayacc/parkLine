package com.kl.parkLine.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel("车辆类型")
public enum CarType implements BaseEnum
{
    @ApiModelProperty("1: 燃油车")
    fuel(1, "1: 燃油车"),
    @ApiModelProperty("2: 新能源车")
    newEnergy(2, "2: 新能源车");
    
    private Integer value;
    private String text;
}
