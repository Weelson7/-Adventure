# Clan Class Documentation

**Package:** `org.adventure.society`  
**File:** `src/main/java/org/adventure/society/Clan.java`  
**Status:** ✅ Complete (Phase 1.6)

---

## Overview

The `Clan` class represents a social organization with members, treasury, and diplomatic relationships. Clans are the fundamental unit of social organization in !Adventure, supporting membership management, financial operations, and diplomatic interactions.

### Key Features
- **Membership management:** Add/remove members with validation
- **Treasury operations:** Deposit/withdraw with negative balance protection
- **Diplomatic relationships:** Track relationships with other societies
- **Clan merging:** Combine clans with unified treasury and membership
- **Immutable design:** All modifications return new instances
- **Builder pattern:** Flexible construction with sensible defaults

---

## Design Principles

### Immutability
All clan operations return new `Clan` instances, preserving the original. This:
- Ensures thread safety
- Simplifies state tracking
- Prevents accidental mutations
- Aligns with functional programming patterns

### Validation
- Treasury never goes negative (enforced at construction and withdrawal)
- Member operations validate existence before modifications
- Required fields (id, name, type) enforced via builder
- Name cannot be empty or whitespace-only

### Type Safety
- Uses `ClanType` enum (CLAN, KINGDOM, GUILD) for organization types
- Leverages Java's type system to prevent invalid states
- Collections are defensively copied to prevent external mutations

---

## Data Model

### Fields

| Field | Type | Description | Constraints |
|-------|------|-------------|-------------|
| `id` | `String` | Unique clan identifier | Required, non-null |
| `name` | `String` | Clan display name | Required, non-empty |
| `type` | `ClanType` | Organization type | Required (CLAN, KINGDOM, GUILD) |
| `members` | `List<String>` | Character IDs of members | Immutable list, no duplicates |
| `treasury` | `double` | Clan funds | >= 0.0 |
| `relationships` | `Map<String, RelationshipRecord>` | Diplomatic relationships | Keyed by target society ID |
| `foundingTick` | `long` | Tick when clan was founded | >= 0 |
| `lastActiveTick` | `long` | Most recent activity tick | >= 0 |
| `schemaVersion` | `int` | Persistence schema version | Default: 1 |

### Schema Version
- Current version: **1**
- Used for save/load migration (see `docs/persistence_versioning.md`)
- Future schema changes will increment this version

---

## Core Operations

### Membership Management

#### Add Member
```java
Clan clan = ...;
Clan updated = clan.addMember("char-001");
```
- **Validation:** Throws `IllegalArgumentException` if member already exists
- **Immutability:** Returns new clan instance with added member
- **Use case:** Player joins clan, NPC recruited

#### Remove Member
```java
Clan updated = clan.removeMember("char-001");
```
- **Validation:** Throws `IllegalArgumentException` if member doesn't exist
- **Immutability:** Returns new clan instance with removed member
- **Use case:** Player leaves clan, member expelled, death

#### Query Operations
```java
boolean hasMember = clan.hasMember("char-001");
int count = clan.getMemberCount();
boolean empty = clan.isEmpty();
```

### Treasury Management

#### Deposit
```java
Clan updated = clan.deposit(100.0);
```
- **Validation:** Amount must be positive
- **Result:** Treasury increases by amount
- **Use case:** Tax collection, donations, loot distribution

#### Withdraw
```java
Clan updated = clan.withdraw(50.0);
```
- **Validation:** 
  - Amount must be positive
  - Amount must not exceed treasury balance
- **Result:** Treasury decreases by amount
- **Protection:** Treasury can never go negative
- **Use case:** Clan expenses, member payouts, structure maintenance

### Diplomatic Operations

#### Update Relationship
```java
RelationshipRecord rel = new RelationshipRecord("clan-002", 50.0, 30.0, 40.0, 10.0, 1000);
Clan updated = clan.updateRelationship(rel);
```
- **Purpose:** Track reputation, influence, alignment with other societies
- **Integration:** Works with `Diplomacy` class for event-driven updates
- **See also:** `RelationshipRecord.md`, `Diplomacy.md`

### Clan Merging

