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

import com.alibaba.fastjson.annotation.JSONField;
import com.kl.parkLine.annotation.NeedToCompare;
import com.kl.parkLine.enums.OrderStatus;
import com.kl.parkLine.enums.OrderType;
import com.kl.parkLine.enums.PlaceType;

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
    @Column(name = "type", nullable=false, columnDefinition="varchar(255) comment '订单类型:parking(停车订单)/monthlyTicket(月票)/coupon(优惠券激活)/walletIn(钱包充值)'")
    private OrderType type;
    
    /*停车订单特有字段*/
    /**
     * 停车场
     */
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "park_id", columnDefinition="int comment '停车场Id'")
    private Park park;
    
    /**
     * 车辆
     */
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", columnDefinition="int comment '车辆Id'")
    private Car car;
    
    /**
     * 入场时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "in_time", columnDefinition="datetime comment '入场时间'")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
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
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NeedToCompare(name = "出场时间")
    private Date outTime; 
    
    /**
     * 是否已经抬杆出场
     */
    @ApiModelProperty("是否已经抬杆出场")
    @Column(name = "is_out", columnDefinition="bool comment '是否已经抬杆出场'")
    private Boolean isOut;
    
    /**
     * 提前交费离开时间限制
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "out_time_limit", columnDefinition="datetime comment '提前交费离开时间限制'")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
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
    @JSONField(format="yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate; 
    
    /**
     * 结束时间
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "end_time", columnDefinition="date comment '月票结束时间'")
    @JSONField(format="yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate; 
    
    /**
     * 车位类型: 地面/地下
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "place_type", nullable=false, columnDefinition="varchar(16) comment '订单类型:parking(停车订单)/monthlyTicket(月票)/coupon(优惠券激活)/walletIn(钱包充值)'")
    private PlaceType placeTye;
    
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
    @Column(name = "status", columnDefinition="varchar(16) comment '订单状态: in(已入场)/needToPay(待支付)/payed(已支付)/noNeedToPay(无需支付)/expired(已过期)/canceled(已取消)'")
    @Enumerated(EnumType.STRING)
    @NeedToCompare(name = "状态")
    private OrderStatus status;
    
    /**
     * 拥有者
     */
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "owner_id", columnDefinition="int comment '拥有者'")
    @JSONField(serialize = false)
    private User owner;
    
    /**
     * 微信支付订单号
     */
    @Column(name = "wx_transaction_id", length = 48, columnDefinition="varchar(48) comment '微信支付订单号'")
    @NeedToCompare(name = "微信支付订单号")
    private String wxTransactionId;
    
    /**
     * 使用优惠券前订单金额(元)
     */
    @Column(name = "amt", precision = 15 ,scale = 2, columnDefinition="decimal(15,2) comment '使用优惠券前订单金额(元)'")
    private BigDecimal amt;

    /**
     * 使用优惠券前已付款金额(元)
     */
    @Column(name = "payed_amt", precision = 15 ,scale = 2, columnDefinition="decimal(15,2) comment '使用优惠券前已付款金额(元)'")
    private BigDecimal payedAmt;
    
    /**
     * 使用优惠券后未付金额(元)
     */
    @Column(name = "real_unpayed_amt", precision = 15 ,scale = 2, columnDefinition="decimal(15,2) comment '使用优惠券后金额(元)'")
    private BigDecimal realUnpayedAmt;
    
    /**
     * 使用优惠券后实际已付金额(元)
     */
    @Column(name = "real_payed_amt", precision = 15 ,scale = 2, columnDefinition="decimal(15,2) comment '使用优惠券后实际已付金额(元)'")
    private BigDecimal realPayedAmt;
    
    /**
     * 最后一次付款时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_payment_time", columnDefinition="datetime comment '最后一次付款时间'")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NeedToCompare(name = "最后一次付款时间")
    private Date lastPaymentTime; 
    
    /**
     * 付款记录
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order", cascade = {CascadeType.ALL})  
    @OrderBy(value = "paymentTime desc")
    @JSONField(serialize = false)
    private List<OrderPayment> orderPayments;
    
    /**
     * 操作记录
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order", cascade = {CascadeType.ALL})  
    @OrderBy(value = "createdDate desc")
    @JSONField(serialize = false)
    private List<OrderLog> logs;

    /**
     * 订单变动备注
     */
    @ApiModelProperty(hidden = true)
    @JSONField(serialize = false)
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
