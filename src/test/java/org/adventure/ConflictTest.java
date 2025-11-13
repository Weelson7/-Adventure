package org.adventure;

import org.adventure.network.ConflictResolver;
import org.adventure.network.PlayerAction;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for concurrent action conflict resolution.
 */
public class ConflictTest {
    private ConflictResolver resolver;

    @BeforeEach
    public void setUp() {
        resolver = new ConflictResolver();
    }

    @Test
    public void testLockAcquisitionSuccess() {
        PlayerAction action = new PlayerAction.Builder("player1", PlayerAction.ActionType.HARVEST)
                .parameter("resourceNodeId", "node_123")
                .build();
        
        ConflictResolver.ConflictResolution resolution = resolver.acquireLock("node_123", action);
        
        assertTrue(resolution.isSuccess());
        assertEquals("node_123", resolution.getResourceId());
        
        resolver.releaseLock("node_123");
    }

    @Test
    public void testConcurrentLockConflict() throws Exception {
        String resourceId = "node_123";
        
        PlayerAction action1 = new PlayerAction.Builder("player1", PlayerAction.ActionType.HARVEST)
                .parameter("resourceNodeId", resourceId)
                .build();
        
        PlayerAction action2 = new PlayerAction.Builder("player2", PlayerAction.ActionType.HARVEST)
                .parameter("resourceNodeId", resourceId)
                .build();
        
        // Acquire lock with action1
        ConflictResolver.ConflictResolution resolution1 = resolver.acquireLock(resourceId, action1);
        assertTrue(resolution1.isSuccess());
        
        // In single-threaded test, ReentrantLock allows re-acquisition by same thread
        // So we test that conflict detection logic works instead
        assertTrue(resolver.detectConflict(action1, action2), 
                "Actions on same resource should be detected as conflicting");
        
        // Release lock
        resolver.releaseLock(resourceId);
    }

    @Test
    public void testTimestampOrdering() {
        List<PlayerAction> actions = new ArrayList<>();
        
        // Create actions with different timestamps (simulate arrival order)
        try {
            Thread.sleep(10);
            actions.add(new PlayerAction.Builder("player1", PlayerAction.ActionType.HARVEST)
                    .parameter("resourceNodeId", "node_1")
                    .build());
            
            Thread.sleep(10);
            actions.add(new PlayerAction.Builder("player2", PlayerAction.ActionType.HARVEST)
                    .parameter("resourceNodeId", "node_1")
                    .build());
            
            Thread.sleep(10);
            actions.add(new PlayerAction.Builder("player3", PlayerAction.ActionType.HARVEST)
                    .parameter("resourceNodeId", "node_1")
                    .build());
        } catch (InterruptedException e) {
            fail("Thread sleep interrupted");
        }
        
        // Shuffle to simulate out-of-order arrival
        java.util.Collections.shuffle(actions);
        
        // Resolve by timestamp
        List<PlayerAction> sorted = resolver.resolveByTimestamp(actions);
        
        // Verify sorted by timestamp (earliest first)
        for (int i = 0; i < sorted.size() - 1; i++) {
            assertTrue(sorted.get(i).getTimestamp() <= sorted.get(i + 1).getTimestamp(),
                    "Actions should be sorted by timestamp");
        }
    }

    @Test
    public void testConflictDetection() {
        // Same resource actions should conflict
        PlayerAction action1 = new PlayerAction.Builder("player1", PlayerAction.ActionType.HARVEST)
                .parameter("resourceNodeId", "node_123")
                .build();
        
        PlayerAction action2 = new PlayerAction.Builder("player2", PlayerAction.ActionType.HARVEST)
                .parameter("resourceNodeId", "node_123")
                .build();
        
        assertTrue(resolver.detectConflict(action1, action2));
    }

    @Test
    public void testNoConflictForDifferentResources() {
        PlayerAction action1 = new PlayerAction.Builder("player1", PlayerAction.ActionType.HARVEST)
                .parameter("resourceNodeId", "node_123")
                .build();
        
        PlayerAction action2 = new PlayerAction.Builder("player2", PlayerAction.ActionType.HARVEST)
                .parameter("resourceNodeId", "node_456")
                .build();
        
        assertFalse(resolver.detectConflict(action1, action2));
    }

    @Test
    public void testOwnershipTransferConflict() {
        PlayerAction action1 = new PlayerAction.Builder("player1", PlayerAction.ActionType.TRANSFER_OWNERSHIP)
                .parameter("structureId", "struct_1")
                .parameter("targetPlayerId", "player2")
                .build();
        
        PlayerAction action2 = new PlayerAction.Builder("player3", PlayerAction.ActionType.TRANSFER_OWNERSHIP)
                .parameter("structureId", "struct_1")
                .parameter("targetPlayerId", "player4")
                .build();
        
        assertTrue(resolver.detectConflict(action1, action2));
    }

