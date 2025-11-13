# ActionValidator

**Package:** `org.adventure.network`  
**Phase:** 1.9 (Multiplayer & Networking)  
**Related Classes:** `PlayerAction`, `Server`, `Player`, `Character`

## Purpose

Server-side validation of all player actions before execution. Enforces game rules, checks permissions, validates parameters, and prevents cheating. Acts as the security layer for the authoritative server model.

## Key Responsibilities

1. **Security Validation:** Ensure player owns action, is authenticated, action not expired
2. **Parameter Validation:** Check all required parameters present and valid
3. **Game Rule Enforcement:** Validate action allowed by current game state
4. **Permission Checking:** Verify player has permission to perform action
5. **Replay Attack Prevention:** Reject actions older than 5 seconds

## Design Decisions

### Why Server-Side Validation?
- **Authoritative Server:** Client cannot be trusted (can be modified/hacked)
- **Consistency:** All players see same game state (no client-side cheating)
- **Security:** Prevents duplication exploits, inventory hacks, teleportation, etc.

### Why Separate Validator Class?
- **Single Responsibility:** Server handles action processing, Validator handles validation
- **Testability:** Easy to test validation logic in isolation (35 tests in ValidationTest)
- **Extensibility:** Easy to add new action types without modifying Server

### Two-Phase Validation
1. **Generic Checks:** Authenticated, action ownership, not expired (all actions)
2. **Type-Specific Checks:** Action-specific parameters and game state (per action type)

## Class Structure

```java
public class ActionValidator {
    public static final long MAX_ACTION_AGE_MS = 5000;  // 5 seconds
    
    public ValidationResult validate(PlayerAction action, Player player) {
        // Phase 1: Generic checks
        // Phase 2: Type-specific checks via dispatcher
    }
    
    private ValidationResult validateActionType(PlayerAction action, Player player) {
        // Switch on action.getType(), call action-specific validator
    }
    
    // 13 action-specific validators (one per ActionType)
    private ValidationResult validateMove(PlayerAction action, Player player) { ... }
    private ValidationResult validateHarvest(PlayerAction action, Player player) { ... }
    // ... etc
}
```

## Validation Result

### Structure
```java
public static class ValidationResult {
    private final boolean valid;
    private final String reason;  // Nullable, set if invalid
}
```

### Factory Methods
- **`accept()`:** Return `new ValidationResult(true, null)` for valid actions
- **`reject(String reason)`:** Return `new ValidationResult(false, reason)` for invalid actions

### Usage
```java
ValidationResult result = validator.validate(action, player);
if (result.isValid()) {
    // Execute action
} else {
    action.setStatus(PlayerAction.ActionStatus.REJECTED);
    action.setRejectionReason(result.getReason());
}
```

## Generic Validation (All Actions)

### 1. Player Authentication
```java
if (!player.isAuthenticated()) {
    return ValidationResult.reject("Player not authenticated");
}
```
- **Purpose:** Prevent unauthenticated players from acting
- **Check:** `player.isAuthenticated() == true`
- **Bypass:** CHAT action doesn't require authentication (see special case)

### 2. Action Ownership
```java
if (!action.getPlayerId().equals(player.getPlayerId())) {
    return ValidationResult.reject("Action does not belong to this player");
}
```
- **Purpose:** Prevent player A from submitting actions for player B
- **Check:** `action.playerId == player.playerId`
- **Attack Vector:** Malicious client submits action with different `playerId`

### 3. Replay Attack Prevention
```java
long ageMs = Duration.between(action.getTimestamp(), Instant.now()).toMillis();
if (ageMs > MAX_ACTION_AGE_MS) {
    return ValidationResult.reject("Action expired (age: " + ageMs + "ms)");
}
```
- **Purpose:** Prevent replay of old actions (e.g., duplicate HARVEST)
- **Window:** 5 seconds (configurable via `MAX_ACTION_AGE_MS`)
- **Clock Skew:** Uses server time (`Instant.now()`) to avoid client manipulation

