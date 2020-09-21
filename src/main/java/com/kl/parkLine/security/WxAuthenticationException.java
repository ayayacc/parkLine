package com.kl.parkLine.security;

import org.springframework.security.core.AuthenticationException;

import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("serial")
@Getter
@Setter
public class WxAuthenticationException extends AuthenticationException
{
    private String openId;
    private String unionId;
    public WxAuthenticationException(String msg)
    {
        super(msg);
    }
}
