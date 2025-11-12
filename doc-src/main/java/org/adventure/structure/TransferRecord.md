# TransferRecord

**Package:** `org.adventure.structure`  
**Type:** Immutable Data Class  
**Builder:** `TransferRecord.Builder`

---

## Overview

`TransferRecord` is an immutable audit record documenting a single ownership transfer. It captures all details of the transfer (who, what, when, why) with flexible metadata storage, serving as tamper-proof evidence for disputes and historical analysis.

---

## Class Structure

```java
public final class TransferRecord {
    private final String type = "structure/TransferRecord";
    private final int schemaVersion = 1;
    
    private final String structureId;
    private final String fromOwnerId;
    private final String fromOwnerType;
    private final String toOwnerId;
    private final String toOwnerType;
    private final TransferType transferType;
    private final long transferredAtTick;
    private final Map<String, Object> metadata;
}
```

---

## Fields

### Core Identification
- **`type`** — Constant: `"structure/TransferRecord"`
- **`schemaVersion`** — Current: `1`

### Transfer Participants
- **`structureId`** — ID of structure transferred
- **`fromOwnerId`** — Previous owner's ID
- **`fromOwnerType`** — Previous owner's type (OwnerType enum string)
- **`toOwnerId`** — New owner's ID
- **`toOwnerType`** — New owner's type (OwnerType enum string)

### Transfer Details
- **`transferType`** — Type of transfer (TransferType enum)
- **`transferredAtTick`** — Game tick when transfer occurred

### Flexible Data
- **`metadata`** — Map of transfer-specific data (price, deceased owner, conquest details, etc.)

---

## Builder Pattern

### Constructor
**Private:** Use builder to create instances.

### Builder Usage
```java
TransferRecord record = new TransferRecord.Builder()
    .structureId("struct-001")
    .fromOwnerId("char-001")
    .fromOwnerType(OwnerType.CHARACTER)
    .toOwnerId("char-002")
    .toOwnerType(OwnerType.CHARACTER)
    .transferType(TransferType.SALE)
    .transferredAtTick(1000)
    .metadata(saleMetadata)
    .build();
```

### Validation Rules
- `structureId` — Cannot be null or empty
- `fromOwnerId` — Cannot be null or empty
- `fromOwnerType` — Cannot be null
- `toOwnerId` — Cannot be null or empty
- `toOwnerType` — Cannot be null
- `transferType` — Cannot be null
- `transferredAtTick` — Must be >= 0
- `metadata` — Optional (defaults to empty map)

---

## Metadata Examples

### VOLUNTARY Transfer
```json
{
  "reason": "Birthday gift",
  "witness": "char-witness-001",
  "ceremony": true
}
```

### SALE Transfer
```json
{
  "price": 5000.0,
  "currency": "gold",
  "escrow": true,
  "buyer": "char-002",
  "seller": "char-001"
}
```

### SUCCESSION_INHERITANCE Transfer
```json
{
  "deceasedOwner": "char-001",
  "deceasedOwnerType": "CHARACTER",
  "relationship": "parent-child",
  "bloodline": "Smith Family",
  "deathTick": 950
}
```

### SUCCESSION_WILL Transfer
```json
{
  "deceasedOwner": "char-001",
  "deceasedOwnerType": "CHARACTER",
  "willDocument": "will-001",
  "witnessedBy": ["char-002", "char-003"],
  "notary": "char-notary-001"
}
```

### SUCCESSION_HEIR Transfer
```json
{
  "deceasedOwner": "char-leader-001",
  "deceasedOwnerType": "CHARACTER",
  "clan": "clan-001",
  "heirDesignation": "Charter Article 5",
  "clanLeaderSince": 500
}
```

### CONQUEST Transfer
```json
{
  "conquestVictory": true,
  "defeatedOwner": "char-001",
  "defeatedOwnerType": "CHARACTER",
  "siegeDuration": 120,
  "battleId": "battle-001",
  "attackerClan": "clan-002"
}
```

