package com.kl.parkLine.entity;

import java.util.Date;
import java.util.List;

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
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * 月票
 *
 * <p>月票
 * @author chenc 2020年9月11日
 * @see
 * @since 1.0
 */
@Getter
@Setter
@SuppressWarnings("serial")
@Entity
@Table(name = "TT_MONTHLY_TICKEY")
@DynamicUpdate
@DynamicInsert
@EntityListeners({AuditingEntityListener.class})
public class MonthlyTicket extends AbstractEntity implements java.io.Serializable
{
    @Id
    @Column(name = "monthly_ticket_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer monthlyTicketId;
    
    /**
     * 月票编号
     */
    @Column(name = "code", nullable = false, unique = true, length = 16)
    private String code;
    
    /**
     * 停车场 
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "park_id")
    private Park park;
    
    /**
     * 车辆
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id")
    private Car car;
    
    /**
     * 是否被禁用
     */
    @Column(name = "is_enable")
    private boolean isEnable;
    
    /**
     * 有效期开始时间
     */
    @Temporal(TemporalType.DATE)            
    @Column(name = "start_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;
    
    /**
     * 有效期结束时间
     */
    @Temporal(TemporalType.DATE)            
    @Column(name = "end_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;
    
    /**
     * 操作记录
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "monthlyTicket", cascade = {CascadeType.ALL})  
    @OrderBy(value = "createdDate desc")
    @JsonIgnore
    private List<MonthlyTicketLog> logs;

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("MonthlyTicket [monthlyTicketId=")
                .append(monthlyTicketId).append(", code=").append(code)
                .append("]");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((monthlyTicketId == null) ? 0 : monthlyTicketId.hashCode());
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
        MonthlyTicket other = (MonthlyTicket) obj;
        if (monthlyTicketId == null)
        {
            if (other.monthlyTicketId != null)
            {
                return false;
            }
        }
        else if (!monthlyTicketId.equals(other.monthlyTicketId))
        {
            return false;
        }
        return true;
    }
    
    
   
}
