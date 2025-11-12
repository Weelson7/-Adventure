# OwnershipTransferSystem

**Package:** `org.adventure.structure`  
**Type:** Service Class (Stateful)  
**Default Parameters:** 7200-tick contested expiry (2 hours)

---

## Overview

`OwnershipTransferSystem` is the central orchestrator for all structure ownership changes. It manages voluntary transfers, sales, succession, conquest, and contested ownership disputes with validation, audit logging, and expiry management.

---

## Key Responsibilities

1. **Transfer Execution:** Execute all 8 transfer types (voluntary, sale, succession, conquest, etc.)
2. **Validation:** Enforce transfer rules (consent, payment, contested ownership blocks)
3. **Audit Trail:** Create immutable `TransferRecord` for every ownership change
4. **Dispute Management:** Track contested ownership with evidence and expiry
5. **Resolution:** Resolve disputes in favor of contestant or owner
6. **Expiry Processing:** Auto-deny expired contests after 7200 ticks (default)

---

## Class Structure

```java
public final class OwnershipTransferSystem {
    private final int contestedExpiryTicks;
    private final Map<String, List<ContestedOwnership>> activeContests;
}
```

---

## Default Parameters

- **Contested Expiry:** 7200 ticks (2 hours @ 1 tick/second)
- From `specs_summary.md`: `contestedExpiryTicks = 7200`

---

## Transfer Types Supported

### 1. Voluntary Transfer
**Method:** `executeVoluntaryTransfer(structure, toOwnerId, toOwnerType, tick)`  
**Requirements:** Consent from current owner, no active contested ownership  
**Use Cases:** Gifts, clan membership transfers, friendly handoffs

### 2. Sale Transfer
**Method:** `executeSale(structure, toOwnerId, toOwnerType, price, tick)`  
**Requirements:** Consent, payment, no active contested ownership  
**Metadata:** `{"price": 5000.0, "currency": "gold"}`  
**Use Cases:** Real estate transactions, property trading

### 3. Succession Transfers (3 types)
**Methods:** `executeSuccession(structure, heirId, heirType, successionType, tick)`

- **SUCCESSION_INHERITANCE:** Family bloodline (automatic on death)
- **SUCCESSION_WILL:** Written will designation (automatic on death)
- **SUCCESSION_HEIR:** Clan heir designation (automatic on death)

**Requirements:** Valid succession type, ignores contested ownership (death overrides disputes)  
**Metadata:** `{"deceasedOwner": "char-001", "deceasedOwnerType": "CHARACTER"}`

### 4. Conquest Transfer
**Method:** `executeConquest(structure, conquerorId, conquerorType, tick)`  
**Requirements:** None (forced transfer)  
**Behavior:** Clears all contested ownership immediately  
**Metadata:** `{"conquestVictory": true, "defeatedOwner": "char-001"}`  
**Use Cases:** Military takeovers, siege victories

---

## Contested Ownership System

### Filing a Contest
**Method:** `contestOwnership(structure, contestingPartyId, contestingPartyType, claimBasis, evidence, tick)`

**Parameters:**
- `claimBasis` — Reason for dispute (e.g., "Prior ownership with deed")
- `evidence` — Map of evidence (witnesses, documents, etc.)

**Returns:** `ContestedOwnership` object with 7200-tick expiry

**Rules:**
- Only one active contest per structure at a time
- Blocks voluntary transfers and sales
- Does NOT block succession or conquest

### Resolving Contests

#### In Favor of Contestant
**Method:** `resolveContestedOwnershipInFavorOfContestant(structure, contest, tick)`

**Behavior:**
- Marks contest as resolved with outcome "GRANTED"
- Transfers ownership to contesting party
- Creates transfer record with metadata

#### In Favor of Owner
**Method:** `resolveContestedOwnershipInFavorOfOwner(contest, tick)`

**Behavior:**
- Marks contest as resolved with outcome "DENIED"
- Ownership remains unchanged

### Expiry Processing
**Method:** `processExpiredContests(tick)`

**Behavior:**
- Auto-marks expired contests as resolved with outcome "EXPIRED"
- Returns list of structure IDs with expired contests
- Allows transfers after expiry

