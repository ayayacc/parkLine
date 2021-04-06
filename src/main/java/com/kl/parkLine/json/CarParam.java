package com.kl.parkLine.json;

import com.kl.parkLine.enums.PlateColor;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel("车辆参数")
public class CarParam
{
    @ApiModelProperty(required = true, name="车辆Id")
    private Integer carId;
    
    @ApiModelProperty(required = true, name="车牌号码")
    private String carNo;
    
    @ApiModelProperty(required = true, name="车牌车牌颜色")
    private PlateColor plateColor;
}
