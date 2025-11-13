# !Adventure — Phase 2.10.x: Operations & Advanced Content

**Version:** 0.1.0-SNAPSHOT (Phase 2.10.x)  
**Last Updated:** November 13, 2025  
**Status:** 🔮 POST-MVP — Operational Tools & Content Expansion  
**Priority:** MEDIUM (Required for Multiplayer Operations & Long-Term Health)

---

## 🎯 Overview & Context

Phase 1.11.x delivered core gameplay systems (progression, combat, economy). BUILD-GAMEPLAY.md covers player-facing UI. Phase 2.10.x focuses on **operational needs** for running a live multiplayer game and **advanced content systems** for long-term engagement.

### What This Phase Provides:
1. **Admin Tools** — Manage players, moderate content, fix issues
2. **Error Handling & Logging** — Debug production issues efficiently
3. **Performance Optimization** — Handle 100+ concurrent players smoothly
4. **World Events System** — Dynamic server-wide content
5. **Testing Infrastructure** — Load testing, stress testing, profiling

### Why These Features Are Phase 2 (Not MVP):
- ✅ Core gameplay works without them (Phase 1.11.x + Gameplay)
- ✅ Can launch with basic admin tools and manual processes
- ✅ Scale these systems as player base grows
- ✅ Requires production data to optimize effectively

---

## 🏗️ Phase 2.10.1: Admin Tools & Moderation

### Goal
Provide game operators with tools to manage players, moderate content, and resolve issues.

### Deliverables

#### 1. **AdminCommandSystem.java**
**Purpose:** In-game admin commands for server operators

**Features:**
- Admin role system (permissions: Moderator, Admin, SuperAdmin)
- Player management (kick, ban, mute, teleport)
- World management (spawn items, structures, NPCs)
- Economy management (adjust gold, grant items)
- Debug commands (check state, force save, reload config)

**Key Classes:**
```java
public class AdminCommandSystem {
    public CommandResult executeCommand(String adminId, AdminCommand command);
    public boolean hasPermission(String adminId, AdminPermission permission);
    public List<AdminLog> getRecentActions(int count);
}

public enum AdminPermission {
    KICK_PLAYER,
    BAN_PLAYER,
    MUTE_PLAYER,
    SPAWN_ITEM,
    SPAWN_NPC,
    ADJUST_GOLD,
    VIEW_PLAYER_DATA,
    EDIT_WORLD,
    FORCE_SAVE,
    SHUTDOWN_SERVER,
    SUPER_ADMIN // Has all permissions
}

public class AdminCommand {
    AdminCommandType type;
    String targetId; // Player, NPC, or structure ID
    Map<String, Object> parameters;
    String reason; // Why this action was taken (for audit log)
}

public enum AdminCommandType {
    // Player moderation
    KICK_PLAYER,
    BAN_PLAYER,
    UNBAN_PLAYER,
    MUTE_PLAYER,
    UNMUTE_PLAYER,
    TELEPORT_PLAYER,
    HEAL_PLAYER,
    
    // Economy management
    GIVE_GOLD,
    TAKE_GOLD,
    GIVE_ITEM,
    GIVE_XP,
    SET_LEVEL,
    
    // World management
    SPAWN_NPC,
    REMOVE_NPC,
    SPAWN_STRUCTURE,
    REMOVE_STRUCTURE,
    TRIGGER_EVENT,
    
    // Debug commands
    VIEW_PLAYER_STATE,
    VIEW_REGION_STATE,
    FORCE_SAVE,
    RELOAD_CONFIG,
    RUN_GC
}

public class AdminLog {
    long timestamp;
    String adminId;
    String adminName;
    AdminCommandType command;
    String targetId;
    Map<String, Object> parameters;
    String reason;
    boolean success;
    String errorMessage; // If failed
}
```

