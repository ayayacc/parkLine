package com.kl.parkLine.entity;

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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.alibaba.fastjson.annotation.JSONField;
import com.kl.parkLine.enums.RoleType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@SuppressWarnings("serial")
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "TC_ROLE")
@org.hibernate.annotations.Table(appliesTo = "tc_role",comment = "角色表")
@EntityListeners({AuditingEntityListener.class})
public class Role extends AbstractEntity implements java.io.Serializable
{
    @Id
    @Column(name="role_id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer roleId;
    
    @Column(name="code", nullable=false, unique=true, length=255, columnDefinition="varchar(255) comment '角色唯一编码'")
    private String code;
    
    @Column(name="name", nullable=false, unique=true, length=255, columnDefinition="varchar(255) comment '角色名称'")
    private String name;
    
    /**
     * 订单类型: 公司/停车场/终端用户
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", columnDefinition="varchar(255) comment '角色名称:company(公司总部)/park(停车场)/endUser(终端用户)'")
    private RoleType type;
    
    @ManyToMany(fetch=FetchType.LAZY, cascade={CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REFRESH}) 
    @JoinTable(name="tr_role_menu", joinColumns={ @JoinColumn(name ="roleId") }, inverseJoinColumns={ @JoinColumn(name="menuId") })  
    @OrderBy("sortIdx asc")
    @JSONField(serialize = false) 
    private Set<Menu> menus;

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Role [roleId=").append(roleId).append("]");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((roleId == null) ? 0 : roleId.hashCode());
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
        Role other = (Role) obj;
        if (roleId == null)
        {
            if (other.roleId != null)
            {
                return false;
            }
        }
        else if (!roleId.equals(other.roleId))
        {
            return false;
        }
        return true;
    }
    
    
}
