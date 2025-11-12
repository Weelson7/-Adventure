# TransferType

**Package:** `org.adventure.structure`  
**Type:** Enum  
**Values:** 8 transfer types

---

## Overview

`TransferType` enumerates all supported ownership transfer mechanisms. Each type defines distinct rules for consent, payment, and forced transfers, enabling flexible ownership dynamics across voluntary trades, succession, military conquest, and administrative actions.

---

## Transfer Types

### 1. VOLUNTARY
**Description:** Consensual transfer without payment (gift, handoff)

**Characteristics:**
- Requires consent from both parties
- No payment required
- Can be blocked by contested ownership

**Use Cases:**
- Family member gifts structure to child
- Clan member donates building to clan
- Friends transfer property

**Example Metadata:**
```json
{
  "reason": "Birthday gift",
  "witness": "char-witness-001"
}
```

---

### 2. SALE
**Description:** Consensual transfer with payment

**Characteristics:**
- Requires consent from both parties
- Requires payment (price > 0)
- Can be blocked by contested ownership

**Use Cases:**
- Real estate transactions
- Property trading between players
- NPC merchant sales

**Example Metadata:**
```json
{
  "price": 5000.0,
  "currency": "gold",
  "escrow": true,
  "buyer": "char-002"
}
```

---

### 3. SUCCESSION_INHERITANCE
**Description:** Automatic transfer to family heir on death

**Characteristics:**
- No consent required (automatic on death)
- No payment required
- Bypasses contested ownership
- Family bloodline based

**Use Cases:**
- Parent dies, structure passes to eldest child
- Automatic generational transfer
- Default succession if no will exists

**Example Metadata:**
```json
{
  "deceasedOwner": "char-001",
  "deceasedOwnerType": "CHARACTER",
  "relationship": "parent-child",
  "bloodline": "Smith Family"
}
```

---

### 4. SUCCESSION_WILL
**Description:** Transfer to designated heir per written will

**Characteristics:**
- No consent required (automatic on death)
- No payment required
- Bypasses contested ownership
- Written will designation

**Use Cases:**
- Character specifies heir in will
- Override bloodline default
- Charity bequests to NPCs/clans

**Example Metadata:**
```json
{
  "deceasedOwner": "char-001",
  "deceasedOwnerType": "CHARACTER",
  "willDocument": "will-001",
  "witnessedBy": ["char-002", "char-003"]
}
```

---

### 5. SUCCESSION_HEIR
**Description:** Transfer to designated clan heir on clan leader death

**Characteristics:**
- No consent required (automatic on death)
- No payment required
- Bypasses contested ownership
- Clan heir designation

**Use Cases:**
- Clan leader dies, structure passes to designated heir
- Clan charter-based succession
- Regent transfers

**Example Metadata:**
```json
{
  "deceasedOwner": "char-leader-001",
  "deceasedOwnerType": "CHARACTER",
  "clan": "clan-001",
  "heirDesignation": "Charter Article 5"
}
```

---

### 6. CONQUEST
**Description:** Forced military takeover

**Characteristics:**
- No consent required (forced)
- No payment required
- Clears contested ownership immediately
- Wartime mechanism

**Use Cases:**
- Siege victory transfers castle to victor
- Military occupation of structures
- Kingdom expansion via conquest

**Example Metadata:**
```json
{
  "conquestVictory": true,
  "defeatedOwner": "char-001",
  "siegeDuration": 120,
  "battleId": "battle-001"
}
```

---

### 7. TAX_SEIZURE
**Description:** Administrative seizure for unpaid taxes

**Characteristics:**
- No consent required (forced)
- No payment required (debt-based)
- Can be blocked by contested ownership (admin must resolve)

**Use Cases:**
- Tax delinquency after grace period
- Government repossession
- Unpaid property taxes

**Example Metadata:**
```json
{
  "unpaidTaxes": 2500.0,
  "taxDelinquentSince": 5000,
  "gracePeriodExpired": true,
  "taxAuthority": "clan-royal-tax-office"
}
```

