# ServerTest

**Package:** `org.adventure`  
**Phase:** 1.9 (Multiplayer & Networking)  
**Related Classes:** `Server`, `Player`, `PlayerAction`, `ActionValidator`  
**Test Count:** 14 tests

## Purpose

Integration test suite for the authoritative game server. Validates server lifecycle, player management, action processing pipeline, performance metrics, and security enforcement.

## Test Coverage

### Server Lifecycle Tests (3 tests)

#### `testServerStartStop()`
**Validates:** Server can start and stop cleanly

**Steps:**
1. Create server on port 8080
2. Verify `isRunning() == false` initially
3. Call `start()`
4. Verify `isRunning() == true`
5. Call `stop()`
6. Verify `isRunning() == false`

**Assertions:**
- Server state transitions correctly
- No exceptions thrown

#### `testServerCannotStartTwice()`
**Validates:** Starting already-running server throws exception

**Steps:**
1. Start server
2. Attempt to start again
3. Expect `IllegalStateException`

**Assertions:**
- Second `start()` throws "Server already running"

#### `testServerShutdownGracefully()`
**Validates:** Server waits for in-flight actions before stopping

**Steps:**
1. Start server
2. Submit long-running action (mock 500ms execution)
3. Call `stop()` immediately
4. Verify action completes before shutdown
5. Verify shutdown completes within 5 seconds (grace period)

**Assertions:**
- Action processed despite shutdown signal
- Shutdown duration < 5 seconds

**MVP Status:** Simplified test (no actual long-running actions yet)

### Player Management Tests (4 tests)

#### `testPlayerRegistration()`
**Validates:** Server can register new players

**Steps:**
1. Call `server.registerPlayer("alice", "password123")`
2. Verify playerId returned (UUID format)
3. Verify player stored in `activePlayers`

**Assertions:**
- `playerId != null`
- `server.getPlayer(playerId) != null`

#### `testPlayerLogin()`
**Validates:** Player can login and receive session

**Steps:**
1. Register player "bob"
2. Call `server.login("bob", "password123")`
3. Verify `Player` returned
4. Verify `player.isAuthenticated() == true`
5. Verify session attached to player

**Assertions:**
- Login returns valid Player
- Player has active session
- Session contains JWT token

#### `testPlayerLogout()`
**Validates:** Player logout invalidates session but preserves player

**Steps:**
1. Register and login player "charlie"
2. Store playerId
3. Call `server.logout(playerId)`
4. Re-fetch player from server
5. Verify player exists but `isAuthenticated() == false`
6. Verify session removed from activeSessions

**Assertions:**
- `server.getPlayer(playerId) != null` (player preserved)
- `player.isAuthenticated() == false` (session cleared)
- `player.getSession() == null`

**Note:** Test re-fetches player after logout to avoid stale reference

#### `testMultiplePlayersCanLogin()`
**Validates:** Server supports concurrent player sessions

**Steps:**
1. Register and login 5 players: alice, bob, charlie, dave, eve
2. Verify all 5 players authenticated
3. Verify all sessions distinct

**Assertions:**
- All 5 players have `isAuthenticated() == true`
- All session IDs unique

### Action Processing Tests (4 tests)

#### `testActionSubmission()`
**Validates:** Actions queued and processed

**Steps:**
1. Register and login player "frank"
2. Create CHAT action with message "Hello"
3. Call `server.submitAction(action)`
4. Wait 100ms for processing
5. Verify action status changed from PENDING

**Assertions:**
- Action initially PENDING
- Action processed (status VALIDATED or EXECUTED)

#### `testActionValidation()`
**Validates:** Invalid actions rejected with reason

**Steps:**
1. Register and login player "grace"
2. Create MOVE action with missing `targetRegionId` parameter
3. Submit action
4. Wait 100ms
5. Verify action status REJECTED with reason "Missing parameter: targetRegionId"

**Assertions:**
- `action.getStatus() == ActionStatus.REJECTED`
- `action.getRejectionReason()` contains "Missing parameter"

#### `testActionQueueing()`
**Validates:** Multiple actions processed in order

**Steps:**
1. Login player "harry"
2. Submit 10 CHAT actions in sequence
3. Wait for all to process
4. Verify all executed

**Assertions:**
- All 10 actions have status EXECUTED
- Processing order matches submission order (timestamp-based)

#### `testUnauthenticatedActionRejected()`
**Validates:** Actions from unauthenticated players rejected