**Example Commands:**
```java
// Kick player with reason
AdminCommand kick = new AdminCommand(
    AdminCommandType.KICK_PLAYER,
    "player_12345",
    Map.of("reason", "Spamming chat"),
    "Spamming chat after warning"
);

// Give legendary sword to player
AdminCommand giveItem = new AdminCommand(
    AdminCommandType.GIVE_ITEM,
    "player_67890",
    Map.of(
        "itemPrototypeId", "legendary_sword",
        "quantity", 1,
        "reason", "Compensation for bug"
    ),
    "Compensation for lost item due to server rollback"
);

// Spawn world boss NPC
AdminCommand spawnBoss = new AdminCommand(
    AdminCommandType.SPAWN_NPC,
    "region_456",
    Map.of(
        "npcType", "dragon_boss",
        "level", 50,
        "x", 128,
        "y", 256
    ),
    "Scheduled world event"
);
```

---

#### 2. **PlayerReportSystem.java**
**Purpose:** Handle player-reported issues (abuse, bugs, exploits)

**Features:**
- Report submission (chat abuse, bug, exploit, harassment)
- Report queue for moderators
- Report priority (auto-flag severe issues)
- Resolution tracking (resolved, dismissed, escalated)

**Key Classes:**
```java
public class PlayerReportSystem {
    public Report submitReport(String reporterId, ReportType type, String targetId, String description);
    public List<Report> getPendingReports(ReportPriority minPriority);
    public void resolveReport(String reportId, String moderatorId, ResolutionAction action);
}

public class Report {
    String id;
    String reporterId;
    String reporterName;
    ReportType type;
    String targetId; // Player, chat message, or bug context
    String description;
    long timestamp;
    ReportPriority priority; // Auto-calculated
    ReportStatus status;
    String assignedModeratorId;
    ResolutionAction resolution;
    String resolutionNote;
    long resolvedTimestamp;
}

public enum ReportType {
    CHAT_ABUSE,
    HARASSMENT,
    CHEATING_EXPLOIT,
    BUG,
    INAPPROPRIATE_NAME,
    GRIEFING,
    OTHER
}

public enum ReportPriority {
    LOW,     // Minor issues, non-urgent
    MEDIUM,  // Standard reports
    HIGH,    // Repeated offender, severe abuse
    URGENT   // Exploit or game-breaking bug
}

public enum ReportStatus {
    PENDING,
    ASSIGNED,
    RESOLVED,
    DISMISSED,
    ESCALATED
}

public enum ResolutionAction {
    NO_ACTION,
    WARNING_ISSUED,
    PLAYER_MUTED,
    PLAYER_KICKED,
    PLAYER_BANNED,
    BUG_FIXED,
    ESCALATED_TO_DEV
}
```

**Auto-Priority Logic:**
```java
public ReportPriority calculatePriority(Report report) {
    // Check for keywords indicating severity
    String description = report.getDescription().toLowerCase();
    if (description.contains("exploit") || description.contains("hack") || description.contains("cheat")) {
        return ReportPriority.URGENT;
    }
    
    // Check reporter's history (trusted reporter?)
    if (getReporterAccuracy(report.getReporterId()) > 0.8) {
        return ReportPriority.HIGH;
    }
    
    // Check target's history (repeat offender?)
    if (getPlayerWarningCount(report.getTargetId()) >= 2) {
        return ReportPriority.HIGH;
    }
    
    return ReportPriority.MEDIUM;
}
```

---

#### 3. **AdminDashboard.java** (Web UI)
**Purpose:** Web-based admin panel for server operators

**Features:**
- Player list (online status, level, location, ban status)
- Server statistics (uptime, TPS, player count, memory usage)
- Recent admin actions log
- Pending reports queue
- Quick actions (kick, ban, message all)
- World state viewer (NPCs, structures, events)

**Tech Stack:**
- Backend: Spring Boot REST API (extends Phase 1.9 server)
- Frontend: React dashboard in `client/admin/` directory
- Authentication: Admin-only JWT tokens

