package com.kl.parkLine.json;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel("短信登录参数")
public class SmsLoginParam
{
    @ApiModelProperty("手机号")
    private String mobile;
    
    @ApiModelProperty("验证码")
    private String validCode;
}
