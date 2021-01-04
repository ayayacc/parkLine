package com.kl.parkLine.json;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel("充值选项")
public class ChargeOpts
{
    @ApiModelProperty("重置选项")
    private List<Integer> opts;
}
