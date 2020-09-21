package com.kl.parkLine.component;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

@Component
public class WXMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter 
{
    public WXMappingJackson2HttpMessageConverter() 
    {
        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.TEXT_PLAIN);
        setSupportedMediaTypes(mediaTypes);
    }

}
