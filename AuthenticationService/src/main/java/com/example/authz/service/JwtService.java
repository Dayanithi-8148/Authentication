package com.example.authz.service;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.authz.config.JwtConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.TimeUnit;

@Service
public class JwtService {
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private final JwtConfig jwtConfig;
    private final JwkProvider jwkProvider;

    public JwtService(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;

        String issuerUrl = jwtConfig.getIssuer();
        if (!issuerUrl.endsWith("/")) {
            issuerUrl += "/";
        }

        try {
            this.jwkProvider = new UrlJwkProvider(
                    new URL(issuerUrl + ".well-known/jwks.json"),
                    (int) TimeUnit.SECONDS.toMillis(10),
                    (int) TimeUnit.SECONDS.toMillis(10)
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to create JWK provider: " + e.getMessage(), e);
        }
    }

    public String validateAndExtractUserId(String token) throws JWTVerificationException {
        if (token == null || token.isBlank()) {
            throw new JWTVerificationException("Token is null or empty");
        }

        try {
            DecodedJWT jwt = JWT.decode(token);

            String keyId = jwt.getKeyId();
            if (keyId == null) {
                throw new JWTVerificationException("Token does not contain key ID (kid)");
            }

            Jwk jwk = jwkProvider.get(keyId);
            RSAPublicKey publicKey = (RSAPublicKey) jwk.getPublicKey();

            Algorithm algorithm = Algorithm.RSA256(publicKey, null);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(jwtConfig.getIssuer())
                    .withAudience(jwtConfig.getAudience())
                    .build();

            jwt = verifier.verify(token);

            String userId = jwt.getClaim(jwtConfig.getUserIdClaim()).asString();
            if (userId == null || userId.isBlank()) {
                throw new JWTVerificationException("User ID claim is missing or empty");
            }

            logger.debug("Validated token for user: {}", userId);
            return userId;

        } catch (JwkException e) {
            logger.error("JWK error: {}", e.getMessage());
            throw new JWTVerificationException("Key retrieval failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Token validation error: {}", e.getMessage());
            throw new JWTVerificationException("Token validation failed: " + e.getMessage(), e);
        }
    }
}