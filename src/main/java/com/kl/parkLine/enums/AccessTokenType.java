package com.kl.parkLine.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel("api令牌类型")
public enum AccessTokenType implements BaseEnum
{
    @ApiModelProperty("0: 公众号")
    gzh(0, "公众号"),
    
    @ApiModelProperty("1: 小程序")
    xcx(1, "小程序");
    
    private Integer value;
    private String text;
}