### TAX_SEIZURE Transfer
```json
{
  "unpaidTaxes": 2500.0,
  "taxDelinquentSince": 5000,
  "gracePeriodExpired": true,
  "taxAuthority": "clan-royal-tax-office",
  "lastPaymentTick": 3000
}
```

### ABANDONED Transfer
```json
{
  "abandonedSince": 100000,
  "inactivityThreshold": 90,
  "lastLoginTick": 50000,
  "revertedTo": "region-authority",
  "condition": "disrepair"
}
```

---

## Methods

### Getters
- `String getType()` — Returns `"structure/TransferRecord"`
- `int getSchemaVersion()` — Returns `1`
- `String getStructureId()`
- `String getFromOwnerId()`
- `String getFromOwnerType()`
- `String getToOwnerId()`
- `String getToOwnerType()`
- `TransferType getTransferType()`
- `long getTransferredAtTick()`
- `Map<String, Object> getMetadata()` — Returns unmodifiable map

### Defensive Copies
**Metadata:** Returned as unmodifiable map to prevent external mutation.

---

## Equality & Hashing

### equals()
**Comparison Fields:**
- All fields except `type` and `schemaVersion`

**Logic:**
- Two records are equal if all transfer details match (structure, owners, type, tick, metadata)

### hashCode()
**Hash Fields:**
- Same as `equals()` (structureId, owners, transferType, tick, metadata)

**Usage:** Enables use in `HashSet`, `HashMap`

---

## JSON Schema

### Example JSON Serialization
```json
{
  "type": "structure/TransferRecord",
  "schemaVersion": 1,
  "structureId": "struct-001",
  "fromOwnerId": "char-001",
  "fromOwnerType": "CHARACTER",
  "toOwnerId": "char-002",
  "toOwnerType": "CHARACTER",
  "transferType": "SALE",
  "transferredAtTick": 1000,
  "metadata": {
    "price": 5000.0,
    "currency": "gold"
  }
}
```

### Jackson Annotations
- Uses default Jackson serialization (no custom annotations needed)
- Metadata serialized as nested JSON object

---

## Design Decisions

### 1. Why Immutable?
**Decision:** All fields `final`, no setters.

**Rationale:**
- Audit trail integrity (tamper-proof)
- Thread-safe (can be shared across systems)
- Blockchain-like immutability

**Alternative:** Mutable records would allow corrections but sacrifice integrity.

---

### 2. Why Store Owner Type as String?
**Decision:** `fromOwnerType` and `toOwnerType` stored as strings (enum names).

