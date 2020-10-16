package com.kl.parkLine.json;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel("激活优惠券参数")
public class ActiveCouponParam
{
    @ApiModelProperty("优惠券编码")
    private Integer couponId;
    
    @ApiModelProperty("金额(元)")
    private BigDecimal amt;
}
