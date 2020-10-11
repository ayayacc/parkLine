package com.kl.parkLine.vo;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@ApiModel("优惠券定义查询结果")
public class CouponDefVo
{
private Integer couponDefId;
    
    /**
     * 优惠券定义编号
     */
    @ApiModelProperty(name="优惠券定义编号", position=0)
    private String code;
    
    /**
     * 优惠券名称
     */
    @ApiModelProperty(name="优惠券名称", position=1)
    private String name;

    /**
     * 金额
     */
    @ApiModelProperty(name="金额", position=2)
    private BigDecimal amt;
    
    /**
     *使用支付的最小金额（满xx使用）
     */
    @ApiModelProperty(name="使用支付的最小金额（满xx使用）", position=3)
    private BigDecimal minAmt;
    
    /**
     * 优惠券总数
     */
    @ApiModelProperty(name="优惠券总数", position=4)
    private Integer totalCnt;
    
    /**
     * 已经领取数量
     */
    @ApiModelProperty(name="已经领取数量", position=5)
    private Integer appliedCnt;
    
    /**
     * 已经使用数量
     */
    @ApiModelProperty(name="已经使用数量", position=6)
    private Integer usedCnt;
    
    /**
     * 是否有效
     */
    private String enabled;
    
    /**
     * 有效期开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(name="有效期开始时间", position=7)
    private Date startDate;
    
    /**
     * 有效期结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(name="有效期开始时间", position=8)
    private Date endDate;
}
