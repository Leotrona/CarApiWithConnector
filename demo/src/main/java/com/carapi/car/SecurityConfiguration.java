package com.carapi.car;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String ADVERTISER_ROLE = "Advertiser";
    private static final String SINGLE_CAR_PATH = "/api/v1/cars/*";

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeRequests()
                .antMatchers(PUT, SINGLE_CAR_PATH).hasRole(ADVERTISER_ROLE)
                .antMatchers(DELETE, SINGLE_CAR_PATH).hasRole(ADVERTISER_ROLE);
    }
}