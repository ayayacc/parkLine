package com.kl.parkLine.security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.alibaba.fastjson.JSONObject;
import com.kl.parkLine.json.WxLoginParam;
public class WxAuthenticationFilter
        extends AbstractAuthenticationProcessingFilter
{
    public WxAuthenticationFilter() 
    {
        super(new AntPathRequestMatcher("/wxlogin", "POST"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
            HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException
    {
        BufferedReader streamReader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
        StringBuilder body = new StringBuilder();
        String inputStr;
        while ((inputStr = streamReader.readLine()) != null)
        {
            body.append(inputStr);
        }
            
        
        WxLoginParam wxLoginParam = JSONObject.parseObject(body.toString(), WxLoginParam.class);
        
        WxAuthenticationToken authRequest = new WxAuthenticationToken(wxLoginParam);
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
        
        return this.getAuthenticationManager().authenticate(authRequest);
    }
}
