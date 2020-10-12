package com.kl.parkLine.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.kl.parkLine.component.EnumConvertFactory;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer
{
    @Autowired
    private EnumConvertFactory enumConvertFactory;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) 
    {
        registry
            .addResourceHandler("/swagger-ui/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
            .resourceChain(false);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) 
    {
        registry.addViewController("/swagger-ui/")
            .setViewName("forward:/swagger-ui/index.html");
    }
    
    @Override
    public void addFormatters(FormatterRegistry registry) 
    {
        registry.addConverterFactory(enumConvertFactory);
    }
}
