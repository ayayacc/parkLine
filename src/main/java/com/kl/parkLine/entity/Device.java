package com.kl.parkLine.entity;

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

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kl.parkLine.annotation.NeedToCompare;
import com.kl.parkLine.enums.DeviceUseage;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * 车辆信息
 *
 * <p>车辆数据
 * @author chenc 2020年9月11日
 * @see
 * @since 1.0
 */
@Getter
@Setter
@SuppressWarnings("serial")
@Entity
@Table(name = "TC_DEVICE")
@DynamicUpdate
@DynamicInsert
@EntityListeners({AuditingEntityListener.class})
public class Device implements java.io.Serializable
{
    @Id
    @Column(name = "device_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer deviceId;
    
    /**
     * 所属停车场
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY) 
    @JoinColumn(name = "park_id")
    @JsonIgnore
    private Park park;
    
    /**
     * 设备序列号
     */
    @NeedToCompare(name = "序列号")
    @Column(name = "serial_no", nullable = false, length = 64, unique = true)
    private String serialNo;
    
    /**
     * 设备名称
     */
    @NeedToCompare(name = "名称")
    @Column(name = "name", nullable = false, length = 64)
    private String name;
    
    /**
     * 设备用途，监控入场/出场
     */
    @Column(name = "useage")
    @Enumerated(EnumType.STRING)
    private DeviceUseage useage;
}
