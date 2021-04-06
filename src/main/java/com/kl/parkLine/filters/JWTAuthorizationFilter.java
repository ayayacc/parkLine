package com.kl.parkLine.filters;

import java.io.IOException;
import java.text.ParseException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.alibaba.fastjson.JSON;
import com.kl.parkLine.component.JwtCmpt;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.enums.RetCode;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.RestResult;
import com.kl.parkLine.security.JWTAuthenticationToken;
import com.kl.parkLine.security.MyUserDetailsService;
import com.kl.parkLine.util.Const;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;


public class JWTAuthorizationFilter extends OncePerRequestFilter 
{
    private final static Logger logger = LoggerFactory.getLogger(JWTAuthorizationFilter.class);
    
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
            //logger.info(String.format("NO Authorization header, path: %s", request.getServletPath()));
            chain.doFilter(request, response);
            return;
        }
        
        try
        {
            //验证jwt
            SignedJWT signedJWT = jwtCmpt.verifyJwt(tokenHeader);
            //logger.info(String.format("Token: %s", signedJWT));
            
            //得到用户名
            String username = (String) signedJWT.getJWTClaimsSet().getClaim(Const.JWT_CLAIM_USER_NAME);
            
            // 如果请求头中有token，则进行解析，并且设置认证信息
            User user = userDetailsService.loadUserByName(username);
            if (null == user)
            {
                throw new BusinessException(RetCode.userCanNotFind, String.format("不存在的用户: %s", username));
            }
            if (!user.isEnabled())
            {
                throw new BusinessException(RetCode.userDisabled, String.format("被禁用用户: %s", username));
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
        catch (BusinessException e)
        {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSON.toJSONString(RestResult.failed(e.getRetCode(), e.getMessage())));
            return;
        }
        catch (ParseException | JOSEException e)
        {
            logger.error("invalidToken");
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSON.toJSONString(RestResult.failed(RetCode.invalidToken, e.getMessage())));
            return;
        }
    }
}