```java
Clan merged = Clan.merge(clan1, clan2, "new-id", "United Clan", currentTick);
```
- **Members:** Combined, duplicates removed
- **Treasury:** Summed from both clans
- **Relationships:** Combined (clan1's relationships take precedence for conflicts)
- **Type:** Preserves clan1's type
- **Founding tick:** Set to merge tick
- **Use case:** Alliances solidify, kingdoms form, guilds consolidate

---

## Builder Pattern

### Basic Usage
```java
Clan clan = new Clan.Builder()
    .id("clan-001")
    .name("Test Clan")
    .type(ClanType.CLAN)
    .foundingTick(1000)
    .build();
```

### Copy Constructor
```java
Clan modified = new Clan.Builder(original)
    .name("New Name")
    .treasury(1000.0)
    .build();
```
- Copies all fields from original
- Override specific fields as needed
- Used internally for immutable updates

### Defaults
- `type`: `ClanType.CLAN`
- `members`: Empty list
- `treasury`: `0.0`
- `relationships`: Empty map
- `foundingTick`: `0`
- `lastActiveTick`: `0`
- `schemaVersion`: `1`

---

## Quality Gates (Phase 1.6)

### ✅ Treasury Validation
- **Rule:** Treasury never goes negative
- **Enforcement:** Construction-time validation, withdrawal limits
- **Tests:** `ClanTest.testTreasuryNeverGoesNegative()`, `testCannotWithdrawMoreThanBalance()`

### ✅ Membership Logic
- **Rule:** Join/leave updates member lists correctly
- **Enforcement:** Validation on add/remove, duplicate prevention
- **Tests:** `ClanTest.testAddMember()`, `testRemoveMember()`, `testCannotAddDuplicateMember()`

### ✅ Merge Logic
- **Rule:** Combines members and treasuries correctly, no data loss
- **Enforcement:** Set-based member deduplication, arithmetic treasury sum
- **Tests:** `ClanTest.testMergeClans()`, `testMergeClansWithDuplicateMembers()`, `testMergeClansRelationships()`

### ✅ Immutability
- **Rule:** Original clan unchanged after operations
- **Enforcement:** Defensive copying, builder pattern
- **Tests:** All tests verify original clan remains unchanged

---

## Integration Points

### With Diplomacy System
- `Diplomacy` class uses `Clan.updateRelationship()` for event-driven updates
- Decay processing: `Diplomacy.processPeriodicDecay(clan, currentTick)`
- Event handlers: Trade missions, betrayals, gifts, war declarations

### With Persistence
- JSON serialization via Jackson annotations (`@JsonCreator`, `@JsonProperty`)
- Schema versioning for migration
- Immutable fields ensure consistent serialization

### With Game Simulation
- `lastActiveTick` updated by simulation loop
- Treasury changes triggered by economy events (taxes, trade)
- Member changes driven by player/NPC actions

---

## Performance Considerations

### Memory
- Immutable design creates new instances per operation
- For frequent updates, batch operations or use builder with multiple changes
- Defensive copying adds overhead for large member lists/relationships

### Optimization Strategies
- **Batch updates:** Use builder to modify multiple fields before building
- **Lazy evaluation:** Defer expensive calculations (e.g., relationship decay) until needed
- **Caching:** Consider caching derived values (member count already stored as list size)

---

## Testing

### Test Coverage
- **Total tests:** 25 in `ClanTest.java`
- **Coverage:** ~95% line coverage (all critical paths)
- **Quality gates:** All passing (treasury, membership, merging)

### Key Test Cases
1. **Construction validation:** Null checks, negative treasury, empty name
2. **Membership operations:** Add, remove, duplicates, non-existent members
3. **Treasury operations:** Deposit, withdraw, balance protection
4. **Merging:** Member deduplication, treasury sum, relationship merge
5. **Immutability:** Original clan unchanged after operations
6. **Equality:** Based on clan ID, not content

---

## Future Enhancements

### Phase 2+
- **Loyalty metrics:** Track member loyalty based on wealth, military, relationships
- **Governance structures:** Monarchy, council, democracy, tribal chiefdom
- **Internal politics:** Factions, power struggles, decision-making
- **Complex diplomacy:** Secret relationships, hidden agendas, crisis resolution
- **Asset management:** Territory, structures, resources beyond simple treasury

### Modding Support
- Data-only mods can define custom clan presets via JSON
- Scripted mods (post-MVP) can extend clan behavior via WASM sandbox

---

## Related Documentation
- **RelationshipRecord:** `RelationshipRecord.md` — Diplomacy metrics
- **Diplomacy:** `Diplomacy.md` — Event-driven relationship updates
- **ClanType:** Simple enum (CLAN, KINGDOM, GUILD)
- **Design:** `docs/societies_clans_kingdoms.md` — Society system design
- **Specs:** `docs/specs_summary.md` — Canonical defaults

---

## Version History
- **v1.0 (Phase 1.6):** Initial implementation with core membership, treasury, and diplomacy features

---

**Status:** ✅ Complete for MVP Phase 1.6  
**Test Status:** 25/25 tests passing  
**Coverage:** ~95% line coverage
