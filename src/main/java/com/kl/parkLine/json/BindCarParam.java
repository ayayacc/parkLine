package com.kl.parkLine.json;

import com.kl.parkLine.enums.PlateColor;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel("绑定或解绑车辆参数")
public class BindCarParam
{
    @ApiModelProperty(required = true, name="车牌号码")
    private String carNo;
    
    @ApiModelProperty(required = true, name="车牌车牌颜色")
    private PlateColor plateColor;
}
