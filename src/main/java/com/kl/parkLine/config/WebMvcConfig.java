package com.kl.parkLine.config;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer
{
    @Bean
    public FastJsonHttpMessageConverter fastJsonHttpMessageConverter() 
    {
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(SerializerFeature.PrettyFormat);

        //处理中文乱码问题
        List<MediaType> fastMediaTypes = new ArrayList<>();
        fastMediaTypes.add(MediaType.APPLICATION_JSON);
        fastMediaTypes.add(MediaType.TEXT_PLAIN);
        fastConverter.setSupportedMediaTypes(fastMediaTypes);

        fastConverter.setFastJsonConfig(fastJsonConfig);
        return fastConverter;
    }
    
    @Bean
    public StringHttpMessageConverter stringHttpMessageConverter()
    {
        return new StringHttpMessageConverter(Charset.forName("UTF-8"));
    }
    
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
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) 
    {
        converters.add(stringHttpMessageConverter());
        converters.add(fastJsonHttpMessageConverter());
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) 
    {
        converters.clear();
        converters.add(stringHttpMessageConverter());
        converters.add(fastJsonHttpMessageConverter());
    }
}
