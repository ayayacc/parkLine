package com.kl.parkLine.json;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@ApiModel("计算订单价格结果")
public class CalOrderAmtResult
{
    @ApiModelProperty("订单金额")
    private BigDecimal amt;
}
