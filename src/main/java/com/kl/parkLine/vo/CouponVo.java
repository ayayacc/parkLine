package com.kl.parkLine.vo;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.alibaba.fastjson.annotation.JSONField;
import com.kl.parkLine.enums.CouponStatus;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
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
@ToString
@ApiModel("优惠券实例VO")
public class CouponVo
{
    @ApiModelProperty("优惠券实例id")
    private Integer couponId;
    
    @ApiModelProperty(name="所属优惠券定义Id", value="couponDefId")
    @JSONField(name="couponDefId")
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
    @JSONField(name="ownerName")
    private String ownerName;
    
    @ApiModelProperty("折扣（例如8折）")
    private BigDecimal discount;
    
    @ApiModelProperty("激活价格")
    private BigDecimal activePrice;
    
    @ApiModelProperty("实际抵扣金额")
    private BigDecimal usedAmt;
    
    @ApiModelProperty("使用支付的最大金额")
    private BigDecimal maxAmt;
    
    @ApiModelProperty("优惠券状态")
    private CouponStatus status;
    
    @JSONField(format="yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty("有效期开始时间")
    private Date startDate;
    
    @JSONField(format="yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty("有效期结束时间")
    private Date endDate;

    @JSONField(format="yyyy-MM-dd")
    @ApiModelProperty("使用时间")
    private Date usedDate;
}
