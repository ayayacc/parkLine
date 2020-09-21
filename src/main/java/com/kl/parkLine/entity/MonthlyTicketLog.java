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
public class MonthlyTicketLog extends AbstractLog implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "monthly_ticket_log_id")
    @JsonIgnore
    private Integer monthlyTicketLogId;
    
    @ManyToOne(optional = false, fetch = FetchType.LAZY) 
    @JoinColumn(name = "monthly_ticket_id")
    @JsonIgnore
    private MonthlyTicket monthlyTicket;

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("MonthlyTicketLog [monthlyTicketLogId=")
                .append(monthlyTicketLogId).append("]");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((monthlyTicketLogId == null) ? 0
                : monthlyTicketLogId.hashCode());
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
        MonthlyTicketLog other = (MonthlyTicketLog) obj;
        if (monthlyTicketLogId == null)
        {
            if (other.monthlyTicketLogId != null)
            {
                return false;
            }
        }
        else if (!monthlyTicketLogId.equals(other.monthlyTicketLogId))
        {
            return false;
        }
        return true;
    }
    
    
}
