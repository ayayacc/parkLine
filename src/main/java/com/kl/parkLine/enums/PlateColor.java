package com.kl.parkLine.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel("车牌颜色")
public enum PlateColor implements BaseEnum
{
    @ApiModelProperty("1:未知")
    unknown(1, "未知"),
    
    @ApiModelProperty("2:蓝")
    blue(2, "蓝"),
    
    @ApiModelProperty("3:黄")
    yellow(3, "黄"),
    
    @ApiModelProperty("4:黑")
    black(4, "黑"),
    
    @ApiModelProperty("5:白")
    white(5, "白"),
    
    @ApiModelProperty("5:绿")
    green(6, "绿");
    
    private Integer value;
    private String text;

}
