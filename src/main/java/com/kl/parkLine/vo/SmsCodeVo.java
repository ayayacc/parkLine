package com.kl.parkLine.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;

@Builder
@Value
@AllArgsConstructor
@ToString
@ApiModel("短信登录参数")
public class SmsCodeVo
{
    @ApiModelProperty("验证码")
    private String code;
}
