package org.adventure.network;

import org.adventure.character.Character;

import java.util.*;
import java.util.concurrent.*;

/**
 * Authoritative server for multiplayer game.
 * Validates all actions and maintains game state.
 */
public class Server {
    private final AuthenticationManager authManager;
    private final ActionValidator actionValidator;
    private final ConflictResolver conflictResolver;
    private final Map<String, Player> activePlayers; // playerId -> Player
    private final BlockingQueue<PlayerAction> actionQueue;
    private final ExecutorService actionProcessor;
    private volatile boolean running;
    private final int port;
    
    // Performance tracking
    private final Queue<Long> actionProcessingTimes; // Last 100 action processing times
    private static final int STATS_WINDOW_SIZE = 100;

    public Server(int port) {
        this.port = port;
        this.authManager = new AuthenticationManager();
        this.actionValidator = new ActionValidator();
        this.conflictResolver = new ConflictResolver();
        this.activePlayers = new ConcurrentHashMap<>();
        this.actionQueue = new LinkedBlockingQueue<>();
        this.actionProcessor = Executors.newFixedThreadPool(4);
        this.actionProcessingTimes = new ConcurrentLinkedQueue<>();
        this.running = false;
    }

    /**
     * Starts the server.
     */
    public void start() {
        if (running) {
            throw new IllegalStateException("Server already running");
        }
        
        running = true;
        
        // Start action processing thread
        actionProcessor.submit(this::processActions);
        
        System.out.println("Server started on port " + port);
    }

