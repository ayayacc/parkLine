package com.kl.parkLine.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import com.alibaba.fastjson.JSON;
import com.kl.parkLine.json.RestResult;


public class MyAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler
{
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException 
    {
        response.setContentType("application/json;charset=UTF-8");
        //返回验证结果
        response.getWriter().write(JSON.toJSONString(RestResult.failed(exception.getMessage())));
    }
}
