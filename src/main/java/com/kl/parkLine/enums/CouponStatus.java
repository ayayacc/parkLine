package com.kl.parkLine.enums;

import com.kl.parkLine.annotation.SwaggerDisplayEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@SwaggerDisplayEnum
public enum CouponStatus implements BaseEnum
{
    valid(1, "有效"),
    
    used(2, "已使用"),
    
    invalid(3, "无效"),
    
    expired(4, "已过期");
    
    private Integer value;
    private String text;

}