---

### 8. ABANDONED
**Description:** Transfer to governing body after abandonment

**Characteristics:**
- No consent required (forced)
- No payment required
- Triggered by inactivity threshold
- Reverts to regional authority

**Use Cases:**
- Player inactive for 90 days
- Structure falls into disrepair
- Government reclaims abandoned property

**Example Metadata:**
```json
{
  "abandonedSince": 100000,
  "inactivityThreshold": 90,
  "lastLoginTick": 50000,
  "revertedTo": "region-authority"
}
```

---

## Helper Methods

### requiresPayment()
**Signature:** `public boolean requiresPayment()`

**Returns:** `true` if transfer requires payment

**Values:**
- `SALE` → `true`
- All others → `false`

**Usage:**
```java
if (transferType.requiresPayment()) {
    // Validate price and currency
}
```

---

### requiresConsent()
**Signature:** `public boolean requiresConsent()`

**Returns:** `true` if transfer requires consent from current owner

**Values:**
- `VOLUNTARY`, `SALE` → `true`
- All others → `false`

**Usage:**
```java
if (transferType.requiresConsent()) {
    // Validate current owner approval
    // Check for contested ownership
}
```

---

### isForced()
**Signature:** `public boolean isForced()`

**Returns:** `true` if transfer is non-consensual

**Values:**
- `CONQUEST`, `TAX_SEIZURE`, `ABANDONED` → `true`
- All others → `false`

**Usage:**
```java
if (transferType.isForced()) {
    // Skip consent validation
    // Log forced transfer event
}
```

---

### isSuccession()
**Signature:** `public boolean isSuccession()`

**Returns:** `true` if transfer is death-based succession

**Values:**
- `SUCCESSION_INHERITANCE`, `SUCCESSION_WILL`, `SUCCESSION_HEIR` → `true`
- All others → `false`

**Usage:**
```java
if (transferType.isSuccession()) {
    // Bypass contested ownership validation
    // Record deceased owner metadata
}
```

---

## Design Decisions

### 1. Why 8 Distinct Transfer Types?
**Decision:** Granular types instead of flags (e.g., "isForced", "requiresPayment").

**Rationale:**
- Clear semantics for each transfer scenario
- Easier to add type-specific behavior (e.g., conquest clears disputes)
- Human-readable audit logs

**Alternative:** Flags would be more flexible but less readable.

---

### 2. Why Separate Succession Types?
**Decision:** Three distinct succession types (INHERITANCE, WILL, HEIR).

**Rationale:**
- Different data sources (bloodline, will document, clan charter)
- Different validation rules (will requires witnesses, heir requires clan)
- Allows future customization (e.g., will can be contested, inheritance cannot)

**Future:** May consolidate if behavior converges.

---

### 3. Why Conquest Clears Contested Ownership?
**Decision:** Conquest immediately clears all disputes.

**Rationale:**
- "Might makes right" gameplay mechanic
- Wartime invalidates peacetime legal claims
- Prevents post-conquest legal challenges

**Gameplay:** Losers can re-contest after conquest, but start from scratch.

---

### 4. Why Tax Seizure Checks Contested Ownership?
**Decision:** Tax seizure can be blocked by active disputes (admin must resolve).

**Rationale:**
- Legal disputes may affect tax liability (e.g., ownership in question)
- Prevents seizure of property with unclear ownership
- Forces admin to resolve disputes before seizure

**Alternative:** Forced seizure ignoring disputes (simpler, less realistic).

---

## Validation Rules

### By Transfer Type

