package com.kl.parkLine.json;

import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel("计算订单价格参数")
public class CalOrderAmtParam
{
    @ApiModelProperty("订单Id")
    private Integer orderId;
    
    @ApiModelProperty("出场时间")
    @JSONField(format="yyyy-MM-dd HH:mm")
    private Date outTime;
}