**API Endpoints:**
```java
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {
    @GetMapping("/players")
    public List<PlayerSummary> getOnlinePlayers();
    
    @GetMapping("/players/{id}")
    public PlayerDetails getPlayerDetails(@PathVariable String id);
    
    @PostMapping("/commands")
    public CommandResult executeCommand(@RequestBody AdminCommand command);
    
    @GetMapping("/reports")
    public List<Report> getPendingReports(@RequestParam(required = false) ReportPriority minPriority);
    
    @PostMapping("/reports/{id}/resolve")
    public void resolveReport(@PathVariable String id, @RequestBody ResolutionRequest request);
    
    @GetMapping("/stats")
    public ServerStats getServerStats();
    
    @GetMapping("/logs/recent")
    public List<AdminLog> getRecentLogs(@RequestParam int count);
}
```

---

### Quality Gates (Phase 2.10.1)

**Admin Tools:**
- [ ] All admin commands work and are logged
- [ ] Permissions system prevents unauthorized actions
- [ ] Admin actions don't crash server or corrupt data
- [ ] Audit log captures all admin activity for review

**Moderation:**
- [ ] Player reports submitted successfully
- [ ] Report queue visible to moderators
- [ ] Resolved reports marked and logged
- [ ] No way for players to abuse report system

**Dashboard:**
- [ ] Web dashboard accessible to admins only
- [ ] Player list shows accurate online status
- [ ] Server stats update in real-time (WebSocket)
- [ ] Admin can execute all commands from dashboard

---

## 🏗️ Phase 2.10.2: Error Handling & Logging

### Goal
Comprehensive error handling, logging, and debugging infrastructure.

### Deliverables

#### 1. **ErrorHandlingFramework.java**
**Purpose:** Unified error handling across all systems

**Features:**
- Categorized exceptions (NetworkError, ValidationError, PersistenceError)
- User-friendly error messages (no stack traces to players)
- Automatic error recovery where possible
- Error reporting to developers (crash reports)

**Key Classes:**
```java
public class ErrorHandlingFramework {
    public ErrorResponse handleError(Exception e, ErrorContext context);
    public void logError(Exception e, ErrorSeverity severity, ErrorContext context);
    public void reportCrash(Exception e, GameState state);
}

public enum ErrorSeverity {
    DEBUG,    // Development-only
    INFO,     // Non-error informational message
    WARNING,  // Potential issue, but operation succeeded
    ERROR,    // Operation failed, but game continues
    CRITICAL  // Game-breaking error, server shutdown imminent
}

public class ErrorContext {
    String playerId;
    String characterId;
    String actionAttempted; // "crafting iron_sword", "moving to (10,20)"
    Map<String, Object> gameState; // Relevant state for debugging
    long timestamp;
    String sessionId;
}

public class ErrorResponse {
    boolean showToPlayer; // Should player see this error?
    String playerMessage; // User-friendly message
    String technicalMessage; // For logs/developers
    ErrorSeverity severity;
    boolean canRetry; // Can player retry the action?
    String suggestedAction; // "Try again in a few seconds", "Contact admin"
}
```

**Error Categories:**
```java
// Network errors
public class NetworkException extends GameException {
    public NetworkException(String message, ErrorContext context) {
        super(message, ErrorSeverity.ERROR, context);
    }
}

// Validation errors
public class ValidationException extends GameException {
    public ValidationException(String message, ErrorContext context) {
        super(message, ErrorSeverity.WARNING, context);
    }
}

// Persistence errors
public class PersistenceException extends GameException {
    public PersistenceException(String message, ErrorContext context) {
        super(message, ErrorSeverity.CRITICAL, context);
    }
}

// Exploit detection
public class ExploitDetectedException extends GameException {
    public ExploitDetectedException(String message, ErrorContext context) {
        super(message, ErrorSeverity.CRITICAL, context);
        // Auto-flag for admin review
    }
}
```

