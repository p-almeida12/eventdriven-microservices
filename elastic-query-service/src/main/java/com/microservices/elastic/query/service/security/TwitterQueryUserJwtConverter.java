package com.microservices.elastic.query.service.security;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;

import static com.microservices.elastic.query.service.security.Constants.*;

@RequiredArgsConstructor
public class TwitterQueryUserJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final TwitterQueryUserDetailsService twitterQueryUserDetailsService;

    /**
     * Converts the provided {@link Jwt} token into an {@link AbstractAuthenticationToken} by extracting
     * the username and granted authorities from the token's claims.
     *
     * @param jwt The {@link Jwt} token containing user information and authorities.
     * @return An {@link AbstractAuthenticationToken} with the user's details and granted authorities.
     * @throws BadCredentialsException if the user cannot be found based on the username claim in the JWT.
     */
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authoritiesFromJwt = getAuthoritiesFromJwt(jwt);
        return Optional.ofNullable(
                twitterQueryUserDetailsService.loadUserByUsername(jwt.getClaimAsString(USERNAME_CLAIM)))
                .map(userDetails -> {
                    ((TwitterQueryUser) userDetails).setAuthorities(authoritiesFromJwt);
                    return new UsernamePasswordAuthenticationToken(userDetails, NA, authoritiesFromJwt);
                })
                .orElseThrow(() -> new BadCredentialsException("User could not be found!"));
    }

    /**
     * Extracts the granted authorities from the provided JWT by combining roles and scopes claims.
     *
     * @param jwt The {@link Jwt} token from which to extract authorities.
     * @return A collection of {@link GrantedAuthority} representing both roles and scopes in the JWT.
     */
    private Collection<GrantedAuthority> getAuthoritiesFromJwt(Jwt jwt) {
        return getCombinedAuthorities(jwt).stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    /**
     * Combines the roles and scopes extracted from the JWT claims into a single collection of authorities.
     *
     * @param jwt The {@link Jwt} token containing roles and scopes claims.
     * @return A collection of strings representing combined roles and scopes as authorities.
     */
    private Collection<String> getCombinedAuthorities(Jwt jwt) {
        Collection<String> authorities = getRoles(jwt);
        authorities.addAll(getScopes(jwt));
        return authorities;
    }

    /**
     * Extracts and formats roles from the JWT claims, prefixing each role with {@code "ROLE_"}.
     * <p>
     * If the {@code roles} claim is absent, returns an empty collection.
     * </p>
     *
     * @param jwt The {@link Jwt} token from which to extract roles.
     * @return A collection of formatted role strings, or an empty collection if no roles are present.
     */
    @SuppressWarnings("unchecked")
    private Collection<String> getRoles(Jwt jwt) {
        Object roles = ((Map<String, Object>) jwt.getClaims().get(REALM_ACCESS_CLAIM)).get(ROLES_CLAIM);
        if (roles instanceof Collection) {
            return ((Collection<String>) roles).stream()
                    .map(authority -> DEFAULT_ROLE_PREFIX + authority.toUpperCase())
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Extracts and formats scopes from the JWT claims, prefixing each scope with {@code "SCOPE_"}.
     * <p>
     * Scopes are expected to be a space-separated string; if absent, returns an empty collection.
     * </p>
     *
     * @param jwt The {@link Jwt} token from which to extract scopes.
     * @return A collection of formatted scope strings, or an empty collection if no scopes are present.
     */
    private Collection<String> getScopes(Jwt jwt) {
        Object scopes = jwt.getClaims().get(SCOPE_CLAIM);
        if (scopes instanceof String) {
            return Arrays.stream(((String) scopes).split(SCOPE_SEPARATOR))
                    .map(authority -> DEFAULT_SCOPE_PREFIX + authority.toUpperCase())
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
