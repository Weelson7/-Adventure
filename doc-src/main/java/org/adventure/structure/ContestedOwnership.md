# ContestedOwnership

**Package:** `org.adventure.structure`  
**Type:** Mutable Data Class  
**Builder:** `ContestedOwnership.Builder`  
**Default Expiry:** 7200 ticks (2 hours)

---

## Overview

`ContestedOwnership` tracks ownership disputes with evidence, expiry, and resolution outcomes. It enables legal challenges to structure ownership while preventing indefinite locks through automatic expiry. Only one active contest per structure is allowed.

---

## Class Structure

```java
public final class ContestedOwnership {
    private final String type = "structure/ContestedOwnership";
    private final int schemaVersion = 1;
    
    private final String structureId;
    private final String contestingPartyId;
    private final String contestingPartyType;
    private final String claimBasis;
    private final long contestedAtTick;
    private final long expiresAtTick;
    private final Map<String, Object> evidence;
    
    private boolean resolved;
    private Long resolvedAtTick;
    private String resolutionOutcome;
}
```

---

## Fields

### Core Identification
- **`type`** — Constant: `"structure/ContestedOwnership"`
- **`schemaVersion`** — Current: `1`

### Dispute Participants
- **`structureId`** — ID of contested structure
- **`contestingPartyId`** — ID of party filing dispute
- **`contestingPartyType`** — Type of contesting party (OwnerType enum string)

### Dispute Details
- **`claimBasis`** — Reason for dispute (human-readable string)
- **`contestedAtTick`** — Game tick when contest filed
- **`expiresAtTick`** — Game tick when contest auto-expires (default: contestedAtTick + 7200)
- **`evidence`** — Map of evidence (witnesses, documents, etc.)

### Resolution State (Mutable)
- **`resolved`** — Whether contest has been resolved (default: `false`)
- **`resolvedAtTick`** — Game tick when contest resolved (null until resolved)
- **`resolutionOutcome`** — Outcome string: "GRANTED", "DENIED", or "EXPIRED" (null until resolved)

---

## Resolution Outcomes

### GRANTED
**Meaning:** Contest accepted, ownership transferred to contesting party

**Set By:** `OwnershipTransferSystem.resolveContestedOwnershipInFavorOfContestant()`

**Side Effects:**
- Contest marked as resolved
- Ownership transferred to contesting party
- Transfer record created

---

### DENIED
**Meaning:** Contest rejected, ownership remains unchanged

**Set By:** `OwnershipTransferSystem.resolveContestedOwnershipInFavorOfOwner()`

**Side Effects:**
- Contest marked as resolved
- Ownership unchanged
- No transfer record created

---

### EXPIRED
**Meaning:** Contest expired due to timeout (auto-denied)

**Set By:** `OwnershipTransferSystem.processExpiredContests()`

**Side Effects:**
- Contest marked as resolved
- Ownership unchanged
- No transfer record created

---

## Builder Pattern

### Constructor
**Private:** Use builder to create instances.

### Builder Usage
```java
Map<String, Object> evidence = new HashMap<>();
evidence.put("witness", "char-witness-001");
evidence.put("deed", "deed-001");

ContestedOwnership contest = new ContestedOwnership.Builder()
    .structureId("struct-001")
    .contestingPartyId("char-002")
    .contestingPartyType(OwnerType.CHARACTER)
    .claimBasis("Valid ownership deed from 1995")
    .contestedAtTick(1000)
    .expiresAtTick(8200)  // Optional (defaults to contestedAtTick + 7200)
    .evidence(evidence)
    .build();
```

### Validation Rules
- `structureId` — Cannot be null or empty
- `contestingPartyId` — Cannot be null or empty
- `contestingPartyType` — Cannot be null
- `claimBasis` — Cannot be null or empty
- `contestedAtTick` — Must be >= 0
- `expiresAtTick` — Must be > contestedAtTick (defaults to contestedAtTick + 7200)
- `evidence` — Optional (defaults to empty map)

---

## Methods

### Getters (Public)
- `String getType()` — Returns `"structure/ContestedOwnership"`
- `int getSchemaVersion()` — Returns `1`
- `String getStructureId()`
- `String getContestingPartyId()`
- `String getContestingPartyType()`
- `String getClaimBasis()`
- `long getContestedAtTick()`
- `long getExpiresAtTick()`
- `Map<String, Object> getEvidence()` — Returns unmodifiable map
- `boolean isResolved()`
- `Long getResolvedAtTick()` — Nullable
- `String getResolutionOutcome()` — Nullable

