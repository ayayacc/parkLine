package com.kl.parkLine.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aliyun.oss.OSSClient;

@Configuration
public class AliyunConfig
{
    @Value("${cloud.aliyun.accessKey}")
    private String accessKey;

    @Value("${cloud.aliyun.secretKey}")
    private String secretKey;

    @Value("${cloud.aliyun.endpoint}")
    private String endpoint;

    @Bean
    public OSSClient aliYunOssClient() 
    {
        OSSClient ossClient = new OSSClient(endpoint, accessKey, secretKey);
        return ossClient;
    }
}
