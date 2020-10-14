package com.kl.parkLine.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@ApiModel("车辆VO")
public class CarVo
{
    @ApiModelProperty("车辆Id")
    private Integer carId;
    
    @ApiModelProperty("车牌号")
    private String carNo;
    
    @ApiModelProperty("绑定的用户名")
    private String userName;
}
