# ValidationTest

**Package:** `org.adventure`  
**Phase:** 1.9 (Multiplayer & Networking)  
**Related Classes:** `ActionValidator`, `PlayerAction`, `Player`  
**Test Count:** 35 tests

## Purpose

Comprehensive test suite for server-side action validation. Validates security checks, parameter validation, game rule enforcement, and action-specific requirements for all 13 action types.

## Test Coverage

### Generic Validation Tests (4 tests)

#### `testUnauthenticatedPlayerRejected()`
**Validates:** Unauthenticated players cannot act

**Steps:**
1. Create player with `authenticated = false`
2. Create MOVE action
3. Call `validator.validate(action, player)`
4. Verify rejection with reason "Player not authenticated"

**Assertions:**
- `result.isValid() == false`
- `result.getReason()` contains "not authenticated"

**Exception:** CHAT actions allowed without authentication

#### `testActionBelongsToDifferentPlayer()`
**Validates:** Player cannot submit actions for other players

**Steps:**
1. Create action with `playerId = "player-1"`
2. Create player with `playerId = "player-2"`
3. Validate action
4. Verify rejection

**Assertions:**
- `result.getReason()` contains "does not belong to this player"

**Security:** Prevents player impersonation attacks

#### `testExpiredActionRejected()`
**Validates:** Actions older than 5 seconds rejected (replay attack prevention)

**Steps:**
1. Create action with timestamp 6 seconds in past
2. Wait to ensure age > MAX_ACTION_AGE_MS (5000ms)
3. Validate action
4. Verify rejection

**Assertions:**
- `result.getReason()` contains "expired" and age in milliseconds

**Implementation:** Uses `Thread.sleep(6000)` to ensure action age exceeds limit

#### `testNonChatActionRequiresCharacter()`
**Validates:** Actions (except CHAT) require player to have character

**Steps:**
1. Create player with `characterId = null`
2. Create MOVE action
3. Validate action
4. Verify rejection with reason "Player has no character"

**Assertions:**
- `result.isValid() == false`

### MOVE Action Tests (2 tests)

#### `testValidMoveAction()`
**Validates:** MOVE action with valid parameters accepted

**Steps:**
1. Create authenticated player with character
2. Create MOVE action with `targetRegionId = "region-123"`
3. Validate action
4. Verify acceptance

**Assertions:**
- `result.isValid() == true`

#### `testMoveActionMissingParameters()`
**Validates:** MOVE action missing `targetRegionId` rejected

**Steps:**
1. Create MOVE action without `targetRegionId` parameter
2. Validate action
3. Verify rejection

**Assertions:**
- `result.getReason()` contains "Missing parameter: targetRegionId"

### HARVEST Action Tests (2 tests)

#### `testValidHarvestAction()`
**Validates:** HARVEST action with valid parameters accepted

**Steps:**
1. Create HARVEST action with `resourceNodeId = "node-123"`
2. Validate action
3. Verify acceptance

**Assertions:**
- `result.isValid() == true`

#### `testHarvestActionMissingParameters()`
**Validates:** HARVEST missing `resourceNodeId` rejected

**Steps:**
1. Create HARVEST action without `resourceNodeId`
2. Validate action
3. Verify rejection

**Assertions:**
- `result.getReason()` contains "Missing parameter: resourceNodeId"

### CRAFT Action Tests (4 tests)

#### `testValidCraftAction()`
**Validates:** CRAFT action with valid parameters accepted

**Steps:**
1. Create CRAFT action with `recipeId = "recipe-sword"`, `quantity = 1`
2. Validate action
3. Verify acceptance

**Assertions:**
- `result.isValid() == true`

#### `testCraftMissingRecipeId()`
**Validates:** CRAFT missing `recipeId` rejected

**Steps:**
1. Create CRAFT action with only `quantity`
2. Validate action
3. Verify rejection

**Assertions:**
- `result.getReason()` contains "Missing parameter: recipeId"

#### `testCraftMissingQuantity()`
**Validates:** CRAFT missing `quantity` rejected

**Steps:**
1. Create CRAFT action with only `recipeId`
2. Validate action
3. Verify rejection

**Assertions:**
- `result.getReason()` contains "Missing parameter: quantity"

#### `testCraftNegativeQuantity()`
**Validates:** CRAFT with negative quantity rejected

**Steps:**
1. Create CRAFT action with `quantity = -5`
2. Validate action
3. Verify rejection

**Assertions:**
- `result.getReason()` contains "Quantity must be positive"

### ATTACK Action Tests (2 tests)

#### `testValidAttackAction()`
**Validates:** ATTACK action with valid target accepted

