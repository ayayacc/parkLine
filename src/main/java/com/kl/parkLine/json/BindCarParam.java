package com.kl.parkLine.json;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel("绑定/解绑车辆参数")
public class BindCarParam
{
    @ApiModelProperty(name="车牌号码")
    private String carNo;
}
