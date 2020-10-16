package com.kl.parkLine.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@ApiModel("停车位置")
public class ParkPlaceVo
{
    @ApiModelProperty("停车场Id")
    private String parkId;
    
    @ApiModelProperty("停车场编码")
    private String parkCode;
    
    @ApiModelProperty("停车场名称")
    private String parkName;
    
    @ApiModelProperty("停车场地理位置")
    private String parkGeo;
    
    @ApiModelProperty("车位编号")
    private String placeNo;
    
    @ApiModelProperty("车位地理位置")
    private String placeGeo;
}