**Steps:**
1. Create ATTACK action with `targetCharacterId = "char-456"`
2. Validate action
3. Verify acceptance

**Assertions:**
- `result.isValid() == true`

#### `testAttackMissingTarget()`
**Validates:** ATTACK missing `targetCharacterId` rejected

**Steps:**
1. Create ATTACK action without target
2. Validate action
3. Verify rejection

**Assertions:**
- `result.getReason()` contains "Missing parameter: targetCharacterId"

### TRADE Action Tests (2 tests)

#### `testValidTradeAction()`
**Validates:** TRADE action with all parameters accepted

**Steps:**
1. Create TRADE action with `targetPlayerId`, `offeredItems`, `requestedItems`
2. Validate action
3. Verify acceptance

**Assertions:**
- `result.isValid() == true`

#### `testTradeMissingParameters()`
**Validates:** TRADE missing any parameter rejected

**Steps:**
1. Create TRADE action with only `targetPlayerId`
2. Validate action
3. Verify rejection

**Assertions:**
- `result.getReason()` contains "Missing parameter"

### BUILD Action Tests (2 tests)

#### `testValidBuildAction()`
**Validates:** BUILD action with valid parameters accepted

**Steps:**
1. Create BUILD action with `structureType`, `roomCategory`, `location`
2. Validate action
3. Verify acceptance

**Assertions:**
- `result.isValid() == true`

#### `testBuildMissingStructureType()`
**Validates:** BUILD missing `structureType` rejected

**Steps:**
1. Create BUILD action with only `roomCategory` and `location`
2. Validate action
3. Verify rejection

**Assertions:**
- `result.getReason()` contains "Missing parameter: structureType"

### CHAT Action Tests (4 tests)

#### `testValidChatAction()`
**Validates:** CHAT action with valid message accepted

**Steps:**
1. Create CHAT action with `message = "Hello, world!"`
2. Validate action
3. Verify acceptance

**Assertions:**
- `result.isValid() == true`

#### `testChatActionWithEmptyMessage()`
**Validates:** CHAT with empty message rejected

**Steps:**
1. Create CHAT action with `message = ""`
2. Validate action
3. Verify rejection

**Assertions:**
- `result.getReason()` contains "Message cannot be empty"

#### `testChatActionWithTooLongMessage()`
**Validates:** CHAT with message >500 chars rejected

**Steps:**
1. Create CHAT action with 520-character message
2. Validate action
3. Verify rejection

**Assertions:**
- `result.getReason()` contains "Message too long (max 500 characters)"

#### `testChatDoesNotRequireCharacter()`
**Validates:** CHAT action allowed without character (special case)

**Steps:**
1. Create player with `characterId = null`
2. Create CHAT action
3. Validate action
4. Verify acceptance

**Assertions:**
- `result.isValid() == true`

**Rationale:** Allow lobby chat before character creation

### USE_ITEM Action Tests (2 tests)

#### `testValidUseItemAction()`
**Validates:** USE_ITEM action with itemId accepted

**Steps:**
1. Create USE_ITEM action with `itemId = "item-789"`
2. Validate action
3. Verify acceptance

**Assertions:**
- `result.isValid() == true`

#### `testUseItemMissingItemId()`
**Validates:** USE_ITEM missing `itemId` rejected

**Steps:**
1. Create USE_ITEM action without `itemId`
2. Validate action
3. Verify rejection

**Assertions:**
- `result.getReason()` contains "Missing parameter: itemId"

### DROP_ITEM Action Tests (2 tests)

#### `testValidDropItemAction()`
**Validates:** DROP_ITEM action with itemId accepted

**Steps:**
1. Create DROP_ITEM action with `itemId = "item-101"`
2. Validate action
3. Verify acceptance

**Assertions:**
- `result.isValid() == true`

#### `testDropItemMissingItemId()`
**Validates:** DROP_ITEM missing `itemId` rejected

**Steps:**
1. Create DROP_ITEM action without `itemId`
2. Validate action
3. Verify rejection

**Assertions:**
- `result.getReason()` contains "Missing parameter: itemId"

### PICK_UP_ITEM Action Tests (2 tests)

#### `testValidPickUpItemAction()`
**Validates:** PICK_UP_ITEM action with itemId accepted

**Steps:**
1. Create PICK_UP_ITEM action with `itemId = "item-202"`
2. Validate action
3. Verify acceptance

**Assertions:**
- `result.isValid() == true`

#### `testPickUpItemMissingItemId()`
**Validates:** PICK_UP_ITEM missing `itemId` rejected

**Steps:**
1. Create PICK_UP_ITEM action without `itemId`
2. Validate action
3. Verify rejection

