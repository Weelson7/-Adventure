package org.adventure.network;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Resolves conflicts for concurrent player actions.
 * Uses locks and event ordering to ensure deterministic outcomes.
 */
public class ConflictResolver {
    private final Map<String, ReentrantLock> resourceLocks; // resourceId -> lock
    private final Map<String, List<PlayerAction>> pendingActions; // resourceId -> actions
    private final long lockTimeoutMs;

    public ConflictResolver() {
        this(5000); // 5 second default timeout
    }

    public ConflictResolver(long lockTimeoutMs) {
        this.resourceLocks = new ConcurrentHashMap<>();
        this.pendingActions = new ConcurrentHashMap<>();
        this.lockTimeoutMs = lockTimeoutMs;
    }

    /**
     * Attempts to acquire a lock on a resource for an action.
     * 
     * @param resourceId the ID of the resource to lock
     * @param action the action requiring the lock
     * @return ConflictResolution indicating success or conflict
     */
    public ConflictResolution acquireLock(String resourceId, PlayerAction action) {
        Objects.requireNonNull(resourceId, "resourceId cannot be null");
        Objects.requireNonNull(action, "action cannot be null");

        ReentrantLock lock = resourceLocks.computeIfAbsent(resourceId, k -> new ReentrantLock());
        
        if (lock.tryLock()) {
            return ConflictResolution.success(resourceId);
        } else {
            // Lock is held by another action - queue this action
            queueAction(resourceId, action);
            return ConflictResolution.queued(resourceId, "Resource locked by another action");
        }
    }

    /**
     * Releases a lock on a resource.
     */
    public void releaseLock(String resourceId) {
        ReentrantLock lock = resourceLocks.get(resourceId);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
            
            // Process next queued action if any
            processNextQueuedAction(resourceId);
        }
    }

    /**
     * Resolves concurrent actions on the same resource using timestamp ordering.
     * Earlier actions take precedence.
     */
    public List<PlayerAction> resolveByTimestamp(List<PlayerAction> actions) {
        if (actions == null || actions.size() <= 1) {
            return actions;
        }

        // Sort by timestamp (earlier actions first)
        List<PlayerAction> sorted = new ArrayList<>(actions);
        sorted.sort(Comparator.comparingLong(PlayerAction::getTimestamp));
        
        return sorted;
    }

    /**
     * Detects if two actions conflict with each other.
     */
    public boolean detectConflict(PlayerAction action1, PlayerAction action2) {
        if (action1 == null || action2 == null) {
            return false;
        }

        // Same resource operations conflict
        String resource1 = extractResourceId(action1);
        String resource2 = extractResourceId(action2);
        
        if (resource1 != null && resource1.equals(resource2)) {
            return true;
        }

        // Ownership transfers conflict if targeting same structure
        if (action1.getType() == PlayerAction.ActionType.TRANSFER_OWNERSHIP &&
            action2.getType() == PlayerAction.ActionType.TRANSFER_OWNERSHIP) {
            String struct1 = (String) action1.getParameter("structureId");
            String struct2 = (String) action2.getParameter("structureId");
            return Objects.equals(struct1, struct2);
        }

        // Crafting from same inventory conflicts
        if (action1.getType() == PlayerAction.ActionType.CRAFT &&
            action2.getType() == PlayerAction.ActionType.CRAFT &&
            action1.getPlayerId().equals(action2.getPlayerId())) {
            return true; // Same player crafting concurrently
        }

        return false;
    }

    /**
     * Extracts resource ID from an action if applicable.
     */
    private String extractResourceId(PlayerAction action) {
        switch (action.getType()) {
            case HARVEST:
                return (String) action.getParameter("resourceNodeId");
            case BUILD:
                Object x = action.getParameter("x");
                Object y = action.getParameter("y");
                return x != null && y != null ? "location_" + x + "_" + y : null;
            case TRANSFER_OWNERSHIP:
                return (String) action.getParameter("structureId");
            case USE_ITEM:
            case DROP_ITEM:
            case PICK_UP_ITEM:
                return (String) action.getParameter("itemId");
            default:
                return null;
        }
    }

    /**
     * Queues an action for later processing when lock becomes available.
     */
    private void queueAction(String resourceId, PlayerAction action) {
        pendingActions.computeIfAbsent(resourceId, k -> new ArrayList<>()).add(action);
    }

    /**
     * Processes the next queued action for a resource.
     */
    private void processNextQueuedAction(String resourceId) {
        List<PlayerAction> queue = pendingActions.get(resourceId);
        if (queue != null && !queue.isEmpty()) {
            // Next action will attempt to acquire lock when processed
            // This is handled by the server's action processing loop
        }
    }

    /**
     * Gets all queued actions for a resource.
     */
    public List<PlayerAction> getQueuedActions(String resourceId) {
        List<PlayerAction> queue = pendingActions.get(resourceId);
        return queue != null ? new ArrayList<>(queue) : Collections.emptyList();
    }

    /**
     * Removes a queued action.
     */
    public boolean removeQueuedAction(String resourceId, String actionId) {
        List<PlayerAction> queue = pendingActions.get(resourceId);
        if (queue != null) {
            return queue.removeIf(action -> action.getActionId().equals(actionId));
        }
        return false;
    }

    /**
     * Result of conflict resolution attempt.
     */
    public static class ConflictResolution {
        private final String resourceId;
        private final ResolutionStatus status;
        private final String message;

        public enum ResolutionStatus {
            SUCCESS,    // Lock acquired, can proceed
            QUEUED,     // Action queued, will retry later
            CONFLICT    // Conflict detected, action rejected
        }

        private ConflictResolution(String resourceId, ResolutionStatus status, String message) {
            this.resourceId = resourceId;
            this.status = status;
            this.message = message;
        }

        public static ConflictResolution success(String resourceId) {
            return new ConflictResolution(resourceId, ResolutionStatus.SUCCESS, null);
        }

        public static ConflictResolution queued(String resourceId, String message) {
            return new ConflictResolution(resourceId, ResolutionStatus.QUEUED, message);
        }

        public static ConflictResolution conflict(String resourceId, String message) {
            return new ConflictResolution(resourceId, ResolutionStatus.CONFLICT, message);
        }

        public String getResourceId() {
            return resourceId;
        }

        public ResolutionStatus getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public boolean isSuccess() {
            return status == ResolutionStatus.SUCCESS;
        }

        public boolean isQueued() {
            return status == ResolutionStatus.QUEUED;
        }

        public boolean isConflict() {
            return status == ResolutionStatus.CONFLICT;
        }
    }
}
