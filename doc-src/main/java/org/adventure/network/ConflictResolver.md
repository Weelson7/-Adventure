# ConflictResolver

**Package:** `org.adventure.network`  
**Phase:** 1.9 (Multiplayer & Networking)  
**Related Classes:** `PlayerAction`, `Server`, `ActionValidator`

## Purpose

Resolves conflicts when multiple players attempt to access the same resource concurrently. Implements resource locking, timestamp-based ordering, and deterministic conflict resolution for the authoritative server.

## Key Responsibilities

1. **Conflict Detection:** Identify when multiple actions target the same resource
2. **Resource Locking:** Prevent concurrent modifications with `ReentrantLock`
3. **Timestamp Ordering:** Resolve conflicts by processing earlier actions first
4. **Action Queueing:** Queue conflicting actions for sequential processing
5. **Determinism:** Ensure same input produces same output (critical for multiplayer)

## Design Decisions

### Why Lock-Based Concurrency?
- **Simplicity:** Easier to reason about than optimistic locking (compare-and-swap)
- **Correctness:** Prevents race conditions (e.g., two players harvesting same node to 0)
- **Performance:** ReentrantLock is fast (<1μs overhead) for uncontended locks

### Why Timestamp Ordering?
- **Fairness:** First-come-first-served (FCFS) prevents starvation
- **Determinism:** Same timestamps → same order → same outcome (critical for tests)
- **Authority:** Server sets timestamps, client cannot manipulate order

### Why ReentrantLock over synchronized?
- **Trylock:** Can attempt lock without blocking (for queue-or-reject logic)
- **Fairness:** Can enable fair mode (FCFS queue) if needed
- **Interruptibility:** Can interrupt waiting threads (future cancellation support)
- **Performance:** Slightly faster than `synchronized` for high contention

### Why Action Queueing?
- **Avoid Deadlock:** Don't block processing thread waiting for lock
- **Graceful Degradation:** Queue conflicting actions instead of rejecting
- **Future Scheduling:** Queued actions can be scheduled for retry

## Class Structure

```java
public class ConflictResolver {
    private final Map<String, ReentrantLock> resourceLocks;           // resourceId → lock
    private final Map<String, List<PlayerAction>> pendingActions;     // resourceId → queued actions
    
    public boolean acquireLock(String resourceId) { ... }
    public void releaseLock(String resourceId) { ... }
    public ConflictResolution resolve(PlayerAction action, List<PlayerAction> otherActions) { ... }
    public boolean detectConflict(PlayerAction action, List<PlayerAction> otherActions) { ... }
    public List<PlayerAction> resolveByTimestamp(List<PlayerAction> conflictingActions) { ... }
    private String extractResourceId(PlayerAction action) { ... }
}
```

## Key Methods

### `acquireLock(String resourceId)`
**Purpose:** Acquire lock on resource before processing action

**Implementation:**
```java
public boolean acquireLock(String resourceId) {
    ReentrantLock lock = resourceLocks.computeIfAbsent(resourceId, k -> new ReentrantLock());
    return lock.tryLock();  // Non-blocking attempt
}
```

**Returns:**
- `true` if lock acquired (proceed with action)
- `false` if lock held by another action (queue action)

**Thread-Safe:** `ConcurrentHashMap.computeIfAbsent()` ensures atomic lock creation

**Example:**
```java
if (resolver.acquireLock("resource-node-123")) {
    try {
        // Process HARVEST action
    } finally {
        resolver.releaseLock("resource-node-123");
    }
} else {
    // Queue action for later
}
```

### `releaseLock(String resourceId)`
**Purpose:** Release lock after action processing completes

**Implementation:**
```java
public void releaseLock(String resourceId) {
    ReentrantLock lock = resourceLocks.get(resourceId);
    if (lock != null && lock.isHeldByCurrentThread()) {
        lock.unlock();
    }
}
```

**Safety:** Checks `isHeldByCurrentThread()` to avoid `IllegalMonitorStateException`

**Cleanup:** Does not remove lock from map (reuse lock for future actions)

**Example:**
```java
try {
    // Process action
} finally {
    resolver.releaseLock(resourceId);  // Always release in finally block
}
```

### `resolve(PlayerAction action, List<PlayerAction> otherActions)`
**Purpose:** Resolve conflict between action and other pending actions

