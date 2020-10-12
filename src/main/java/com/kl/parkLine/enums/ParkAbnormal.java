package com.kl.parkLine.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel("异常停车类型")
public enum ParkAbnormal implements BaseEnum
{
    @ApiModelProperty("1:正常")
    zc(1, "正常"),
    
    @ApiModelProperty("2:跨位")
    kw(2, "跨位"),
    
    @ApiModelProperty("3:斜位")
    xw(3, "斜位"),
    
    @ApiModelProperty("4:压线")
    yx(4, "压线");
    
    private Integer value;
    private String text;

}
