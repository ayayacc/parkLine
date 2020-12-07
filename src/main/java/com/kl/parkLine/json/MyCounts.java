package com.kl.parkLine.json;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel("我的各种计数")
public class MyCounts
{
    @ApiModelProperty("月票数量")
    private Integer monthlyTktCnt;
    
    @ApiModelProperty("优惠券数量")
    private Integer couponCnt;

    /*@ApiModelProperty("积分")
    private Integer point;*/
}
