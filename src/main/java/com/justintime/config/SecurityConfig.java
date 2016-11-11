package com.justintime.config;

import com.justintime.security.SecUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter{

    @Autowired
    public SecUserDetailsService secUserDetailsService;

    public void configAuthBuilder(AuthenticationManagerBuilder builder) throws Exception{
        builder.userDetailsService(secUserDetailsService);
    }
}
