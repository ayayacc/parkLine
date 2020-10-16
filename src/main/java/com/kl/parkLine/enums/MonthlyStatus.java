package com.kl.parkLine.enums;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel("月票状态")
public enum MonthlyStatus implements BaseEnum
{
    needToPay(1, "待支付"),
    valid(2, "生效"),
    expired(3, "已过期"),
    invalid(4, "无效");
    
    private Integer value;
    private String text;

}
