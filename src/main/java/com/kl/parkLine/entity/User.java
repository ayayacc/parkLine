package com.kl.parkLine.entity;

import java.math.BigDecimal;
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

import com.alibaba.fastjson.annotation.JSONField;
import com.kl.parkLine.enums.Gender;
import com.kl.parkLine.enums.RoleType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@SuppressWarnings("serial")
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "TT_USER")
@org.hibernate.annotations.Table(appliesTo = "tt_user",comment = "用户表")
@ApiModel("用户")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({AuditingEntityListener.class})
public class User extends AbstractDateEntity implements UserDetails
{
    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty("用户Id")
    private Integer userId;
    
    /**
     * 微信用户openid
     */
    @Column(name = "wx_open_id", length = 64, unique = true, columnDefinition="varchar(64) comment '微信用户openid'")
    @ApiModelProperty("微信用户openid")
    @JSONField(serialize = false)
    private String wxOpenId;
    
    /**
     * 微信用户登录的session_key
     */
    @Column(name = "wx_session_key", length = 64, unique = true, columnDefinition="varchar(64) comment '微信用户登录的session_key'")
    @ApiModelProperty("微信用户登录的session_key")
    @JSONField(serialize = false)
    private String wxSessionKey;
    
    /**
     * 用户唯一标识
     */
    @Column(name = "name", nullable = false, length = 64, unique = true, columnDefinition="varchar(64) comment '用户唯一标识'")
    @ApiModelProperty("用户唯一标识")
    private String name;
    
    @Column(name = "nick_name", length = 1024, columnDefinition="varchar(1024) comment '用户昵称'")
    @ApiModelProperty("用户昵称")
    private String nickName;
    
    @Column(name = "mobile", length = 16, columnDefinition="varchar(16) comment '手机号码'")
    @ApiModelProperty("手机号码")
    private String mobile;
    
    @Column(name = "country", length = 64, columnDefinition="varchar(64) comment '国家'")
    private String country;
    
    @Column(name = "province", length = 64, columnDefinition="varchar(64) comment '省'")
    private String province;
    
    @Column(name = "city", length = 64, columnDefinition="varchar(64) comment '市'")
    private String city;
    
    @Column(name = "gender", columnDefinition="varchar(255) comment '性别:unkonwn/male/female'")
    @Enumerated(EnumType.STRING)
    private Gender gender;
    
    /**
     * 是否开通了免密支付
     */
    @ApiModelProperty("是否开通无感支付")
    @Column(name = "is_quick_pay", nullable = false, columnDefinition="bool default false comment '是否开通无感支付'")
    private Boolean isQuickPay;
    
    @Column(name = "password", length = 16, columnDefinition="varchar(16) comment '用户密码,MD5散列'")
    @JSONField(serialize = false)
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
    
    @Column(name = "is_enabled", columnDefinition="bool comment '用户是否有效'")
    private boolean isEnabled;
    
    /**
     * 钱包余额(元)
     */
    @Column(name = "balance", precision = 15 ,scale = 2, nullable = false, columnDefinition = "decimal(15,2) default 0 comment '钱包余额(元)'")
    private BigDecimal balance;
    
    /**
     * 是否订阅了公众号
     */
    @Column(name = "subscribe", columnDefinition = "varchar(255) comment '是否订阅了公众号Y/N'")
    private String subscribe;
    
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

    @JSONField(serialize = false)
    @Override
    public String getUsername()
    {
        return name;
    }

    @JSONField(serialize = false)
    @Override
    public boolean isAccountNonExpired()
    {
        return isEnabled;
    }

    @JSONField(serialize = false)
    @Override
    public boolean isAccountNonLocked()
    {
        return isEnabled;
    }

    @JSONField(serialize = false)
    @Override
    public boolean isCredentialsNonExpired()
    {
        return isEnabled;
    }

    @JSONField(serialize = false)
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
        if (!(obj instanceof User))
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
