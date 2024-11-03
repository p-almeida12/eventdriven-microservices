package com.microservices.elastic.query.service.security;

import com.microservices.elastic.query.service.common.model.ElasticQueryServiceRequestModel;
import com.microservices.elastic.query.service.common.model.ElasticQueryServiceResponseModel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Custom permission evaluator for the query service, responsible for handling authorization
 * logic based on the user's permissions. This class provides methods for evaluating permissions
 * on both domain objects and specific object IDs, allowing finer-grained access control.
 * <p>
 * It extends {@link PermissionEvaluator} and defines custom logic for determining if a user has
 * the required permission on a particular object. Additionally, the evaluator provides an override
 * for superuser access, bypassing standard permission checks.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class QueryServicePermissionEvaluator implements PermissionEvaluator {

    private final HttpServletRequest httpServletRequest;

    /**
     * Evaluates whether the authenticated user has permission to perform an action on
     * a specific domain object. Bypasses standard checks if the user is a superuser.
     *
     * @param authentication The {@link Authentication} object representing the current user.
     * @param targetDomain   The domain object on which permission is being evaluated.
     * @param permission     The required permission for accessing the target domain.
     * @return {@code true} if the user has the required permission, {@code false} otherwise.
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean hasPermission(Authentication authentication,
                                 Object targetDomain,
                                 Object permission) {
        if (isSuperUser()) {
            return true;
        }
        if (targetDomain instanceof ElasticQueryServiceRequestModel) {
            return preAuthorize(authentication, ((ElasticQueryServiceRequestModel) targetDomain).getId(), permission);
        } else if (targetDomain instanceof ResponseEntity || targetDomain == null) {
            if (targetDomain == null) {
                return true;
            }
            List<ElasticQueryServiceResponseModel> responseBody =
                    ((ResponseEntity<List<ElasticQueryServiceResponseModel>>) targetDomain).getBody();
            Objects.requireNonNull(responseBody);
            return postAuthorize(authentication, responseBody, permission);
        }
        return false;
    }

    /**
     * Evaluates whether the authenticated user has permission to perform an action
     * on a specific object identified by its ID.
     *
     * @param authentication The {@link Authentication} object representing the current user.
     * @param targetId       The ID of the object for which permission is being evaluated.
     * @param targetType     The type of the target object.
     * @param permission     The required permission for accessing the target object.
     * @return {@code true} if the user has the required permission, {@code false} otherwise.
     */
    @Override
    public boolean hasPermission(Authentication authentication,
                                 Serializable targetId,
                                 String targetType,
                                 Object permission) {
        if (isSuperUser()) {
            return true;
        }
        if (targetId == null) {
            return false;
        }
        return preAuthorize(authentication, (String) targetId, permission);
    }

    /**
     * Pre-authorization check for a specific domain object. This method verifies
     * if the user has the necessary permission for a given object ID.
     *
     * @param authentication The {@link Authentication} object representing the current user.
     * @param id             The ID of the target object.
     * @param permission     The required permission for accessing the target object.
     * @return {@code true} if the user has the required permission, {@code false} otherwise.
     */
    private boolean preAuthorize(Authentication authentication, String id, Object permission) {
        TwitterQueryUser twitterQueryUser = (TwitterQueryUser) authentication.getPrincipal();
        PermissionType userPermission = twitterQueryUser.getPermissions().get(id);
        return hasPermission((String) permission, userPermission);
    }

    /**
     * Post-authorization check for a list of response models. This method checks each
     * object in the response body to ensure the user has the required permission.
     *
     * @param authentication The {@link Authentication} object representing the current user.
     * @param responseBody   The list of response models for which permissions are being checked.
     * @param permission     The required permission for accessing the target objects.
     * @return {@code true} if the user has the required permission for all objects, {@code false} otherwise.
     */
    private boolean postAuthorize(Authentication authentication,
                                  List<ElasticQueryServiceResponseModel> responseBody,
                                  Object permission) {
        TwitterQueryUser twitterQueryUser = (TwitterQueryUser) authentication.getPrincipal();
        for (ElasticQueryServiceResponseModel responseModel : responseBody) {
            PermissionType userPermission = twitterQueryUser.getPermissions().get(responseModel.getId());
            if (!hasPermission((String) permission, userPermission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the user has the specified permission type by comparing
     * the required permission with the user's permission.
     *
     * @param requiredPermission The required permission as a string.
     * @param userPermission     The user's {@link PermissionType}.
     * @return {@code true} if the user's permission matches the required permission, {@code false} otherwise.
     */
    private boolean hasPermission(String requiredPermission, PermissionType userPermission) {
        return userPermission != null && requiredPermission.equals(userPermission.getType());
    }

    /**
     * Checks if the current user has the superuser role, which grants
     * unrestricted access to all resources.
     *
     * @return {@code true} if the user has the superuser role, {@code false} otherwise.
     */
    private boolean isSuperUser() {
        return httpServletRequest.isUserInRole(Constants.SUPER_USER_ROLE);
    }

}
