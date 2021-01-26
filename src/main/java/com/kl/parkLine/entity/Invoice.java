package com.kl.parkLine.entity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

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
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.alibaba.fastjson.annotation.JSONField;
import com.kl.parkLine.enums.InvoiceStatus;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * 开票申请
 *
 * <p>记录用户的开票申请行为
 * @author chenc 2020年9月11日
 * @see
 * @since 1.0
 */
@Getter
@Setter
@SuppressWarnings("serial")
@Entity
@Table(name = "TT_INVOICE")
@org.hibernate.annotations.Table(appliesTo = "tt_invoice",comment = "开票申请")
@DynamicUpdate
@DynamicInsert
@EntityListeners({AuditingEntityListener.class})
public class Invoice extends AbstractEntity implements java.io.Serializable
{
    @Id
    @Column(name = "invoice_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer invoiceId;
    
    /**
     * 开票编号
     */
    @Column(name = "code", nullable = false, unique = true, length = 16, columnDefinition="varchar(16) comment '开票编号,领取优惠券时自动生成'")
    private String code;
    
    /**
     * 开票金额
     */
    @Column(name = "amt", precision = 15 ,scale = 2, columnDefinition="decimal(15,2) comment '开票金额'")
    private BigDecimal amt;
    
    /*所有订单类型公用*/
    /**
     * 发票状态: 已经申请/开票成功/开票失败
     */
    @Column(name = "status", columnDefinition="varchar(64) comment 'submited/successed/failed'")
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;
    
    /**
     * 开票所包含的订单
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "invoice", cascade = {CascadeType.ALL})  
    @OrderBy(value = "paymentTime desc")
    @JSONField(serialize = false)
    private Set<OrderPayment> orderPayments;
    
    /**
     * 发票定义变动备注
     */
    @ApiModelProperty("发票定义备注")
    @Transient
    private String changeRemark;
    
    /**
     * 操作记录
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "invoice", cascade = {CascadeType.ALL})  
    @OrderBy(value = "createdDate desc")
    @JSONField(serialize = false)
    @ApiModelProperty(hidden = true)
    private List<InvoiceLog> logs;

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Invoice [invoiceId=").append(invoiceId).append("]");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((invoiceId == null) ? 0 : invoiceId.hashCode());
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
        Invoice other = (Invoice) obj;
        if (invoiceId == null)
        {
            if (other.invoiceId != null)
            {
                return false;
            }
        }
        else if (!invoiceId.equals(other.invoiceId))
        {
            return false;
        }
        return true;
    }
    
    
}
