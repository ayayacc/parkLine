package com.kl.parkLine.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kl.parkLine.enums.MonthlyStatus;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@Table(name = "TT_MONTHLY_TKT")
@DynamicUpdate
@DynamicInsert
@EntityListeners({AuditingEntityListener.class})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTkt extends AbstractEntity implements java.io.Serializable
{
    @Id
    @Column(name = "monthly_tkt_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer monthlyTktId;
    
    /**
     * 月票编号
     */
    @Column(name = "code", nullable = false, unique = true, length = 16)
    private String code;
    
    /**
     * 停车场 
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "park_id", nullable = false)
    private Park park;
    
    /**
     * 车辆
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;
    
    /**
     * 是否被禁用
     */
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private MonthlyStatus status;
    
    /**
     * 变动备注
     */
    @ApiModelProperty("变动备注")
    @Transient
    private String changeRemark;
    
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
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "monthlyTkt", cascade = {CascadeType.ALL})  
    @OrderBy(value = "createdDate desc")
    @JsonIgnore
    private List<MonthlyTktLog> logs;

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("MonthlyTktVo [monthlyTicketId=")
                .append(monthlyTktId).append(", code=").append(code)
                .append("]");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((monthlyTktId == null) ? 0 : monthlyTktId.hashCode());
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
        MonthlyTkt other = (MonthlyTkt) obj;
        if (monthlyTktId == null)
        {
            if (other.monthlyTktId != null)
            {
                return false;
            }
        }
        else if (!monthlyTktId.equals(other.monthlyTktId))
        {
            return false;
        }
        return true;
    }
    
    
   
}
