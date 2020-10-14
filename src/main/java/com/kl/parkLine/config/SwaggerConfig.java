package com.kl.parkLine.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;

import com.kl.parkLine.swagger.plugin.EnumModelPropertyBuilderPlugin;

import io.swagger.annotations.ApiOperation;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SwaggerConfig
{
    @Value("${swagger.enable}")
    private boolean enableSwagger;
    
    @Bean
    public Docket createRestApi() 
    {
        return new Docket(DocumentationType.OAS_30)
                .enable(enableSwagger)
                .ignoredParameterTypes(Authentication.class)
                .pathMapping("/")
                .select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.any())
                .build().apiInfo(new ApiInfoBuilder()
                        .title("停车线接口文档")
                        .description("停车线服务端接口文档")
                        .version("1.0")
                        .build());
    }
    
    @Bean
    public EnumModelPropertyBuilderPlugin enumModelPropertyBuilderPlugin() {
        return new EnumModelPropertyBuilderPlugin();
    }
}
