package com.kl.parkLine.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@ApiModel("订单VO")
public class OrderVo
{
    @ApiModelProperty(name="订单Id", position=0)
    private Integer orderId;
    
    @ApiModelProperty(name="订单编码", position=1)
    private String code;
    
    @ApiModelProperty(name="订单类型", position=2)
    private String type;
    
    @ApiModelProperty(name="订单状态", position=3)
    private String status;
    
    /**
     * 停车场Id
     */
    @ApiModelProperty(name="停车场Id")
    private Integer parkId;
    
    /**
     * 停车场名称
     */
    @ApiModelProperty(name="停车场名称")
    private String parkName;
    
    /**
     * 车辆Id
     */
    @ApiModelProperty(name="车辆Id")
    private Integer carId;
    
    /**
     * 车牌号码
     */
    @ApiModelProperty(name="车牌号码")
    private String carNo;
}