**Example Error Handling:**
```java
try {
    craftingSystem.craft(characterId, recipeId);
} catch (InsufficientMaterialsException e) {
    // User error, show friendly message
    return ErrorResponse.builder()
        .showToPlayer(true)
        .playerMessage("You don't have enough materials to craft this item.")
        .severity(ErrorSeverity.WARNING)
        .canRetry(true)
        .suggestedAction("Gather more materials and try again.")
        .build();
} catch (PersistenceException e) {
    // Critical server error
    logError(e, ErrorSeverity.CRITICAL, context);
    return ErrorResponse.builder()
        .showToPlayer(true)
        .playerMessage("A server error occurred. Your progress has been saved. Please try again in a moment.")
        .severity(ErrorSeverity.CRITICAL)
        .canRetry(true)
        .suggestedAction("If this persists, contact support.")
        .build();
}
```

---

#### 2. **StructuredLoggingSystem.java**
**Purpose:** Structured, searchable logs for debugging

**Features:**
- Log levels (DEBUG, INFO, WARN, ERROR, CRITICAL)
- Structured JSON logs (easy to parse/search)
- Correlation IDs (track actions across systems)
- Log rotation and archival (daily rotation, keep 30 days)
- Performance logging (slow operations flagged)

**Key Classes:**
```java
public class StructuredLogger {
    public void log(LogLevel level, String message, LogContext context);
    public void logPerformance(String operation, long durationMs, LogContext context);
    public void logPlayerAction(String playerId, PlayerAction action, LogContext context);
}

public class LogContext {
    String correlationId; // UUID tracking action across systems
    String playerId;
    String characterId;
    String sessionId;
    Map<String, Object> metadata;
    long timestamp;
}

public enum LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR,
    CRITICAL
}
```

**Log Format (JSON):**
```json
{
  "timestamp": "2025-11-13T15:42:33.123Z",
  "level": "ERROR",
  "correlationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "playerId": "player_12345",
  "characterId": "char_67890",
  "sessionId": "session_abc123",
  "message": "Crafting failed due to insufficient materials",
  "operation": "crafting",
  "metadata": {
    "recipeId": "iron_sword",
    "materialsHad": ["iron_ingot:1"],
    "materialsNeeded": ["iron_ingot:3", "wood:1"]
  },
  "stackTrace": null
}
```

**Performance Logging:**
```java
public void logPerformance(String operation, long durationMs, LogContext context) {
    if (durationMs > SLOW_OPERATION_THRESHOLD_MS) {
        log(LogLevel.WARN, 
            String.format("Slow operation: %s took %dms", operation, durationMs),
            context.withMetadata("duration_ms", durationMs));
    }
}
```

---

#### 3. **CrashReportGenerator.java**
**Purpose:** Generate detailed crash reports for developers

**Features:**
- Full game state snapshot at crash time
- Stack trace and exception details
- Player actions leading up to crash (last 10 actions)
- System information (OS, Java version, memory usage)
- Automatic submission to developer endpoint (opt-in)

**Key Classes:**
```java
public class CrashReportGenerator {
    public CrashReport generateReport(Exception e, GameState state);
    public void saveCrashReport(CrashReport report);
    public void submitCrashReport(CrashReport report); // Optional telemetry
}

public class CrashReport {
    String id;
    long timestamp;
    String exceptionType;
    String exceptionMessage;
    String stackTrace;
    GameStateSnapshot gameState;
    List<PlayerAction> recentActions; // Last 10 actions
    SystemInfo systemInfo;
    String playerConsent; // "yes", "no", "ask_later"
}

public class GameStateSnapshot {
    int playerCount;
    long worldTick;
    int activeRegions;
    String playerLocation;
    int playerHealth;
    int playerLevel;
    // Minimal state for privacy
}

public class SystemInfo {
    String osName;
    String osVersion;
    String javaVersion;
    long totalMemory;
    long freeMemory;
    int availableProcessors;
}
```

---

### Quality Gates (Phase 2.10.2)

**Error Handling:**
- [ ] All exceptions caught and handled gracefully
- [ ] User-facing errors always show friendly messages
- [ ] Critical errors trigger automatic save before crash
- [ ] Error recovery works for common failures (network, validation)

