package com.example.authz.model;

import lombok.Data;

@Data
public class Permission {
    private Long id;
    private String userId;
    private String action;
    private String resource;
    private String effect;
}