---

## Validation Rules

### Voluntary Transfers & Sales
- `toOwnerId` cannot be null or empty
- `toOwnerType` cannot be null
- Structure cannot have active contested ownership
- Sales require non-negative price

### Succession
- Transfer type must be a succession type (INHERITANCE, WILL, or HEIR)
- Ignores contested ownership (death overrides disputes)

### Conquest
- No validation (forced transfer)
- Clears contested ownership immediately

---

## Usage Examples

### Voluntary Transfer
```java
OwnershipTransferSystem transferSystem = new OwnershipTransferSystem();

TransferRecord record = transferSystem.executeVoluntaryTransfer(
    structure,
    "char-002",
    OwnerType.CHARACTER,
    1000
);

// Structure ownership changed
assertEquals("char-002", structure.getOwnerId());
```

### Sale with Payment
```java
TransferRecord record = transferSystem.executeSale(
    structure,
    "char-002",
    OwnerType.CHARACTER,
    5000.0,  // price
    1000     // tick
);

// Metadata includes price
assertEquals(5000.0, record.getMetadata().get("price"));
assertEquals("gold", record.getMetadata().get("currency"));
```

### Succession (Inheritance)
```java
// Owner dies, structure passes to heir
TransferRecord record = transferSystem.executeSuccession(
    structure,
    "char-heir",
    OwnerType.CHARACTER,
    TransferType.SUCCESSION_INHERITANCE,
    1000
);

// Works even with active contested ownership
assertEquals("char-heir", structure.getOwnerId());
```

### Contest Ownership
```java
Map<String, Object> evidence = new HashMap<>();
evidence.put("witness", "char-witness-001");
evidence.put("deed", "deed-001");

ContestedOwnership contest = transferSystem.contestOwnership(
    structure,
    "char-002",
    OwnerType.CHARACTER,
    "Valid ownership deed from 1995",
    evidence,
    1000
);

// Contest expires at 1000 + 7200 = 8200
assertEquals(8200, contest.getExpiresAtTick());
```

### Resolve Contest
```java
// In favor of contestant (transfer ownership)
TransferRecord record = transferSystem.resolveContestedOwnershipInFavorOfContestant(
    structure,
    contest,
    2000
);

assertEquals("char-002", structure.getOwnerId());
assertEquals("GRANTED", contest.getResolutionOutcome());

// OR in favor of current owner (no transfer)
transferSystem.resolveContestedOwnershipInFavorOfOwner(contest, 2000);
assertEquals("DENIED", contest.getResolutionOutcome());
```

### Process Expired Contests
```java
// Move to expiry time
List<String> expired = transferSystem.processExpiredContests(8200);

// Expired contests auto-denied
for (String structureId : expired) {
    // Contest outcome is "EXPIRED"
    // Structure can now be transferred
}
```

---

## Query Methods

### Check Active Contest
```java
boolean hasContest = transferSystem.hasActiveContest("struct-001", currentTick);

if (hasContest) {
    ContestedOwnership contest = transferSystem.getActiveContest("struct-001", currentTick);
    // Handle active dispute
}
```

### Get All Contests (History)
```java
List<ContestedOwnership> allContests = transferSystem.getAllContests("struct-001");
// Returns active, resolved, and expired contests
```

### Clear Contests
```java
// Used when structure destroyed or forcibly transferred
transferSystem.clearContestedOwnership("struct-001");
```

---

## Design Decisions

### 1. Why Single Active Contest Per Structure?
**Decision:** Only one active contest allowed at a time.

**Rationale:**
- Prevents competing claims from paralyzing structures
- Forces dispute resolution before new contests
- Simplifies logic (no contest priority system needed)

**Alternative:** Admin can resolve first contest, then file second.

---

### 2. Why Succession Ignores Contested Ownership?
**Decision:** Death-based transfers (inheritance, will, heir) bypass contest blocks.

**Rationale:**
- Death is non-negotiable (cannot be contested)
- Legal disputes freeze on death in many real-world systems
- Prevents contests from blocking legitimate succession

**Gameplay:** Contested ownership resolves with new owner or expires.

---

