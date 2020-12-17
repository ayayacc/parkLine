package com.kl.parkLine.vo;

import java.math.BigDecimal;
import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;

@Builder
@Value
@AllArgsConstructor
@ToString
@ApiModel("优惠券定义VO")
public class CouponDefVo
{
    @ApiModelProperty(name="优惠券定义Id", position=0)
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
     *折扣后的封顶金额
     */
    @ApiModelProperty("最大金额")
    private BigDecimal maxAmt;
    
    /**
     * 优惠券折扣
     */
    @ApiModelProperty("折扣（例如8折）")
    private BigDecimal discount;
    
    /**
     * 激活价格
     */
    @ApiModelProperty("激活价格")
    private BigDecimal activePrice;
    
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
     * 有效期开始时间
     */
    @JSONField(format="yyyy-MM-dd")
    @ApiModelProperty(name="有效期开始时间", position=7)
    private Date startDate;
    
    /**
     * 有效期结束时间
     */
    @JSONField(format="yyyy-MM-dd")
    @ApiModelProperty(name="有效期结束时间", position=8)
    private Date endDate;
    
    /**
     * 是否有效
     */
    @ApiModelProperty(name="是否有效", position=9)
    private String enabled;
}
