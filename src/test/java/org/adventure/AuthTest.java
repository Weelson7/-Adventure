package org.adventure;

import org.adventure.network.AuthenticationManager;
import org.adventure.network.PlayerSession;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for authentication and JWT token validation.
 */
public class AuthTest {
    private AuthenticationManager authManager;

    @BeforeEach
    public void setUp() {
        authManager = new AuthenticationManager();
    }

    @Test
    public void testUserRegistration() {
        authManager.registerUser("testuser", "password123");
        assertTrue(authManager.userExists("testuser"));
    }

    @Test
    public void testCannotRegisterDuplicateUsername() {
        authManager.registerUser("testuser", "password123");
        assertThrows(IllegalArgumentException.class, () -> {
            authManager.registerUser("testuser", "password456");
        });
    }

    @Test
    public void testPasswordTooShort() {
        assertThrows(IllegalArgumentException.class, () -> {
            authManager.registerUser("testuser", "short");
        });
    }

    @Test
    public void testEmptyUsername() {
        assertThrows(IllegalArgumentException.class, () -> {
            authManager.registerUser("", "password123");
        });
    }

    @Test
    public void testNullUsername() {
        assertThrows(IllegalArgumentException.class, () -> {
            authManager.registerUser(null, "password123");
        });
    }

    @Test
    public void testNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            authManager.registerUser("testuser", null);
        });
    }

    @Test
    public void testSuccessfulAuthentication() throws Exception {
        authManager.registerUser("testuser", "password123");
        
        PlayerSession session = authManager.authenticate("testuser", "password123");
        
        assertNotNull(session);
        assertNotNull(session.getSessionId());
        assertNotNull(session.getPlayerId());
        assertNotNull(session.getJwtToken());
        assertFalse(session.isExpired());
    }

    @Test
    public void testAuthenticationWithWrongPassword() {
        authManager.registerUser("testuser", "password123");
        
        assertThrows(AuthenticationManager.AuthenticationException.class, () -> {
            authManager.authenticate("testuser", "wrongpassword");
        });
    }

    @Test
    public void testAuthenticationWithNonexistentUser() {
        assertThrows(AuthenticationManager.AuthenticationException.class, () -> {
            authManager.authenticate("nonexistent", "password123");
        });
    }

    @Test
    public void testAuthenticationWithNullCredentials() {
        assertThrows(IllegalArgumentException.class, () -> {
            authManager.authenticate(null, "password");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            authManager.authenticate("username", null);
        });
    }

    @Test
    public void testJWTTokenValidation() throws Exception {
        authManager.registerUser("testuser", "password123");
        PlayerSession session = authManager.authenticate("testuser", "password123");
        
        String token = session.getJwtToken();
        String playerId = authManager.validateToken(token);
        
        assertEquals(session.getPlayerId(), playerId);
    }

    @Test
    public void testInvalidJWTToken() {
        assertThrows(AuthenticationManager.AuthenticationException.class, () -> {
            authManager.validateToken("invalid.token.string");
        });
    }

    @Test
    public void testSessionCreation() throws Exception {
        authManager.registerUser("testuser", "password123");
        PlayerSession session = authManager.authenticate("testuser", "password123");
        
        assertNotNull(session.getCreatedAt());
        assertNotNull(session.getExpiresAt());
        assertTrue(session.getCreatedAt().isBefore(session.getExpiresAt()));
        
        // Session should be valid for at least 23 hours (24 hour expiry - 1 hour buffer)
        long validDurationHours = (session.getExpiresAt().toEpochMilli() - 
                                   session.getCreatedAt().toEpochMilli()) / (1000 * 60 * 60);
        assertTrue(validDurationHours >= 23, 
                "Session should be valid for at least 23 hours, was: " + validDurationHours);
    }

    @Test
    public void testSessionRetrieval() throws Exception {
        authManager.registerUser("testuser", "password123");
        PlayerSession session = authManager.authenticate("testuser", "password123");
        
        PlayerSession retrieved = authManager.getSession(session.getSessionId());
        assertNotNull(retrieved);
        assertEquals(session.getSessionId(), retrieved.getSessionId());
        assertEquals(session.getPlayerId(), retrieved.getPlayerId());
    }

    @Test
    public void testSessionInvalidation() throws Exception {
        authManager.registerUser("testuser", "password123");
        PlayerSession session = authManager.authenticate("testuser", "password123");
        
        assertNotNull(authManager.getSession(session.getSessionId()));
        
        authManager.invalidateSession(session.getSessionId());
        
        assertNull(authManager.getSession(session.getSessionId()));
    }

    @Test
    public void testMultipleSessions() throws Exception {
        authManager.registerUser("user1", "password1");
        authManager.registerUser("user2", "password2");
        
        PlayerSession session1 = authManager.authenticate("user1", "password1");
        PlayerSession session2 = authManager.authenticate("user2", "password2");
        
        assertNotEquals(session1.getSessionId(), session2.getSessionId());
        assertNotEquals(session1.getPlayerId(), session2.getPlayerId());
        
        Map<String, PlayerSession> activeSessions = authManager.getActiveSessions();
        assertEquals(2, activeSessions.size());
    }

    @Test
    public void testSessionExpiry() throws Exception {
        authManager.registerUser("testuser", "password123");
        PlayerSession session = authManager.authenticate("testuser", "password123");
        
        assertFalse(session.isExpired());
        
        // Create a mock expired session (would need to manipulate time in real test)
        // For now, we test that newly created sessions are not expired
        assertTrue(session.getExpiresAt().isAfter(Instant.now()));
    }

    @Test
    public void testCleanupExpiredSessions() throws Exception {
        authManager.registerUser("user1", "password1");
        authManager.registerUser("user2", "password2");
        
        PlayerSession session1 = authManager.authenticate("user1", "password1");
        PlayerSession session2 = authManager.authenticate("user2", "password2");
        
        // No expired sessions yet
        int removed = authManager.cleanupExpiredSessions();
        assertEquals(0, removed);
        
        // Both sessions should still be active
        assertEquals(2, authManager.getActiveSessions().size());
    }

    @Test
    public void testSessionActivityTracking() throws Exception {
        authManager.registerUser("testuser", "password123");
        PlayerSession session = authManager.authenticate("testuser", "password123");
        
        Instant initialActivity = session.getLastActivity();
        assertNotNull(initialActivity);
        
        // Wait a bit
        Thread.sleep(100);
        
        session.updateActivity();
        Instant updatedActivity = session.getLastActivity();
        
        assertTrue(updatedActivity.isAfter(initialActivity));
    }

    @Test
    public void testUserExists() {
        assertFalse(authManager.userExists("nonexistent"));
        
        authManager.registerUser("testuser", "password123");
        assertTrue(authManager.userExists("testuser"));
    }

    @Test
    public void testDifferentUsersGetDifferentTokens() throws Exception {
        authManager.registerUser("user1", "password1");
        authManager.registerUser("user2", "password2");
        
        PlayerSession session1 = authManager.authenticate("user1", "password1");
        PlayerSession session2 = authManager.authenticate("user2", "password2");
        
        assertNotEquals(session1.getJwtToken(), session2.getJwtToken());
    }
}
