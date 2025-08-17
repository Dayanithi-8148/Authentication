package com.example.authz.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.authz.model.*;
import com.example.authz.util.PathPatternMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthorizationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationService.class);

    private final JwtService jwtService;
    private final PermissionService permissionService;

    public AuthorizationService(JwtService jwtService, PermissionService permissionService) {
        this.jwtService = jwtService;
        this.permissionService = permissionService;
    }

    public AuthorizationResponse authorize(AuthorizationRequest request) {
        try {
            // Validate token and extract user ID
            String userId = jwtService.validateAndExtractUserId(request.getAccess_token());
            logger.debug("Authorizing request for user: {}", userId);

            // Map HTTP method to action
            String action = mapMethodToAction(request.getMethod());
            String resource = normalizePath(request.getPath());

            logger.debug("Action: {}, Resource: {}", action, resource);

            // Retrieve permissions for this user/action
            List<Permission> permissions = permissionService.findPermissions(userId, action);
            logger.debug("Found {} permissions for user: {}", permissions.size(), userId);

            // Find matching permissions
            List<Permission> matchingPermissions = findMatchingPermissions(permissions, resource);

            if (matchingPermissions.isEmpty()) {
                logger.info("DENY: No matching permissions for user {} on {} {}", userId, action, resource);
                return buildDenyResponse(userId, "No matching permissions found");
            }

            // Separate allow and deny permissions
            Map<Boolean, List<Permission>> groupedByEffect = matchingPermissions.stream()
                    .collect(Collectors.groupingBy(p -> "deny".equalsIgnoreCase(p.getEffect())));

            List<Permission> denyPermissions = groupedByEffect.getOrDefault(true, Collections.emptyList());
            List<Permission> allowPermissions = groupedByEffect.getOrDefault(false, Collections.emptyList());

            // Deny takes precedence
            if (!denyPermissions.isEmpty()) {
                Permission mostSpecificDeny = findMostSpecificPermission(denyPermissions, resource);
                logger.info("DENY: Explicit deny for user {} on {} {} (matched: {})",
                        userId, action, resource, mostSpecificDeny.getResource());
                return buildDenyResponse(userId, "Explicit deny on resource: " + mostSpecificDeny.getResource());
            }

            // Find most specific allow permission
            Permission mostSpecificAllow = findMostSpecificPermission(allowPermissions, resource);
            logger.info("ALLOW: User {} granted {} on {} (matched: {})",
                    userId, action, resource, mostSpecificAllow.getResource());
            return buildAllowResponse(userId, mostSpecificAllow);

        } catch (JWTVerificationException e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            return buildDenyResponse(null, "Invalid token: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Authorization error: {}", e.getMessage(), e);
            return buildDenyResponse(null, "Internal server error: " + e.getMessage());
        }
    }

    private List<Permission> findMatchingPermissions(List<Permission> permissions, String resource) {
        List<Permission> matching = new ArrayList<>();
        for (Permission p : permissions) {
            if (PathPatternMatcher.matches(p.getResource(), resource)) {
                matching.add(p);
            }
        }
        return matching;
    }

    private String normalizePath(String path) {
        // Remove leading slash if present
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }

    private Permission findMostSpecificPermission(List<Permission> permissions, String resource) {
        return permissions.stream()
                .max(Comparator.comparingInt(p ->
                        PathPatternMatcher.computeSpecificity(p.getResource())))
                .orElseThrow(() -> new IllegalStateException("Expected at least one permission"));
    }

    private AuthorizationResponse buildAllowResponse(String userId, Permission permission) {
        AuthorizationResponse response = new AuthorizationResponse();
        response.setDecision("ALLOW");
        response.setUser_id(userId);
        response.setReason("User has " + permission.getEffect() + " permission for " + permission.getResource());
        response.setMatched_permissions(Collections.singletonList(
                new MatchedPermission(permission.getAction(), permission.getResource(), permission.getEffect())
        ));
        return response;
    }

    private AuthorizationResponse buildDenyResponse(String userId, String reason) {
        AuthorizationResponse response = new AuthorizationResponse();
        response.setDecision("DENY");
        response.setUser_id(userId != null ? userId : "unknown");
        response.setReason(reason);
        return response;
    }

    private String mapMethodToAction(String method) {
        if (method == null) {
            throw new IllegalArgumentException("HTTP method cannot be null");
        }

        return switch (method.toUpperCase()) {
            case "GET" -> "read";
            case "POST", "PUT", "PATCH" -> "write";
            case "DELETE" -> "delete";
            default -> throw new IllegalArgumentException("Invalid HTTP method: " + method);
        };
    }
}