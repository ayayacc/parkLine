package com.kl.parkLine.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
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
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kl.parkLine.enums.CarType;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * 停车场
 *
 * <p>停车场特殊计费时段
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
@Table(name = "TC_PARK_SPECIAL_FEE")
public class ParkSpecialFee implements java.io.Serializable
{
    @Id
    @Column(name = "park_step_fee_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer parkStepFeeId;
    
    /**
     * 停车场
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY) 
    @JoinColumn(name = "park_id")
    @JsonIgnore
    private Park park;
    
    /**
     * 适用车型: 燃油车/新能源车
     */
    @Column(name = "car_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CarType carType;
    
    /**
     * 起始时间
     */
    @Temporal(TemporalType.TIME)       
    @DateTimeFormat(pattern = "HH:mm" )
    @JsonFormat(pattern="HH:mm",timezone="GMT+8")
    @Column(name = "start_time", nullable = false)
    private Date startTime;
    
    /**
     * 结束时间
     */
    @Temporal(TemporalType.TIME)       
    @DateTimeFormat(pattern = "HH:mm" )
    @JsonFormat(pattern="HH:mm",timezone="GMT+8")
    @Column(name = "end_time", nullable = false)
    private Date endTime;
        
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
}