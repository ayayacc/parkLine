package com.kl.parkLine.entity;

import java.math.BigDecimal;

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

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kl.parkLine.enums.CarType;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * 停车场
 *
 * <p>停车场计费配置
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
@Table(name = "TC_PARK_STEP_FEE")
@org.hibernate.annotations.Table(appliesTo = "tc_park_step_fee",comment = "停车场阶梯计费配置")
public class ParkStepFee implements java.io.Serializable
{
    @Id
    @Column(name = "park_step_fee_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer parkStepFeeId;
    
    /**
     * 停车场
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY) 
    @JoinColumn(name = "park_id", columnDefinition="int comment '停车场Id'")
    @JsonIgnore
    private Park park;
    
    /**
     * 适用车型: 燃油车/新能源车
     */
    @Column(name = "car_type", nullable = false, columnDefinition="varchar(32) comment '适用车型: fuel(燃油车)/newEnergy(新能源车)'")
    @Enumerated(EnumType.STRING)
    private CarType carType;
    
    /**
     * 起始分钟
     */
    @Column(name = "start_min", nullable = false, columnDefinition="int comment '起始分钟'")
    private Integer startMin;
    
    /**
     * 结束分钟
     */
    @Column(name = "end_min", nullable = false, columnDefinition="int comment '结束分钟'")
    private Integer endMin;
        
    /**
     * 本阶梯费用(元)
     */
    @Column(name = "amt", precision = 8 ,scale = 2, nullable = false, columnDefinition="decimal(8,2) comment '本阶梯费用(元)'")
    private BigDecimal amt;
}
