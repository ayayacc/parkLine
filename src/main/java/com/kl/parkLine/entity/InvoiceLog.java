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
@Table(name = "TT_INVOICE_LOG")
@org.hibernate.annotations.Table(appliesTo = "tt_invoice_log",comment = "开票申请变动记录")
@Getter
@Setter
@DynamicUpdate
@DynamicInsert
@EntityListeners({AuditingEntityListener.class})
public class InvoiceLog extends AbstractLog implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_log_id")
    @JsonIgnore
    private Integer invoiceLogId;
    
    @ManyToOne(optional = false, fetch = FetchType.LAZY) 
    @JoinColumn(name = "invoice_id", columnDefinition="int comment '发票Id'")
    @JsonIgnore
    private Invoice invoice;

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((invoiceLogId == null) ? 0 : invoiceLogId.hashCode());
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
        InvoiceLog other = (InvoiceLog) obj;
        if (invoiceLogId == null)
        {
            if (other.invoiceLogId != null)
            {
                return false;
            }
        }
        else if (!invoiceLogId.equals(other.invoiceLogId))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("InvoiceLog [invoiceLogId=").append(invoiceLogId)
                .append("]");
        return builder.toString();
    }

}
