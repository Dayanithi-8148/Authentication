package com.example.authz.model;

import lombok.Data;

@Data
public class AuthorizationRequest {
    private String access_token;
    private String method;
    private String path;
}