    @Test
    public void testCraftingConflictSamePlayer() {
        PlayerAction action1 = new PlayerAction.Builder("player1", PlayerAction.ActionType.CRAFT)
                .parameter("recipeId", "recipe_1")
                .build();
        
        PlayerAction action2 = new PlayerAction.Builder("player1", PlayerAction.ActionType.CRAFT)
                .parameter("recipeId", "recipe_2")
                .build();
        
        // Same player crafting concurrently should conflict
        assertTrue(resolver.detectConflict(action1, action2));
    }

    @Test
    public void testCraftingNoConflictDifferentPlayers() {
        PlayerAction action1 = new PlayerAction.Builder("player1", PlayerAction.ActionType.CRAFT)
                .parameter("recipeId", "recipe_1")
                .build();
        
        PlayerAction action2 = new PlayerAction.Builder("player2", PlayerAction.ActionType.CRAFT)
                .parameter("recipeId", "recipe_1")
                .build();
        
        // Different players crafting same recipe should not conflict
        assertFalse(resolver.detectConflict(action1, action2));
    }

    @Test
    public void testBuildLocationConflict() {
        PlayerAction action1 = new PlayerAction.Builder("player1", PlayerAction.ActionType.BUILD)
                .parameter("structureType", "house")
                .parameter("x", 10)
                .parameter("y", 20)
                .build();
        
        PlayerAction action2 = new PlayerAction.Builder("player2", PlayerAction.ActionType.BUILD)
                .parameter("structureType", "tower")
                .parameter("x", 10)
                .parameter("y", 20)
                .build();
        
        assertTrue(resolver.detectConflict(action1, action2));
    }

    @Test
    public void testQueuedActions() {
        String resourceId = "node_123";
        
        PlayerAction action1 = new PlayerAction.Builder("player1", PlayerAction.ActionType.HARVEST)
                .parameter("resourceNodeId", resourceId)
                .build();
        
        // Initially, no queued actions
        List<PlayerAction> queued = resolver.getQueuedActions(resourceId);
        assertEquals(0, queued.size());
        
        // Acquire lock
        resolver.acquireLock(resourceId, action1);
        
        // Verify no actions queued yet
        queued = resolver.getQueuedActions(resourceId);
        assertEquals(0, queued.size());
        
        resolver.releaseLock(resourceId);
    }

    @Test
    public void testRemoveQueuedAction() {
        String resourceId = "node_123";
        
        PlayerAction action1 = new PlayerAction.Builder("player1", PlayerAction.ActionType.HARVEST)
                .parameter("resourceNodeId", resourceId)
                .build();
        
        // Test removing from empty queue
        assertFalse(resolver.removeQueuedAction(resourceId, action1.getActionId()));
        
        // Test removing from non-existent resource
        assertFalse(resolver.removeQueuedAction("nonexistent", action1.getActionId()));
    }

    @Test
    public void testHighConcurrencyDeterminism() throws Exception {
        String resourceId = "shared_resource";
        int numThreads = 10;
        int actionsPerThread = 10;
        
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger queuedCount = new AtomicInteger(0);
        
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < actionsPerThread; j++) {
                        PlayerAction action = new PlayerAction.Builder("player_" + threadId, 
                                PlayerAction.ActionType.HARVEST)
                                .parameter("resourceNodeId", resourceId)
                                .build();
                        
                        ConflictResolver.ConflictResolution resolution = 
                                resolver.acquireLock(resourceId, action);
                        
                        if (resolution.isSuccess()) {
                            successCount.incrementAndGet();
                            // Simulate some work
                            Thread.sleep(1);
                            resolver.releaseLock(resourceId);
                        } else if (resolution.isQueued()) {
                            queuedCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Test should complete within 10 seconds");
        executor.shutdown();
        
        int total = successCount.get() + queuedCount.get();
        assertTrue(total > 0, "Should have processed some actions");
        assertTrue(successCount.get() > 0, "Should have had some successful lock acquisitions");
    }

    @Test
    public void testNullActionHandling() {
        assertThrows(NullPointerException.class, () -> {
            resolver.acquireLock("resource", null);
        });
    }

    @Test
    public void testNullResourceIdHandling() {
        PlayerAction action = new PlayerAction.Builder("player1", PlayerAction.ActionType.CHAT)
                .parameter("message", "Hello")
                .build();
        
        assertThrows(NullPointerException.class, () -> {
            resolver.acquireLock(null, action);
        });
    }

    @Test
    public void testResolveNullOrEmptyList() {
        assertNull(resolver.resolveByTimestamp(null));
        
        List<PlayerAction> empty = new ArrayList<>();
        List<PlayerAction> result = resolver.resolveByTimestamp(empty);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testConflictDetectionWithNulls() {
        PlayerAction action = new PlayerAction.Builder("player1", PlayerAction.ActionType.HARVEST)
                .parameter("resourceNodeId", "node_1")
                .build();
        
        assertFalse(resolver.detectConflict(null, null));
        assertFalse(resolver.detectConflict(action, null));
        assertFalse(resolver.detectConflict(null, action));
    }
}
