package com.kl.parkLine.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.alibaba.fastjson.JSON;
import com.kl.parkLine.component.JwtCmpt;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.json.RestResult;
import com.kl.parkLine.security.JWTAuthenticationToken;
import com.kl.parkLine.security.MyUserDetailsService;
import com.kl.parkLine.util.Const;
import com.nimbusds.jwt.SignedJWT;

public class JWTAuthorizationFilter extends OncePerRequestFilter 
{

    private JwtCmpt jwtCmpt;

    private MyUserDetailsService userDetailsService;
    
    public void setJwtCmpt(JwtCmpt jwtCmpt) 
    {
        this.jwtCmpt = jwtCmpt;
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
            //验证jwt
            SignedJWT signedJWT = jwtCmpt.verifyJwt(tokenHeader);
            
            //得到用户名
            String username = (String) signedJWT.getJWTClaimsSet().getClaim(Const.JWT_CLAIM_USER_NAME);
            
            // 如果请求头中有token，则进行解析，并且设置认证信息
            User user = userDetailsService.loadUserByName(username);
            if (null == user)
            {
                throw new Exception(String.format("无效的用户: %s", username));
            }
            
            JWTAuthenticationToken token = new JWTAuthenticationToken(username, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(token);
            
            //30分钟内即将超时，签发新的jwt
            DateTime dateTime = new DateTime().plusMinutes(Const.JWT_EXPIRED_TIME_MIN/2);
            if (signedJWT.getJWTClaimsSet().getExpirationTime().before(dateTime.toDate()))
            {
                response.addHeader("New-Token", jwtCmpt.signJwt(username));
            }
            
            chain.doFilter(request, response);
            return;
        }
        catch (Exception e)
        {
            //这里也可以filterChain.doFilter(request,response)然后return,那最后就会调用
            //.exceptionHandling().authenticationEntryPoint,也就是本列中的"需要登陆"
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSON.toJSONString(RestResult.failed(e.getMessage())));
            return;
        }
    }

}
