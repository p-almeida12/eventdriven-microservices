package com.microservices.reactive.elastic.query.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class WebSecurityConfig {

    @Bean
    public SecurityWebFilterChain webFluxSecurityConfig(ServerHttpSecurity httpSecurity) {
        //allow all requests
        httpSecurity.authorizeExchange()
                .anyExchange()
                .permitAll();

        //disable CSRF
        httpSecurity.csrf().disable();
        return httpSecurity.build();
    }

}