**Logging:**
- [ ] All player actions logged with correlation IDs
- [ ] Slow operations (>500ms) flagged in logs
- [ ] Log rotation works (daily, 30-day retention)
- [ ] Logs searchable by player ID, action type, timestamp

**Crash Reports:**
- [ ] Crash reports generated on all uncaught exceptions
- [ ] Reports saved locally before submission
- [ ] Player consent respected (opt-in telemetry)
- [ ] No sensitive data (passwords, emails) in reports

---

## 🏗️ Phase 2.10.3: Performance Optimization

### Goal
Optimize server performance to handle 100+ concurrent players smoothly.

### Deliverables

#### 1. **PerformanceMonitoring.java**
**Purpose:** Real-time performance tracking and alerting

**Features:**
- TPS (ticks per second) monitoring
- Memory usage tracking (heap, GC pauses)
- Slow operation detection (queries, saves)
- Network latency tracking (per-player)
- Performance alerts (TPS drops below 15, memory >90%)

**Key Classes:**
```java
public class PerformanceMonitor {
    public PerformanceMetrics getCurrentMetrics();
    public void recordOperation(String operation, long durationMs);
    public void alertIfSlow(String operation, long durationMs, long threshold);
}

public class PerformanceMetrics {
    double currentTPS; // Target: 20 TPS (50ms per tick)
    long heapUsedMB;
    long heapMaxMB;
    int gcPausesLast10s;
    Map<String, Long> slowOperations; // Operation -> average duration
    int activeConnections;
    int authenticatedPlayers;
}
```

---

#### 2. **OptimizationStrategies.java**
**Purpose:** Implement common performance optimizations

**Features:**
- Region caching (hot regions stay in memory)
- Lazy loading (load regions on-demand)
- Connection pooling (database, network)
- Batch processing (save multiple characters at once)
- Async operations (non-blocking saves)

**Optimization Techniques:**
```java
// 1. Region caching (LRU cache)
public class RegionCache {
    private final int MAX_CACHED_REGIONS = 100;
    private final Map<String, Region> cache = new LinkedHashMap<>(MAX_CACHED_REGIONS, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Region> eldest) {
            return size() > MAX_CACHED_REGIONS;
        }
    };
    
    public Region getRegion(String regionId) {
        return cache.computeIfAbsent(regionId, this::loadFromDisk);
    }
}

// 2. Batch saves (save all dirty characters every 10 ticks)
public class BatchSaveSystem {
    private final Set<Character> dirtyCharacters = new HashSet<>();
    
    public void markDirty(Character character) {
        dirtyCharacters.add(character);
    }
    
    public void flushDirtyCharacters() {
        if (dirtyCharacters.isEmpty()) return;
        
        saveManager.saveBatch(new ArrayList<>(dirtyCharacters));
        dirtyCharacters.clear();
    }
}

// 3. Async saves (non-blocking)
public class AsyncSaveManager {
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    
    public CompletableFuture<Void> saveAsync(Character character) {
        return CompletableFuture.runAsync(() -> {
            saveManager.save(character);
        }, executor);
    }
}
```

---

#### 3. **LoadTestingFramework.java**
**Purpose:** Simulate high load for performance testing

**Features:**
- Bot players (simulate movement, combat, crafting)
- Stress tests (100+ concurrent connections)
- Performance regression tests (detect slowdowns)
- Profiling integration (JFR, VisualVM)

**Key Classes:**
```java
public class LoadTestRunner {
    public LoadTestResult runTest(LoadTestConfig config);
}

public class LoadTestConfig {
    int botCount; // Number of simulated players
    int testDurationMinutes;
    List<BotBehavior> behaviors; // Movement, combat, crafting
    boolean enableProfiling;
}

public class LoadTestResult {
    double avgTPS;
    double minTPS;
    double maxTPS;
    long avgMemoryUsageMB;
    long peakMemoryUsageMB;
    Map<String, Long> operationDurations; // Operation -> avg duration
    int errorsEncountered;
}
```

---

### Quality Gates (Phase 2.10.3)

