# StructureLifecycleManagerTest

**Package:** `org.adventure.simulation`  
**Since:** Phase 1.10.3  
**Purpose:** Unit tests for StructureLifecycleManager natural disasters, neglect, and ruin conversion

---

## Overview

Comprehensive test suite validating structure aging, disaster damage, neglect decay, and ruin conversion mechanics.

---

## Test Coverage

**Total Tests:** 6  
**Coverage:** 100% of StructureLifecycleManager methods  
**Focus Areas:**
- Disaster damage (earthquakes, fires, floods)
- Neglect-based decay (poor treasury, inactivity)
- Ruin conversion from destroyed structures
- Active clan maintenance

---

## Test Cases

### 1. `testDisastersCauseStructureDamage()`
**Purpose:** Verify disasters randomly damage structures

**Setup:**
- 1 house at full health (100.0)
- 100,000 ticks simulated (100 disaster checks)

**Execution:**
- Simulate 100 disaster checks (every 1000 ticks)

**Assertions:**
- Structure health < 100.0 OR destroyed
- Statistical probability: ~99.4% chance of at least 1 disaster in 100 checks

**Critical Path:** Disaster interval → random check → apply damage

---

### 2. `testNeglectedStructuresDecay()`
**Purpose:** Verify poor clans cause structure neglect decay

**Setup:**
- Clan with 50 gold (below 200 threshold)
- 1 house at full health
- 35,000 ticks (5 neglect checks)

**Execution:**
- Simulate 5 neglect checks (every 7000 ticks)

**Assertions:**
- Structure health < 100.0
- 5% decay per check = ~22.6% total decay expected

**Critical Path:** Neglect interval → treasury check → apply decay

---

### 3. `testDestroyedStructuresConvertToRuins()`
**Purpose:** Verify destroyed structures become ancient ruins

**Setup:**
- Structure with 1.0 health
- Deal 1.0 damage (destroy)

**Execution:**
- Simulate 1 tick

**Assertions:**
- Structure type changed to ANCIENT_RUINS
- ID contains "_ruin" suffix
- Health set to 0.0
- Owner removed (OwnerType.NONE)

**Critical Path:** Health check → health = 0 → convert to ruin

---

### 4. `testAbandonedStructuresDecay()`
**Purpose:** Verify structures with missing owner clans decay

**Setup:**
- Structure with owner "nonexistent_clan"
- 35,000 ticks (5 neglect checks)

**Execution:**
- Simulate 5 neglect checks

**Assertions:**
- Structure health < 100.0
- Abandoned structures decay as if neglected

**Critical Path:** Owner check → clan not found → apply decay

---

### 5. `testWealthyClanStructuresDoNotDecay()`
**Purpose:** Verify wealthy, active clans prevent decay

**Setup:**
- Clan with 1000 gold (above threshold)
- Clan active at tick 35000
- 1 house at full health

**Execution:**
- Simulate 5 neglect checks (35,000 ticks)

**Assertions:**
- Structure health remains 100.0
- No decay applied

**Critical Path:** Neglect check → treasury sufficient → active clan → skip decay

---

### 6. `testInactiveClanStructuresDecay()`
**Purpose:** Verify inactive clans cause structure decay

**Setup:**
- Clan with 500 gold (sufficient treasury)
- Clan lastActiveTick = 0 (inactive for 56,000 ticks)
- 1 house at full health

**Execution:**
- Simulate at tick 56,000 (1 neglect check)

**Assertions:**
- Structure health < 100.0
- Inactivity threshold: 50,000 ticks triggers decay

**Critical Path:** Neglect check → inactivity check → apply decay

---

## Test Data

### Default Structure
```java
Structure.Builder()
    .id("struct1")
    .type(StructureType.HOUSE)
    .ownerId("clan1")
    .ownerType(OwnerType.CLAN)
    .locationTileId("50,50")
    .health(100.0)
    .maxHealth(100.0)
    .createdAtTick(0)
    .build()
```

### Default Clan (Poor)
```java
Clan.Builder()
    .id("clan1")
    .name("Poor Clan")
    .type(ClanType.CLAN)
    .treasury(50.0) // Below threshold
    .centerX(50)
    .centerY(50)
    .foundingTick(0)
    .lastActiveTick(0)
    .build()
```

### Default Clan (Wealthy)
```java
Clan.Builder()
    .id("clan1")
    .name("Wealthy Clan")
    .type(ClanType.CLAN)
    .treasury(1000.0) // Above threshold
    .centerX(50)
    .centerY(50)
    .foundingTick(0)
    .lastActiveTick(35000) // Active
    .build()
```

---

## Key Intervals Tested

| Interval | Ticks | Test |
|----------|-------|------|
| Disaster Check | 1000 | testDisastersCauseStructureDamage |
| Neglect Check | 7000 | testNeglectedStructuresDecay |
| Inactivity Threshold | 50000 | testInactiveClanStructuresDecay |
| Multiple Checks | 35000 | Various (5 checks) |

---

## Decay Rates

| Condition | Rate | Formula |
|-----------|------|---------|
| Neglect (Poor Clan) | 5% per 7000 ticks | `health * 0.95` |
| Disaster (Earthquake) | 50% | `health - 50.0` |
| Disaster (Fire) | 40% | `health - 40.0` |
| Disaster (Flood) | 30% | `health - 30.0` |

---

## Test Patterns

### Setup Pattern
```java
@BeforeEach
public void setUp() {
    manager = new StructureLifecycleManager();
    structures = new ArrayList<>();
    clans = new ArrayList<>();
}
```

### Simulation Pattern (Interval)
```java
for (long tick = 0; tick < 35000; tick += 7000) {
    manager.simulateTick(structures, clans, tick);
}
```

### Simulation Pattern (Single Check)
```java
manager.simulateTick(structures, clans, 1000);
```

---

## Statistical Validation

### Disaster Probability
- **Single check:** 5% chance
- **100 checks:** 1 - (0.95^100) = 99.4% chance of at least 1 disaster
- **Expected disasters:** 5 per 100 checks

### Neglect Decay
- **Single check:** 5% decay
- **5 checks:** 1 - (0.95^5) = 22.6% total decay
- **Final health (5 checks):** 100 * 0.95^5 ≈ 77.4

---

## Dependencies

- JUnit 5
- org.adventure.structure.Structure
- org.adventure.society.Clan
- org.adventure.structure.StructureType
- org.adventure.structure.OwnerType

---

## See Also

- [StructureLifecycleManager](../../main/java/org/adventure/simulation/StructureLifecycleManager.md)
- [Structure](../../main/java/org/adventure/structure/Structure.md)
- [Clan](../../main/java/org/adventure/society/Clan.md)
