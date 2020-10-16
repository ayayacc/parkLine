package com.kl.parkLine.component;

import java.util.Date;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kl.parkLine.util.Const;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Component
public class JwtCmpt
{
    @Autowired
    private JWSSigner jwsSigner;
    
    @Autowired
    private JWSVerifier jwsVerifier;
    
    /**
     * 签发令牌
     * @param username 用户名
     * @return
     */
    public String signJwt(String username) throws JOSEException
    {
        // Prepare JWT with claims set
        DateTime dateTime = new DateTime().plusMinutes(Const.JWT_EXPIRED_TIME_MIN);
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
        signedJWT.sign(jwsSigner);
        String token = signedJWT.serialize();
        return token;
    }
    
    /**
     * 校验jwt令牌
     * @param tokenHeader 消息头
     * @throws Exception
     */
    public SignedJWT verifyJwt(String tokenHeader) throws Exception
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
        return signedJWT;
    }
}
