package com.hydraz.store.ai;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.hydraz.store.database.DatabaseManager;
import com.hydraz.store.WebsiteCommandExecutor;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

public class AdminAuth {

    private final String secret;
    private final DatabaseManager databaseManager;
    private final Logger logger;

    public AdminAuth(String secret, DatabaseManager databaseManager, Logger logger) {
        this.secret = secret;
        this.databaseManager = databaseManager;
        this.logger = logger;
    }

    public static String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    public boolean authenticate(String username, String password) {
        if (username == null || password == null) return false;

        Map<String, String> creds = databaseManager.getAdminCredentials(username);
        if (creds == null) {
            return false;
        }

        String salt = creds.get("salt");
        String storedHash = creds.get("hash");
        
        String inputHash = WebsiteCommandExecutor.hashPassword(password, salt);
        return inputHash.equals(storedHash);
    }

    public String generateToken(String username) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
                .withIssuer("HydrazWebsite")
                .withClaim("admin", username)
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // 24 hours
                .sign(algorithm);
    }

    public boolean verifyToken(String token) {
        if (token == null || token.isEmpty()) return false;
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("HydrazWebsite")
                    .build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException exception) {
            return false;
        }
    }

    public String getAdminUsername(String token) {
        if (token == null || token.isEmpty()) return null;
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("HydrazWebsite")
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("admin").asString();
        } catch (JWTVerificationException exception) {
            return null;
        }
    }
}
