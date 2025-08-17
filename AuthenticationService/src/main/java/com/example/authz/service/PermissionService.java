package com.example.authz.service;

import com.example.authz.model.Permission;
import com.example.authz.repository.PermissionRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionService {
    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Cacheable(value = "permissions", key = "{#userId, #action}")
    public List<Permission> findPermissions(String userId, String action) {
        return permissionRepository.findByUserIdAndAction(userId, action);
    }
}