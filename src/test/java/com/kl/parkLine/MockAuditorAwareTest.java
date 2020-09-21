package com.kl.parkLine;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
@Component(value = "mockAuditorAware")
public class MockAuditorAwareTest implements AuditorAware<String>
{
    private String currentAuditor;
    
    public Optional<String> getCurrentAuditor()
    {
        return Optional.of(currentAuditor);
    }

    public void setCurrentAuditor(String currentAuditor)
    {
        this.currentAuditor = currentAuditor;
    }

}
