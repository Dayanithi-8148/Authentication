package com.example.authz.model;

import lombok.Data;

import java.util.List;

@Data
public class AuthorizationResponse {
    private String decision;
    private String user_id;
    private String reason;
    private List<MatchedPermission> matched_permissions;
}