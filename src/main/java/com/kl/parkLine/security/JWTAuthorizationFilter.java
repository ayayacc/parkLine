package com.kl.parkLine.security;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.kl.parkLine.util.Const;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jwt.SignedJWT;

public class JWTAuthorizationFilter extends OncePerRequestFilter 
{
    private JWSVerifier jwsVerifier;

    public void setJwsVerifier(JWSVerifier jwsVerifier)
    {
        this.jwsVerifier = jwsVerifier;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException
    {
        String tokenHeader = request.getHeader("Authorization");
        // 如果请求头中没有Authorization信息则直接放行了
        if (tokenHeader == null) {
            chain.doFilter(request, response);
            return;
        }
        
        SignedJWT signedJWT;
        String username;
        try
        {
            signedJWT = SignedJWT.parse(tokenHeader.replace("Bearer ", ""));
            if (false == signedJWT.verify(jwsVerifier))
            {
                throw new Exception("invalid token");
            }
            username = (String) signedJWT.getJWTClaimsSet().getClaim(Const.JWT_CLAIM_USER_NAME);
        }
        catch (Exception e)
        {
            //这里也可以filterChain.doFilter(request,response)然后return,那最后就会调用
            //.exceptionHandling().authenticationEntryPoint,也就是本列中的"需要登陆"
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(e.getMessage());
            return;
        }
        
        // 如果请求头中有token，则进行解析，并且设置认证信息
        JWTAuthenticationToken token = new JWTAuthenticationToken(username,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(token);
        chain.doFilter(request, response);
    }

}
