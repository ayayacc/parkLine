package com.kl.parkLine.vo;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CouponDefVo
{
private Integer couponDefId;
    
    /**
     * 优惠券定义编号
     */
    private String code;
    
    /**
     * 优惠券名称
     */
    private String name;

    /**
     * 金额
     */
    private BigDecimal amt;
    
    /**
     *使用支付的最小金额（满xx使用）
     */
    private BigDecimal minAmt;
    
    /**
     * 优惠券总数
     */
    private Integer totalCnt;
    
    /**
     * 已经领取数量
     */
    private Integer usedCnt;
    
    /**
     * 是否有效
     */
    private String enabled;
    
    /**
     * 有效期开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startDate;
    
    /**
     * 有效期结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;
}
