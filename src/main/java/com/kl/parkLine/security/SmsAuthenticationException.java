package com.kl.parkLine.security;

import org.springframework.security.core.AuthenticationException;

import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("serial")
@Getter
@Setter
public class SmsAuthenticationException extends AuthenticationException
{
    public SmsAuthenticationException(String msg)
    {
        super(msg);
    }
}