    /**
     * Stops the server.
     */
    public void stop() {
        if (!running) {
            return;
        }
        
        running = false;
        actionProcessor.shutdownNow();
        
        try {
            if (!actionProcessor.awaitTermination(5, TimeUnit.SECONDS)) {
                System.err.println("Action processor did not terminate in time");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Server stopped");
    }

    /**
     * Registers a new player account.
     */
    public void registerPlayer(String username, String password) {
        authManager.registerUser(username, password);
    }

    /**
     * Authenticates a player and creates a session.
     */
    public PlayerSession login(String username, String password) throws AuthenticationManager.AuthenticationException {
        PlayerSession session = authManager.authenticate(username, password);
        
        // Create or update player
        Player player = activePlayers.computeIfAbsent(session.getPlayerId(), 
            id -> new Player(username));
        player.setSession(session);
        player.setAuthenticated(true);
        
        System.out.println("Player logged in: " + username);
        return session;
    }

    /**
     * Logs out a player.
     */
    public void logout(String playerId) {
        Player player = activePlayers.get(playerId);
        if (player != null && player.getSession() != null) {
            authManager.invalidateSession(player.getSession().getSessionId());
            player.setAuthenticated(false);
            player.setSession(null);
            System.out.println("Player logged out: " + player.getUsername());
        }
    }

    /**
     * Submits an action to the server for validation and execution.
     */
    public void submitAction(PlayerAction action) {
        Objects.requireNonNull(action, "action cannot be null");
        
        try {
            actionQueue.put(action);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            action.setStatusFailed("Server shutting down");
        }
    }

    /**
     * Main action processing loop (runs in dedicated thread).
     */
    private void processActions() {
        while (running) {
            try {
                PlayerAction action = actionQueue.poll(100, TimeUnit.MILLISECONDS);
                if (action == null) {
                    continue;
                }
                
                long startTime = System.currentTimeMillis();
                processAction(action);
                long processingTime = System.currentTimeMillis() - startTime;
                
                // Track processing time
                recordProcessingTime(processingTime);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Error processing action: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Processes a single action.
     */
    private void processAction(PlayerAction action) {
        Player player = activePlayers.get(action.getPlayerId());
        if (player == null) {
            action.setStatusRejected("Player not found");
            return;
        }

        // Validate action
        ActionValidator.ValidationResult validation = actionValidator.validate(action, player);
        if (!validation.isValid()) {
            action.setStatusRejected(validation.getErrorMessage());
            return;
        }

        action.setStatus(PlayerAction.ActionStatus.VALIDATED);

        // Check for conflicts
        String resourceId = extractResourceId(action);
        if (resourceId != null) {
            ConflictResolver.ConflictResolution resolution = 
                conflictResolver.acquireLock(resourceId, action);
            
            if (resolution.isConflict()) {
                action.setStatusRejected("Conflict: " + resolution.getMessage());
                return;
            }
            
            if (resolution.isQueued()) {
                // Action queued, will be processed later
                return;
            }
            
            try {
                // Execute action
                executeAction(action, player);
            } finally {
                // Always release lock
                conflictResolver.releaseLock(resourceId);
            }
        } else {
            // No lock needed, execute directly
            executeAction(action, player);
        }
    }

    /**
     * Executes a validated action.
     */
    private void executeAction(PlayerAction action, Player player) {
        try {
            // Here we would dispatch to game logic
            // For MVP, we just mark as executed
            action.setStatus(PlayerAction.ActionStatus.EXECUTED);
            player.updateLastActionTimestamp();
            
            // Log action for debugging
            System.out.println("Executed: " + action);
            
        } catch (Exception e) {
            action.setStatusFailed("Execution failed: " + e.getMessage());
        }
    }

    /**
     * Extracts resource ID from action for conflict detection.
     */
    private String extractResourceId(PlayerAction action) {
        switch (action.getType()) {
            case HARVEST:
                return (String) action.getParameter("resourceNodeId");
            case BUILD:
            case MOVE:
                Object x = action.getParameter("x");
                Object y = action.getParameter("y");
                return x != null && y != null ? "location_" + x + "_" + y : null;
            case TRANSFER_OWNERSHIP:
                return (String) action.getParameter("structureId");
            case CRAFT:
                return "inventory_" + action.getPlayerId();
            case USE_ITEM:
            case DROP_ITEM:
            case PICK_UP_ITEM:
                return (String) action.getParameter("itemId");
            default:
                return null;
        }
    }

    /**
     * Associates a character with a player.
     */
    public void setPlayerCharacter(String playerId, Character character) {
        Player player = activePlayers.get(playerId);
        if (player != null) {
            player.setCharacter(character);
        }
    }

    /**
     * Gets a player by ID.
     */
    public Player getPlayer(String playerId) {
        return activePlayers.get(playerId);
    }

    /**
     * Gets all active players.
     */
    public Collection<Player> getActivePlayers() {
        return new ArrayList<>(activePlayers.values());
    }

    /**
     * Gets the number of queued actions.
     */
    public int getQueuedActionCount() {
        return actionQueue.size();
    }

    /**
     * Records action processing time for performance tracking.
     */
    private void recordProcessingTime(long timeMs) {
        actionProcessingTimes.add(timeMs);
        if (actionProcessingTimes.size() > STATS_WINDOW_SIZE) {
            actionProcessingTimes.poll();
        }
    }

    /**
     * Gets 95th percentile processing time in milliseconds.
     */
    public double get95thPercentileLatency() {
        if (actionProcessingTimes.isEmpty()) {
            return 0.0;
        }
        
        List<Long> times = new ArrayList<>(actionProcessingTimes);
        Collections.sort(times);
        
        int index = (int) Math.ceil(times.size() * 0.95) - 1;
        index = Math.max(0, Math.min(index, times.size() - 1));
        
        return times.get(index);
    }

    /**
     * Gets average processing time in milliseconds.
     */
    public double getAverageLatency() {
        if (actionProcessingTimes.isEmpty()) {
            return 0.0;
        }
        
        long sum = actionProcessingTimes.stream().mapToLong(Long::longValue).sum();
        return (double) sum / actionProcessingTimes.size();
    }

    public boolean isRunning() {
        return running;
    }

    public int getPort() {
        return port;
    }

    public AuthenticationManager getAuthManager() {
        return authManager;
    }

    public ActionValidator getActionValidator() {
        return actionValidator;
    }

    public ConflictResolver getConflictResolver() {
        return conflictResolver;
    }
}
