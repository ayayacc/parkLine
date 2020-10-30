package com.kl.parkLine.vo;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kl.parkLine.enums.CouponStatus;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * 
 * 优惠券
 *
 * <p>优惠券 CouponDef 的实例
 * @author chenc 2020年9月11日
 * @see
 * @since 1.0
 */
@Builder
@Value
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel("优惠券实例VO")
public class CouponVo
{
    @ApiModelProperty("优惠券实例id")
    private Integer couponId;
    
    @ApiModelProperty(name="所属优惠券定义Id", value="couponDefId")
    @JsonProperty(value = "couponDefId")
    private Integer couponDefCouponDefId;
    
    @ApiModelProperty("所属优惠券定义编号")
    private String couponDefCode;
    
    @ApiModelProperty("所属优惠券定义名称")
    private String couponDefName;
    
    @ApiModelProperty("优惠券实例唯一编号")
    private String code;
    
    @ApiModelProperty("优惠券名称")
    private String name;
    
    @ApiModelProperty(name="优惠券拥有者", value="couponDefCode")
    @JsonProperty(value = "ownerName")
    private String ownerName;
    
    @ApiModelProperty("优惠券实例金额")
    private BigDecimal amt;
    
    @ApiModelProperty("使用支付的最小金额（满xx使用）")
    private BigDecimal minAmt;
    
    @ApiModelProperty("优惠券状态")
    private CouponStatus status;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty("有效期开始时间")
    private Date startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty("有效期结束时间")
    private Date endDate;
}