### 3. Why Conquest Clears Contested Ownership?
**Decision:** Military victory clears all disputes immediately.

**Rationale:**
- "Might makes right" gameplay mechanic
- Wartime invalidates peacetime legal claims
- Prevents post-conquest legal challenges

**Gameplay:** Losers can re-contest after conquest, but start from scratch.

---

### 4. Why 7200 Tick (2 Hour) Expiry?
**Decision:** Default contest expiry after 2 hours of game time.

**Rationale:**
- Long enough for investigation and resolution
- Short enough to prevent indefinite locks
- Admin can intervene for complex disputes

**Customization:** Constructor accepts custom expiry ticks.

---

### 5. Why Immutable Transfer Records?
**Decision:** Transfer records cannot be modified after creation.

**Rationale:**
- Audit trail integrity (tamper-proof history)
- Legal evidence for future disputes
- Blockchain-like immutability

**Alternative:** External audit log service if needed.

---

## Performance Considerations

- **Transfer execution:** O(1) — structure update + record creation
- **Contest validation:** O(1) — HashMap lookup
- **Expiry processing:** O(n) — iterates all active contests
- **Memory:** O(c) where c = number of contests per structure (typically 1-5)

### Optimization Tips
- Lazy expiry processing (only on explicit call)
- Periodic cleanup of resolved contests (archive to database)
- Index contests by expiry time for efficient batch processing

---

## Integration Points

### With Structure Class
- Calls `structure.transferOwnership(newOwnerId, newOwnerType, tick)`
- Could store transfer history in `Structure` (future enhancement)

### With TaxationSystem
- Tax seizure should call `executeConquest()` or add TAX_SEIZURE support
- Integration deferred to Phase 1.5.2 or Phase 2

### With Event System (Future)
- Transfer events trigger story progression
- Contested ownership generates "dispute" events

### With Societies & Clans (Phase 1.6)
- Clan heir designation logic
- Clan merger transfers (multiple structures)

---

## Error Handling

### Common Exceptions
- **IllegalArgumentException:** Invalid parameters (null owner, negative price, etc.)
- **IllegalStateException:** Invalid operation (contest already active, already resolved, etc.)

### Best Practices
```java
try {
    transferSystem.executeVoluntaryTransfer(structure, newOwner, type, tick);
} catch (IllegalStateException e) {
    // Check for active contested ownership
    ContestedOwnership contest = transferSystem.getActiveContest(structureId, tick);
    // Handle dispute resolution before retry
}
```

---

## Testing

**Test Class:** `OwnershipTransferTest.java`  
**Test Count:** 33 tests  
**Coverage:** 85%+

### Test Categories
- Transfer types (6 tests)
- Transfer type helpers (4 tests)
- Validation (4 tests)
- Contested ownership (12 tests)
- TransferRecord (3 tests)
- ContestedOwnership (3 tests)

---

## Future Enhancements

### Phase 1.5.2 (Integration)
- [ ] Store transfer history in `Structure.transferHistory`
- [ ] Integrate with `TaxationSystem` for auto-seizure
- [ ] Add `getTransferHistory(structureId)` API

### Phase 2.x (Advanced)
- [ ] Family tree integration for SUCCESSION_INHERITANCE
- [ ] Will document parsing for SUCCESSION_WILL
- [ ] Fraud detection (auto-contest suspicious transfers)
- [ ] Multi-party transfers (clan mergers, acquisitions)
- [ ] Transfer insurance and escrow
- [ ] Ownership share fractions (co-ownership)
- [ ] Transfer cost calculations (notary fees, taxes)

---

## Related Classes

- **TransferType:** Enumeration of transfer mechanisms
- **TransferRecord:** Immutable audit record
- **ContestedOwnership:** Dispute tracking
- **Structure:** Structure being transferred
- **OwnerType:** Type of owner (Character, Clan, Society, etc.)

---

## References

- Design: `docs/structures_ownership.md` → Ownership Transfer Rules
- Specs: `docs/specs_summary.md` → Contested Expiry Defaults
- Summary: `archive/PHASE_1.5.1_SUMMARY.md`
- Tests: `doc-src/test/java/org/adventure/OwnershipTransferTest.md`