### Status Checks
#### isActive(currentTick)
**Signature:** `public boolean isActive(long currentTick)`

**Returns:** `true` if contest is not resolved and not expired

**Usage:**
```java
if (contest.isActive(currentTick)) {
    // Block voluntary transfers
}
```

#### isExpired(currentTick)
**Signature:** `public boolean isExpired(long currentTick)`

**Returns:** `true` if current tick >= expiry tick

**Usage:**
```java
if (contest.isExpired(currentTick)) {
    // Auto-deny contest
    system.processExpiredContests(currentTick);
}
```

### Setters (Package-Private)
**Note:** Only callable by `OwnershipTransferSystem` (same package).

- `void setResolved(boolean resolved)` — Mark as resolved
- `void setResolvedAtTick(Long resolvedAtTick)` — Set resolution tick
- `void setResolutionOutcome(String outcome)` — Set outcome ("GRANTED", "DENIED", "EXPIRED")

**Rationale:** Prevents external mutation of resolution state.

---

## Evidence Examples

### Ownership Deed
```json
{
  "deed": "deed-001",
  "issuedBy": "region-authority",
  "issuedAtTick": 500,
  "validUntilTick": 10000
}
```

### Witness Testimony
```json
{
  "witness": "char-witness-001",
  "testimony": "I saw char-002 occupy this structure first",
  "witnessReputation": 85
}
```

### Historical Records
```json
{
  "historicalOwner": "char-002",
  "ownedFromTick": 500,
  "ownedToTick": 1000,
  "stolenBy": "char-001",
  "theftReport": "crime-001"
}
```

### Legal Documents
```json
{
  "contract": "contract-001",
  "signedBy": ["char-001", "char-002"],
  "signedAtTick": 500,
  "notary": "char-notary-001"
}
```

### Prior Ownership
```json
{
  "priorOwnerId": "char-002",
  "priorOwnershipStartTick": 500,
  "priorOwnershipEndTick": 1000,
  "transferType": "CONQUEST",
  "invalidReason": "Illegal conquest"
}
```

---

## Equality & Hashing

### equals()
**Comparison Fields:**
- All fields (structure, contesting party, claim basis, ticks, evidence, resolution state)

**Logic:**
- Two contests are equal if all fields match (including resolution state)

### hashCode()
**Hash Fields:**
- Same as `equals()` (structureId, contestingPartyId, claimBasis, ticks, etc.)

**Usage:** Enables use in `HashSet`, `HashMap`

---

## Design Decisions

### 1. Why Only One Active Contest Per Structure?
**Decision:** `OwnershipTransferSystem` enforces single active contest.

**Rationale:**
- Prevents competing claims from paralyzing structures
- Forces dispute resolution before new contests
- Simplifies logic (no contest priority system needed)

**Alternative:** Admin can resolve first contest, then file second.

---

### 2. Why 7200 Tick (2 Hour) Default Expiry?
**Decision:** Default expiry after 2 hours of game time.

**Rationale:**
- Long enough for investigation and resolution
- Short enough to prevent indefinite locks
- Admin can intervene for complex disputes

**Customization:** Builder accepts custom expiry tick.

---

### 3. Why Mutable Resolution State?
**Decision:** `resolved`, `resolvedAtTick`, `resolutionOutcome` are mutable.

**Rationale:**
- Contests start unresolved, then transition to resolved
- Immutability would require creating new object (inefficient)
- Package-private setters prevent external mutation

**Alternative:** Immutable with factory methods (more objects, less efficient).

---

### 4. Why Store Contesting Party Type as String?
**Decision:** `contestingPartyType` stored as string (enum name).

