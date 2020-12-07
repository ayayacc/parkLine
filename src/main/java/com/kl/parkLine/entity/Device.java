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

import com.alibaba.fastjson.annotation.JSONField;
import com.kl.parkLine.annotation.NeedToCompare;
import com.kl.parkLine.enums.DeviceUseage;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * 车辆信息
 *
 * <p>设备表
 * @author chenc 2020年9月11日
 * @see
 * @since 1.0
 */
@Getter
@Setter
@SuppressWarnings("serial")
@Entity
@Table(name = "TC_DEVICE")
@org.hibernate.annotations.Table(appliesTo = "tc_device",comment = "设备表")
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
    @JoinColumn(name = "park_id", columnDefinition="int comment '所属停车场Id'")
    @JSONField(serialize = false)
    private Park park;
    
    /**
     * 设备序列号
     */
    @NeedToCompare(name = "序列号")
    @Column(name = "serial_no", nullable = false, length = 64, unique = true, columnDefinition="varchar(64) comment '序列号'")
    private String serialNo;
    
    /**
     * 设备名称
     */
    @NeedToCompare(name = "名称")
    @Column(name = "name", nullable = false, length = 64, columnDefinition="varchar(64) comment '名称'")
    private String name;
    
    /**
     * 设备用途，监控入场/出场
     */
    @Column(name = "useage", columnDefinition="varchar(255) comment '设备用途:in(入场)/out(出场)'")
    @Enumerated(EnumType.STRING)
    private DeviceUseage useage;
}
