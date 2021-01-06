package com.kl.parkLine.json;

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
@ApiModel("是否绑定手机号结果")
public class MobileBindResult
{
    @ApiModelProperty("是否已经绑定")
    private Boolean isBinded;
    
    @ApiModelProperty("绑定的手机号")
    private String mobile;
}
