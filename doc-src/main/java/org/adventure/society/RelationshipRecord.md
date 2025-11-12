# RelationshipRecord Class Documentation

**Package:** `org.adventure.society`  
**File:** `src/main/java/org/adventure/society/RelationshipRecord.java`  
**Status:** ✅ Complete (Phase 1.6)

---

## Overview

`RelationshipRecord` tracks diplomatic relationships between societies/clans with bounded metrics and time-based decay. It's an immutable data class used by `Clan` and `Diplomacy` to represent inter-society relations.

### Key Features
- **Four core metrics:** Reputation, influence, alignment, race affinity
- **Automatic clamping:** Values constrained to defined ranges
- **Decay support:** Built-in time-based degradation formulas
- **Derived calculations:** Alliance strength, war likelihood
- **Immutability:** All updates return new instances

---

## Metrics

### Reputation (-100 to +100)
- **Meaning:** Trust and goodwill between societies
- **Range:** -100 (hostile) to +100 (trusted ally)
- **Decay:** Toward 0 (neutral) at 0.01 per 100 ticks
- **Affects:** Trade willingness, alliance stability, war likelihood

### Influence (0 to 100)
- **Meaning:** Political/economic leverage one society has over another
- **Range:** 0 (no leverage) to 100 (dominant)
- **Decay:** -0.05 per 100 ticks unless maintained
- **Affects:** Diplomatic negotiation power, tribute demands

### Alignment (-100 to +100)
- **Meaning:** Ideological compatibility and shared values
- **Range:** -100 (opposing values) to +100 (shared values)
- **Decay:** -0.001 per tick (minimal, nearly static)
- **Affects:** Cooperation willingness, long-term alliance viability

### Race Affinity (-50 to +50)
- **Meaning:** Racial kinship or tension
- **Range:** -50 (racial tension) to +50 (racial kinship)
- **Decay:** None (static after initialization)
- **Affects:** Base interaction outcomes, NPC behavior modifiers

---

## Derived Metrics

### Alliance Strength
```java
double strength = relationship.getAllianceStrength();
// Formula: (reputation + alignment) / 2
```
- **Threshold:** Must be > 30 to form alliances
- **Use:** Determines if alliance formation is possible

### War Likelihood
```java
double likelihood = relationship.getWarLikelihood();
// Formula: max(0, (-reputation - 20) / 50)
```
- **Range:** 0.0 (unlikely) to 1.0+ (imminent)
- **Interpretation:** > 0.5 considered "war likely"
- **Use:** AI threat assessment, UI warnings

### Can Form Alliance
```java
boolean canAlly = relationship.canFormAlliance();
// True if getAllianceStrength() > 30
```
- **Shortcut:** Checks alliance strength threshold
- **Use:** Validate alliance proposals

---

## Decay System

### Reputation Decay (Toward Neutral)
```java
// After 100 ticks:
// - Positive reputation decreases by 0.01
// - Negative reputation increases by 0.01
// Stops at exactly 0
```
**Rationale:** Without active maintenance, relations drift toward neutral

### Influence Decay (Downward)
```java
// After 100 ticks:
// - Influence decreases by 0.05
// Never goes below 0
```
**Rationale:** Influence requires active maintenance (trade, tribute)

### Alignment Decay (Minimal)
```java
// Per tick:
// - Alignment decreases by 0.001
// Clamped to [-100, 100]
```
**Rationale:** Ideological values change slowly

### Race Affinity (No Decay)
- **Static:** Set at initialization, never changes
- **Rationale:** Racial relationships are foundational, not time-dependent

---

## Immutable Updates

### Update Single Metric
```java
RelationshipRecord updated = relationship.withReputation(newValue, currentTick);
RelationshipRecord updated = relationship.withInfluence(newValue, currentTick);
RelationshipRecord updated = relationship.withAlignment(newValue, currentTick);
RelationshipRecord updated = relationship.withRaceAffinity(newValue, currentTick);
```
- All return new `RelationshipRecord` instances
- Automatically clamps to valid ranges
- Updates `lastUpdatedTick`

### Apply Decay
```java
long ticksSinceUpdate = currentTick - relationship.getLastUpdatedTick();
RelationshipRecord decayed = relationship.applyDecay(ticksSinceUpdate, currentTick);
```
- Applies all decay formulas
- Used by `Diplomacy.processPeriodicDecay()`
- Returns new instance with decayed values

