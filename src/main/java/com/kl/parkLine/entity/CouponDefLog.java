package com.kl.parkLine.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("serial")
@Entity
@Table(name = "TT_COUPON_DEF_LOG")
@org.hibernate.annotations.Table(appliesTo = "tt_coupon_def_log",comment = "优惠券定义变动记录")
@Getter
@Setter
@DynamicUpdate
@DynamicInsert
@EntityListeners({AuditingEntityListener.class})
public class CouponDefLog extends AbstractLog implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_def_log_id")
    @JSONField(serialize = false)
    private Integer couponDefLogId;
    
    @ManyToOne(optional = false, fetch = FetchType.LAZY) 
    @JoinColumn(name = "coupon_def_id", columnDefinition="int comment '优惠券定义Id'")
    @JSONField(serialize = false)
    private CouponDef couponDef;

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("CouponDefLog [couponDefLogId=").append(couponDefLogId)
                .append("]");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((couponDefLogId == null) ? 0 : couponDefLogId.hashCode());
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
        CouponDefLog other = (CouponDefLog) obj;
        if (couponDefLogId == null)
        {
            if (other.couponDefLogId != null)
            {
                return false;
            }
        }
        else if (!couponDefLogId.equals(other.couponDefLogId))
        {
            return false;
        }
        return true;
    }
    
    
}
