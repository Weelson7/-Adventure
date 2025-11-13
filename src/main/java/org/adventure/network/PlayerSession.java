package org.adventure.network;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents an active player session with JWT token.
 */
public class PlayerSession {
    private final String sessionId;
    private final String playerId;
    private String jwtToken;
    private Instant createdAt;
    private Instant expiresAt;
    private Instant lastActivity;

    public PlayerSession(String sessionId, String playerId, String jwtToken, Instant expiresAt) {
        this.sessionId = sessionId;
        this.playerId = playerId;
        this.jwtToken = jwtToken;
        this.createdAt = Instant.now();
        this.expiresAt = expiresAt;
        this.lastActivity = Instant.now();
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getLastActivity() {
        return lastActivity;
    }

    public void updateActivity() {
        this.lastActivity = Instant.now();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerSession that = (PlayerSession) o;
        return Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }

    @Override
    public String toString() {
        return String.format("PlayerSession{sessionId='%s', playerId='%s', expiresAt=%s}",
                sessionId, playerId, expiresAt);
    }
}
