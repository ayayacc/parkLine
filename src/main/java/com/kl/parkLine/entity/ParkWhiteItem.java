package com.kl.parkLine.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
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

import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("serial")
@Entity
@Table(name = "TC_PARK_WHITE_LIST")
@org.hibernate.annotations.Table(appliesTo = "tc_park_white_list",comment = "停车场白名单")
@Getter
@Setter
@DynamicUpdate
@DynamicInsert
public class ParkWhiteItem implements Serializable
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
     * 车牌号
     */
    @Column(name = "car_no", length = 16, nullable = false, columnDefinition="varchar(16) comment '车牌号'")
    private String carNo;
    
}
