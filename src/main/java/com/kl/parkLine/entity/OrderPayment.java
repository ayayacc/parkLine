package com.kl.parkLine.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

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
import com.kl.parkLine.enums.PaymentType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@SuppressWarnings("serial")
@Entity
@Table(name = "TT_ORDER_PAYMENT")
@org.hibernate.annotations.Table(appliesTo = "tt_order_payment",comment = "订单付款记录")
@Getter
@Setter
@DynamicUpdate
@DynamicInsert
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({AuditingEntityListener.class})
public class OrderPayment implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_payment_id")
    @JsonIgnore
    private Integer orderPaymentId;
    
    @ManyToOne(optional = false, fetch = FetchType.LAZY) 
    @JoinColumn(name = "order_id", columnDefinition="int comment '订单Id'")
    @JsonIgnore
    private Order order;
    

    /**
     * 支付方式: 微信/钱包
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", columnDefinition="varchar(255) comment '支付方式:wx(微信)/qb(钱包)'")
    private PaymentType paymentType;
    
    /**
     * 付款时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "payment_time", columnDefinition="datetime comment '付款时间'")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date paymentTime; 
    
    /**
     * 使用优惠券前付款金额(元)
     */
    @Column(name = "amt", nullable = false, precision = 15 ,scale = 2, columnDefinition="decimal(15,2) comment '付款金额'")
    private BigDecimal amt;
    
    /**
     * 使用优惠券后实付款金额(元)
     */
    @Column(name = "real_amt", nullable = false, precision = 15 ,scale = 2, columnDefinition="decimal(15,2) comment '使用优惠券后实付款金额(元)'")
    private BigDecimal realAmt;
    
    /**
     * 涉及钱包操作后的钱包余额
     */
    @Column(name = "wallet_balance", precision = 15 ,scale = 2, columnDefinition="decimal(15,2) comment '涉及钱包操作后的钱包余额'")
    private BigDecimal walletBalance;
    

    /**
     * 付款银行代码
     */
    @Column(name = "bank_type", length = 48, columnDefinition="varchar(48) comment '付款银行代码,微信返回'")
    @NeedToCompare(name = "银行代码")
    private String bankType;

    /**
     * 使用的优惠券
     */
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "used_coupon", columnDefinition="int comment '使用的优惠券'")
    private Coupon usedCoupon;
    
    /**
     * 开票ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", columnDefinition="int comment '发票Id'")
    private Invoice invoice;

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("OrderPayment [orderPaymentId=").append(orderPaymentId)
                .append("]");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((orderPaymentId == null) ? 0 : orderPaymentId.hashCode());
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
        OrderPayment other = (OrderPayment) obj;
        if (orderPaymentId == null)
        {
            if (other.orderPaymentId != null)
            {
                return false;
            }
        }
        else if (!orderPaymentId.equals(other.orderPaymentId))
        {
            return false;
        }
        return true;
    }
    
    
}
