# ClanExpansionSimulatorTest

**Package:** `org.adventure.simulation`  
**Since:** Phase 1.10.3  
**Purpose:** Unit tests for ClanExpansionSimulator dynamic world simulation

---

## Overview

Comprehensive test suite validating the ClanExpansionSimulator's behavior for NPC-led clan expansion, player clan detection, expansion requirements, warfare, and alliance formation.

---

## Test Coverage

**Total Tests:** 6  
**Coverage:** 100% of ClanExpansionSimulator methods  
**Focus Areas:**
- Player vs NPC clan differentiation
- Expansion requirements (treasury, population)
- Warfare between hostile clans
- Alliance formation between friendly clans

---

## Test Cases

### 1. `testPlayerControlledClanSkipsExpansion()`
**Purpose:** Verify player-controlled clans do NOT auto-expand

**Setup:**
- Clan with 1 player NPC + 10 regular NPCs
- Sufficient treasury (1000 gold)
- Population above threshold (11 total)

**Execution:**
- Simulate 1000 ticks

**Assertions:**
- Structure count unchanged (no expansion)
- Player presence prevents auto-expansion

**Critical Path:** Player detection → skip expansion logic

---

### 2. `testNPCClanExpands()`
**Purpose:** Verify NPC-only clans automatically expand

**Setup:**
- Clan with 15 NPCs (no players)
- Sufficient treasury (1000 gold)
- Population above threshold

**Execution:**
- Simulate 600 ticks (past 500-tick expansion interval)

**Assertions:**
- At least one structure built
- Treasury decreased from construction costs

**Critical Path:** NPC-only clan → expansion eligible → build structures

---

### 3. `testExpansionRequiresSufficientTreasury()`
**Purpose:** Verify expansion blocked by low treasury

**Setup:**
- Clan with 100 gold (below 200 threshold)
- 15 NPCs (sufficient population)

**Execution:**
- Simulate 600 ticks

**Assertions:**
- No structures built
- Treasury too low prevents expansion

**Critical Path:** Treasury check → fail → skip expansion

---

### 4. `testExpansionRequiresSufficientPopulation()`
**Purpose:** Verify expansion blocked by low population

**Setup:**
- Clan with 1000 gold (sufficient treasury)
- 5 NPCs (below 10 threshold)

**Execution:**
- Simulate 600 ticks

**Assertions:**
- No structures built
- Population too low prevents expansion

**Critical Path:** Population check → fail → skip expansion

---

### 5. `testWarfareBetweenHostileClans()`
**Purpose:** Verify hostile clans engage in warfare

**Setup:**
- Clan 1 with 3 barracks (military strength)
- Clan 2 with 1 house (target)
- Hostile relationship (reputation -60)
- 1 warrior NPC in clan 1

**Execution:**
- Simulate 1000 ticks

**Assertions:**
- Target structure damaged or destroyed
- Warfare occurs between hostile clans

**Critical Path:** Hostile check → military strength → attack enemy structures

---

### 6. `testAllianceFormationBetweenFriendlyClans()`
**Purpose:** Verify friendly clans form alliances

**Setup:**
- Clan 1 and Clan 2 with friendly relationship (reputation 60)
- Both clans hostile to Clan 3 (mutual enemy)
- 1 warrior NPC

**Execution:**
- Simulate 1000 ticks

**Assertions:**
- Reputation maintained or improved (≥60)
- Alliance conditions favorable

**Critical Path:** Friendly check → mutual enemy → strengthen alliance

---

## Test Data

### Default Clan
```java
Clan.Builder()
    .id("clan1")
    .name("Test Clan")
    .type(ClanType.CLAN)
    .treasury(1000.0)
    .centerX(50)
    .centerY(50)
    .foundingTick(0)
    .build()
```

### Default NPC
```java
NamedNPC.Builder()
    .id("npc1")
    .name("Test NPC")
    .clanId("clan1")
    .age(25)
    .gender(Gender.MALE)
    .job(NPCJob.FARMER)
    .homeStructureId("struct1")
    .isPlayer(false)
    .birthTick(0)
    .build()
```

### World Data
- **Size:** 100x100
- **Biome:** Grassland (uniform)
- **Elevation:** 0.5 (flat, buildable)

---

## Key Thresholds Tested

| Threshold | Value | Test |
|-----------|-------|------|
| Min Treasury | 200 | testExpansionRequiresSufficientTreasury |
| Min Population | 10 | testExpansionRequiresSufficientPopulation |
| Expansion Interval | 500 ticks | testNPCClanExpands |
| War Threshold | -60 reputation | testWarfareBetweenHostileClans |
| Alliance Threshold | 60 reputation | testAllianceFormationBetweenFriendlyClans |

---

## Test Patterns

### Setup Pattern
```java
@BeforeEach
public void setUp() {
    simulator = new ClanExpansionSimulator();
    clans = new ArrayList<>();
    npcs = new ArrayList<>();
    structures = new ArrayList<>();
    roads = new ArrayList<>();
    biomes = new Biome[100][100]; // Fill with GRASSLAND
    elevation = new double[100][100]; // Fill with 0.5
}
```

### Simulation Pattern
```java
simulator.setWorldData(elevation);
for (long tick = 0; tick < 1000; tick++) {
    simulator.simulateTick(clans, npcs, structures, roads, 
        biomes, elevation, worldWidth, worldHeight, tick);
}
```

---

## Dependencies

- JUnit 5
- org.adventure.npc.NamedNPC
- org.adventure.society.Clan
- org.adventure.structure.Structure
- org.adventure.settlement.RoadTile
- org.adventure.world.Biome

---

## See Also

- [ClanExpansionSimulator](../../main/java/org/adventure/simulation/ClanExpansionSimulator.md)
- [Clan](../../main/java/org/adventure/society/Clan.md)
- [NamedNPC](../../main/java/org/adventure/npc/NamedNPC.md)
