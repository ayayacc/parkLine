package com.kl.parkLine.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@ApiModel("短信登录参数")
public class SmsCodeVo
{
    @ApiModelProperty("验证码")
    private String code;
}
