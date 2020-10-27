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
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kl.parkLine.annotation.NeedToCompare;
import com.kl.parkLine.enums.OrderStatus;
import com.kl.parkLine.enums.OrderType;
import com.kl.parkLine.enums.PaymentType;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * 停车订单
 *
 * <p>记录用户的停车行为
 * @author chenc 2020年9月11日
 * @see
 * @since 1.0
 */
@Getter
@Setter
@SuppressWarnings("serial")
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "TT_ORDER")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({AuditingEntityListener.class})
public class Order extends AbstractEntity implements java.io.Serializable
{
    @Id
    @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;
    
    /**
     * 订单编号
     */
    @Column(name = "code", nullable = false, unique = true, length = 16)
    private String code;
    
    /**
     * 订单类型: 停车订单/月票订单/优惠券激活订单/钱包充值订单
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private OrderType type;
    
    /*停车订单特有字段*/
    /**
     * 停车场
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "park_id")
    private Park park;
    
    /**
     * 车辆
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id")
    private Car car;
    
    /**
     * 入场时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "in_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NeedToCompare(name = "入场时间")
    private Date inTime; 
    
    /**
     * 离开时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "out_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NeedToCompare(name = "出场时间")
    private Date outTime; 
    
    /**
     * 行为唯一标识符
     */
    @Column(name = "act_id", length = 48, unique = true)
    private String actId;
    
    /**
     * 开始时间
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "start_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate; 
    
    /**
     * 结束时间
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "end_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate; 
    
    /**
     * 使用的优惠券
     */
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "used_coupon")
    private Coupon usedCoupon;
    
    /*优惠券激活订单特有字段*/
    /**
     * 被激活的优惠券
     */
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "activated_coupon")
    private Coupon activatedCoupon;
    
    /*所有订单类型公用*/
    /**
     * 订单状态: 已入场/待支付（已出场）/已支付/开票中/已开票
     */
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    @NeedToCompare(name = "状态")
    private OrderStatus status;
    
    /**
     * 拥有者
     */
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "owner_id")
    @JsonIgnore
    @NeedToCompare(name = "拥有者")
    private User owner;
    
    /**
     * 付款时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "payment_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NeedToCompare(name = "付款时间")
    private Date paymentTime; 
    
    /**
     * 付款银行代码
     */
    @Column(name = "bank_type", length = 48)
    @NeedToCompare(name = "银行代码")
    private String bankType;
    
    /**
     * 微信支付订单号
     */
    @Column(name = "wx_transaction_id", length = 48)
    @NeedToCompare(name = "微信支付订单号")
    private String wxTransactionId;
    
    /**
     * 支付方式: 微信/钱包
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type")
    @NeedToCompare(name = "支付方式")
    private PaymentType paymentType;
    
    /**
     * 金额
     */
    @Column(name = "amt", precision = 15 ,scale = 2)
    @NeedToCompare(name = "金额")
    private BigDecimal amt;

    /**
     * 开票ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    @NeedToCompare(name = "发票")
    private Invoice invoice;
    
    /**
     * 操作记录
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order", cascade = {CascadeType.ALL})  
    @OrderBy(value = "createdDate desc")
    @JsonIgnore
    private List<OrderLog> logs;

    /**
     * 订单变动备注
     */
    @ApiModelProperty("订单变动备注")
    @Transient
    private String changeRemark;
    
    @ApiModelProperty("订单变动点")
    @Transient
    private String diff;
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((orderId == null) ? 0 : orderId.hashCode());
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
        Order other = (Order) obj;
        if (orderId == null)
        {
            if (other.orderId != null)
            {
                return false;
            }
        }
        else if (!orderId.equals(other.orderId))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Order [orderId=").append(orderId).append("]");
        return builder.toString();
    }
}
