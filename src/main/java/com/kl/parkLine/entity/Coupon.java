package com.kl.parkLine.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.kl.parkLine.annotation.NeedToCompare;
import com.kl.parkLine.enums.CouponStatus;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@SuppressWarnings("serial")
@Entity
@Table(name = "TT_COUPON")
@org.hibernate.annotations.Table(appliesTo = "tt_coupon",comment = "优惠券 CouponDef 的实例")
@DynamicUpdate
@DynamicInsert
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("优惠券实例")
@EntityListeners({AuditingEntityListener.class})
public class Coupon extends AbstractDateEntity implements java.io.Serializable
{
    @Id
    @Column(name = "coupon_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty("优惠券实例id")
    private Integer couponId;
    
    /**
     * 优惠券的定义
     */
    @ManyToOne
    @JoinColumn(name = "coupon_def", columnDefinition="int comment '优惠券所属优惠券定义'")
    @JSONField(serialize = false)
    @ApiModelProperty(hidden = true)
    private CouponDef couponDef; 
    
    /**
     * 优惠券编号
     */
    @Column(name = "code", nullable = false, unique = true, length = 32, columnDefinition="varchar(32) comment '优惠券实例唯一编号,领取优惠券时自动生成'")
    @ApiModelProperty("优惠券实例唯一编号")
    private String code;
    
    /**
     * 优惠券名称
     */
    @NeedToCompare(name = "名称")
    @Column(name = "name", nullable = false, length = 64, columnDefinition="varchar(64) comment '优惠券名称,默认等于优惠券定义名称'")
    @ApiModelProperty("优惠券名称")
    private String name;
    
    /**
     * 状态
     */
    @Column(name = "status", columnDefinition="varchar(255) comment 'valid(有效)/used(已使用)/invalid(无效)/expired(已过期)'")
    @Enumerated(EnumType.STRING)
    @ApiModelProperty("优惠券状态")
    private CouponStatus status;
    
    /**
     *最大折扣金额
     */
    @NeedToCompare(name = "最大折扣金额")
    @Column(name = "max_amt", nullable = false, precision = 15 ,scale = 2, columnDefinition="decimal(15,2) comment '最大折扣金额(元)'")
    @ApiModelProperty("最大折扣金额")
    private BigDecimal maxAmt;
    
    /**
     * 优惠券折扣
     */
    @NeedToCompare(name = "折扣")
    @Column(name = "discount", nullable = false, precision = 15 ,scale = 2, columnDefinition="decimal(15,2) comment '折扣（例如8折）'")
    @ApiModelProperty("折扣（例如8折）")
    private BigDecimal discount;
    
    /**
     * 激活价格
     */
    @NeedToCompare(name = "激活价格")
    @Column(name = "active_price", nullable = false, precision = 15 ,scale = 2, columnDefinition="decimal(15,2) comment '激活价格(元)'")
    @ApiModelProperty("激活价格")
    private BigDecimal activePrice;
    
    /**
     * 适用的停车场
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "tr_coupon_park", joinColumns = { @JoinColumn(name="coupon_id") }, inverseJoinColumns={ @JoinColumn(name="park_id") })  
    @ApiModelProperty(hidden = true)
    private List<Park> applicableParks;
    
    /**
     * 有效期开始时间
     */
    @Temporal(TemporalType.DATE)            
    @Column(name = "start_date", nullable = false, columnDefinition="date comment '有效期开始时间(含当天)'")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty("有效期开始时间")
    private Date startDate;
    
    /**
     * 有效期结束时间
     */
    @Temporal(TemporalType.DATE)            
    @Column(name = "end_date", nullable = false, columnDefinition="date comment '有效期结束时间(含当天)'")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty("有效期结束时间")
    private Date endDate;
    
    /**
     * 使用时间
     */
    @Temporal(TemporalType.TIMESTAMP)            
    @Column(name = "used_date", columnDefinition="datetime comment '使用时间'")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("使用时间")
    private Date usedDate;
    
    /**
     * 实际抵扣的金额
     */
    @NeedToCompare(name = "实际抵扣的金额")
    @Column(name = "used_amt", precision = 15 ,scale = 2, columnDefinition="decimal(15,2) comment '实际抵扣的金额'")
    @ApiModelProperty("实际抵扣的金额")
    private BigDecimal usedAmt;
    
    /**
     * 拥有者
     */
    @JSONField(serialize = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner", nullable = false, columnDefinition="int comment '拥有者'")
    @ApiModelProperty("拥有者")
    private User owner;

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("CouponVo [couponId=").append(couponId)
                .append(", couponDef=").append(couponDef).append("]");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((couponId == null) ? 0 : couponId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        Coupon other = (Coupon) obj;
        if (couponId == null)
        {
            if (other.couponId != null)
            {
                return false;
            }
        }
        else if (!couponId.equals(other.couponId))
        {
            return false;
        }
        return true;
    }
    
    
   
}