**Steps:**
1. Register player "iris" (but don't login)
2. Create Player object manually with `authenticated = false`
3. Create and submit action
4. Wait for processing
5. Verify action REJECTED with reason "Player not authenticated"

**Assertions:**
- `action.getStatus() == ActionStatus.REJECTED`
- `action.getRejectionReason()` equals "Player not authenticated"

### Performance Tests (3 tests)

#### `testLatencyTracking()`
**Validates:** Server records action processing times

**Steps:**
1. Login player "jack"
2. Submit 50 CHAT actions
3. Wait for all to process
4. Verify latency metrics populated
5. Verify average latency calculated

**Assertions:**
- `server.getAverageLatency() > 0.0`
- Latency queue contains up to 100 samples (STATS_WINDOW_SIZE)

#### `testPerformanceTarget()`
**Validates:** 95th percentile latency <50ms

**Steps:**
1. Login player "karen"
2. Submit 100 mixed actions (MOVE, HARVEST, CHAT)
3. Wait for all to process
4. Calculate 95th percentile latency
5. Verify meets target

**Assertions:**
- `server.get95thPercentileLatency() < 50` (milliseconds)

**Critical:** This test validates Phase 1.9 quality gate

#### `test95thPercentileCalculation()`
**Validates:** Percentile calculation correct

**Steps:**
1. Login player "larry"
2. Submit 100 actions
3. Manually calculate 95th percentile from processing times
4. Verify matches `server.get95thPercentileLatency()`

**Assertions:**
- Server calculation matches manual calculation

## Key Test Scenarios

### Integration Test: Full Game Flow
```java
@Test
public void testFullGameFlow() {
    Server server = new Server(8080);
    server.start();
    
    // Register and login
    String playerId = server.registerPlayer("alice", "password");
    Player player = server.login("alice", "password");
    
    // Submit actions
    server.submitAction(createChatAction(player, "Hello!"));
    server.submitAction(createMoveAction(player, "region-north"));
    server.submitAction(createHarvestAction(player, "node-123"));
    
    // Wait for processing
    Thread.sleep(500);
    
    // Logout
    server.logout(playerId);
    
    // Shutdown
    server.stop();
}
```

### Concurrency Test: Multiple Players Acting
```java
@Test
public void testConcurrentPlayerActions() {
    Server server = new Server(8080);
    server.start();
    
    // Create 10 players
    List<Player> players = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
        String playerId = server.registerPlayer("player" + i, "password");
        players.add(server.login("player" + i, "password"));
    }
    
    // Each player submits 10 actions
    for (Player player : players) {
        for (int j = 0; j < 10; j++) {
            server.submitAction(createChatAction(player, "Message " + j));
        }
    }
    
    // Wait for all actions to process
    Thread.sleep(2000);
    
    // Verify performance target met
    assertTrue(server.get95thPercentileLatency() < 50);
    
    server.stop();
}
```

## Test Utilities

### Helper Methods
```java
private PlayerAction createChatAction(Player player, String message) {
    return new PlayerAction.Builder()
        .playerId(player.getPlayerId())
        .type(PlayerAction.ActionType.CHAT)
        .parameter("message", message)
        .build();
}

private PlayerAction createMoveAction(Player player, String targetRegionId) {
    return new PlayerAction.Builder()
        .playerId(player.getPlayerId())
        .type(PlayerAction.ActionType.MOVE)
        .parameter("targetRegionId", targetRegionId)
        .build();
}

private void waitForActionProcessing(int milliseconds) throws InterruptedException {
    Thread.sleep(milliseconds);
}
```

### Test Fixtures
```java
@BeforeEach
public void setup() {
    server = new Server(8080);
    server.start();
}

@AfterEach
public void teardown() throws InterruptedException {
    if (server.isRunning()) {
        server.stop();
    }
}
```

## Performance Benchmarks

### Measured Metrics (MVP)
- **Average Latency:** ~15ms (CHAT actions)
- **95th Percentile:** ~35ms (all action types)
- **Throughput:** ~100 actions/second (single processor thread)

### Target Metrics (Production)
- **Average Latency:** <10ms
- **95th Percentile:** <50ms ✅ (Phase 1.9 quality gate)
- **Throughput:** 1,000 actions/second

## Known Limitations

### MVP Test Gaps
- **No Network Tests:** Tests use direct `Server` reference (no TCP/WebSocket)
- **No Load Tests:** No sustained load testing (e.g., 1000 players for 1 hour)
- **No Failure Tests:** No simulation of network failures, crashes, data corruption
- **Limited Concurrency:** Tests use small player counts (5-10 players, not 1000+)

### Flaky Tests
- **testActionQueueing:** May fail if action processing slower than expected (timing-dependent)
- **testPerformanceTarget:** May fail on slow CI machines (CPU-dependent)

**Mitigation:** Use generous timeouts (500ms-2000ms), skip on slow CI

## Future Test Enhancements

1. **Network Tests:** Use real TCP sockets
   ```java
   ServerSocket serverSocket = new ServerSocket(8080);
   Socket clientSocket = new Socket("localhost", 8080);
   ```

2. **Load Tests:** JMeter or Gatling for sustained load
   ```scala
   scenario("Player Actions")
       .exec(http("Submit Action").post("/api/action"))
       .inject(rampUsers(1000).during(60.seconds))
   ```

3. **Chaos Engineering:** Simulate failures
   ```java
   testActionProcessingWithNetworkPartition();
   testActionProcessingWithDatabaseTimeout();
   testActionProcessingWithMemoryPressure();
   ```

4. **Property-Based Tests:** QuickCheck-style testing
   ```java
   @Property
   public void actionOrderPreservedUnderLoad(@ForAll List<PlayerAction> actions) {
       // Submit actions in random order
       // Verify processed in timestamp order
   }
   ```

## Bug Fixes During Testing

### Issue 1: testPlayerLogout Stale Reference
**Problem:** Test stored Player reference before logout, reference not updated after logout

**Fix:** Re-fetch player from server after logout
```java
// Before
server.logout(playerId);
assertFalse(player.isAuthenticated());  // FAIL: stale reference

// After
server.logout(playerId);
Player updatedPlayer = server.getPlayer(playerId);
assertFalse(updatedPlayer.isAuthenticated());  // PASS
```

### Issue 2: Flaky testActionQueueing
**Problem:** Actions not processed before assertion (timing issue)

**Fix:** Increased wait time from 100ms to 500ms
```java
// Before
Thread.sleep(100);  // Sometimes too short

// After
Thread.sleep(500);  // Generous timeout
```

## Related Documentation

- `Server.md` - Server implementation
- `AuthTest.md` - Authentication tests
- `ValidationTest.md` - Action validation tests
- `ConflictTest.md` - Conflict resolution tests
- `docs/testing_plan.md` - Overall testing strategy
- `archive/PHASE_1.9_SUMMARY.md` - Phase 1.9 test results
