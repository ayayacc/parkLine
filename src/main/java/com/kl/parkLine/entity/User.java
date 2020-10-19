package com.kl.parkLine.entity;

import java.util.ArrayList;
import java.util.Collection;
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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kl.parkLine.enums.Gender;
import com.kl.parkLine.enums.RoleType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@SuppressWarnings("serial")
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "TT_USER")
@EntityListeners({AuditingEntityListener.class})
@ApiModel("用户")
public class User extends AbstractEntity implements UserDetails
{
    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty("用户Id")
    private Integer userId;
    
    /**
     * 微信用户openid
     */
    @Column(name = "wx_open_id", length = 64, unique = true)
    @ApiModelProperty("微信用户openid")
    private String wxOpenId;
    
    /**
     * 用户唯一标识
     */
    @Column(name = "name", nullable = false, length = 64, unique = true)
    @ApiModelProperty("用户唯一标识")
    private String name;
    
    @Column(name = "nick_name", length = 64)
    @ApiModelProperty("用户昵称")
    private String nickName;
    
    @Column(name = "mobile", nullable = false, length = 16)
    @ApiModelProperty("手机号码")
    private String mobile;
    
    @Column(name = "country", length = 64)
    private String country;
    
    @Column(name = "province", length = 64)
    private String province;
    
    @Column(name = "city", length = 64)
    private String city;
    
    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    private Gender gender;
    
    @Column(name = "password", length = 16)
    @JsonIgnore
    @ApiModelProperty(hidden = true)
    private String password;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "tr_user_park", joinColumns = { @JoinColumn(name="user_id") }, inverseJoinColumns={ @JoinColumn(name="park_id") })  
    @ApiModelProperty("所属的停车场")
    private Set<Park> parks;
    
    /**
     * 用户绑定的车辆
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = {CascadeType.MERGE, CascadeType.PERSIST}) 
    private Set<Car> cars;
    
    @Column(name = "is_enabled")
    private boolean isEnabled;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "tr_user_role", joinColumns = { @JoinColumn(name="user_id") }, inverseJoinColumns={ @JoinColumn(name="role_id") })  
    private Set<Role> roles;
    
    public boolean hasRoleType(RoleType type)
    {
        for (Role role : roles)
        {
            if (role.getType().getValue().equals(type.getValue()))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 是否有任一角色
     * @param roleCodes
     * @return
     */
    public boolean hasAnyRole(List<String> roleCodes)
    {
        for (String roleCode : roleCodes)
        {
            if (this.hasRole(roleCode))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 是否有指定
     * @param roleCodes
     * @return
     */
    public boolean hasRole(String roleCode)
    {
        for (Role role : roles)
        {
            if (role.getCode().equalsIgnoreCase(roleCode))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        for (Role role : roles)
        {
            authorities.add(new SimpleGrantedAuthority(role.getCode()));
        }
        
        return authorities;
    }

    @JsonIgnore
    @Override
    public String getUsername()
    {
        return name;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired()
    {
        return isEnabled;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked()
    {
        return isEnabled;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired()
    {
        return isEnabled;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled()
    {
        return isEnabled;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("User [userId=").append(userId).append(", wxOpenId=")
                .append(wxOpenId).append(", name=").append(name).append("]");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
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
        User other = (User) obj;
        if (userId == null)
        {
            if (other.userId != null)
            {
                return false;
            }
        }
        else if (!userId.equals(other.userId))
        {
            return false;
        }
        return true;
    }
}
