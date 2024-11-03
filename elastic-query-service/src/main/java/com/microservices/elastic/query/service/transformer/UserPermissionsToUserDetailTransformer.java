package com.microservices.elastic.query.service.transformer;

import com.microservices.elastic.query.service.dataaccess.entity.UserPermission;
import com.microservices.elastic.query.service.security.PermissionType;
import com.microservices.elastic.query.service.security.TwitterQueryUser;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Transformer class responsible for converting a list of {@link UserPermission} objects
 * into a {@link TwitterQueryUser} object with associated permissions.
 * <p>
 * This class is marked as a Spring {@code @Component}, making it eligible for component scanning
 * and dependency injection in other parts of the application. It contains a method that maps
 * document permissions from a list of {@link UserPermission} instances to create a user details
 * object for authentication purposes.
 * </p>
 *
 * <p><b>Annotations:</b></p>
 * <ul>
 *   <li>{@code @Component} - Marks this class as a Spring component, allowing it to be
 *       discovered and managed by the Spring container.</li>
 * </ul>
 *
 */
@Component
public class UserPermissionsToUserDetailTransformer {

    /**
     * Converts a list of {@link UserPermission} objects into a {@link TwitterQueryUser}
     * by mapping each {@link UserPermission} to its respective permission type and document ID.
     * <p>
     * This method assumes that all {@link UserPermission} objects in the list belong to the
     * same user, using the username from the first entry. It builds a map where each key is
     * a document ID and each value is a {@link PermissionType} for that document.
     * </p>
     *
     * @param userPermissions A list of {@link UserPermission} objects containing permissions
     *                        associated with a specific user.
     * @return A {@link TwitterQueryUser} object with the specified username and mapped permissions.
     * @throws IndexOutOfBoundsException if the list of {@code userPermissions} is empty.
     */
    public TwitterQueryUser getUserDetails(List<UserPermission> userPermissions) {
        return TwitterQueryUser.builder()
                .username(userPermissions.get(0).getUsername())
                .permissions(userPermissions.stream()
                        .collect(Collectors.toMap(
                                UserPermission::getDocumentId,
                                permission -> PermissionType.valueOf(permission.getPermissionType()))))
                .build();
    }

}
