# PlayerAction

**Package:** `org.adventure.network`  
**Phase:** 1.9 (Multiplayer & Networking)  
**Related Classes:** `ActionValidator`, `Server`, `ConflictResolver`

## Purpose

Represents a player-initiated action in the game, including action type, parameters, validation state, and execution tracking. Serves as the data model for the authoritative server's action processing pipeline.

## Key Responsibilities

1. **Action Representation:** Encapsulates all data needed to describe a player action
2. **Type Safety:** Enumerates all valid action types (13 types in MVP)
3. **Status Tracking:** Tracks action lifecycle (pending → validated → executed/failed)
4. **Parameter Storage:** Flexible key-value map for action-specific data
5. **Builder Pattern:** Provides fluent API for action construction

## Design Decisions

### Why Enum for ActionType?
- **Type Safety:** Prevents invalid action strings (e.g., "MOV" vs "MOVE")
- **Exhaustiveness:** Switch statements can enforce handling all types
- **Documentation:** Self-documenting list of all supported actions
- **Performance:** Enum comparison faster than string comparison

### Why Map for Parameters?
- **Flexibility:** Each action type has different parameter requirements
- **Extensibility:** Easy to add new parameters without breaking API
- **JSON-Friendly:** Easy to serialize/deserialize for network protocol (future)
- **Type Erasure Tradeoff:** Lost type safety, but gained flexibility

### Status State Machine
```
PENDING → VALIDATED → EXECUTED (success)
        ↘ REJECTED
        ↘ FAILED (validated but execution failed)
```

### Timestamp for Replay Attack Prevention
- **Server Authority:** Server sets timestamp on action receipt
- **5-Second Window:** Actions older than 5 seconds rejected (see `ActionValidator.MAX_ACTION_AGE_MS`)
- **Clock Skew:** Uses server time to avoid client clock manipulation

## Class Structure

```java
public class PlayerAction {
    private final String actionId;          // UUID, unique identifier
    private final String playerId;          // Foreign key to Player
    private final ActionType type;          // Enum: MOVE, HARVEST, etc.
    private final Map<String, Object> parameters;  // Action-specific data
    private ActionStatus status;            // Mutable: PENDING → VALIDATED → EXECUTED
    private final Instant timestamp;        // Server receipt time (UTC)
    private String rejectionReason;         // Nullable, set on REJECTED status
}
```

## Action Types (13 Total)

### Movement & Interaction
1. **MOVE:** Change character location
   - Parameters: `targetRegionId` (String)
   - Validation: Region exists, accessible, character can travel
   
2. **CHAT:** Send message to other players
   - Parameters: `message` (String, max 500 chars)
   - Validation: Message not empty, length <= 500, no profanity (future)

### Resource Management
3. **HARVEST:** Gather resources from node
   - Parameters: `resourceNodeId` (String)
   - Validation: Node exists, in character's region, sufficient skill, node not depleted
   
4. **PICK_UP_ITEM:** Collect item from region
   - Parameters: `itemId` (String)
   - Validation: Item exists, in character's region, inventory has space
   
5. **DROP_ITEM:** Drop item in current region
   - Parameters: `itemId` (String)
   - Validation: Item in character's inventory

### Crafting & Building
6. **CRAFT:** Create item from recipe
   - Parameters: `recipeId` (String), `quantity` (Integer)
   - Validation: Recipe known, materials available, sufficient proficiency
   
7. **BUILD:** Construct structure
   - Parameters: `structureType` (String), `roomCategory` (String), `location` (String)
   - Validation: Character has permission, resources available, location valid

8. **USE_ITEM:** Consume or activate item
   - Parameters: `itemId` (String), `targetId` (String, optional)
   - Validation: Item in inventory, item usable, target valid (if applicable)

### Combat & Trading
9. **ATTACK:** Initiate combat
   - Parameters: `targetCharacterId` (String)
   - Validation: Target exists, in same region, not self, not allied

10. **TRADE:** Exchange items/currency
    - Parameters: `targetPlayerId` (String), `offeredItems` (List), `requestedItems` (List)
    - Validation: Both players online, items exist, trade fair (future)

### Ownership & Structures
11. **TRANSFER_OWNERSHIP:** Transfer structure ownership
    - Parameters: `structureId` (String), `newOwnerId` (String), `transferType` (String)
    - Validation: Character is owner, new owner valid, transfer type allowed

### Society & Clans
12. **JOIN_CLAN:** Request to join clan
    - Parameters: `clanId` (String)
    - Validation: Clan exists, character not in clan, meets requirements

13. **LEAVE_CLAN:** Leave current clan
    - Parameters: None
    - Validation: Character in clan, not clan leader (must transfer first)

## Action Status States

### PENDING
- **Meaning:** Action received, awaiting validation
- **Next States:** VALIDATED, REJECTED
- **Duration:** Milliseconds (action validation is fast)

### VALIDATED
- **Meaning:** Action passed all validation checks
- **Next States:** EXECUTED, FAILED
- **Duration:** Milliseconds to seconds (depending on execution complexity)

### REJECTED
- **Meaning:** Action failed validation
- **Terminal State:** No further processing
- **Reason:** Set in `rejectionReason` field (e.g., "Insufficient materials")

### EXECUTED
- **Meaning:** Action successfully executed
- **Terminal State:** No further processing
- **Side Effects:** Game state modified (inventory changed, character moved, etc.)

### FAILED
- **Meaning:** Action validated but execution failed (rare)
- **Terminal State:** No further processing
- **Reason:** Set in `rejectionReason` (e.g., "Concurrent modification conflict")

