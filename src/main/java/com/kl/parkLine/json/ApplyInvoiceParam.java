package com.kl.parkLine.json;

import java.util.Set;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel("申请开票参数")
public class ApplyInvoiceParam
{
    @ApiModelProperty(name="订单Id清单")
    private Set<String> orderIds;
}
