package com.kl.parkLine.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel("返回代码")
public enum RetCode implements BaseEnum
{
    @ApiModelProperty("0: 成功")
    ok(0, "成功"),
    
    @ApiModelProperty("1: 通用错误")
    failed(1, "通用错误"),
    
    @ApiModelProperty("2: 无效令牌")
    invalidToken(2, "无效令牌"),
    
    @ApiModelProperty("1: 过期令牌")
    expiredToke(3, "过期令牌"),
    
    @ApiModelProperty("4: 余额不足")
    balanceNotEnough(4, "余额不足");
    
    private Integer value;
    private String text;
}
