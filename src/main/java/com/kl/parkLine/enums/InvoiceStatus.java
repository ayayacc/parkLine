package com.kl.parkLine.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel("发票状态")
public enum InvoiceStatus implements BaseEnum
{
    @ApiModelProperty("1:已申请")
    submited(1, "已申请"),
    
    @ApiModelProperty("2:开票成功")
    successed(2, "开票成功"),
    
    @ApiModelProperty("3:开票失败")
    failed(3, "开票失败");
    
    private Integer value;
    private String text;

}
