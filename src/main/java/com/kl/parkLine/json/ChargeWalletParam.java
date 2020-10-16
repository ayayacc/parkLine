package com.kl.parkLine.json;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel("钱包充值参数")
public class ChargeWalletParam
{
    @ApiModelProperty("充值金额(元)")
    private BigDecimal amt;
}
