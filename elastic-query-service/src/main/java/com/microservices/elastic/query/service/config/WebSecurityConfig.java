package com.microservices.elastic.query.service.config;

import com.microservices.elastic.query.service.security.TwitterQueryUserDetailsService;
import com.microservices.elastic.query.service.security.TwitterQueryUserJwtConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;

/**
 * Security configuration class for configuring web security settings in a Spring Boot application.
 * <p>
 * This class extends {@link WebSecurityConfigurerAdapter} to customize security settings, including
 * JWT-based authentication, session management policies, CSRF settings, and URL path exclusions.
 * </p>
 *
 * <p><b>Annotations:</b></p>
 * <ul>
 *   <li>{@code @Configuration} - Marks this class as a configuration class for Spring.</li>
 *   <li>{@code @EnableWebSecurity} - Enables Spring Security for the application.</li>
 *   <li>{@code @RequiredArgsConstructor} - Automatically generates a constructor for all
 *       {@code final} fields, supporting dependency injection.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final TwitterQueryUserDetailsService twitterQueryUserDetailsService;
    private final OAuth2ResourceServerProperties oAuth2ResourceServerProperties;

    @Value("${security.paths-to-ignore}")
    private String[] pathsToIgnore;

    /**
     * Configures the HTTP security settings, including session management, CSRF disabling,
     * request authorization, and JWT configuration for OAuth2 resource server.
     *
     * @param http The {@link HttpSecurity} object used to configure web-based security.
     * @throws Exception If there is an error in the configuration.
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .csrf()
                .disable()
                .authorizeRequests()
                .anyRequest()
                .fullyAuthenticated()
                .and()
                .oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(twitterQueryUserJwtConverter());
    }

    /**
     * Configures a {@link JwtDecoder} bean, which decodes and validates incoming JWTs,
     * applying both issuer and audience validation.
     *
     * @param audienceValidator The audience validator for ensuring the JWT's intended audience.
     * @return A configured {@link JwtDecoder} for decoding JWTs with specified validation.
     */
    @Bean
    JwtDecoder jwtDecoder(@Qualifier("elasticQueryServiceAudienceValidator")
                          OAuth2TokenValidator<Jwt> audienceValidator) {
        NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) JwtDecoders.fromOidcIssuerLocation(
                oAuth2ResourceServerProperties.getJwt().getIssuerUri());
        OAuth2TokenValidator<Jwt> withIssuer =
                JwtValidators.createDefaultWithIssuer(
                        oAuth2ResourceServerProperties.getJwt().getIssuerUri());
        OAuth2TokenValidator<Jwt> withAudience =
                new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);
        jwtDecoder.setJwtValidator(withAudience);
        return jwtDecoder;
    }


    /**
     * Configures web security settings to ignore specified paths from security filtering.
     *
     * @param webSecurity The {@link WebSecurity} object used to configure web security.
     */
    @Override
    public void configure(WebSecurity webSecurity) {
        webSecurity
                .ignoring()
                .antMatchers(pathsToIgnore);
    }

    /**
     * Configures a {@link Converter} bean for converting JWT tokens into an authentication token.
     *
     * @return A {@link Converter} instance to map JWTs to {@link AbstractAuthenticationToken}.
     */
    @Bean
    Converter<Jwt, ? extends AbstractAuthenticationToken> twitterQueryUserJwtConverter() {
        return new TwitterQueryUserJwtConverter(twitterQueryUserDetailsService);
    }

}