| Type | Consent | Payment | Contested Block | Metadata Required |
|------|---------|---------|-----------------|-------------------|
| VOLUNTARY | Yes | No | Yes | Optional |
| SALE | Yes | Yes | Yes | Price + Currency |
| SUCCESSION_INHERITANCE | No | No | No | Deceased Owner |
| SUCCESSION_WILL | No | No | No | Deceased + Will Doc |
| SUCCESSION_HEIR | No | No | No | Deceased + Clan |
| CONQUEST | No | No | Clears | Defeated Owner |
| TAX_SEIZURE | No | No | Yes | Unpaid Taxes |
| ABANDONED | No | No | No | Abandonment Reason |

---

## Usage Examples

### Check Payment Requirement
```java
TransferType type = TransferType.SALE;
if (type.requiresPayment()) {
    // Validate price > 0
    // Process payment
}
```

### Validate Consent
```java
TransferType type = TransferType.VOLUNTARY;
if (type.requiresConsent()) {
    // Check current owner approval
    // Verify contested ownership status
}
```

### Process Forced Transfer
```java
TransferType type = TransferType.CONQUEST;
if (type.isForced()) {
    // Skip consent validation
    // Clear contested ownership if CONQUEST
}
```

### Handle Succession
```java
TransferType type = TransferType.SUCCESSION_INHERITANCE;
if (type.isSuccession()) {
    // Bypass contested ownership check
    // Validate deceased owner metadata
}
```

---

## Metadata Guidelines

### Required Metadata by Type

**SALE:**
```json
{
  "price": 5000.0,
  "currency": "gold"
}
```

**All SUCCESSION Types:**
```json
{
  "deceasedOwner": "char-001",
  "deceasedOwnerType": "CHARACTER"
}
```

**CONQUEST:**
```json
{
  "conquestVictory": true,
  "defeatedOwner": "char-001"
}
```

**TAX_SEIZURE:**
```json
{
  "unpaidTaxes": 2500.0,
  "taxDelinquentSince": 5000
}
```

**ABANDONED:**
```json
{
  "abandonedSince": 100000,
  "inactivityThreshold": 90
}
```

---

## Performance Considerations

- Enum values cached at class load
- Helper methods are O(1) (simple boolean checks)
- No dynamic allocation

---

## Integration Points

### With OwnershipTransferSystem
- Used to determine validation rules
- Helper methods called during transfer execution
- Stored in `TransferRecord.transferType`

### With TaxationSystem
- `TAX_SEIZURE` type used for delinquent property seizure
- Integration deferred to Phase 1.5.2

### With Event System (Future)
- Transfer type determines event generation
- CONQUEST generates war events
- SUCCESSION generates death/inheritance events

---

## Testing

**Test Class:** `OwnershipTransferTest.java`  
**Test Methods:**
- `testTransferType_RequiresPayment()`
- `testTransferType_RequiresConsent()`
- `testTransferType_IsForced()`
- `testTransferType_IsSuccession()`

**Coverage:** 100% (all 8 types tested)

---

## Future Enhancements

### Phase 1.6 (Societies & Clans)
- [ ] `CLAN_MERGER` — Combine clan properties
- [ ] `CLAN_DISSOLUTION` — Distribute assets to members

### Phase 2.x (Advanced)
- [ ] `EMINENT_DOMAIN` — Government seizure for public use
- [ ] `FORECLOSURE` — Bank repossession for unpaid loans
- [ ] `GIFT_DEED` — Legal gift with notary
- [ ] `AUCTION` — Public sale to highest bidder
- [ ] `INHERITANCE_CONTESTED` — Succession with dispute
- [ ] `BANKRUPTCY` — Court-ordered liquidation

---

## Related Classes

- **OwnershipTransferSystem:** Uses transfer types for validation
- **TransferRecord:** Stores transfer type in audit record
- **ContestedOwnership:** Affects voluntary and sale transfers

---

## References

- Design: `docs/structures_ownership.md` → Transfer Mechanisms
- Specs: `docs/specs_summary.md` → Transfer Defaults
- Summary: `archive/PHASE_1.5.1_SUMMARY.md`
- Tests: `doc-src/test/java/org/adventure/OwnershipTransferTest.md`