## Builder Pattern

### Usage
```java
PlayerAction action = new PlayerAction.Builder()
    .playerId("player-123")
    .type(PlayerAction.ActionType.MOVE)
    .parameter("targetRegionId", "region-456")
    .build();
```

### Why Builder?
- **Readability:** Fluent API easier to read than 7-parameter constructor
- **Flexibility:** Optional parameters (e.g., `rejectionReason`) handled gracefully
- **Validation:** Can add validation in `build()` method (future enhancement)
- **Immutability:** Encourages creating complete objects

## Parameter Conventions

### Naming
- **camelCase:** Use Java naming conventions (e.g., `targetRegionId`)
- **Descriptive:** Avoid abbreviations (e.g., `itemId` not `iid`)
- **Consistent:** Same parameter name across actions (e.g., `itemId` always means Item UUID)

### Types
- **IDs:** String (UUID format)
- **Quantities:** Integer
- **Booleans:** Boolean
- **Collections:** List<String> for multiple IDs
- **Complex:** Map<String, Object> for nested data (avoid in MVP)

### Validation
- **Type Checking:** `ActionValidator` casts parameters and handles `ClassCastException`
- **Null Handling:** Missing required parameters rejected with clear error message
- **Range Checking:** Quantities must be positive, strings non-empty, etc.

## Integration Points

### With Server
- **Submission:** Client calls `server.submitAction()` → action added to `actionQueue`
- **Processing:** Server's `processActions()` loop consumes queue, validates and executes
- **Response:** Server returns action with updated status (VALIDATED/REJECTED/EXECUTED)

### With ActionValidator
- **Validation:** `validator.validate(action, player)` returns `ValidationResult`
- **Status Update:** Action status set to VALIDATED or REJECTED based on result
- **Reason Storage:** Rejection reason from `ValidationResult` stored in action

### With ConflictResolver
- **Conflict Detection:** `resolver.detectConflict(action, otherActions)` checks resource overlap
- **Queueing:** Conflicting actions queued by `resolver.resolveByTimestamp()`
- **Resource Locking:** `resolver.acquireLock(resourceId)` prevents concurrent modifications

## Thread Safety

### Immutable Fields
- **Final:** `actionId`, `playerId`, `type`, `parameters`, `timestamp` are immutable
- **Safe:** Can be read by multiple threads without synchronization

### Mutable Fields
- **Status:** Changed by Server during processing (PENDING → VALIDATED → EXECUTED)
- **RejectionReason:** Set once on REJECTED status
- **Risk:** Race condition if multiple threads process same action (shouldn't happen)

### Thread-Safe Collections
- **Parameters:** `HashMap` wrapped in `Collections.unmodifiableMap()` in builder
- **Builder:** Not thread-safe (shouldn't be shared across threads)

## Performance Considerations

### Memory
- **Parameters Map:** ~100 bytes per action (UUID + enum + small map)
- **Queue Depth:** Default `LinkedBlockingQueue` unbounded (risk of OOM)
- **Future:** Implement max queue size (e.g., 10,000 actions) with rejection

### Latency
- **Validation:** <10ms per action (simple checks, no DB queries)
- **Execution:** Varies (MOVE <1ms, CRAFT 10-100ms, BUILD 100-500ms)
- **Target:** 95th percentile <50ms (validated in `ServerTest.testPerformanceTarget`)

## Testing

### ServerTest Coverage
- `testActionSubmission`: Validates action queued and processed
- `testActionValidation`: Validates action rejected if validation fails
- `testLatencyTracking`: Validates performance metrics tracked

### ValidationTest Coverage
- 35 tests covering all action types and validation scenarios
- `testValidMoveAction`: Validates MOVE action with valid parameters
- `testMoveActionMissingParameters`: Validates rejection on missing `targetRegionId`
- `testExpiredActionRejected`: Validates rejection of 6-second-old action
- `testChatActionWithTooLongMessage`: Validates 500-char limit on CHAT

### ConflictTest Coverage
- `testConflictDetection`: Validates two HARVEST actions on same node conflict
- `testNoConflictForDifferentResources`: Validates different resources don't conflict
- `testTimestampOrdering`: Validates earlier action processed first

## Known Limitations

### MVP Constraints
- **No Undo:** Actions are irreversible (no rollback mechanism)
- **No Batching:** Each action processed individually (no transaction support)
- **No Priority:** All actions equal priority (no VIP queue)
- **No Cancellation:** Cannot cancel action once submitted

### Security Considerations
- **Parameter Injection:** Malicious parameters not sanitized (trust ActionValidator)
- **Replay Attacks:** 5-second window prevents most replays, but not all
- **Flood Protection:** No rate limiting per player (risk of action spam)

## Future Enhancements

1. **Action Chaining:** Allow actions to trigger other actions (e.g., auto-harvest on arrival)
2. **Scheduled Actions:** Delay action execution (e.g., "craft in 1 hour")
3. **Conditional Actions:** Execute only if condition met (e.g., "harvest if node available")
4. **Macro Support:** Record and replay action sequences
5. **Audit Logging:** Persist all actions to database for admin review
6. **Analytics:** Track action frequency, success rate, latency per type

## Related Documentation

- `ActionValidator.md` - Validation logic for all action types
- `Server.md` - Action processing pipeline
- `ConflictResolver.md` - Conflict detection and resolution
- `docs/design_decisions.md` - Action-based architecture decisions
- `archive/PHASE_1.9_SUMMARY.md` - Phase 1.9 implementation details
