package com.kl.parkLine.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.alibaba.fastjson.annotation.JSONField;

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
@Entity
@Table(name = "TC_KEY_MAP")
@org.hibernate.annotations.Table(appliesTo = "tc_key_map",comment = "公钥私钥匹配")
@DynamicUpdate
@DynamicInsert
@ApiModel("公钥私钥匹配")
public class KeyMap
{
    @Id
    @Column(name = "key_map_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JSONField(serialize = false)
    @ApiModelProperty(hidden = true)
    private Integer keyMapId;
    
    /**
     * 公钥
     */
    @Column(name = "public_key", length = 32, nullable = false, unique = true, columnDefinition="varchar(32) comment '公钥'")
    @ApiModelProperty("public_key")
    private String publicKey;
    
    /**
     * 私钥
     */
    @Column(name = "private_key", length = 32, nullable = false, unique = true, columnDefinition="varchar(32) comment '私钥'")
    @ApiModelProperty("private_key")
    private String privateKey;

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((keyMapId == null) ? 0 : keyMapId.hashCode());
        result = prime * result
                + ((privateKey == null) ? 0 : privateKey.hashCode());
        result = prime * result
                + ((publicKey == null) ? 0 : publicKey.hashCode());
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
        KeyMap other = (KeyMap) obj;
        if (keyMapId == null)
        {
            if (other.keyMapId != null)
            {
                return false;
            }
        }
        else if (!keyMapId.equals(other.keyMapId))
        {
            return false;
        }
        if (privateKey == null)
        {
            if (other.privateKey != null)
            {
                return false;
            }
        }
        else if (!privateKey.equals(other.privateKey))
        {
            return false;
        }
        if (publicKey == null)
        {
            if (other.publicKey != null)
            {
                return false;
            }
        }
        else if (!publicKey.equals(other.publicKey))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("KeyMap [keyMapId=").append(keyMapId)
                .append(", publicKey=").append(publicKey)
                .append(", privateKey=").append(privateKey).append("]");
        return builder.toString();
    }
    
    
}
