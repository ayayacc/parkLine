package com.kl.parkLine.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.kl.parkLine.annotation.NeedToCompare;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * 停车场
 *
 * <p>停车场固定计费配置:freeTime分钟内免费，每feePeriod分钟收费price元, maxPeriod小时内，最高maxAmt元
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
@Table(name = "TC_PARK_FIXED_FEE")
public class ParkFixedFee implements java.io.Serializable
{
    @Id
    @Column(name = "park_fixed_fee_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer parkFixedFeeId;
    
    /*
     * 免费时长（分钟）
     */
    @NeedToCompare(name = "免费时长（分钟）")
    @Column(name = "free_time", nullable = false, columnDefinition ="int default 0") 
    private Integer freeTime;
    
    
    /**
     * 计费周期（分钟）
     */
    @Column(name = "fee_period", nullable = false)
    private Integer feePeriod;
    
    /**
     * 单价
     */
    @Column(name = "price", precision = 8 ,scale = 2, nullable = false)
    private BigDecimal price;
    
    /**
     * 计算封顶金额的周期(小时)
     */
    @Column(name = "max_period", nullable = false)
    private Integer maxPeriod;
    
    /**
     * 最高金额
     */
    @Column(name = "max_amt", precision = 8 ,scale = 2, nullable = false, columnDefinition ="int default 999999") 
    private BigDecimal maxAmt;
        
}
