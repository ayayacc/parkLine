package com.kl.parkLine.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kl.parkLine.enums.CouponStatus;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@SuppressWarnings("serial")
@Entity
@Table(name = "TT_COUPON")
@DynamicUpdate
@DynamicInsert
@EntityListeners({AuditingEntityListener.class})
@ApiModel("优惠券实例")
public class Coupon extends AbstractEntity implements java.io.Serializable
{
    @Id
    @Column(name = "coupon_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty("优惠券实例id")
    private Integer couponId;
    
    /**
     * 优惠券的定义
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_def")
    @JsonIgnore
    @ApiModelProperty(hidden = true)
    private CouponDef couponDef; 
    
    /**
     * 优惠券编号
     */
    @Column(name = "code", nullable = false, unique = true, length = 16)
    @ApiModelProperty("优惠券实例唯一编号")
    private String code;
    
    /**
     * 金额
     */
    @Column(name = "amt", precision = 15 ,scale = 2)
    @ApiModelProperty("优惠券实例金额")
    private BigDecimal amt;
    
    /**
     *使用支付的最小金额（满xx使用）
     */
    @Column(name = "min_amt", precision = 15 ,scale = 2)
    @ApiModelProperty("使用支付的最小金额（满xx使用）")
    private BigDecimal minAmt;
    
    /**
     * 状态: 已使用/无效
     */
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    @ApiModelProperty("优惠券状态")
    private CouponStatus status;
    
    /**
     * 有效期开始时间
     */
    @Temporal(TemporalType.DATE)            
    @Column(name = "start_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty("有效期开始时间")
    private Date startDate;
    
    /**
     * 有效期结束时间
     */
    @Temporal(TemporalType.DATE)            
    @Column(name = "end_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty("有效期结束时间")
    private Date endDate;
    
    /**
     * 拥有者
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner", nullable = false)
    @ApiModelProperty("拥有者")
    private User owner;

    /**
     * 操作记录
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "coupon", cascade = {CascadeType.ALL})  
    @OrderBy(value = "createdDate desc")
    @JsonIgnore
    @ApiModelProperty(hidden = true)
    private List<CouponLog> logs;

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Coupon [couponId=").append(couponId)
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
