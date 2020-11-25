package com.kl.parkLine.entity;

import java.util.LinkedHashSet;
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
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name="TC_MENU")
@org.hibernate.annotations.Table(appliesTo = "tc_menu",comment = "菜单")
@SuppressWarnings("serial")
@Getter
@Setter
@DynamicUpdate
@DynamicInsert
@EntityListeners({AuditingEntityListener.class})
public class Menu extends AbstractEntity implements java.io.Serializable
{
    @Id
    @Column(name = "menu_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer menuId;

    @Column(name = "name", nullable = false, unique = true, length = 255, columnDefinition="varchar(255) comment '菜单名称'")
    private String name;

    @Column(name = "url", nullable = false, length = 255, columnDefinition="varchar(255) comment '菜单url'")
    private String url;
    
    @Column(name = "sort_idx", nullable = false, columnDefinition="varchar(255) comment '菜单排序'")
    private String sortIdx;
    
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name="parent_menu_id", columnDefinition="int comment '上级菜单Id'")
    private Menu parentMenu; 
    
    @ManyToMany(mappedBy = "menus", fetch = FetchType.EAGER)
    private Set<Role> roles;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parentMenu", cascade = {CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REFRESH})  
    @OrderBy(value="sort_idx asc") 
    private Set<Menu> childMenus; 
    
    @Column(name = "enabled", length = 4, nullable = false, columnDefinition="varchar(4) comment '是否有效Y/N'")
    private String enabled;

    public void addChildMenu(Menu childMenu)
    {
        if (null == this.childMenus)
        {
            this.childMenus = new LinkedHashSet<Menu>();
        }
        this.childMenus.add(childMenu);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((menuId == null) ? 0 : menuId.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        Menu other = (Menu) obj;
        if (menuId == null)
        {
            if (other.menuId != null)
            {
                return false;
            }
        }
        else if (!menuId.equals(other.menuId))
        {
            return false;
        }
        if (name == null)
        {
            if (other.name != null)
            {
                return false;
            }
        }
        else if (!name.equals(other.name))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Menu [menuId=").append(menuId).append(", name=")
                .append(name).append("]");
        return builder.toString();
    }

    
}