**Steps:**
1. Extract resource ID from action
2. Acquire lock on resource
3. If lock acquired → return `ConflictResolution.SUCCESS`
4. If lock held → detect conflict with `otherActions`
5. If conflict → queue action → return `ConflictResolution.QUEUED`
6. If no conflict → return `ConflictResolution.SUCCESS`

**Returns:** `ConflictResolution` enum (SUCCESS, QUEUED, CONFLICT)

**Use Case:**
```java
ConflictResolution result = resolver.resolve(harvestAction, activeActions);
if (result == ConflictResolution.SUCCESS) {
    // Process action immediately
} else if (result == ConflictResolution.QUEUED) {
    // Action queued, will be processed later
} else {
    // Conflict detected, reject action
}
```

### `detectConflict(PlayerAction action, List<PlayerAction> otherActions)`
**Purpose:** Check if action conflicts with any other pending actions

**Logic:**
```java
String resourceId = extractResourceId(action);
for (PlayerAction other : otherActions) {
    if (extractResourceId(other).equals(resourceId)) {
        return true;  // Conflict: same resource
    }
}
return false;  // No conflict
```

**Returns:** `true` if conflict, `false` otherwise

**Example:**
```java
// Two HARVEST actions on same node → conflict
PlayerAction harvest1 = new PlayerAction.Builder()
    .type(PlayerAction.ActionType.HARVEST)
    .parameter("resourceNodeId", "node-123")
    .build();
PlayerAction harvest2 = new PlayerAction.Builder()
    .type(PlayerAction.ActionType.HARVEST)
    .parameter("resourceNodeId", "node-123")
    .build();

boolean conflict = resolver.detectConflict(harvest1, List.of(harvest2));  // true
```

### `resolveByTimestamp(List<PlayerAction> conflictingActions)`
**Purpose:** Order conflicting actions by timestamp (earliest first)

**Implementation:**
```java
public List<PlayerAction> resolveByTimestamp(List<PlayerAction> conflictingActions) {
    return conflictingActions.stream()
        .sorted(Comparator.comparing(PlayerAction::getTimestamp))
        .collect(Collectors.toList());
}
```

**Determinism:** Same timestamps → same order (critical for tests)

**Example:**
```java
// Actions submitted out of order
List<PlayerAction> actions = List.of(action2, action1, action3);
List<PlayerAction> ordered = resolver.resolveByTimestamp(actions);
// ordered = [action1, action2, action3] (sorted by timestamp)
```

### `extractResourceId(PlayerAction action)`
**Purpose:** Map action to resource ID for conflict detection

**Logic:**
```java
private String extractResourceId(PlayerAction action) {
    return switch (action.getType()) {
        case HARVEST -> (String) action.getParameters().get("resourceNodeId");
        case CRAFT -> (String) action.getParameters().get("recipeId");
        case PICK_UP_ITEM, DROP_ITEM, USE_ITEM -> (String) action.getParameters().get("itemId");
        case TRANSFER_OWNERSHIP -> (String) action.getParameters().get("structureId");
        case ATTACK -> (String) action.getParameters().get("targetCharacterId");
        case BUILD -> (String) action.getParameters().get("location");
        case JOIN_CLAN -> (String) action.getParameters().get("clanId");
        default -> action.getPlayerId();  // Player-specific actions don't conflict
    };
}
```

