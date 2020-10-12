package com.kl.parkLine.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel("事件类型")
public enum EventType implements BaseEnum
{
    @ApiModelProperty("1:入")
    in(1, "入"),
    
    @ApiModelProperty("2:出")
    out(2, "出"),
    
    @ApiModelProperty("3:完成")
    complete(3, "完成"),
    
    @ApiModelProperty("4:取消")
    cancel(4, "取消");
    
    private Integer value;
    private String text;

}
