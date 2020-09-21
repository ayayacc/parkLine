package com.kl.parkLine.entity;

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

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
    @Column(name = "code", nullable = false, length = 64, unique = true)
    private String code;
    
    /**
     * 停车场名称
     */
    @Column(name = "name", nullable = false, length = 64, unique = true)
    private String name;
    
    /**
     * 停车场总车位数量
     */
    @Column(name = "total_cnt")
    private Integer totalCnt;
    
    /**
     * 当前可用车位数量
     */
    @Column(name = "free_cnt")
    private Integer freeCnt;
    
    /**
     * 经纬度
     */
    @Column(name = "geo", length = 32)
    private String geo;
    
    /**
     * 联系方式
     */
    @Column(name = "contact", nullable = false, length = 16)
    private String contact;
    
    /**
     * 省
     */
    @Column(name = "province", length = 16)
    private String province;
    
    /**
     * 市
     */
    @Column(name = "city", length = 16)
    private String city;
    
    /**
     * 详细地址
     */
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
    @Column(name = "enabled", length = 4, nullable = false)
    private String enabled;

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