**Performance:**
- [ ] Server maintains 18+ TPS with 100 concurrent players
- [ ] Memory usage stable (no leaks) over 24-hour test
- [ ] Save operations complete in <100ms (95th percentile)
- [ ] Network latency <50ms for local connections

**Scalability:**
- [ ] Load tests pass with 200 bots (stress test)
- [ ] No performance regression vs baseline (automated tests)
- [ ] GC pauses <100ms (99th percentile)

---

## 🏗️ Phase 2.10.4: World Events System

### Goal
Dynamic server-wide events that create memorable shared experiences.

### Deliverables

#### 1. **WorldEventSystem.java**
**Purpose:** Schedule and manage large-scale world events

**Features:**
- Event types (boss raids, resource invasions, festivals)
- Event scheduling (daily, weekly, special)
- Server-wide announcements
- Participation rewards (XP, items, titles)
- Event leaderboards

**Key Classes:**
```java
public class WorldEventSystem {
    public void scheduleEvent(WorldEvent event, long startTick);
    public void triggerEvent(WorldEvent event);
    public void endEvent(WorldEvent event, EventResult result);
    public List<WorldEvent> getActiveEvents();
}

public class WorldEvent {
    String id;
    String name;
    WorldEventType type;
    long startTick;
    long durationTicks; // How long event lasts
    String regionId; // Where event occurs
    EventObjective objective;
    EventReward rewards;
    List<String> participants; // Player IDs
    EventStatus status;
}

public enum WorldEventType {
    BOSS_RAID,          // Giant dragon spawns, players must defeat it
    RESOURCE_INVASION,  // Ore rush, limited-time resource nodes
    FESTIVAL,           // NPCs offer special trades, XP boost
    SIEGE,              // NPC clan attacks player structures
    MYSTERY,            // Random event, players investigate
    WEATHER_CATACLYSM   // Extreme weather, survival challenge
}

public enum EventStatus {
    SCHEDULED,  // Not started yet
    ACTIVE,     // In progress
    COMPLETED,  // Successfully completed
    FAILED,     // Players failed objective
    CANCELLED   // Admin cancelled event
}

public class EventObjective {
    EventObjectiveType type;
    int targetValue; // Kill 1 boss, gather 1000 ore, survive 5000 ticks
    int currentValue; // Progress tracking
}

public enum EventObjectiveType {
    DEFEAT_ENEMY,
    GATHER_RESOURCES,
    SURVIVE_DURATION,
    DEFEND_STRUCTURE,
    ESCORT_NPC
}

public class EventReward {
    int xpBonus; // +100% XP during event
    List<Item> lootPool; // Random loot for participants
    String title; // "Dragon Slayer" for top contributors
    int goldBonus;
}
```

**Example Events:**
```java
// Weekly boss raid (Sunday 8pm)
WorldEvent dragonRaid = new WorldEvent(
    "dragon_raid_week_46",
    "Ancient Dragon Awakening",
    WorldEventType.BOSS_RAID,
    calculateStartTick("Sunday 20:00"),
    3600 * 1000, // 1 hour duration
    "dragon_lair_region",
    new EventObjective(EventObjectiveType.DEFEAT_ENEMY, 1), // Kill 1 dragon
    new EventReward(
        100, // +100% XP
        List.of(createItem("legendary_sword"), createItem("dragon_scale")),
        "Dragon Slayer",
        10000 // 10k gold
    )
);

// Daily ore rush (random region)
WorldEvent oreRush = new WorldEvent(
    "ore_rush_daily",
    "Gold Vein Discovered!",
    WorldEventType.RESOURCE_INVASION,
    currentTick + 1000,
    600 * 1000, // 10 minutes
    selectRandomRegion(),
    new EventObjective(EventObjectiveType.GATHER_RESOURCES, 1000), // 1000 ore total
    new EventReward(50, List.of(createItem("gold_ingot")), null, 500)
);
```

---

#### 2. **EventAnnouncementSystem.java**
**Purpose:** Notify all players about events

