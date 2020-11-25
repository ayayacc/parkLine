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
@org.hibernate.annotations.Table(appliesTo = "tt_order",comment = "订单")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({AuditingEntityListener.class})
public class Order extends AbstractDateEntity implements java.io.Serializable, Cloneable
{
    @Id
    @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;
    
    /**
     * 订单编号
     */
    @Column(name = "code", nullable = false, unique = true, length = 128, columnDefinition="varchar(128) comment '订单唯一编号,系统自动生成'")
    private String code;
    
    /**
     * 订单类型: 停车订单/月票订单/优惠券激活订单/钱包充值订单
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", columnDefinition="varchar(255) comment '订单类型'")
    private OrderType type;
    
    /*停车订单特有字段*/
    /**
     * 停车场
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "park_id", columnDefinition="int comment '停车场Id'")
    private Park park;
    
    /**
     * 车辆
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", columnDefinition="int comment '车辆Id'")
    private Car car;
    
    /**
     * 入场时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "in_time", columnDefinition="datetime comment '入场时间'")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NeedToCompare(name = "入场时间")
    private Date inTime; 
    
    /**
     * 使用的月票Id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monthly_tkt_id", columnDefinition="int comment '使用的月票Id'")
    private Order usedMonthlyTkt;
    
    /**
     * 出场时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "out_time", columnDefinition="datetime comment '出场时间'")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NeedToCompare(name = "出场时间")
    private Date outTime; 
    
    /**
     * 提前交费离开时间限制
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "out_time_limit", columnDefinition="datetime comment '提前交费离开时间限制'")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NeedToCompare(name = "提前交费离开时间限制")
    private Date outTimeLimit; 
    
    /**
     * 行为唯一标识符
     */
    @Column(name = "act_id", length = 48, unique = true, columnDefinition="varchar(48) comment '行为唯一标识符'")
    private String actId;
    
    /**
     * 入场截图url
     */
    @Column(name = "in_img_code", length = 128, columnDefinition="varchar(128) comment '入场截图url'")
    private String inImgCode;
    
    /**
     * 出场截图url
     */
    @Column(name = "out_img_code", length = 128, columnDefinition="varchar(128) comment '出场截图url'")
    private String outImgCode;
    
    /**
     * 博粤设备识别车牌Id
     */
    @Column(name = "plate_id", columnDefinition="int comment '博粤设备识别车牌Id'")
    private Integer plateId;
    
    /**
     * 入场抓拍设备序列号(为了提高查询效率，不关联到设备表)
     */
    @Column(name = "in_device_sn", length = 255, columnDefinition="varchar(255) comment '入场抓拍设备序列号'")
    private String inDeviceSn;
    
    /**
     * 出场抓拍设备序列号(为了提高查询效率，不关联到设备表)
     */
    @Column(name = "out_device_sn", length = 255, columnDefinition="varchar(255) comment '出场抓拍设备序列号'")
    private String outDeviceSn;
    
    /**
     * 开始时间
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "start_time", columnDefinition="date comment '月票开始时间'")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate; 
    
    /**
     * 结束时间
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "end_time", columnDefinition="date comment '月票结束时间'")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate; 
    
    /**
     * 使用的优惠券
     */
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "used_coupon", columnDefinition="int comment '使用的优惠券'")
    private Coupon usedCoupon;
    
    /*优惠券激活订单特有字段*/
    /**
     * 被激活的优惠券
     */
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "activated_coupon", columnDefinition="int comment '被激活的优惠券'")
    private Coupon activatedCoupon;
    
    /*所有订单类型公用*/
    /**
     * 订单状态: 已入场/待支付（已出场）/已支付/开票中/已开票
     */
    @Column(name = "status", columnDefinition="varchar(255) comment '订单状态: in(已入场)/needToPay(待支付)/payed(已支付)/noNeedToPay(无需支付)/canceled(已取消)'")
    @Enumerated(EnumType.STRING)
    @NeedToCompare(name = "状态")
    private OrderStatus status;
    
    /**
     * 拥有者
     */
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "owner_id", columnDefinition="int comment '拥有者'")
    @JsonIgnore
    private User owner;
    
    /**
     * 付款时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "payment_time", columnDefinition="datetime comment '付款时间'")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NeedToCompare(name = "付款时间")
    private Date paymentTime; 
    
    /**
     * 付款银行代码
     */
    @Column(name = "bank_type", length = 48, columnDefinition="varchar(48) comment '付款银行代码,微信返回'")
    @NeedToCompare(name = "银行代码")
    private String bankType;
    
    /**
     * 微信支付订单号
     */
    @Column(name = "wx_transaction_id", length = 48, columnDefinition="varchar(48) comment '微信支付订单号'")
    @NeedToCompare(name = "微信支付订单号")
    private String wxTransactionId;
    
    /**
     * 支付方式: 微信/钱包
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", columnDefinition="varchar(255) comment '支付方式:wx(微信)/qb(钱包)'")
    @NeedToCompare(name = "支付方式")
    private PaymentType paymentType;
    
    /**
     * 金额（使用优惠券前）
     */
    @Column(name = "amt", precision = 15 ,scale = 2, columnDefinition="decimal(15,2) comment '订单金额（使用优惠券前）'")
    @NeedToCompare(name = "金额（使用优惠券前）")
    private BigDecimal amt;
    
    /**
     * 实际金额（使用优惠券后）
     */
    @Column(name = "real_amt", precision = 15 ,scale = 2, columnDefinition="decimal(15,2) comment '实际金额（使用优惠券后）'")
    @NeedToCompare(name = "金额（使用优惠券后）")
    private BigDecimal realAmt;
    
    /**
     * 涉及钱包操作后的钱包余额
     */
    @Column(name = "wallet_balance", precision = 15 ,scale = 2, columnDefinition="decimal(15,2) comment '涉及钱包操作后的钱包余额'")
    @NeedToCompare(name = "涉及钱包操作后的钱包余额")
    private BigDecimal walletBalance;

    /**
     * 开票ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", columnDefinition="int comment '发票Id'")
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
    @ApiModelProperty(hidden = true)
    @JsonIgnore
    @Transient
    private StringBuilder changeRemark;
    
    public String getChangeRemark() 
    {
        if (null == changeRemark)
        {
            return "";
        }
        else
        {
            return changeRemark.toString();
        }
    }
    
    public void appedChangeRemark(String remark)
    {
        if (null == changeRemark)
        {
            changeRemark = new StringBuilder();
        }
        changeRemark.append(remark);
    }
    
    /**
     * 是否需要自动匹配优惠券
     */
    @ApiModelProperty(hidden = true)
    @Transient
    @JsonIgnore
    private Boolean autoCoupon;
    
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
