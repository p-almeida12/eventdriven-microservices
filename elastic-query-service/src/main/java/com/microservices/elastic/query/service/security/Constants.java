package com.microservices.elastic.query.service.security;

public class Constants {

    public static final String NA = "N/A";
    public static final String SUPER_USER_ROLE = "APP_SUPER_USER_ROLE";
    public static final String REALM_ACCESS_CLAIM = "realm_access";
    public static final String ROLES_CLAIM = "roles";
    public static final String SCOPE_CLAIM = "scope";
    public static final String USERNAME_CLAIM = "preferred_username";
    public static final String DEFAULT_ROLE_PREFIX = "ROLE_";
    public static final String DEFAULT_SCOPE_PREFIX = "SCOPE_";
    public static final String SCOPE_SEPARATOR = " ";

    private Constants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated!");
    }

}