### 4. Character Requirement (Most Actions)
```java
if (player.getCharacterId() == null) {
    return ValidationResult.reject("Player has no character");
}
```
- **Purpose:** Ensure player has created character before acting
- **Exception:** CHAT action doesn't require character (can chat before character creation)

## Type-Specific Validation

### MOVE Action
**Required Parameters:** `targetRegionId` (String)

**Validation Checks:**
1. Parameter present: `parameters.containsKey("targetRegionId")`
2. Region exists: (Future: query `RegionSimulator.regions` map)
3. Region accessible: (Future: check pathfinding, locked doors, etc.)
4. Character can travel: (Future: check movement points, encumbrance, etc.)

**Current MVP:**
- Checks parameter presence only
- Assumes all regions accessible (no pathfinding yet)

### HARVEST Action
**Required Parameters:** `resourceNodeId` (String)

**Validation Checks:**
1. Parameter present: `parameters.containsKey("resourceNodeId")`
2. Node exists: (Future: query `Region.resourceNodes`)
3. Node in character's region: (Future: check `node.regionId == character.regionId`)
4. Node not depleted: (Future: check `node.currentQuantity > 0`)
5. Character has skill: (Future: check `character.skills` contains required skill)

**Current MVP:**
- Checks parameter presence only
- Assumes node exists and accessible

### CRAFT Action
**Required Parameters:** `recipeId` (String), `quantity` (Integer)

**Validation Checks:**
1. Parameters present and correct type
2. Recipe exists: (Future: query `CraftingSystem.recipes`)
3. Character knows recipe: (Future: check `character.knownRecipes`)
4. Materials available: (Future: check `character.inventory` against `recipe.materials`)
5. Sufficient proficiency: (Future: check `character.proficiency >= recipe.minProficiency`)
6. Quantity positive: `quantity > 0`

**Current MVP:**
- Checks parameters present and quantity > 0
- Assumes recipe known and materials available

### ATTACK Action
**Required Parameters:** `targetCharacterId` (String)

**Validation Checks:**
1. Parameter present
2. Target exists: (Future: query `Character` storage)
3. Target in same region: (Future: check `target.regionId == character.regionId`)
4. Not attacking self: `targetCharacterId != character.characterId`
5. Not allied: (Future: check `Diplomacy.getRelationship()` not ALLIED)
6. Combat allowed: (Future: check region flags, peace treaties, etc.)

**Current MVP:**
- Checks parameter present only
- No combat system yet (placeholder for Phase 2)

### TRADE Action
**Required Parameters:** `targetPlayerId` (String), `offeredItems` (List), `requestedItems` (List)

**Validation Checks:**
1. Parameters present
2. Target online: (Future: check `Server.activePlayers`)
3. Items exist: (Future: validate all item IDs in both lists)
4. Items owned: (Future: check `character.inventory` contains `offeredItems`)
5. Trade fair: (Future: check value balance, no scam detection)

**Current MVP:**
- Checks parameters present only
- No trading system yet (placeholder for Phase 2)

### BUILD Action
**Required Parameters:** `structureType` (String), `roomCategory` (String), `location` (String)

**Validation Checks:**
1. Parameters present
2. Structure type valid: (Future: validate against `StructureType` enum)
3. Room category valid: (Future: validate against `RoomCategory` enum)
4. Location valid: (Future: check coordinates, region ownership)
5. Resources available: (Future: check materials in inventory)
6. Permission granted: (Future: check `OwnershipTransferSystem.hasPermission()`)

**Current MVP:**
- Checks parameters present only
- Integrates with existing `Structure` system (Phase 1.5)

### CHAT Action
**Required Parameters:** `message` (String)

**Validation Checks:**
1. Parameter present
2. Message not empty: `!message.trim().isEmpty()`
3. Message length <= 500: `message.length() <= 500`
4. No profanity: (Future: profanity filter)
5. Not rate-limited: (Future: max 10 messages/minute)

