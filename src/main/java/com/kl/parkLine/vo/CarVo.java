package com.kl.parkLine.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
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