**Rationale:**
- JSON serialization simplicity
- Schema evolution (new owner types don't break old records)
- Human-readable audit logs

**Alternative:** Store as enum (type-safe but harder to serialize).

---

### 3. Why Flexible Metadata Map?
**Decision:** `Map<String, Object>` instead of type-specific fields.

**Rationale:**
- Each transfer type has unique data (price, deceased owner, etc.)
- Extensible (can add new metadata without schema changes)
- Avoids 8 separate record classes

**Trade-offs:**
- Less type safety (metadata keys are strings)
- Need to document expected keys per transfer type

---

### 4. Why No Transfer ID?
**Decision:** No unique identifier field (e.g., `transferId`).

**Rationale:**
- Composite key: `(structureId, transferredAtTick)` is unique enough for MVP
- Can add `transferId` in future if external systems need it

**Future:** Add UUID if records stored in separate database.

---

## Usage Examples

### Create Transfer Record (Sale)
```java
Map<String, Object> metadata = new HashMap<>();
metadata.put("price", 5000.0);
metadata.put("currency", "gold");

TransferRecord record = new TransferRecord.Builder()
    .structureId("struct-001")
    .fromOwnerId("char-001")
    .fromOwnerType(OwnerType.CHARACTER)
    .toOwnerId("char-002")
    .toOwnerType(OwnerType.CHARACTER)
    .transferType(TransferType.SALE)
    .transferredAtTick(1000)
    .metadata(metadata)
    .build();
```

### Create Transfer Record (Succession)
```java
Map<String, Object> metadata = new HashMap<>();
metadata.put("deceasedOwner", "char-001");
metadata.put("deceasedOwnerType", "CHARACTER");
metadata.put("relationship", "parent-child");

TransferRecord record = new TransferRecord.Builder()
    .structureId("struct-001")
    .fromOwnerId("char-001")
    .fromOwnerType(OwnerType.CHARACTER)
    .toOwnerId("char-heir")
    .toOwnerType(OwnerType.CHARACTER)
    .transferType(TransferType.SUCCESSION_INHERITANCE)
    .transferredAtTick(1000)
    .metadata(metadata)
    .build();
```

### Query Metadata
```java
if (record.getTransferType() == TransferType.SALE) {
    Double price = (Double) record.getMetadata().get("price");
    String currency = (String) record.getMetadata().get("currency");
    
    System.out.println("Sale price: " + price + " " + currency);
}
```

### Equality Check
```java
TransferRecord record1 = /* ... */;
TransferRecord record2 = /* ... */;

if (record1.equals(record2)) {
    // Same transfer (all fields match)
}
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
- **Equality:** O(k) where k = metadata key count
- **Hashing:** O(k) where k = metadata key count
- **Memory:** ~200 bytes per record (depends on metadata size)

### Optimization Tips
- Reuse metadata maps when possible (avoid allocations)
- Archive old records to database (remove from memory)
- Index records by structure ID for fast lookup

---

## Integration Points

### With OwnershipTransferSystem
- Created by all transfer methods (`executeVoluntaryTransfer`, `executeSale`, etc.)
- Returned to caller for audit/logging

### With Structure Class
- Could store transfer history in `Structure.transferHistory` (future)
- Integration deferred to Phase 1.5.2

### With Event System (Future)
- Transfer records generate story events
- Metadata included in event details

---

## Error Handling

### Builder Validation Errors
```java
try {
    TransferRecord record = new TransferRecord.Builder()
        .structureId(null)  // Invalid
        .build();
} catch (IllegalArgumentException e) {
    // Handle validation error
}
```

### Common Errors
- `structureId` is null or empty
- `transferredAtTick` is negative
- Missing required builder fields

---

## Testing

**Test Class:** `OwnershipTransferTest.java`  
**Test Methods:**
- `testTransferRecord_BuilderValidation()` — Tests builder validation
- `testTransferRecord_Equality()` — Tests equals/hashCode
- `testTransferRecord_Metadata()` — Tests metadata storage

**Coverage:** 95%+ (all fields, builder, equality)

---

## Future Enhancements

### Phase 1.5.2 (Integration)
- [ ] Store transfer history in `Structure.transferHistory`
- [ ] Add `getTransferHistory(structureId)` API in OwnershipTransferSystem
- [ ] Persist records to JSON file

### Phase 2.x (Advanced)
- [ ] Add `transferId` (UUID) for external system integration
- [ ] Add digital signatures (cryptographic proof of transfer)
- [ ] Add witness signatures (multi-party approval)
- [ ] Add transfer cost calculations (notary fees, taxes)
- [ ] Add transfer insurance tracking
- [ ] Add escrow status (pending, completed, cancelled)
- [ ] Add transfer rollback support (undo transfers)
- [ ] Add bulk transfer records (clan merger, kingdom acquisition)

---

## Related Classes

- **OwnershipTransferSystem:** Creates transfer records
- **TransferType:** Enum stored in record
- **Structure:** Structure being transferred
- **OwnerType:** Type of owner (stored as string)

---

## References

- Design: `docs/structures_ownership.md` → Transfer Audit Trail
- Specs: `docs/specs_summary.md` → Transfer Metadata Defaults
- Summary: `archive/PHASE_1.5.1_SUMMARY.md`
- Tests: `doc-src/test/java/org/adventure/OwnershipTransferTest.md`
