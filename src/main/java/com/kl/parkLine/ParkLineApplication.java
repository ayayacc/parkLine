package com.kl.parkLine;

import javax.persistence.EntityManager;

import org.locationtech.jts.io.WKTReader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.github.wxpay.sdk.WXPay;
import com.kl.parkLine.config.MyWXConfig;
import com.querydsl.jpa.impl.JPAQueryFactory;

import springfox.documentation.oas.annotations.EnableOpenApi;

@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
@EnableSpringDataWebSupport
@EnableFeignClients
@EnableHystrix
@EnableJpaAuditing(auditorAwareRef = "authAuditorAware")
@EnableOpenApi
@EnableAsync
@EnableScheduling
public class ParkLineApplication 
{

	public static void main(String[] args) 
	{
		SpringApplication.run(ParkLineApplication.class, args);
	}

	@Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager)
	{
        return new JPAQueryFactory(entityManager);
    }
	
	@Bean
	public WXPay wxPay(MyWXConfig config)
	{
	    return new WXPay(config);
	}
	
	@Bean
	public WKTReader wKTReader()
	{
	    return new WKTReader();
	}
}