**Special Case:**
- **No Authentication Required:** Chat works even if `player.isAuthenticated() == false`
- **No Character Required:** Chat works even if `player.getCharacterId() == null`
- **Rationale:** Allow players to chat while creating character, lobby chat, etc.

### USE_ITEM Action
**Required Parameters:** `itemId` (String), optional `targetId` (String)

**Validation Checks:**
1. `itemId` present
2. Item exists: (Future: query `Item` storage)
3. Item in inventory: (Future: check `character.inventory`)
4. Item usable: (Future: check `item.category.isUsable()`)
5. Target valid: (Future: if `targetId` present, validate target exists and in range)

**Current MVP:**
- Checks `itemId` present only
- Integrates with existing `Item` system (Phase 1.4)

### DROP_ITEM / PICK_UP_ITEM Actions
**Required Parameters:** `itemId` (String)

**Validation Checks:**
1. `itemId` present
2. Item exists: (Future: query `Item` storage)
3. **DROP_ITEM:** Item in inventory
4. **PICK_UP_ITEM:** Item in region, inventory has space

**Current MVP:**
- Checks `itemId` present only
- Assumes item exists

### TRANSFER_OWNERSHIP Action
**Required Parameters:** `structureId` (String), `newOwnerId` (String), `transferType` (String)

**Validation Checks:**
1. Parameters present
2. Structure exists: (Future: query `Structure` storage)
3. Character is owner: (Future: check `structure.ownerId == character.characterId`)
4. New owner valid: (Future: validate `newOwnerId` is valid character/clan)
5. Transfer type valid: (Future: validate against `TransferType` enum: SALE, GIFT, INHERITANCE, SEIZURE)

**Current MVP:**
- Checks parameters present only
- Integrates with existing `OwnershipTransferSystem` (Phase 1.5.1)

### JOIN_CLAN / LEAVE_CLAN Actions
**Required Parameters:**
- **JOIN_CLAN:** `clanId` (String)
- **LEAVE_CLAN:** None

