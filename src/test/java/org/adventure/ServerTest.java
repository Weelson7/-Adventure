package org.adventure;

import org.adventure.character.Character;
import org.adventure.character.Race;
import org.adventure.network.*;
import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Server functionality.
 */
public class ServerTest {
    private Server server;
    private static final int TEST_PORT = 8080;

    @BeforeEach
    public void setUp() {
        server = new Server(TEST_PORT);
    }

    @AfterEach
    public void tearDown() {
        if (server != null && server.isRunning()) {
            server.stop();
        }
    }

    @Test
    public void testServerStartStop() {
        assertFalse(server.isRunning());
        
        server.start();
        assertTrue(server.isRunning());
        
        server.stop();
        assertFalse(server.isRunning());
    }

    @Test
    public void testCannotStartServerTwice() {
        server.start();
        assertThrows(IllegalStateException.class, () -> server.start());
    }

    @Test
    public void testPlayerRegistration() {
        server.registerPlayer("testuser", "password123");
        
        // Should not throw exception
        assertDoesNotThrow(() -> {
            server.getAuthManager().registerUser("testuser2", "password456");
        });
    }

    @Test
    public void testPlayerLogin() throws Exception {
        server.registerPlayer("testuser", "password123");
        
        PlayerSession session = server.login("testuser", "password123");
        
        assertNotNull(session);
        assertNotNull(session.getSessionId());
        assertNotNull(session.getJwtToken());
        assertFalse(session.isExpired());
        
        Player player = server.getPlayer(session.getPlayerId());
        assertNotNull(player);
        assertTrue(player.isAuthenticated());
        assertEquals("testuser", player.getUsername());
    }

    @Test
    public void testPlayerLoginFailsWithWrongPassword() {
        server.registerPlayer("testuser", "password123");
        
        assertThrows(AuthenticationManager.AuthenticationException.class, () -> {
            server.login("testuser", "wrongpassword");
        });
    }

    @Test
    public void testPlayerLogout() throws Exception {
        server.registerPlayer("testuser", "password123");
        PlayerSession session = server.login("testuser", "password123");
        
        String playerId = session.getPlayerId();
        Player player = server.getPlayer(playerId);
        assertTrue(player.isAuthenticated(), "Player should be authenticated after login");
        assertNotNull(player.getSession(), "Player should have a session after login");
        
        server.logout(playerId);
        
        // Re-fetch player to ensure we're checking the same object
        Player playerAfterLogout = server.getPlayer(playerId);
        assertFalse(playerAfterLogout.isAuthenticated(), "Player should not be authenticated after logout");
        assertNull(playerAfterLogout.getSession(), "Player session should be null after logout");
    }

    @Test
    public void testActionSubmission() throws Exception {
        server.start();
        server.registerPlayer("testuser", "password123");
        PlayerSession session = server.login("testuser", "password123");
        
        Player player = server.getPlayer(session.getPlayerId());
        
        // Create a character for the player
        Character character = new Character("char_1", "TestChar", Race.HUMAN);
        server.setPlayerCharacter(player.getPlayerId(), character);
        
        PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.CHAT)
                .parameter("message", "Hello, world!")
                .build();
        
        server.submitAction(action);
        
        // Give server time to process
        Thread.sleep(200);
        
        assertTrue(server.getQueuedActionCount() == 0, "Action should have been processed");
    }

    @Test
    public void testActionValidation() throws Exception {
        server.start();
        server.registerPlayer("testuser", "password123");
        PlayerSession session = server.login("testuser", "password123");
        
        Player player = server.getPlayer(session.getPlayerId());
        
        // Submit action without character (should be rejected for non-CHAT actions)
        PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.MOVE)
                .parameter("x", 10)
                .parameter("y", 20)
                .build();
        
        server.submitAction(action);
        Thread.sleep(200);
        
        // Action should be processed but rejected
        assertEquals(PlayerAction.ActionStatus.REJECTED, action.getStatus());
    }

    @Test
    public void testMultiplePlayersCanLogin() throws Exception {
        server.registerPlayer("player1", "password1");
        server.registerPlayer("player2", "password2");
        
        PlayerSession session1 = server.login("player1", "password1");
        PlayerSession session2 = server.login("player2", "password2");
        
        assertNotNull(session1);
        assertNotNull(session2);
        assertNotEquals(session1.getPlayerId(), session2.getPlayerId());
        
        assertEquals(2, server.getActivePlayers().size());
    }

    @Test
    public void testServerPort() {
        assertEquals(TEST_PORT, server.getPort());
    }

    @Test
    public void testLatencyTracking() throws Exception {
        server.start();
        server.registerPlayer("testuser", "password123");
        PlayerSession session = server.login("testuser", "password123");
        
        Player player = server.getPlayer(session.getPlayerId());
        
        // Submit some actions
        for (int i = 0; i < 10; i++) {
            PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.CHAT)
                    .parameter("message", "Test message " + i)
                    .build();
            server.submitAction(action);
        }
        
        // Wait for processing
        Thread.sleep(500);
        
        double avgLatency = server.getAverageLatency();
        double p95Latency = server.get95thPercentileLatency();
        
        assertTrue(avgLatency >= 0, "Average latency should be non-negative");
        assertTrue(p95Latency >= 0, "95th percentile latency should be non-negative");
    }

    @Test
    public void testPerformanceTarget() throws Exception {
        server.start();
        server.registerPlayer("testuser", "password123");
        PlayerSession session = server.login("testuser", "password123");
        
        Player player = server.getPlayer(session.getPlayerId());
        
        // Create character
        Character character = new Character("char_1", "TestChar", Race.HUMAN);
        server.setPlayerCharacter(player.getPlayerId(), character);
        
        // Submit 100 actions
        CountDownLatch latch = new CountDownLatch(100);
        for (int i = 0; i < 100; i++) {
            PlayerAction action = new PlayerAction.Builder(player.getPlayerId(), PlayerAction.ActionType.CHAT)
                    .parameter("message", "Performance test " + i)
                    .build();
            server.submitAction(action);
        }
        
        // Wait for all actions to process (timeout 5 seconds)
        latch.await(5, TimeUnit.SECONDS);
        
        double p95Latency = server.get95thPercentileLatency();
        
        // Quality gate: 95th percentile should be under 50ms
        assertTrue(p95Latency < 50, 
                String.format("95th percentile latency %.2fms exceeds 50ms target", p95Latency));
    }

    @Test
    public void testGetActivePlayers() throws Exception {
        server.registerPlayer("player1", "password1");
        server.registerPlayer("player2", "password2");
        
        assertEquals(0, server.getActivePlayers().size());
        
        server.login("player1", "password1");
        assertEquals(1, server.getActivePlayers().size());
        
        server.login("player2", "password2");
        assertEquals(2, server.getActivePlayers().size());
    }

    @Test
    public void testServerComponents() {
        assertNotNull(server.getAuthManager());
        assertNotNull(server.getActionValidator());
        assertNotNull(server.getConflictResolver());
    }
}
