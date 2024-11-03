package com.microservices.elastic.query.service.business.impl;

import com.microservices.elastic.query.service.business.QueryUserService;
import com.microservices.elastic.query.service.dataaccess.entity.UserPermission;
import com.microservices.elastic.query.service.dataaccess.repository.UserPermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwitterQueryUserService implements QueryUserService {

    private final UserPermissionRepository userPermissionRepository;

    @Override
    public Optional<List<UserPermission>> findAllPermissionsByUsername(String username) {
        log.info("Finding permissions by username {}", username);
        return userPermissionRepository.findPermissionsByUsername(username);
    }

}
