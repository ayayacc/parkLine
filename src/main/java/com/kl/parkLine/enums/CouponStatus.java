package com.kl.parkLine.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.kl.parkLine.annotation.SwaggerDisplayEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@SwaggerDisplayEnum
public enum CouponStatus implements BaseEnum
{
    notStart(1, "未开始"),
    
    valid(2, "有效"),
    
    used(3, "已使用"),
    
    invalid(4, "无效"),
    
    expired(5, "已过期");
    
    @JsonValue
    private Integer value;
    private String text;

}
