package com.kl.parkLine.component;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component(value = "authAuditorAware")
public class AuthAuditorAware implements AuditorAware<String>
{

    @Override
    public Optional<String> getCurrentAuditor()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (null == authentication)
        {
            return Optional.of("admin");
        }
        
        return Optional.of(authentication.getName());
    }
}
