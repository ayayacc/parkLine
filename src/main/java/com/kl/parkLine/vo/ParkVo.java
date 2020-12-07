package com.kl.parkLine.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
@AllArgsConstructor
@ApiModel("停车场Vo")
public class ParkVo
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
    
    @ApiModelProperty("位子信息")
    private String geo;
    
    @ApiModelProperty("联系方式")
    private String contact;
    
    @ApiModelProperty("是否有效")
    private String enabled;
}
