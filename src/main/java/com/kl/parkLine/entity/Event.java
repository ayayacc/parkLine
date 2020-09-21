package com.kl.parkLine.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * 原始数据
 *
 * <p>从接口接收到的原始数据
 * @author chenc 2020年9月11日
 * @see
 * @since 1.0
 */
@Getter
@Setter
@SuppressWarnings("serial")
@Entity
@Table(name = "TT_EVENT")
@DynamicUpdate
@DynamicInsert
@EntityListeners({AuditingEntityListener.class})
public class Event extends AbstractEntity implements java.io.Serializable
{
    @Id
    @Column(name = "event_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer eventId;
    
    /**
     * 设备厂商名称
     */
    @Column(name = "device_company", length = 16)
    private String deviceCompany;
    
    /**
     * 事件类型
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type", nullable = false)
    private Dict type; 
    
    /**
     * 事件唯一标识符
     */
    @Column(name = "evt_guid", length = 48, nullable = false)
    private String evtGuid;
    
    /**
     * 行为唯一标识符
     */
    @Column(name = "act_id", length = 48)
    private String actId;
    
    
    //事件发生时间 yyyy-MM-dd HH:mm:ss
    @Temporal(TemporalType.TIMESTAMP)       
    @Column(name = "happen_time", nullable = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")     
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date happenTime;
    
    //车牌号码
    @Column(name = "plate_no", length = 16)
    private String plateNo;
    
    //车牌颜色
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plact_color")
    private Dict plateColor; 
    
    /**
     * 鉴定图片集
     * 
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "event", cascade = {CascadeType.ALL})  
    private Set<EventPic> pics;
    
    //停车场编码
    @Column(name = "park_code", length = 64, nullable = false)
    private String parkCode;
    
    //停车场名称
    @Column(name = "park_name", length = 64)
    private String parkName;
    
    //泊位号
    @Column(name = "place_code", length = 8)
    private String placeCode;
    
    //设备编号
    @Column(name = "device_sn", length = 64)
    private String deviceSn;
    
    //设备安装详细地址
    @Column(name = "device_place", length = 256)
    private String devicePlace;
    
    //车牌识别可信度
    @Column(name = "plate_credible", precision = 8 ,scale = 2)
    private BigDecimal plateCredible;
    
    //出入行为可信度
    @Column(name = "action_credible", precision = 8 ,scale = 2)
    private BigDecimal actionCredible;
    
    //异常停车类型
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_abnormal")
    private Dict parkingAbnormal;
    
    //设备经纬度，不为空时，格式如下： “108.23 22.45”
    @Column(name = "geo", length = 32)
    private String geo;
    
    //入场时间
    @Temporal(TemporalType.TIMESTAMP)       
    @Column(name = "time_in")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")     
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date timeIn;
    
    //入场鉴定图片，地址为图片绝对地址，如：http://10.7.7.15:8880/group1.jpg
    @Column(name = "pic_url_in", length = 128)
    private String picUrlIn;
    
    //出场时间
    @Temporal(TemporalType.TIMESTAMP)       
    @Column(name = "time_outn")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")     
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date timeOut;
    
    //出场鉴定图片，地址为图片绝对地址，如：http://10.7.7.15:8880/group1.jpg
    @Column(name = "pic_url_out", length = 128)
    private String picUrlOut;
    
    //作废目标事件的guid
    @Column(name = "target_guid", length = 48)
    private String targetGuid;
    
    //事件类型，此处为作废目标事件类型
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_type")
    private Dict targetType;
    
    /**
     * 是否有效
     */
    @Column(name = "enabled", length = 4, nullable = false)
    private String enabled;
}
