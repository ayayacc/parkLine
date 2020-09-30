package com.kl.parkLine.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import com.kl.parkLine.util.Const;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

public class MyAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler 
{

    private JWSSigner jwsSigner;
    
    public void setSigner(JWSSigner signer) 
    {
        this.jwsSigner = signer;
    }
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException 
    {
        response.setContentType("application/json;charset=UTF-8");
        
        //获取登录的用户名
        String username = authentication.getName();
        // Prepare JWT with claims set
        DateTime dateTime = new DateTime().plusMinutes(60);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
            .subject("parkLine")
            .issuer("com.kl.parkLine")
            .claim(Const.JWT_CLAIM_USER_NAME, username)
            .expirationTime(dateTime.toDate()) //有效期60分钟
            .build();
        
        
        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
                claimsSet);

        // Compute the RSA signature
        try
        {
            signedJWT.sign(jwsSigner);
        }
        catch (JOSEException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String token = signedJWT.serialize();
        //签发token
        response.getWriter().write(String.format("{\"token\":\"%s\"}", token));
    }

    
}
