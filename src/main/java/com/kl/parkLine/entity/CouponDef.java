package com.kl.parkLine.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
import com.kl.parkLine.annotation.NeedToCompare;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * 优惠券定义
 *
 * <p>优惠券定义
 * @author chenc 2020年9月11日
 * @see
 * @since 1.0
 */
@Getter
@Setter
@SuppressWarnings("serial")
@Entity
@Table(name = "TT_COUPON_DEF")
@DynamicUpdate
@DynamicInsert
@EntityListeners({AuditingEntityListener.class})
public class CouponDef extends AbstractEntity implements java.io.Serializable
{
    @Id
    @Column(name = "coupon_def_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer couponDefId;
    
    /**
     * 优惠券定义编号
     */
    @Column(name = "code", nullable = false, unique = true, length = 16)
    private String code;
    
    /**
     * 优惠券名称
     */
    @NeedToCompare(name = "名称")
    @Column(name = "name", nullable = false, length = 64, unique = true)
    private String name;

    /**
     * 金额
     */
    @NeedToCompare(name = "金额")
    @Column(name = "amt", precision = 15 ,scale = 2)
    private BigDecimal amt;
    
    /**
     *使用支付的最小金额（满xx使用）
     */
    @NeedToCompare(name = "使用支付的最小金额")
    @Column(name = "min_amt", precision = 15 ,scale = 2)
    private BigDecimal minAmt;
    
    /**
     * 优惠券总数
     */
    @NeedToCompare(name = "优惠券总数")
    @Column(name = "total_cnt")
    private Integer totalCnt;
    
    /**
     * 已经领取数量
     */
    @NeedToCompare(name = "已经领取数量")
    @Column(name = "applied_cnt", nullable = false, columnDefinition ="int default 0")
    private Integer appliedCnt;
    
    /**
     * 已经使用数量
     */
    @NeedToCompare(name = "已经使用数量")
    @Column(name = "used_cnt", nullable = false, columnDefinition ="int default 0")
    private Integer usedCnt;
    
    /**
     * 是否有效
     */
    @NeedToCompare(name = "是否有效")
    @Column(name = "enabled", length = 4, nullable = false)
    private String enabled;
    
    /**
     * 有效期开始时间
     */
    @Temporal(TemporalType.DATE)            
    @Column(name = "start_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NeedToCompare(name = "有效期开始时间")
    private Date startDate;
    
    /**
     * 有效期结束时间
     */
    @Temporal(TemporalType.DATE)            
    @Column(name = "end_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NeedToCompare(name = "有效期结束时间")
    private Date endDate;
    
    /**
     * 操作记录
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "couponDef", cascade = {CascadeType.ALL})  
    @OrderBy(value = "createdDate desc")
    @JsonIgnore
    private List<CouponDefLog> logs;

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("CouponDef [couponDefId=").append(couponDefId)
                .append(", name=").append(name).append("]");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((couponDefId == null) ? 0 : couponDefId.hashCode());
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
        CouponDef other = (CouponDef) obj;
        if (couponDefId == null)
        {
            if (other.couponDefId != null)
            {
                return false;
            }
        }
        else if (!couponDefId.equals(other.couponDefId))
        {
            return false;
        }
        return true;
    }
    
    
}