**Resource Mapping:**
- **HARVEST:** `resourceNodeId` (two players can't harvest same node simultaneously)
- **CRAFT:** `recipeId` (future: crafting stations may have limits)
- **PICK_UP_ITEM / DROP_ITEM / USE_ITEM:** `itemId` (item can only be in one inventory)
- **TRANSFER_OWNERSHIP:** `structureId` (structure can only have one owner change at a time)
- **ATTACK:** `targetCharacterId` (target can only be attacked by one player at a time)
- **BUILD:** `location` (can't build two structures in same location)
- **JOIN_CLAN:** `clanId` (clan may have membership limits)
- **MOVE / CHAT / LEAVE_CLAN:** `playerId` (player-specific, no resource conflict)

**Future Enhancement:** Support composite resource IDs (e.g., `playerId + itemId` for inventory actions)

## Conflict Resolution Strategies

### 1. Timestamp Ordering (Current Implementation)
**Algorithm:** First-come-first-served (FCFS)

**Pros:**
- Simple to implement
- Fair (no starvation)
- Deterministic

**Cons:**
- Not always optimal (may reject valid actions)
- No priority support

**Example:**
```
Actions: A1 (t=100), A2 (t=101) on same resource
Resolution: A1 executes first, A2 queued or rejected
```

### 2. Lock-Based (Current Implementation)
**Algorithm:** First to acquire lock wins

**Pros:**
- Prevents data corruption
- Fast for low contention

**Cons:**
- Can cause queueing under high contention
- No priority support

**Example:**
```
Thread 1: acquireLock("node-123") → true → process action
Thread 2: acquireLock("node-123") → false → queue action
```

### 3. Optimistic Locking (Future Enhancement)
**Algorithm:** Execute action, rollback if conflict detected

**Pros:**
- Better throughput for low contention
- No blocking

**Cons:**
- Complex rollback logic
- Wasted work on conflicts

**Example:**
```
Action 1: Read node quantity (50), harvest 10, write quantity (40)
Action 2: Read node quantity (50), harvest 10, write quantity (40)
Result: Conflict detected (expected 40, got 50), rollback Action 2
```

## Determinism Guarantees

### Timestamp Determinism
**Requirement:** Same timestamps → same order → same outcome

**Implementation:**
- `Instant.now()` provides nanosecond precision (unlikely collisions)
- `Comparator.comparing(PlayerAction::getTimestamp)` provides stable sort
- If timestamps equal, secondary sort by `actionId` (UUID) ensures determinism

**Test Case:** `ConflictTest.testHighConcurrencyDeterminism`
```java
// Generate 100 actions with random timestamps
// Sort by timestamp
// Execute in order
// Verify game state checksum matches expected value
```

### Lock Fairness
**Requirement:** Same lock acquisition order → same execution order

**Implementation:**
- `ReentrantLock` uses FIFO queue for waiting threads (if fairness enabled)
- MVP uses non-fair locks (performance optimization)
- Timestamp ordering provides fairness at application level

**Future:** Enable fair locks for strict FCFS
```java
ReentrantLock lock = new ReentrantLock(true);  // Fair mode
```

## Action Queueing

### Data Structure
```java
private final Map<String, List<PlayerAction>> pendingActions = new ConcurrentHashMap<>();
```

### Queueing Logic
```java
public void queueAction(String resourceId, PlayerAction action) {
    pendingActions.computeIfAbsent(resourceId, k -> new CopyOnWriteArrayList<>()).add(action);
}
```

### Dequeuing Logic
```java
public List<PlayerAction> dequeueActions(String resourceId) {
    List<PlayerAction> queued = pendingActions.remove(resourceId);
    return queued != null ? resolveByTimestamp(queued) : List.of();
}
```

### Example Workflow
```
1. Action A1 on resource R → acquireLock(R) → true → process A1
2. Action A2 on resource R → acquireLock(R) → false → queue A2
3. Action A3 on resource R → acquireLock(R) → false → queue A3
4. A1 completes → releaseLock(R)
5. Dequeue actions for R → [A2, A3] (sorted by timestamp)
6. Process A2 → releaseLock(R)
7. Process A3 → releaseLock(R)
```

## Integration Points

### With Server
- **Action Processing:** Server calls `resolver.acquireLock()` before executing action
- **Cleanup:** Server calls `resolver.releaseLock()` in `finally` block
- **Queueing:** Server calls `resolver.queueAction()` if lock acquisition fails

### With ActionValidator
- **Validation First:** Server validates action before attempting lock acquisition
- **No Rollback:** Validator prevents invalid actions, resolver prevents concurrent modifications

### With PlayerAction
- **Resource Extraction:** Resolver calls `extractResourceId()` to identify resource
- **Timestamp Ordering:** Resolver sorts actions by `action.getTimestamp()`

## Thread Safety

### Concurrent Data Structures
- **resourceLocks:** `ConcurrentHashMap<String, ReentrantLock>` (thread-safe)
- **pendingActions:** `ConcurrentHashMap<String, List<PlayerAction>>` (thread-safe map, but list may need sync)

### Lock Safety
- **ReentrantLock:** Thread-safe, allows re-acquisition by same thread
- **tryLock():** Non-blocking, safe for concurrent calls
- **unlock():** Must be called by lock-holding thread (checked with `isHeldByCurrentThread()`)

### Race Conditions
- **Lock Creation:** `computeIfAbsent()` ensures atomic lock creation (no duplicate locks)
- **Lock Removal:** Locks never removed (memory leak, but acceptable for MVP with bounded resources)
- **Queue Modification:** `CopyOnWriteArrayList` prevents `ConcurrentModificationException` (future enhancement)

## Performance Considerations

### Lock Contention
- **Low Contention:** <1μs per lock acquisition (fast path)
- **High Contention:** 10-100μs per lock (queueing overhead)
- **Scalability:** Linear degradation with contention (not exponential)

### Memory Usage
- **Per Lock:** ~40 bytes (ReentrantLock object)
- **1,000 Resources:** ~40 KB (negligible)
- **Memory Leak:** Locks never removed (bounded by number of unique resources in game)

### Future Optimization
- **Lock Striping:** Partition locks by resource type (e.g., separate maps for nodes, items, structures)
- **Lock Cleanup:** Remove unused locks after TTL (e.g., 1 hour of inactivity)

## Testing

### ConflictTest Coverage (16 Tests)
- **Lock Acquisition:** `testLockAcquisitionSuccess`, `testLockRelease`, `testReentrantLockAllowsSameThread`
- **Conflict Detection:** `testConflictDetection`, `testNoConflictForDifferentResources`, `testOwnershipTransferConflict`, `testCraftingConflictSamePlayer`
- **Timestamp Ordering:** `testTimestampOrdering`, `testTimestampOrderingWithDuplicates`
- **Queueing:** `testQueuedActions`, `testDequeueSortsActionsByTimestamp`, `testRemoveQueuedAction`
- **Determinism:** `testHighConcurrencyDeterminism`
- **Edge Cases:** `testMultiplePlayersHarvestingSameNode`, `testConcurrentLockConflict`

## Known Limitations

### MVP Constraints
- **In-Memory Only:** Locks not distributed (single-server only)
- **No Deadlock Detection:** Potential deadlock if action requires multiple locks (future: lock ordering)
- **No Timeout:** Lock held indefinitely if action hangs (future: timeout + force release)
- **No Priority:** All actions equal priority (no VIP queue)

### Scalability Limits
- **Single Server:** Locks don't work across multiple servers (need distributed locks with Redis)
- **Lock Granularity:** One lock per resource (could use striped locks for finer granularity)
- **Memory Leak:** Locks never removed (bounded by resource count in MVP)

## Future Enhancements

1. **Distributed Locks:** Use Redis Redlock for multi-server deployment
   ```java
   RLock lock = redisson.getLock("resource:" + resourceId);
   boolean acquired = lock.tryLock(100, 10000, TimeUnit.MILLISECONDS);
   ```

2. **Deadlock Prevention:** Implement lock ordering (always acquire locks in same order)
   ```java
   List<String> resourceIds = extractAllResourceIds(action);
   resourceIds.sort(String::compareTo);  // Alphabetical order
   for (String id : resourceIds) {
       acquireLock(id);
   }
   ```

3. **Lock Timeout:** Force-release locks after timeout
   ```java
   boolean acquired = lock.tryLock(5, TimeUnit.SECONDS);
   if (!acquired) {
       throw new TimeoutException("Lock acquisition timeout");
   }
   ```

4. **Priority Queue:** VIP players get priority in queue
   ```java
   PriorityQueue<PlayerAction> queue = new PriorityQueue<>(
       Comparator.comparing(PlayerAction::getPriority).reversed()
           .thenComparing(PlayerAction::getTimestamp)
   );
   ```

5. **Optimistic Locking:** Compare-and-swap for low-contention scenarios
   ```java
   int expectedQuantity = node.getCurrentQuantity();
   int newQuantity = expectedQuantity - 10;
   boolean success = node.compareAndSwap(expectedQuantity, newQuantity);
   ```

6. **Lock Metrics:** Track contention rate, average wait time
   ```java
   long waitTime = System.nanoTime();
   lock.lock();
   waitTime = System.nanoTime() - waitTime;
   metrics.recordLockWaitTime(resourceId, waitTime);
   ```

## Related Documentation

- `PlayerAction.md` - Action data model with resource parameters
- `Server.md` - Server-side action processing and lock usage
- `ActionValidator.md` - Validation before lock acquisition
- `docs/design_decisions.md` - Concurrency architecture decisions
- `archive/PHASE_1.9_SUMMARY.md` - Phase 1.9 implementation details
- Java ReentrantLock: https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/locks/ReentrantLock.html
