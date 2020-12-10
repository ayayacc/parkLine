package com.kl.parkLine.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aliyun.com.viapi.FileUtils;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;

@Configuration
public class AliyunConfig
{
    @Value("${cloud.aliyun.accessKey}")
    private String accessKey;

    @Value("${cloud.aliyun.secretKey}")
    private String secretKey;

    @Value("${cloud.aliyun.oss.endpoint}")
    private String endpoint;

    @Bean
    public OSS aliYunOssClient() 
    {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKey, secretKey);
        return ossClient;
    }
    
    @Bean(name="ascClientSh")
    public DefaultAcsClient aliYunAcsShClient()
    {
        DefaultProfile profile = DefaultProfile.getProfile("cn-shanghai", accessKey, secretKey);
        DefaultAcsClient acsClient = new DefaultAcsClient(profile);
        return acsClient;
    }
    
    @Bean(name="ascClientHz")
    public DefaultAcsClient aliYunAcsHzClient()
    {
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKey, secretKey);
        DefaultAcsClient acsClient = new DefaultAcsClient(profile);
        return acsClient;
    }
    
    @Bean
    public FileUtils fileUtils() throws ClientException 
    {
        return FileUtils.getInstance(accessKey, secretKey);
    }
}
