package com.kl.parkLine.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
public class User extends AbstractEntity implements UserDetails
{
    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;
    
    /**
     * 微信用户openid
     */
    @Column(name = "wx_open_id", length = 64, unique = true)
    private String wxOpenId;
    
    /**
     * 用户唯一标识
     */
    @Column(name = "name", nullable = false, length = 64, unique = true)
    private String name;
    
    @Column(name = "nick_name", length = 64)
    private String nickName;
    
    @Column(name = "mobile", nullable = false, length = 16)
    private String mobile;
    
    @Column(name = "country", length = 64)
    private String country;
    
    @Column(name = "province", length = 64)
    private String province;
    
    @Column(name = "city", length = 64)
    private String city;
    
    @Column(name = "gender")
    private int gender;
    
    @Column(name = "password", length = 16)
    @JsonIgnore
    private String password;
    
    /**
     * 用户绑定的车辆
     */
    @ManyToMany(fetch = FetchType.LAZY) 
    @JoinTable(name = "tr_user_car", joinColumns = { @JoinColumn(name="user_id") }, inverseJoinColumns={ @JoinColumn(name="car_id") })  
    private Set<Car> cars;
    
    @Column(name = "is_enable")
    private boolean isEnable;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        
        return authorities;
    }

    @Override
    public String getUsername()
    {
        return name;
    }

    @Override
    public boolean isAccountNonExpired()
    {
        return isEnable;
    }

    @Override
    public boolean isAccountNonLocked()
    {
        return isEnable;
    }

    @Override
    public boolean isCredentialsNonExpired()
    {
        return isEnable;
    }

    @Override
    public boolean isEnabled()
    {
        return isEnable;
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
