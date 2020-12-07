package com.kl.parkLine.entity;

import java.io.Serializable;

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

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("serial")
@Entity
@Table(name = "TT_CAR_LOG")
@org.hibernate.annotations.Table(appliesTo = "tt_car_log",comment = "车辆变动记录表")
@Getter
@Setter
@DynamicUpdate
@DynamicInsert
@EntityListeners({AuditingEntityListener.class})
public class CarLog extends AbstractLog implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_log_id")
    @JSONField(serialize = false)
    private Integer carLogId;
    
    @ManyToOne(optional = false, fetch = FetchType.LAZY) 
    @JoinColumn(name = "car_id", columnDefinition="int comment '车辆Id'")
    @JSONField(serialize = false)
    private Car car;
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("CarLog [carLogId=").append(carLogId)
                .append("]");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((carLogId == null) ? 0 : carLogId.hashCode());
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
        CarLog other = (CarLog) obj;
        if (carLogId == null)
        {
            if (other.carLogId != null)
            {
                return false;
            }
        }
        else if (!carLogId.equals(other.carLogId))
        {
            return false;
        }
        return true;
    }
    
    
}
