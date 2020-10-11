package com.kl.parkLine.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
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
}
