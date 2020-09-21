package com.kl.parkLine.config;

import java.text.ParseException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;

@Configuration
public class JwtKeyConfig
{
    
    private final String RSA_KEY_STRING = "{\"p\":\"0Lwgwtlxg3ElnOQRJHtHObYVrYvoRD8_R3tuIpRJxcY3QBjwFCNJDjvmxhobGzdOuubiWalCtwLrfSPAdSE1vh3o6YpbKqG0W0sB2j8EACUak-KoXHtheTBvB7OhOxRjtGqHxl1RKJywAf2dd5pFdORCRBBrKOxMX4KJQBhZI7U\",\"kty\":\"RSA\",\"q\":\"tdBXwR-_ANpZNYIHBSUqI86lepCOv10PiZx4uKLLwZXS8cQoTMo9agQUj4ETlchotwzOHloSydzg0mUDYPYiBcQf4OWMiAy0vokx8bu0iFHgjxUH7fVNtDXgoIXVl4ApVkQi8R_VLJIvvP_A2vIg4X_zsWAIvD4iLzmjwCDwXHs\",\"d\":\"EfeZT0cBO7PG2O5fc9wQTxwiduibk_rZ3mHEW19gxEUmessoLCLBbXM4fY5t5Lq24vzOh2Vqjzi9EC92jux-ff2sIeDCWl8AbixLUzMH6ol1yUNVJFQ920t-FRSZxX3t8_Dknu94AVmSq23Ppgao0M5cmfyZna7osdutGSrEboE-bjqxoe1_QPErrf2Ek99aepZSbsePNAKfTVLTiqXwa5m-8ux8A-SeWIdkTVyx893h6UFm0rOcGe-H_fBky7V0tXcidA7Khh-J0JYPeb7QgMXpY2qPO0p_M9AaBAeOxcq9MnlLO_BxMv2-CrskG8L08hjALIzBR0V5mq11CLoieQ\",\"e\":\"AQAB\",\"use\":\"sig\",\"kid\":\"vue-app\",\"qi\":\"kzpY-BCKhDFUF01t79G5k-NY-apELu7AUzzvqFHOUFIB8wE3QUW7Ra238uhUfzO4GI9fGL2i1Vivg-OrwsGwoBkZ0zXxIIBvt9YbRTV0f5ZQbxMyVI7C3Cehdpr-uB_CHI_OCZkKLN6t8JFebHEfv9z7afTGnl1rIcYfKnCBEJE\",\"dp\":\"sR-yvSU_CcatoCB323kwy2wToRyjj6YL0GCQ6I7kuwqkP6PptWzrxURXY6srbUDVcgwR7vuOPTYhFiOMYNg5foZq6iIZkmd9YCqZ-9SVVHonv24KZ8_aure6v9JR8owuvx3W8SCmicvMHyRSxvcGEVbQVGmKRUOe2z0tslH28jE\",\"dq\":\"A2jVzHRY854koCFBGnKY-LTuW38MfkAzg2Qgy22aHpY9r3T7QH1ma2-KJyrHgD6S4CPoVZ9gzNJMkQV967qR5sPHyULkwMRkO1J9Ukgv2YKXkHdXf7ni7Cc6gCgiWkX4AHskoJr1N3HCXWv6ZOC3hyMMId5oK4eKwpH1JSw49bM\",\"n\":\"lD7jkcjnXftga_Gz0Q7dARPvubWhMwiJNYWNGxi5gKA0mNI7mh6rJWACmnlb7I2KSarE7ftbhT82JLG8-Y5GZ7hjxLnocb_0DGZH2xOI7gcqVz_cJlVZnZ06c2dzzddO7RvEkCJCGN4glY-eJURzXWg-aGX2Nhkmu4YjalI8snu0v8CLV39jd5El-bZlbN-Jm1FQsQsTlSWzqnx-gXxPryIYOv5_Ct_qJ3qSUaJfq13GbiPJ-e_YRC561XOj8fbttufyNvaTF-6w1CqEQx4xfvSAoy5MHH3hxEU1jj8flpDP7z0YJy7QSDRh1D_T-sGR9Bs3hQuP85GvEPlT1Vkz9w\"}";
    
    @Bean
    public RSAKey rsaKey() throws ParseException {
        return RSAKey.parse(RSA_KEY_STRING);
    }
    
    @Bean
    public JWSSigner jwsSigner(RSAKey rsaJWK) throws JOSEException {
        System.out.println(rsaJWK.toJSONString());
        return new RSASSASigner(rsaJWK);
    }
    
    @Bean
    public JWSVerifier jwsVerifier (RSAKey rsaJWK) throws JOSEException {
        return new RSASSAVerifier(rsaJWK.toPublicJWK());
    }
}
