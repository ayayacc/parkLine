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
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@SuppressWarnings("serial")
@Entity
@Table(name = "TT_ORDER_LOG")
@org.hibernate.annotations.Table(appliesTo = "tt_order_log",comment = "订单变动记录")
@Getter
@Setter
@DynamicUpdate
@DynamicInsert
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({AuditingEntityListener.class})
public class OrderLog extends AbstractLog implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_log_id")
    @JsonIgnore
    private Integer orderLogId;
    
    @ManyToOne(optional = false, fetch = FetchType.LAZY) 
    @JoinColumn(name = "order_id", columnDefinition="int comment '订单Id'")
    @JsonIgnore
    private Order order;
    
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="event_id", columnDefinition="int comment '事件Id'")
    private Event event;

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("OrderLog [orderLogId=").append(orderLogId).append("]");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((orderLogId == null) ? 0 : orderLogId.hashCode());
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
        OrderLog other = (OrderLog) obj;
        if (orderLogId == null)
        {
            if (other.orderLogId != null)
            {
                return false;
            }
        }
        else if (!orderLogId.equals(other.orderLogId))
        {
            return false;
        }
        return true;
    }
}