**Assertions:**
- `result.getReason()` contains "Missing parameter: itemId"

### TRANSFER_OWNERSHIP Action Tests (2 tests)

#### `testValidTransferOwnershipAction()`
**Validates:** TRANSFER_OWNERSHIP with all parameters accepted

**Steps:**
1. Create TRANSFER_OWNERSHIP action with `structureId`, `newOwnerId`, `transferType`
2. Validate action
3. Verify acceptance

**Assertions:**
- `result.isValid() == true`

#### `testTransferOwnershipMissingParameters()`
**Validates:** TRANSFER_OWNERSHIP missing any parameter rejected

**Steps:**
1. Create TRANSFER_OWNERSHIP with only `structureId`
2. Validate action
3. Verify rejection

**Assertions:**
- `result.getReason()` contains "Missing parameter"

### JOIN_CLAN Action Tests (2 tests)

#### `testValidJoinClanAction()`
**Validates:** JOIN_CLAN action with clanId accepted

**Steps:**
1. Create JOIN_CLAN action with `clanId = "clan-303"`
2. Validate action
3. Verify acceptance

**Assertions:**
- `result.isValid() == true`

#### `testJoinClanMissingClanId()`
**Validates:** JOIN_CLAN missing `clanId` rejected

**Steps:**
1. Create JOIN_CLAN action without `clanId`
2. Validate action
3. Verify rejection

**Assertions:**
- `result.getReason()` contains "Missing parameter: clanId"

### LEAVE_CLAN Action Tests (1 test)

#### `testValidLeaveClanAction()`
**Validates:** LEAVE_CLAN action accepted (no parameters required)

**Steps:**
1. Create LEAVE_CLAN action with no parameters
2. Validate action
3. Verify acceptance

**Assertions:**
- `result.isValid() == true`

## Test Utilities

### Helper Methods
```java
private Player createAuthenticatedPlayer(String playerId, String characterId) {
    Player player = new Player("testuser");
    player.setSession(new PlayerSession(playerId, "jwt-token", Instant.now().plus(24, ChronoUnit.HOURS)));
    player.setCharacterId(characterId);
    return player;
}

private PlayerAction createAction(PlayerAction.ActionType type, Map<String, Object> params) {
    PlayerAction.Builder builder = new PlayerAction.Builder()
        .playerId("player-1")
        .type(type);
    
    params.forEach(builder::parameter);
    
    return builder.build();
}
```

### Test Fixtures
```java
@BeforeEach
public void setup() {
    validator = new ActionValidator();
    player = createAuthenticatedPlayer("player-1", "char-1");
}
```

## Security Test Matrix

| Attack Vector | Test Case | Validated |
|---------------|-----------|-----------|
| Player Impersonation | `testActionBelongsToDifferentPlayer()` | ✅ |
| Unauthenticated Access | `testUnauthenticatedPlayerRejected()` | ✅ |
| Replay Attack | `testExpiredActionRejected()` | ✅ |
| Parameter Injection | All "MissingParameter" tests | ✅ |
| Message Spam | `testChatActionWithTooLongMessage()` | ✅ |
| Negative Quantity Exploit | `testCraftNegativeQuantity()` | ✅ |

## Known Limitations

### MVP Validation Gaps
- **No Game State Checks:** Validation assumes all referenced entities exist (no DB queries)
- **No Permission Checks:** Assumes player has permission for all actions
- **No Resource Checks:** Assumes player has sufficient materials, currency, etc.
- **No Rate Limiting:** No flood protection (future: max 10 actions/second)

### Future Validation Enhancements
1. **Deep Validation:** Query actual game state
   ```java
   ResourceNode node = resourceNodeRepository.findById(nodeId);
   if (node.getCurrentQuantity() == 0) {
       return ValidationResult.reject("Resource depleted");
   }
   ```

2. **Permission Checks:** Integrate with fine-grained permissions
   ```java
   if (!permissionSystem.hasPermission(player, "HARVEST", node)) {
       return ValidationResult.reject("Insufficient permissions");
   }
   ```

3. **Business Rules:** Validate economic fairness
   ```java
   if (trade.getOfferedValue() < trade.getRequestedValue() * 0.5) {
       return ValidationResult.reject("Unfair trade (value imbalance > 50%)");
   }
   ```

## Related Documentation

- `ActionValidator.md` - Validation implementation
- `PlayerAction.md` - Action data model
- `ServerTest.md` - Server integration tests
- `docs/testing_plan.md` - Overall testing strategy
- `archive/PHASE_1.9_SUMMARY.md` - Phase 1.9 validation tests
