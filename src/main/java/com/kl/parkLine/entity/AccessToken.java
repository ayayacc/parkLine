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
import javax.validation.constraints.Size;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

/**
 * 配件
 * @author chenc
 */
@Entity
@Table(name = "TT_REST_TOKEN")
@SuppressWarnings("serial")
@DynamicUpdate
@DynamicInsert
@Getter
@Setter
@EntityListeners({AuditingEntityListener.class})
public class AccessToken extends AbstractEntity implements java.io.Serializable
{
    @Id
    @Column(name = "token_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenId;
    
    @Column(name = "token", nullable = false, unique = true, length = 512)
    @Size(max = 512)
    private String token;// 令牌值
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "VALID_TIME" , nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date validTime; //有效期

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("AccessToken [tokenId=").append(tokenId).append("]");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((tokenId == null) ? 0 : tokenId.hashCode());
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
        AccessToken other = (AccessToken) obj;
        if (tokenId == null)
        {
            if (other.tokenId != null)
            {
                return false;
            }
        }
        else if (!tokenId.equals(other.tokenId))
        {
            return false;
        }
        return true;
    }
}
