package com.kl.parkLine.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel("事件类型")
public enum DeviceUseage implements BaseEnum
{
    @ApiModelProperty("1:入")
    in(1, "入"),
    
    @ApiModelProperty("2:出")
    out(2, "出");
    
    private Integer value;
    private String text;

}
