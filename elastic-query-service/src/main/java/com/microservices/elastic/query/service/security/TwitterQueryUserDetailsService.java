package com.microservices.elastic.query.service.security;

import com.microservices.elastic.query.service.business.QueryUserService;
import com.microservices.elastic.query.service.transformer.UserPermissionsToUserDetailTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TwitterQueryUserDetailsService implements UserDetailsService {

    private final QueryUserService queryUserService;
    private final UserPermissionsToUserDetailTransformer userPermissionsToUserDetailTransformer;

    /**
     * Loads the user details for a given username by retrieving permissions from
     * the data source and transforming them into a {@link UserDetails} object.
     * <p>
     * If no user is found with the given username, a {@link UsernameNotFoundException}
     * is thrown.
     * </p>
     *
     * @param username The username of the user to be loaded.
     * @return A {@link UserDetails} object containing the user's details and permissions.
     * @throws UsernameNotFoundException if no user is found with the specified username.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return queryUserService
                .findAllPermissionsByUsername(username)
                .map(userPermissionsToUserDetailTransformer::getUserDetails)
                .orElseThrow(
                        () -> new UsernameNotFoundException("No user found with username " + username));
    }

}
