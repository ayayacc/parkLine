package com.kl.parkLine.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.kl.parkLine.security.AccountAuthenticationFilter;
import com.kl.parkLine.security.JWTAuthorizationFilter;
import com.kl.parkLine.security.MyAuthenticationFailureHandler;
import com.kl.parkLine.security.MyAuthenticationSuccessHandler;
import com.kl.parkLine.security.MyUserDetailsService;
import com.kl.parkLine.security.WxAuthenticationFilter;
import com.kl.parkLine.security.WxAuthenticationProvider;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;

@Configuration
@EnableWebSecurity
public class SecurityConfigAdapter extends WebSecurityConfigurerAdapter
{
    @Autowired
    private MyUserDetailsService myUserDetailsService;
    
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    
    @Autowired 
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JWSSigner jwsSigner;
    
    @Autowired
    private JWSVerifier jwsVerifier;
    
    @Autowired
    private WxAuthenticationProvider wxAuthenticationProvider;
    
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }
    
    @Bean 
    public WxAuthenticationProvider wxAuthenticationProvider(MyUserDetailsService userDetailService) 
    { 
        WxAuthenticationProvider provider = new WxAuthenticationProvider(); 
        provider.setUserDetailsService(userDetailService);
        return provider; 
    }
    
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception
    {
        return super.authenticationManagerBean();
    }
    
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception 
    {
        auth.userDetailsService(myUserDetailsService).passwordEncoder(bCryptPasswordEncoder);
    }
    
    @Override
    public void configure(WebSecurity web) throws Exception 
    {
        web.ignoring()
        .antMatchers("/**/*.jpg")       
        .antMatchers("/**/*.png")
        .antMatchers("/**/*.gif")
        .antMatchers("/**/*.css")
        .antMatchers("/**/*.js")
        .antMatchers("/**/*.js.map")
        .antMatchers("/**/*.eot")
        .antMatchers("/**/*.svg")
        .antMatchers("/**/*.ttf")
        .antMatchers("/**/*.woff")
        .antMatchers("/**/*.woff2")
        .antMatchers("/**/*.otf")
        .antMatchers("/**/*.ico");
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception 
    {
        MyAuthenticationSuccessHandler successHandler = new MyAuthenticationSuccessHandler();  
        successHandler.setSigner(jwsSigner);
        MyAuthenticationFailureHandler failureHandler = new MyAuthenticationFailureHandler();
        
        //用户名密码验证 过滤器
        AccountAuthenticationFilter accountAuthenticationFilter = new AccountAuthenticationFilter(); 
        accountAuthenticationFilter.setAuthenticationManager(authenticationManager);
        accountAuthenticationFilter.setAuthenticationSuccessHandler(successHandler);
        accountAuthenticationFilter.setAuthenticationFailureHandler(failureHandler);
        accountAuthenticationFilter.setContinueChainBeforeSuccessfulAuthentication(false); 
        
        //微信认证 过滤器
        WxAuthenticationFilter wxAuthenticationFilter = new WxAuthenticationFilter(); 
        wxAuthenticationFilter.setAuthenticationSuccessHandler(successHandler); 
        wxAuthenticationFilter.setAuthenticationFailureHandler(failureHandler);
        wxAuthenticationFilter.setAuthenticationManager(authenticationManager); 
        wxAuthenticationFilter.setContinueChainBeforeSuccessfulAuthentication(false); 
        
        //Token验证 JWTAuthorizationFilter
        JWTAuthorizationFilter jwtAuthorizationFilter = new JWTAuthorizationFilter();
        jwtAuthorizationFilter.setJwsVerifier(jwsVerifier);
        
        http
        .headers().frameOptions().sameOrigin()
        .and()
        .authorizeRequests()
        .antMatchers("/MchApi", "/MchApi/**").permitAll()
        .and()          
        .authenticationProvider(wxAuthenticationProvider)
        .addFilterAfter(accountAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(wxAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(jwtAuthorizationFilter, AccountAuthenticationFilter.class)
        .csrf().disable()  // 禁用 Spring Security 自带的跨域处理
         // 调整为让 Spring Security 不创建和使用 session;    
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}
