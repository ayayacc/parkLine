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
@Table(name = "TT_CAR")
@DynamicUpdate
@DynamicInsert
@EntityListeners({AuditingEntityListener.class})
public class Car extends AbstractEntity implements java.io.Serializable
{
    @Id
    @Column(name = "car_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer carId;
    
    /**
     * 车牌号
     */
    @Column(name = "car_no", length = 16, unique = true, nullable = false)
    private String carNo;
    
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Car [carId=").append(carId).append(", carNo=")
                .append(carNo).append("]");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((carId == null) ? 0 : carId.hashCode());
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
        Car other = (Car) obj;
        if (carId == null)
        {
            if (other.carId != null)
            {
                return false;
            }
        }
        else if (!carId.equals(other.carId))
        {
            return false;
        }
        return true;
    }
    
    
}
