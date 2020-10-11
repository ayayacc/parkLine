package com.kl.parkLine.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * 短信验证码
 *
 * <p>短信验证码
 * @author chenc 2020年9月11日
 * @see
 * @since 1.0
 */
@Getter
@Setter
@SuppressWarnings("serial")
@Entity
@Table(name = "TT_SMS_CODE")
@DynamicUpdate
@DynamicInsert
@EntityListeners({AuditingEntityListener.class})
@ApiModel("短信验证码")
public class SmsCode extends AbstractEntity implements java.io.Serializable
{
    @Id
    @Column(name = "sms_code_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @ApiModelProperty(hidden = true)
    private Integer smsCodeId;
    
    /**
     * 手机号码
     */
    @Column(name = "mobile", length = 16, nullable = false)
    @ApiModelProperty("mobile")
    private String mobile;
    
    /**
     * 验证码
     */
    @Column(name = "code", length = 8, nullable = false)
    @ApiModelProperty("验证码")
    private String code;
    
    /**
     * 有效期
     */
    @Temporal(TemporalType.TIMESTAMP)       
    @Column(name = "expier_time", nullable = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")     
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("过期时间")
    private Date expierTime;
    
    /**
     * 是否有效
     */
    @Column(name = "enabled", length = 4, nullable = false)
    @ApiModelProperty("是否有效")
    private String enabled;

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("SmsCode [smsCodeId=").append(smsCodeId)
                .append(", mobile=").append(mobile).append(", code=")
                .append(code).append("]");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((smsCodeId == null) ? 0 : smsCodeId.hashCode());
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
        SmsCode other = (SmsCode) obj;
        if (smsCodeId == null)
        {
            if (other.smsCodeId != null)
            {
                return false;
            }
        }
        else if (!smsCodeId.equals(other.smsCodeId))
        {
            return false;
        }
        return true;
    }
}
