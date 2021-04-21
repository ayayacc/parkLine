package com.kl.parkLine.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel("月票计费方式")
public enum MonthlyMode implements BaseEnum
{
    @ApiModelProperty("1:固定模式")
    fix(1, "固定模式"),
    
    @ApiModelProperty("2:无固定")
    noFix(1, "无固定模式");
    
    private Integer value;
    private String text;

}
