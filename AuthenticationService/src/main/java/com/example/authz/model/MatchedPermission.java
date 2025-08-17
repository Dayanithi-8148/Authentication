package com.example.authz.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchedPermission {
    private String action;
    private String resource;
    private String effect;
}