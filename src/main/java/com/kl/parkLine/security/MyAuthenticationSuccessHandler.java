package com.kl.parkLine.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import com.kl.parkLine.component.JwtCmpt;

public class MyAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler 
{
    private JwtCmpt jwtCmpt;
    
    public void setJwtCmpt(JwtCmpt jwtCmpt) 
    {
        this.jwtCmpt = jwtCmpt;
    }
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException 
    {
        response.setContentType("application/json;charset=UTF-8");
        
        //获取登录的用户名
        String username = authentication.getName();
        //签发token
        response.getWriter().write(String.format("{\"token\":\"%s\"}", jwtCmpt.signJwt(username)));
    }

    
}