**Rationale:**
- JSON serialization simplicity
- Schema evolution (new owner types don't break old records)
- Human-readable audit logs

**Alternative:** Store as enum (type-safe but harder to serialize).

---

## Usage Examples

### File Contest
```java
Map<String, Object> evidence = new HashMap<>();
evidence.put("witness", "char-witness-001");
evidence.put("deed", "deed-001");

ContestedOwnership contest = new ContestedOwnership.Builder()
    .structureId("struct-001")
    .contestingPartyId("char-002")
    .contestingPartyType(OwnerType.CHARACTER)
    .claimBasis("Valid ownership deed from 1995")
    .contestedAtTick(1000)
    .evidence(evidence)
    .build();  // expiresAtTick defaults to 8200 (1000 + 7200)
```

### Check Active Status
```java
long currentTick = 5000;

if (contest.isActive(currentTick)) {
    // Contest still active, block voluntary transfers
    System.out.println("Contest active, transfer blocked");
}
```

### Check Expiry
```java
long currentTick = 9000;

if (contest.isExpired(currentTick)) {
    // Contest expired, auto-deny
    transferSystem.processExpiredContests(currentTick);
}
```

### Resolve Contest (Internal)
```java
// Only callable by OwnershipTransferSystem (package-private setters)

contest.setResolved(true);
contest.setResolvedAtTick(5000L);
contest.setResolutionOutcome("GRANTED");

// Now contest.isActive(5000) == false
```

---

## Persistence

### Storage Location
**Current:** Stored in-memory by `OwnershipTransferSystem`  
**Future:** Persist to JSON file or database

### JSON Schema Version
**Current:** `schemaVersion = 1`  
**Migration:** Increment version when adding/removing fields

### Backup Strategy
**Future:** Follow `Structure` pattern (atomic writes, rotation)

---

## Performance Considerations

- **Creation:** O(1) — simple object allocation
- **Status checks:** O(1) — boolean and tick comparisons
- **Equality:** O(k) where k = evidence key count
- **Hashing:** O(k) where k = evidence key count
- **Memory:** ~250 bytes per contest (depends on evidence size)

### Optimization Tips
- Archive resolved contests (remove from active map)
- Index contests by expiry tick for efficient batch processing
- Reuse evidence maps when possible

---

## Integration Points

### With OwnershipTransferSystem
- Created by `contestOwnership()`
- Resolved by `resolveContestedOwnershipInFavorOfContestant()` and `resolveContestedOwnershipInFavorOfOwner()`
- Expired by `processExpiredContests()`
- Queried by `hasActiveContest()`, `getActiveContest()`, `getAllContests()`

### With Structure Class
- Affects `Structure` transfer eligibility (blocks voluntary/sale transfers)

### With Event System (Future)
- Contest filing generates "dispute" event
- Contest resolution generates "verdict" event

---

## Error Handling

### Builder Validation Errors
```java
try {
    ContestedOwnership contest = new ContestedOwnership.Builder()
        .structureId("")  // Invalid (empty)
        .build();
} catch (IllegalArgumentException e) {
    // Handle validation error
}
```

### Common Errors
- `structureId` is null or empty
- `expiresAtTick` <= `contestedAtTick` (expiry before contest)
- `claimBasis` is null or empty

---

## Testing

**Test Class:** `OwnershipTransferTest.java`  
**Test Methods:**
- `testContestedOwnership_BuilderValidation()` — Tests builder validation
- `testContestedOwnership_IsActive()` — Tests active status logic
- `testContestedOwnership_IsExpired()` — Tests expiry logic
- `testContestedOwnership_Resolution()` — Tests resolution state transitions

**Coverage:** 95%+ (all fields, builder, status checks, resolution)

---

## Future Enhancements

### Phase 1.5.2 (Integration)
- [ ] Store contest history in `Structure.contestHistory`
- [ ] Add `getContestHistory(structureId)` API in OwnershipTransferSystem
- [ ] Persist contests to JSON file

### Phase 2.x (Advanced)
- [ ] Add `contestId` (UUID) for external system integration
- [ ] Add multi-party contests (multiple claimants)
- [ ] Add contest priority levels (urgent, standard, low)
- [ ] Add auto-escalation to admin after expiry
- [ ] Add evidence scoring/weighting system
- [ ] Add witness credibility tracking
- [ ] Add appeal mechanism (re-contest after denial)
- [ ] Add arbitration/mediation mode (neutral third party)
- [ ] Add contest insurance (deposit forfeited if frivolous)
- [ ] Add legal representative support (lawyer NPCs)

---

## Related Classes

- **OwnershipTransferSystem:** Manages contests (creation, resolution, expiry)
- **Structure:** Structure being contested
- **OwnerType:** Type of contesting party (stored as string)
- **TransferRecord:** Created when contest granted

---

## References

- Design: `docs/structures_ownership.md` → Contested Ownership Rules
- Specs: `docs/specs_summary.md` → Contested Expiry Defaults
- Summary: `archive/PHASE_1.5.1_SUMMARY.md`
- Tests: `doc-src/test/java/org/adventure/OwnershipTransferTest.md`
