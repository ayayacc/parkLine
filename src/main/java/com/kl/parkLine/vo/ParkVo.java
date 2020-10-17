package com.kl.parkLine.vo;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
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
    
    @ApiModelProperty("免费时长")
    private Integer freeTime;
    
    @ApiModelProperty("第一计费阶段时长")
    private Integer timeLev1;
    
    @ApiModelProperty("第一计费阶段单价")
    private BigDecimal priceLev1;
    
    @ApiModelProperty("第二计费阶段时长")
    private Integer timeLev2;
    
    @ApiModelProperty("第二计费阶段单价")
    private BigDecimal priceLev2;
    
    @ApiModelProperty("计费最大金额")
    private BigDecimal maxAmt;
    
    @ApiModelProperty("是否有效")
    private String enabled;
}
