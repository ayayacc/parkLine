package com.kl.parkLine.security;

import java.io.IOException;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.alibaba.fastjson.JSON;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.json.RestResult;
import com.kl.parkLine.util.Const;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jwt.SignedJWT;

public class JWTAuthorizationFilter extends OncePerRequestFilter 
{
    private JWSVerifier jwsVerifier;
    
    private MyUserDetailsService userDetailsService;

    public void setJwsVerifier(JWSVerifier jwsVerifier)
    {
        this.jwsVerifier = jwsVerifier;
    }
    
    public void setUserDetailsService(MyUserDetailsService userDetailsService)
    {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException
    {
        String tokenHeader = request.getHeader("Authorization");
        // 如果请求头中没有Authorization信息则直接放行了
        if (tokenHeader == null) 
        {
            chain.doFilter(request, response);
            return;
        }
        
        try
        {
            SignedJWT signedJWT = SignedJWT.parse(tokenHeader.replace("Bearer ", ""));
            if (false == signedJWT.verify(jwsVerifier))
            {
                throw new Exception("无效的登录信息，请登录");
            }
            //超时
            if (signedJWT.getJWTClaimsSet().getExpirationTime().before(new Date()))
            {
                throw new Exception("登录信息已经超时，请重新登录");
            }
            
            //得到用户名
            String username = (String) signedJWT.getJWTClaimsSet().getClaim(Const.JWT_CLAIM_USER_NAME);
            
            // 如果请求头中有token，则进行解析，并且设置认证信息
            User user = userDetailsService.loadUserByUsernameAndRole(username);
            if (null == user)
            {
                throw new Exception(String.format("无效的用户: %s", username));
            }
            
            JWTAuthenticationToken token = new JWTAuthenticationToken(username, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(token);
            chain.doFilter(request, response);
            return;
        }
        catch (Exception e)
        {
            //这里也可以filterChain.doFilter(request,response)然后return,那最后就会调用
            //.exceptionHandling().authenticationEntryPoint,也就是本列中的"需要登陆"
            RestResult restResult = new RestResult();
            restResult.setRetCode(Const.RET_LOGIN_TIME_OUT);
            restResult.setErrMsg(e.getMessage());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSON.toJSONString(restResult));
            return;
        }
    }

}
