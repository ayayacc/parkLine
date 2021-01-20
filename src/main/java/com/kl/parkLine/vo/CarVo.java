package com.kl.parkLine.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;

@Builder
@Value
@AllArgsConstructor
@ToString
@ApiModel("车辆VO")
public class CarVo
{
    @ApiModelProperty("车辆Id")
    private Integer carId;
    
    @ApiModelProperty("车牌号")
    private String carNo;
    
    @ApiModelProperty("是否绑定了行驶证")
    private Boolean isLocked;
    
    @ApiModelProperty("绑定的用户名")
    private String userName;
    
}
