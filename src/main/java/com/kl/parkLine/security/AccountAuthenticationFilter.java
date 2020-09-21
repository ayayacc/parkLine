package com.kl.parkLine.security;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class AccountAuthenticationFilter extends UsernamePasswordAuthenticationFilter
{
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) 
                    throws AuthenticationException
    {
        // 从输入流中获取到登录的信息
        String body;
        try
        {
            body = StreamUtils.copyToString(request.getInputStream(), Charset.forName("UTF-8"));
        }
        catch (IOException exception)
        {
            throw new BadCredentialsException(exception.getMessage());
        }
        
        if (StringUtils.isEmpty(body))
        {
            throw new BadCredentialsException("No message body in request");
        }
        
        JSONObject jsonObj = JSON.parseObject(body);
        String username = jsonObj.getString("username");
        if (StringUtils.isEmpty(username))
        {
            throw new BadCredentialsException("No username in request");
        }
        String password = jsonObj.getString("password");
        if (StringUtils.isEmpty(password))
        {
            throw new BadCredentialsException("No username in request");
        }
        
        return this.getAuthenticationManager().authenticate(new UsernamePasswordAuthenticationToken(username, password));
        
    }
}
