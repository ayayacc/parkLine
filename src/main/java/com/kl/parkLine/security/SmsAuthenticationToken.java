package com.kl.parkLine.security;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import com.kl.parkLine.json.SmsCheckParam;

import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("serial")
@Getter
@Setter
public class SmsAuthenticationToken extends AbstractAuthenticationToken
{
    private final Object principal;
    private String validCode;

    public SmsAuthenticationToken(SmsCheckParam loginParam) 
    {
        super(null);
        this.principal = loginParam.getMobile();
        this.validCode = loginParam.getValidCode();
        setAuthenticated(false);
    }
    
    public SmsAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) 
    {
        super(authorities);
        this.principal = principal;
        super.setAuthenticated(true); // must use super, as we override
    }
    
    @Override
    public Object getCredentials()
    {
        return "";
    }

    @Override
    public Object getPrincipal()
    {
        return this.principal;
    }

    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException
    {
        if (isAuthenticated) 
        {
            throw new IllegalArgumentException(
                    "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        }

        super.setAuthenticated(false);
    }

    @Override
    public void eraseCredentials() 
    {
        super.eraseCredentials();
    }
    
}
