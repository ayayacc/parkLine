package com.kl.parkLine.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel("车位类型")
public enum PlaceType implements BaseEnum
{
    @ApiModelProperty("1:地面")
    ground(1, "地面"),
    
    @ApiModelProperty("2:地下")
    underground(2, "地下");
    
    private Integer value;
    private String text;

}
