package com.kl.parkLine.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

@Builder
@Value
@Getter
@Setter
@AllArgsConstructor
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
    
    @ApiModelProperty("免费时长(分钟)")
    private Integer freeTimeMin;
}
