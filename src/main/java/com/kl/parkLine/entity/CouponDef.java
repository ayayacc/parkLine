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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
import com.fasterxml.jackson.annotation.JsonFormat;
import com.kl.parkLine.annotation.NeedToCompare;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@org.hibernate.annotations.Table(appliesTo = "tt_coupon_def",comment = "优惠券定义")
@DynamicUpdate
@DynamicInsert
@ApiModel("优惠券定义")
@EntityListeners({AuditingEntityListener.class})
public class CouponDef extends AbstractDateEntity implements java.io.Serializable
{
    @Id
    @Column(name = "coupon_def_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty("优惠券定义id")
    private Integer couponDefId;
    
    /**
     * 优惠券定义编号
     */
    @Column(name = "code", nullable = false, unique = true, length = 32, columnDefinition="varchar(32) comment '优惠券定义唯一编号'")
    @ApiModelProperty("优惠券定义唯一编号")
    private String code;
    
    /**
     * 优惠券定义名称
     */
    @NeedToCompare(name = "名称")
    @Column(name = "name", nullable = false, length = 64, unique = true, columnDefinition="varchar(64) comment '优惠券定义名称'")
    @ApiModelProperty("优惠券定义名称")
    private String name;
    
    /**
     * 优惠券定义变动备注
     */
    @ApiModelProperty("优惠券定义变动备注")
    @Transient
    private String changeRemark;
    
    /**
     *折扣后的封顶金额
     */
    @NeedToCompare(name = "最大折扣金额")
    @Column(name = "max_amt", nullable = false, precision = 15 ,scale = 2, columnDefinition="decimal(15,2) comment '最大折扣金额'")
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
     * 优惠券总数
     */
    @NeedToCompare(name = "优惠券总数")
    @Column(name = "total_cnt", nullable = false, columnDefinition="int comment '优惠券总数'")
    @ApiModelProperty("优惠券总数")
    private Integer totalCnt;
    
    /**
     * 已经领取数量
     */
    @NeedToCompare(name = "已经领取数量")
    @Column(name = "applied_cnt", nullable = false, columnDefinition ="int default 0 comment '已经领取数量'")
    @ApiModelProperty("已经领取数量")
    private Integer appliedCnt;
    
    /**
     * 已经使用数量
     */
    @NeedToCompare(name = "已经使用数量")
    @Column(name = "used_cnt", nullable = false, columnDefinition ="int default 0 comment '已经使用数量'")
    @ApiModelProperty("已经使用数量")
    private Integer usedCnt;
    
    /**
     * 是否有效
     */
    @NeedToCompare(name = "是否有效")
    @Column(name = "enabled", length = 4, nullable = false, columnDefinition ="varchar(4) comment '是否有熊Y/N'")
    @ApiModelProperty("是否有效")
    private String enabled;
    
    /**
     * 有效期开始时间
     */
    @Temporal(TemporalType.DATE)            
    @Column(name = "start_date", nullable = false, columnDefinition="date comment '有效期开始时间(含当天)'")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NeedToCompare(name = "有效期开始时间")
    @ApiModelProperty("有效期开始时间")
    private Date startDate;
    
    /**
     * 有效期结束时间
     */
    @Temporal(TemporalType.DATE)            
    @Column(name = "end_date", nullable = false, columnDefinition="date comment '有效期结束时间(含当天)'")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NeedToCompare(name = "有效期结束时间")
    @ApiModelProperty("有效期结束时间")
    private Date endDate;
    
    /**
     * 领取后N天有效
     */
    @NeedToCompare(name = "领取后N天有效")
    @Column(name = "term", nullable = false, columnDefinition="int comment '领取后N天有效'")
    @ApiModelProperty("领取后N天有效")
    private Integer term;
    
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
    @JoinTable(name = "tr_coupon_def_park", joinColumns = { @JoinColumn(name="coupon_def_id") }, inverseJoinColumns={ @JoinColumn(name="park_id") })  
    @ApiModelProperty(hidden = true)
    private List<Park> applicableParks;
    
    /**
     * 操作记录
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "couponDef", cascade = {CascadeType.ALL})  
    @OrderBy(value = "createdDate desc")
    @JSONField(serialize = false)
    @ApiModelProperty(hidden = true)
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
