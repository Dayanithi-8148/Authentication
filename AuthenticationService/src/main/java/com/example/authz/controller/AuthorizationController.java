package com.example.authz.controller;

import com.example.authz.model.AuthorizationRequest;
import com.example.authz.model.AuthorizationResponse;
import com.example.authz.service.AuthorizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthorizationController {
    private final AuthorizationService authorizationService;

    public AuthorizationController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @PostMapping("/authorize")
    public ResponseEntity<AuthorizationResponse> authorize(@RequestBody AuthorizationRequest request) {
        AuthorizationResponse response = authorizationService.authorize(request);
        return ResponseEntity.ok(response);
    }
}