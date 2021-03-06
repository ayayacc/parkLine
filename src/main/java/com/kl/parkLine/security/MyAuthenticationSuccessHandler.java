package com.kl.parkLine.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import com.alibaba.fastjson.JSON;
import com.kl.parkLine.component.JwtCmpt;
import com.kl.parkLine.json.JwtToken;
import com.kl.parkLine.json.RestResult;
import com.nimbusds.jose.JOSEException;

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
        try
        {
            //签发token
            JwtToken jwtToken = new JwtToken();
            jwtToken.setToken(jwtCmpt.signJwt(username));
            response.getWriter().write(JSON.toJSONString(RestResult.success(jwtToken)));
        }
        catch (JOSEException e)
        {
            response.getWriter().write(JSON.toJSONString(RestResult.failed(e.getMessage())));
        }
    }
}
