package com.kl.parkLine.entity;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kl.parkLine.annotation.NeedToCompare;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * 停车场
 *
 * <p>停车场数据
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
@Table(name = "TC_PARK")
@EntityListeners({AuditingEntityListener.class})
public class Park extends AbstractEntity implements java.io.Serializable
{
    @Id
    @Column(name = "park_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer parkId;
    
    /**
     * 停车场编号
     */
    @NeedToCompare(name = "编号")
    @Column(name = "code", nullable = false, length = 64, unique = true)
    private String code;
    
    /**
     * 停车场名称
     */
    @NeedToCompare(name = "名称")
    @Column(name = "name", nullable = false, length = 64, unique = true)
    private String name;
    
    /**
     * 停车场变动备注
     */
    @ApiModelProperty("停车场变动备注")
    @Transient
    private String changeRemark;
    
    /**
     * 停车场总车位数量
     */
    @NeedToCompare(name = "车位总数")
    @Column(name = "total_cnt", nullable = false)
    private Integer totalCnt;
    
    /**
     * 当前可用车位数量
     */
    @NeedToCompare(name = "可用车位数")
    @Column(name = "available_cnt", nullable = false)
    private Integer availableCnt;
    
    /**
     * 经纬度
     */
    @NeedToCompare(name = "经纬度")
    @Column(name = "geo", length = 32)
    private String geo;
    
    /**
     * 联系方式
     */
    @NeedToCompare(name = "联系方式")
    @Column(name = "contact", nullable = false, length = 16)
    private String contact;
    
    /**
     * 省
     */
    @NeedToCompare(name = "省")
    @Column(name = "province", length = 16)
    private String province;
    
    /**
     * 市
     */
    @NeedToCompare(name = "市")
    @Column(name = "city", length = 16)
    private String city;
    
    /**
     * 详细地址
     */
    @NeedToCompare(name = "详细地址")
    @Column(name = "address", length = 128)
    private String address;
    
    /**
     * 停车位
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "park", cascade = {CascadeType.ALL})  
    @OrderBy(value = "createdDate desc")
    @JsonIgnore
    private List<Place> places;
    
    /**
     * 是否有效
     */
    @NeedToCompare(name = "是否有效")
    @Column(name = "enabled", length = 4, nullable = false)
    private String enabled;
    
    /**
     * 计费逻辑: x分钟内免费，x小时起x元，每超过1小时加收x元，封顶x元
     */
    /*
     * 免费时长（分钟）
     */
    @NeedToCompare(name = "免费时长（分钟）")
    @Column(name = "free_Time", nullable = false, columnDefinition ="int default 0") 
    private Integer freeTime;
    
    /**
     * 第一阶梯计价时长（分钟）
     */
    @NeedToCompare(name = "第一阶梯计价时长（分钟）")
    @Column(name = "time_lev1", columnDefinition ="int default 0") 
    private Integer timeLev1;
    
    /**
     * 第一阶梯计价单价（元）
     */
    @NeedToCompare(name = "第一阶梯计价单价（元）")
    @Column(name = "price_lev1", precision = 8 ,scale = 2, columnDefinition ="int default 0")
    private BigDecimal priceLev1;
    
    /**
     * 第二阶梯计价时长（分钟）
     */
    @NeedToCompare(name = "第二阶梯计价时长（分钟）")
    @Column(name = "time_lev2", columnDefinition ="int default 0") 
    private Integer timeLev2;
    
    /**
     * 第二阶梯计价单价（元）
     */
    @NeedToCompare(name = "第二阶梯计价单价（元）")
    @Column(name = "price_lev2", precision = 8 ,scale = 2, columnDefinition ="int default 0") 
    private BigDecimal priceLev2;
    
    /**
     * 封顶x元
     */
    @NeedToCompare(name = "封顶x元")
    @Column(name = "max_amt", precision = 8 ,scale = 2, columnDefinition ="int default 999999") 
    private BigDecimal maxAmt;
    
    @NeedToCompare(name = "月票单价")
    @Column(name = "monthly_price", precision = 8 ,scale = 2, columnDefinition ="int default 999999") 
    private BigDecimal monthlyPrice;
    
    /**
     * 操作记录
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "park", cascade = {CascadeType.ALL})  
    @OrderBy(value = "createdDate desc")
    @JsonIgnore
    private List<ParkLog> logs;

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Park [parkId=").append(parkId).append("]");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parkId == null) ? 0 : parkId.hashCode());
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
        Park other = (Park) obj;
        if (parkId == null)
        {
            if (other.parkId != null)
            {
                return false;
            }
        }
        else if (!parkId.equals(other.parkId))
        {
            return false;
        }
        return true;
    }
}
