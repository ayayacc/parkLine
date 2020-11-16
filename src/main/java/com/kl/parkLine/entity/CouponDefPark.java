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

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("serial")
@Entity
@Table(name = "TT_COUPON_DEF_PARK")
@Getter
@Setter
@DynamicUpdate
@DynamicInsert
@EntityListeners({AuditingEntityListener.class})
public class CouponDefPark implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_def_park_id")
    @JsonIgnore
    private Integer couponDefPark;
    
    @ManyToOne(optional = false, fetch = FetchType.LAZY) 
    @JoinColumn(name = "coupon_def_id")
    @JsonIgnore
    private CouponDef couponDef;
    
    @ManyToOne(optional = false, fetch = FetchType.LAZY) 
    @JoinColumn(name = "park_id")
    @JsonIgnore
    private Park park;

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("CouponDefPark [couponDefPark=").append(couponDefPark)
                .append("]");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((couponDefPark == null) ? 0 : couponDefPark.hashCode());
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
        CouponDefPark other = (CouponDefPark) obj;
        if (couponDefPark == null)
        {
            if (other.couponDefPark != null)
            {
                return false;
            }
        }
        else if (!couponDefPark.equals(other.couponDefPark))
        {
            return false;
        }
        return true;
    }
    
    
}
