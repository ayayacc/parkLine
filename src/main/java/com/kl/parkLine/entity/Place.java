package com.kl.parkLine.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
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

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * 停车位
 *
 * <p>停车位数据
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
@Table(name = "TC_PLACE")
@EntityListeners({AuditingEntityListener.class})
public class Place extends AbstractEntity implements java.io.Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id")
    private Integer placeId;
    
    /**
     * 所属停车场
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY) 
    @JoinColumn(name = "park_id")
    @JsonIgnore
    private Park park;
    
    /*
     * 停车位ID, 由接口传递上来的ID
     */
    @Column(name = "original_id", length = 64)
    private String originalId;
    
    /**
     * 停车位编号
     */
    @Column(name = "code", nullable = false, length = 16)
    private String code;
    
    /**
    * 监控此泊位的设备编号
    */
   @Column(name = "device_sn", length = 64)
   private String deviceSn;
    
    /**
     * 停车位经纬度
     */
    @Column(name = "geo", length = 32)
    private String geo;
    
    /**
     * 是否有效
     */
    @Column(name = "is_enable")
    private boolean isEnable;

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Place [placeId=").append(placeId).append("]");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((placeId == null) ? 0 : placeId.hashCode());
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
        Place other = (Place) obj;
        if (placeId == null)
        {
            if (other.placeId != null)
            {
                return false;
            }
        }
        else if (!placeId.equals(other.placeId))
        {
            return false;
        }
        return true;
    }
    
    
}