**Validation Checks (JOIN_CLAN):**
1. `clanId` present
2. Clan exists: (Future: query `Clan` storage)
3. Character not in clan: (Future: check `character.clanId == null`)
4. Meets requirements: (Future: check clan's membership requirements)

**Validation Checks (LEAVE_CLAN):**
1. Character in clan: (Future: check `character.clanId != null`)
2. Not clan leader: (Future: check `clan.leaderId != character.characterId` or leader transferred first)

**Current MVP:**
- Checks parameter presence only (JOIN_CLAN)
- No checks for LEAVE_CLAN (always valid in MVP)
- Integrates with existing `Clan` system (Phase 1.6)

## Integration Points

### With Server
- **Call Site:** `Server.processAction()` calls `validator.validate(action, player)`
- **Success Path:** If valid, server executes action
- **Failure Path:** If invalid, server sets `action.status = REJECTED`, `action.rejectionReason = result.reason`

### With Player
- **Authentication:** Validator checks `player.isAuthenticated()`
- **Character Link:** Validator uses `player.getCharacterId()` to fetch character data

### With Character
- **Future Integration:** Validator will query character inventory, skills, location for validation
- **Current MVP:** Character not queried yet (placeholder validations)

### With Game Systems
- **Region:** Future validation of region accessibility
- **Item:** Future validation of item ownership and usability
- **Crafting:** Future validation of recipe knowledge and materials
- **Structure:** Future validation of building permissions
- **Clan:** Future validation of membership status

## Thread Safety

### Stateless Design
- **No Mutable State:** `ActionValidator` has no instance fields (all static or local)
- **Thread-Safe:** Multiple threads can call `validate()` concurrently without synchronization

### Future Considerations
- **Caching:** If adding cache for performance (e.g., recipe lookup), use `ConcurrentHashMap`
- **Metrics:** If adding validation metrics, use `AtomicLong` for counters

## Performance Considerations

### Validation Latency
- **Current:** <1ms per action (simple parameter checks)
- **Future:** 5-10ms per action (with database queries for game state)
- **Optimization:** Cache frequently-accessed data (e.g., region graph, recipe list)

### Memory
- **Minimal:** No object allocation except `ValidationResult` (small immutable object)
- **GC Pressure:** Negligible (1-2 objects per validation)

## Testing

### ValidationTest Coverage (35 Tests)
- **Generic Checks:** `testUnauthenticatedPlayerRejected`, `testActionBelongsToDifferentPlayer`, `testExpiredActionRejected`
- **MOVE:** `testValidMoveAction`, `testMoveActionMissingParameters`
- **HARVEST:** `testValidHarvestAction`, `testHarvestActionMissingParameters`
- **CRAFT:** `testValidCraftAction`, `testCraftMissingRecipeId`, `testCraftMissingQuantity`, `testCraftNegativeQuantity`
- **ATTACK:** `testValidAttackAction`, `testAttackMissingTarget`
- **TRADE:** `testValidTradeAction`, `testTradeMissingParameters`
- **BUILD:** `testValidBuildAction`, `testBuildMissingStructureType`
- **CHAT:** `testValidChatAction`, `testChatActionWithEmptyMessage`, `testChatActionWithTooLongMessage`, `testChatDoesNotRequireCharacter`
- **USE_ITEM:** `testValidUseItemAction`, `testUseItemMissingItemId`
- **DROP_ITEM / PICK_UP_ITEM:** `testValidDropItemAction`, `testValidPickUpItemAction`
- **TRANSFER_OWNERSHIP:** `testValidTransferOwnershipAction`, `testTransferOwnershipMissingParameters`
- **JOIN_CLAN / LEAVE_CLAN:** `testValidJoinClanAction`, `testValidLeaveClanAction`, `testJoinClanMissingClanId`

## Known Limitations

### MVP Constraints
- **No Game State Queries:** Validation assumes all referenced entities exist
- **No Permission Checks:** Assumes player has permission for all actions
- **No Resource Checks:** Assumes player has sufficient materials, currency, etc.
- **No Rate Limiting:** No flood protection (player can spam actions)

### Security Gaps
- **Parameter Injection:** Malicious parameters not sanitized (e.g., SQL injection if future DB queries)
- **Business Logic Exploits:** No validation of economic fairness (e.g., trade scams)
- **Concurrency Exploits:** No validation of double-spend (handled by `ConflictResolver`, not validator)

## Future Enhancements

1. **Deep Validation:** Query actual game state (regions, items, characters) for validation
2. **Permission System:** Integrate with fine-grained permission checks
3. **Rate Limiting:** Implement per-player action limits (e.g., 10 actions/second)
4. **Business Rules:** Validate economic fairness (e.g., trade value balance)
5. **Contextual Validation:** Reject actions based on game mode (e.g., PvP disabled in safe zones)
6. **Validation Metrics:** Track rejection rate per action type for analytics
7. **Custom Validators:** Plugin system for mod-defined action types

## Error Messages

### Best Practices
- **Be Specific:** "Missing parameter: targetRegionId" not "Invalid action"
- **No Leakage:** Don't reveal internal state (e.g., "Player 123 not found" → "Invalid target")
- **Actionable:** Tell player how to fix (e.g., "Insufficient materials: need 5 wood, have 2")

### Examples
- ✅ "Player not authenticated"
- ✅ "Action expired (age: 6123ms)"
- ✅ "Missing parameter: itemId"
- ✅ "Message too long (520 chars, max 500)"
- ❌ "Validation failed" (too vague)
- ❌ "NullPointerException in validateMove()" (internal error leaked)

## Related Documentation

- `PlayerAction.md` - Action data model and types
- `Server.md` - Action processing pipeline
- `Player.md` - Player authentication and character link
- `Character.md` - Character data used in validation (future)
- `docs/design_decisions.md` - Server authority and validation architecture
- `archive/PHASE_1.9_SUMMARY.md` - Phase 1.9 implementation details
