package com.kl.parkLine.entity;

import java.io.Serializable;

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
import com.kl.parkLine.enums.ParkCarType;

import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("serial")
@Entity
@Table(name = "TC_PARK_CAR_ITEM")
@org.hibernate.annotations.Table(appliesTo = "tc_park_car_item",comment = "停车场黑白名单")
@Getter
@Setter
@DynamicUpdate
@DynamicInsert
public class ParkCarItem implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "park_white_item_id")
    @JsonIgnore
    private Integer parkWhiteItemId;
    
    /**
     * 停车场
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY) 
    @JoinColumn(name = "park_id", columnDefinition="int comment '停车场Id'")
    @JsonIgnore
    private Park park;
    
    /**
     * 名单类型
     */
    @Column(name = "park_car_type", columnDefinition="varchar(255) comment '名单类型: 白名单(white)/黑名单(black)'")
    @Enumerated(EnumType.STRING)
    private ParkCarType parkCarType;
    
    /**
     * 车牌号
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY) 
    @JoinColumn(name = "car_id", columnDefinition="int comment '车辆Id'")
    @JsonIgnore
    private Car car;
    
    /**
     * 备注
     */
    @Column(name = "remark", length=64, columnDefinition="varchar(64) comment '备注'")
    private String remark;
    
}
