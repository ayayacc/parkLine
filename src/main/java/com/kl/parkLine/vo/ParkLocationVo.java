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
@ApiModel("停车场Vo")
public class ParkLocationVo
{
    @ApiModelProperty("停车场Id")
    private Integer parkId;
    
    @ApiModelProperty("停车场编码")
    private String code;
    
    @ApiModelProperty("名称")
    private String name;
    
    @ApiModelProperty("总可用车位")
    private Integer totalCnt;
    
    @ApiModelProperty("可用车位")
    private Integer availableCnt;
    
    @ApiModelProperty("经度")
    private Double lng;
    
    @ApiModelProperty("纬度")
    private Double lat;
    
    @ApiModelProperty("联系方式")
    private String contact;
     
    @ApiModelProperty("距离(KM)")
    private Double distance;
}
