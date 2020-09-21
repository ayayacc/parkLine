package com.kl.parkLine.entity;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
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

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@SuppressWarnings("serial")
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "TC_ROLE")
@EntityListeners({AuditingEntityListener.class})
public class Role extends AbstractEntity implements java.io.Serializable
{
    @Id
    @Column(name="role_id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer roleId;
    
    @Column(name="code", nullable=false, unique=true, length=255)
    private String code;
    
    @Column(name="name", nullable=false, unique=true, length=255)
    private String name;
    
    @ManyToMany(fetch=FetchType.LAZY, cascade={CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REFRESH}) 
    @JoinTable(name="tr_role_menu", joinColumns={ @JoinColumn(name ="roleId") }, inverseJoinColumns={ @JoinColumn(name="menuId") })  
    @OrderBy("sortIdx asc")
    @JsonIgnore 
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
