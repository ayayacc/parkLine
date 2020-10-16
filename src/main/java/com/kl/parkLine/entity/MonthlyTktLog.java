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

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("serial")
@Entity
@Table(name = "TT_MONTHLY_TICKET_LOG")
@Getter
@Setter
@DynamicUpdate
@DynamicInsert
@EntityListeners({AuditingEntityListener.class})
public class MonthlyTktLog extends AbstractLog implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "monthly_tkt_log_id")
    @JsonIgnore
    private Integer monthlyTktLogId;
    
    @ManyToOne(optional = false, fetch = FetchType.LAZY) 
    @JoinColumn(name = "monthly_tkt_id")
    @JsonIgnore
    private MonthlyTkt monthlyTkt;
    
    /**
     * 
     */
    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("MonthlyTktLog [monthlyTicketLogId=")
                .append(monthlyTktLogId).append("]");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((monthlyTktLogId == null) ? 0
                : monthlyTktLogId.hashCode());
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
        MonthlyTktLog other = (MonthlyTktLog) obj;
        if (monthlyTktLogId == null)
        {
            if (other.monthlyTktLogId != null)
            {
                return false;
            }
        }
        else if (!monthlyTktLogId.equals(other.monthlyTktLogId))
        {
            return false;
        }
        return true;
    }
    
    
}
