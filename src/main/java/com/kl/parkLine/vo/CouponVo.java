package com.kl.parkLine.vo;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * 优惠券
 *
 * <p>优惠券 CouponDef 的实例
 * @author chenc 2020年9月11日
 * @see
 * @since 1.0
 */
@Getter
@Setter
@Builder
@ApiModel("优惠券实例VO")
public class CouponVo
{
    @ApiModelProperty("优惠券实例id")
    private Integer couponId;
    
    @ApiModelProperty("优惠券实例唯一编号")
    private String code;
    
    @ApiModelProperty("优惠券名称")
    private String name;
    
    @ApiModelProperty("优惠券实例金额")
    private BigDecimal amt;
    
    @ApiModelProperty("使用支付的最小金额（满xx使用）")
    private BigDecimal minAmt;
    
    @ApiModelProperty("优惠券状态")
    private String status;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty("有效期开始时间")
    private Date startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty("有效期结束时间")
    private Date endDate;
}
