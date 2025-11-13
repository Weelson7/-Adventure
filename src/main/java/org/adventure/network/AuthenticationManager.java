package org.adventure.network;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages authentication and JWT token generation for players.
 * Provides basic username/password authentication and JWT token validation.
 */
public class AuthenticationManager {
    private static final String SECRET_KEY = "!Adventure-Multiplayer-Secret-Key-Min-256-Bits-For-HS256-JWT-Signing-Change-In-Production!";
    private static final int TOKEN_EXPIRY_HOURS = 24;
    
    private final SecretKey signingKey;
    private final Map<String, String> userPasswords; // username -> hashed password (in production, use bcrypt)
    private final Map<String, PlayerSession> activeSessions; // sessionId -> session

    public AuthenticationManager() {
        this.signingKey = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        this.userPasswords = new ConcurrentHashMap<>();
        this.activeSessions = new ConcurrentHashMap<>();
    }

    /**
     * Registers a new user (for testing/MVP).
     * In production, passwords should be hashed with bcrypt/argon2.
     */
    public void registerUser(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (userPasswords.containsKey(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        // In production: hash with bcrypt
        userPasswords.put(username, password);
    }

    /**
     * Authenticates a user and creates a session with JWT token.
     */
    public PlayerSession authenticate(String username, String password) throws AuthenticationException {
        if (username == null || password == null) {
            throw new IllegalArgumentException("Username and password cannot be null");
        }

        String storedPassword = userPasswords.get(username);
        if (storedPassword == null || !storedPassword.equals(password)) {
            throw new AuthenticationException("Invalid username or password");
        }

        // Create player if not exists
        String playerId = "player_" + username; // Simple ID for MVP
        
        // Generate JWT token
        Instant now = Instant.now();
        Instant expiry = now.plus(TOKEN_EXPIRY_HOURS, ChronoUnit.HOURS);
        
        String token = Jwts.builder()
                .subject(playerId)
                .claim("username", username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();

        // Create session
        String sessionId = "session_" + System.currentTimeMillis() + "_" + playerId;
        PlayerSession session = new PlayerSession(sessionId, playerId, token, expiry);
        activeSessions.put(sessionId, session);

        return session;
    }

    /**
     * Validates a JWT token and returns the player ID.
     */
    public String validateToken(String token) throws AuthenticationException {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String playerId = claims.getSubject();
            
            // Check expiration
            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                throw new AuthenticationException("Token expired");
            }

            return playerId;
        } catch (Exception e) {
            throw new AuthenticationException("Invalid token: " + e.getMessage());
        }
    }

    /**
     * Gets a session by session ID.
     */
    public PlayerSession getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }

    /**
     * Invalidates a session (logout).
     */
    public void invalidateSession(String sessionId) {
        activeSessions.remove(sessionId);
    }

    /**
     * Gets all active sessions.
     */
    public Map<String, PlayerSession> getActiveSessions() {
        return new HashMap<>(activeSessions);
    }

    /**
     * Cleans up expired sessions.
     */
    public int cleanupExpiredSessions() {
        Instant now = Instant.now();
        
        int initialSize = activeSessions.size();
        activeSessions.entrySet().removeIf(entry -> 
            entry.getValue().getExpiresAt().isBefore(now)
        );
        
        return initialSize - activeSessions.size();
    }

    /**
     * Checks if a user exists.
     */
    public boolean userExists(String username) {
        return userPasswords.containsKey(username);
    }

    /**
     * Exception thrown when authentication fails.
     */
    public static class AuthenticationException extends Exception {
        public AuthenticationException(String message) {
            super(message);
        }
    }
}