---

## Construction & Validation

### Constructor
```java
RelationshipRecord rel = new RelationshipRecord(
    "target-clan-id",   // targetSocietyId
    50.0,               // reputation (-100 to 100)
    30.0,               // influence (0 to 100)
    40.0,               // alignment (-100 to 100)
    10.0,               // raceAffinity (-50 to 50)
    1000                // lastUpdatedTick
);
```

### Automatic Clamping
Values outside valid ranges are automatically clamped:
```java
// Reputation > 100 → clamped to 100
RelationshipRecord tooHigh = new RelationshipRecord("clan-002", 150.0, 30.0, 40.0, 10.0, 1000);
assertEquals(100.0, tooHigh.getReputation());

// Influence < 0 → clamped to 0
RelationshipRecord negative = new RelationshipRecord("clan-002", 50.0, -10.0, 40.0, 10.0, 1000);
assertEquals(0.0, negative.getInfluence());
```

---

## Equality & Hashing

### Equality
- Based **only** on `targetSocietyId`
- Two records with same target are considered equal, even if metrics differ
- **Rationale:** Only one relationship record per target society

### Hash Code
- Derived from `targetSocietyId`
- Consistent with `equals()`

---

## JSON Serialization

Uses Jackson annotations for persistence:
```json
{
  "targetSocietyId": "clan-002",
  "reputation": 50.0,
  "influence": 30.0,
  "alignment": 40.0,
  "raceAffinity": 10.0,
  "lastUpdatedTick": 1000
}
```

---

## Performance Considerations

### Memory
- Lightweight: 6 fields (5 primitives + 1 String)
- Immutable → safe to share across threads
- No collections → minimal overhead

### Decay Calculation
- `O(1)` complexity for all decay operations
- Simple arithmetic → fast execution
- Can be batched for many relationships

---

## Testing

### Test Coverage
- Metric clamping tests for all four metrics
- Decay tests for reputation, influence, alignment
- Derived metric calculation tests
- Immutability verification
- **Total:** Covered by 30 tests in `DiplomacyTest.java`

---

## Usage Examples

### Initialization (Neutral Relationship)
```java
RelationshipRecord neutral = new RelationshipRecord(
    "clan-002", 0.0, 0.0, 0.0, 0.0, currentTick
);
```

### Positive Relationship (Allies)
```java
RelationshipRecord allies = new RelationshipRecord(
    "clan-002", 80.0, 60.0, 70.0, 20.0, currentTick
);
assertTrue(allies.canFormAlliance()); // strength = (80+70)/2 = 75 > 30
```

### Hostile Relationship (Enemies)
```java
RelationshipRecord enemies = new RelationshipRecord(
    "clan-002", -70.0, 10.0, -50.0, -10.0, currentTick
);
assertTrue(enemies.getWarLikelihood() > 0.5); // war likely
```

### Updating After Event
```java
// Trade mission: +5 reputation
RelationshipRecord updated = relationship.withReputation(
    relationship.getReputation() + 5, currentTick
);
```

---

## Integration Points

### With Clan
- Stored in `Clan.relationships` map (keyed by target society ID)
- Updated via `Clan.updateRelationship(RelationshipRecord)`

### With Diplomacy
- Modified by event methods (`applyTradeMission()`, `applyBetrayal()`, etc.)
- Decayed by `processPeriodicDecay()`
- Queried for alliance/war checks

---

## Future Enhancements

### Phase 2+
- **Trust flag:** Boolean for recent betrayals
- **Event history:** Track recent diplomatic events
- **Trade volume:** Factor into influence decay rate
- **Military balance:** Affect war likelihood calculation
- **Secret relationships:** Hidden metrics revealed under certain conditions

---

## Related Documentation
- **Clan:** `Clan.md` — Uses relationships in diplomacy tracking
- **Diplomacy:** `Diplomacy.md` — Event-driven updates and decay processing
- **Design:** `docs/societies_clans_kingdoms.md` — Diplomacy system design
- **Specs:** `docs/specs_summary.md` — Canonical metric ranges and decay rates

---

## Version History
- **v1.0 (Phase 1.6):** Initial implementation with four metrics and decay support

---

**Status:** ✅ Complete for MVP Phase 1.6  
**Coverage:** Covered by 30 tests in DiplomacyTest.java