**Features:**
- Server-wide broadcasts (chat, UI popup)
- Event countdowns (starts in 5 minutes)
- Participation tracking (who's at the event)
- Leaderboards (top contributors)

**Key Classes:**
```java
public class EventAnnouncementSystem {
    public void announceEvent(WorldEvent event, AnnouncementType type);
    public void updateLeaderboard(WorldEvent event);
}

public enum AnnouncementType {
    EVENT_SCHEDULED,    // "Dragon raid in 1 hour!"
    EVENT_STARTING,     // "Dragon raid starting NOW!"
    EVENT_PROGRESS,     // "Dragon at 50% health!"
    EVENT_COMPLETED,    // "Dragon defeated! Rewards distributed!"
    EVENT_FAILED        // "Dragon escaped! Better luck next time!"
}
```

---

### Quality Gates (Phase 2.10.4)

**World Events:**
- [ ] Events trigger at scheduled times accurately
- [ ] Announcements visible to all online players
- [ ] Participation tracked correctly (no double-counting)
- [ ] Rewards distributed fairly (no duplication)

**Engagement:**
- [ ] At least 50% of online players participate in events
- [ ] Events don't disrupt normal gameplay (optional participation)
- [ ] Event difficulty scales with player count (boss HP adjusts)

---

## 📊 Testing Strategy

### Performance Tests
```java
@Test
public void testServerPerformance100Players() {
    LoadTestConfig config = new LoadTestConfig();
    config.setBotCount(100);
    config.setTestDurationMinutes(30);
    config.setBehaviors(List.of(MOVEMENT, COMBAT, CRAFTING));
    
    LoadTestResult result = loadTestRunner.runTest(config);
    
    assertTrue(result.getAvgTPS() >= 18.0, "TPS dropped below 18");
    assertTrue(result.getPeakMemoryUsageMB() < 2048, "Memory exceeded 2GB");
}

@Test
public void testNoMemoryLeaks() {
    // Run for 24 hours, check memory stable
    long startMemory = getMemoryUsage();
    runServerForDuration(24 * 60 * 60 * 1000); // 24 hours
    long endMemory = getMemoryUsage();
    
    assertTrue(endMemory < startMemory * 1.5, "Memory leaked >50%");
}
```

### Admin Tool Tests
```java
@Test
public void testAdminKickPlayer() {
    AdminCommand kick = new AdminCommand(KICK_PLAYER, "player_123", Map.of("reason", "Test"));
    CommandResult result = adminCommandSystem.executeCommand("admin_1", kick);
    
    assertTrue(result.isSuccess());
    assertFalse(server.isPlayerConnected("player_123"));
    assertTrue(adminLog.contains("admin_1", KICK_PLAYER, "player_123"));
}
```

### Error Handling Tests
```java
@Test
public void testNetworkErrorRecovery() {
    // Simulate network failure
    simulateNetworkFailure();
    
    // Game should show "Reconnecting..." message
    ErrorResponse response = errorHandler.handle(new NetworkException("Connection lost"));
    
    assertTrue(response.isShowToPlayer());
    assertEquals("Lost connection to server. Reconnecting...", response.getPlayerMessage());
    assertTrue(response.isCanRetry());
}
```

---

## 📁 File Structure

```
src/main/java/org/adventure/
├── admin/
│   ├── AdminCommandSystem.java (NEW)
│   ├── AdminCommand.java (NEW)
│   ├── AdminPermission.java (NEW - enum)
│   ├── AdminLog.java (NEW)
│   ├── PlayerReportSystem.java (NEW)
│   ├── Report.java (NEW)
│   └── AdminDashboardController.java (NEW - REST API)
├── error/
│   ├── ErrorHandlingFramework.java (NEW)
│   ├── ErrorResponse.java (NEW)
│   ├── ErrorContext.java (NEW)
│   ├── GameException.java (NEW - base class)
│   ├── NetworkException.java (NEW)
│   ├── ValidationException.java (NEW)
│   └── PersistenceException.java (NEW)
├── logging/
│   ├── StructuredLogger.java (NEW)
│   ├── LogContext.java (NEW)
│   ├── LogLevel.java (NEW - enum)
│   └── CrashReportGenerator.java (NEW)
├── performance/
│   ├── PerformanceMonitor.java (NEW)
│   ├── PerformanceMetrics.java (NEW)
│   ├── RegionCache.java (NEW)
│   ├── BatchSaveSystem.java (NEW)
│   └── LoadTestRunner.java (NEW)
└── events/
    ├── WorldEventSystem.java (NEW)
    ├── WorldEvent.java (NEW)
    ├── EventObjective.java (NEW)
    ├── EventReward.java (NEW)
    └── EventAnnouncementSystem.java (NEW)

client/admin/ (NEW - Admin Dashboard Web UI)
├── public/
├── src/
│   ├── components/
│   │   ├── PlayerList.jsx
│   │   ├── ServerStats.jsx
│   │   ├── ReportQueue.jsx
│   │   └── AdminActions.jsx
│   ├── services/
│   │   └── adminApi.js
│   └── App.jsx
├── package.json
└── vite.config.js
```

---

## 🚀 Implementation Order

### Week 1: Admin Tools
**Days 1-3:** AdminCommandSystem + permissions  
**Days 4-5:** PlayerReportSystem  
**Days 6-7:** AdminDashboard REST API + basic UI

### Week 2: Error Handling & Logging
**Days 1-3:** ErrorHandlingFramework + exception hierarchy  
**Days 4-5:** StructuredLogger + log rotation  
**Days 6-7:** CrashReportGenerator + telemetry

### Week 3: Performance Optimization
**Days 1-3:** PerformanceMonitor + caching  
**Days 4-5:** Batch saves + async operations  
**Days 6-7:** LoadTestRunner + profiling

### Week 4: World Events
**Days 1-3:** WorldEventSystem + event types  
**Days 4-5:** EventAnnouncementSystem  
**Days 6-7:** Integration tests + first event (boss raid)

---

## 📈 Success Metrics

**Phase 2.10.1 Complete When:**
- [ ] Admins can kick, ban, mute players from dashboard
- [ ] Player reports submitted and visible to moderators
- [ ] All admin actions logged for audit
- [ ] Admin dashboard shows real-time server stats

**Phase 2.10.2 Complete When:**
- [ ] All exceptions handled gracefully (no crashes)
- [ ] Error logs searchable by player, action, timestamp
- [ ] Crash reports generated on all uncaught exceptions
- [ ] Player consent respected for telemetry

**Phase 2.10.3 Complete When:**
- [ ] Server maintains 18+ TPS with 100 concurrent players
- [ ] No memory leaks over 24-hour test
- [ ] Performance regression tests pass (no slowdowns)
- [ ] GC pauses <100ms (99th percentile)

**Phase 2.10.4 Complete When:**
- [ ] At least 3 event types implemented (boss, festival, resource)
- [ ] Events trigger at scheduled times accurately
- [ ] 50%+ of online players participate in events
- [ ] Event rewards distributed fairly

**Overall Phase 2.10.x Complete When:**
- [ ] All 4 sub-phases complete
- [ ] Load tests pass with 200 bots
- [ ] Admin tools used successfully in production
- [ ] Error logs help debug 90%+ of production issues

---

## 🔗 Related Documentation

- **Design Docs:**
  - [Operator Runbook](docs/operator_runbook.md) — Server management
  - [Modding & Security](docs/modding_and_security.md) — Admin permissions

- **Build Guides:**
  - [Main Build Guide](BUILD.md) — Phase 1 complete (backend)
  - [Phase 1.11.x Guide](BUILD_PHASE1.11.x.md) — Core gameplay systems
  - [Gameplay Build Guide](BUILD-GAMEPLAY.md) — UI development
  - [Phase 2 Build Guide](BUILD_PHASE2.md) — Magic, diplomacy (other Phase 2 systems)

---

**END OF BUILD_PHASE2.10.x.md**
