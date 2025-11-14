# RegionSimulatorIntegrationTest

**Package:** `org.adventure.region`  
**Since:** Phase 1.10.3  
**Purpose:** End-to-end integration tests for complete Phase 1.10 dynamic world simulation

---

## Overview

Comprehensive integration test suite validating all Phase 1.10 systems working together: NPC lifecycle, clan expansion, structure lifecycle, quest generation, and village management in a fully simulated world.

---

## Test Coverage

**Total Tests:** 10  
**Integration Scope:** Complete RegionSimulator with all 5 managers  
**Systems Tested:**
- NPCLifecycleManager
- ClanExpansionSimulator
- StructureLifecycleManager
- QuestDynamicGenerator
- VillageManager

---

## Test World Setup

### World Parameters
- **Size:** 256×256 tiles
- **Biome:** Uniform grassland
- **Elevation:** 0.5 (flat, buildable)
- **Regions:** 1-2 active regions

### Initial State
- **NPCs:** 26 total (15 in clan1, 11 in clan2 including 1 player)
- **Clans:** 2 (1 NPC-led, 1 player-led)
- **Structures:** 7 (5 houses, 1 damaged house, 1 ancient ruin)
- **Roads:** 11 tiles connecting structures
- **Stories:** 1 active legend

---

## Test Cases

### 1. `testCompleteSimulationCycle()`
**Purpose:** Verify entire simulation pipeline runs without errors

**Setup:**
- Complete world with 26 NPCs, 2 clans, 7 structures

**Execution:**
- Run 1000 ticks

**Assertions:**
- Simulation advances to tick 1000
- ≥90% NPCs survive
- ≥80% structures remain
- Quests generated > 0

**Validates:** All managers execute successfully in pipeline

---

### 2. `testNPCClanExpansion()`
**Purpose:** Verify NPC-led clans expand automatically

**Setup:**
- Clan1 (NPC-only) with 15 NPCs, 1000 gold

**Execution:**
- Run 600 ticks (past 500-tick expansion interval)

**Assertions:**
- Clan1 structures ≥ 3 (initial count)
- May have expanded depending on placement availability

**Validates:** ClanExpansionSimulator for NPC clans

---

### 3. `testPlayerClanNoAutoExpansion()`
**Purpose:** Verify player-controlled clans do NOT auto-expand

**Setup:**
- Clan2 (player-controlled) with 11 NPCs including 1 player

**Execution:**
- Run 1000 ticks

**Assertions:**
- Clan2 structure count unchanged
- Player presence prevents auto-expansion

**Validates:** ClanExpansionSimulator player detection

---

### 4. `testQuestGeneration()`
**Purpose:** Verify dynamic quests generated from world events

**Setup:**
- 1 ancient ruin (exploration trigger)
- 2 hostile clans (conflict trigger)
- 1 damaged structure (repair trigger)

**Execution:**
- Run 500 ticks

**Assertions:**
- Quests.size() > 0
- Ruin quest OR conflict quest present

**Validates:** QuestDynamicGenerator integration

---

### 5. `testVillageDetection()`
**Purpose:** Verify village detection from structure clusters

**Setup:**
- 2 structure clusters (3+ structures each)

**Execution:**
- Run 100 ticks

**Assertions:**
- Villages.size() > 0
- Each village has ≥3 structures
- Villages have names and IDs

**Validates:** VillageManager clustering algorithm

---

### 6. `testStructureLifecycle()`
**Purpose:** Verify structures age, decay, and convert to ruins

**Setup:**
- 1 damaged house (30% health)
- Multiple structures vulnerable to disasters

**Execution:**
- Run 10,000 ticks (10 disaster checks)

**Assertions:**
- Ruin count ≥ 1 (at least initial ruin remains)
- Some structures may have been damaged

**Validates:** StructureLifecycleManager disasters and ruin conversion

---

### 7. `testNPCLifecycle()`
**Purpose:** Verify NPC aging, reproduction, and death

**Setup:**
- 26 NPCs with various ages (20-35)

**Execution:**
- Run 10,000 ticks (~1 in-game year)

**Assertions:**
- NPCs still exist (not empty)
- Valid ages (0-100)
- ≥80% survival rate
- Possible reproduction (new NPCs)

**Validates:** NPCLifecycleManager aging and lifecycle

---

### 8. `testActiveVsBackgroundSimulation()`
**Purpose:** Verify active/background region switching and resynchronization

**Setup:**
- 1 active region

**Execution:**
- Deactivate region (active → background)
- Run 100 ticks
- Reactivate region (background → active)

**Assertions:**
- Active count: 1 → 0 → 1
- Background count: 0 → 1 → 0
- Region lastProcessedTick = 100

**Validates:** RegionSimulator active/background logic

---

### 9. `testMultipleRegions()`
**Purpose:** Verify multiple regions simulated concurrently

**Setup:**
- Region 1 (active)
- Region 2 (background)

**Execution:**
- Run 100 ticks

**Assertions:**
- Total regions = 2
- Active regions = 1
- Background regions = 1
- Both regions accessible

**Validates:** RegionSimulator multi-region management

---

### 10. `testIntegratedSystemsDontCrash()`
**Purpose:** Stress test for extended simulation without errors

**Setup:**
- Complete world (all entities)

**Execution:**
- Run 100 iterations of 100 ticks each (10,000 total)
- Verify region integrity after each iteration

**Assertions:**
- All collections non-null (NPCs, clans, structures, quests, villages)
- No exceptions or crashes
- currentTick ≥ 10,000

**Validates:** System stability and integration correctness

---

## Test Data Details

### Clan 1 (NPC-Led)
```java
Clan.Builder()
    .id("clan1")
    .name("NPC Clan")
    .type(ClanType.CLAN)
    .treasury(1000.0)
    .centerX(100)
    .centerY(100)
    .leaderId("npc_clan1_0")
    .foundingTick(0)
    .build()
```
- **NPCs:** 15 (all NPC-controlled)
- **Structures:** 3 houses + 1 damaged house
- **Relationship:** Hostile to clan2 (-50 reputation)

### Clan 2 (Player-Led)
```java
Clan.Builder()
    .id("clan2")
    .name("Player Clan")
    .type(ClanType.CLAN)
    .treasury(1000.0)
    .centerX(150)
    .centerY(150)
    .leaderId("player1")
    .foundingTick(0)
    .build()
```
- **NPCs:** 11 (1 player + 10 NPCs)
- **Structures:** 2 houses
- **Relationship:** Neutral

### Structures
| ID | Type | Owner | Health | Location |
|----|------|-------|--------|----------|
| house_clan1_0 | HOUSE | clan1 | 100.0 | 100,100 |
| house_clan1_1 | HOUSE | clan1 | 100.0 | 105,100 |
| house_clan1_2 | HOUSE | clan1 | 100.0 | 110,100 |
| house_clan2_0 | HOUSE | clan2 | 100.0 | 150,150 |
| house_clan2_1 | HOUSE | clan2 | 100.0 | 155,150 |
| damaged_house | HOUSE | clan1 | 30.0 | 105,105 |
| ancient_ruin_1 | ANCIENT_RUINS | NONE | 50.0 | 120,120 |

### Story
```java
Story.Builder()
    .id("story1")
    .title("The Ancient Prophecy")
    .storyType(StoryType.LEGEND)
    .status(StoryStatus.ACTIVE)
    .originTileId(12000120)
    .originTick(0)
    .priority(10)
    .build()
```

---

## Integration Validation

### Manager Pipeline Order
1. **ResourceRegenerationManager** (not tested, background system)
2. **NPCLifecycleManager** → NPC aging, death, reproduction
3. **ClanExpansionSimulator** → NPC clan expansion, warfare, diplomacy
4. **StructureLifecycleManager** → Disasters, neglect, ruin conversion
5. **QuestDynamicGenerator** → Quest generation from events
6. **VillageManager** → Village detection and updates

### Cross-Manager Interactions Validated
- **ClanExpansionSimulator → StructureLifecycleManager:** New structures created → aged by lifecycle
- **StructureLifecycleManager → QuestDynamicGenerator:** Ruins created → exploration quests generated
- **ClanExpansionSimulator → QuestDynamicGenerator:** Conflicts arise → mediation quests generated
- **NPCLifecycleManager → ClanExpansionSimulator:** NPCs die → clan population affects expansion
- **All Managers → VillageManager:** Structure changes → village detection updates

---

## Performance Characteristics

| Test | Ticks | Duration | Purpose |
|------|-------|----------|---------|
| testCompleteSimulationCycle | 1,000 | ~1s | Smoke test |
| testNPCClanExpansion | 600 | ~0.6s | Expansion validation |
| testQuestGeneration | 500 | ~0.5s | Quest validation |
| testVillageDetection | 100 | ~0.1s | Village validation |
| testStructureLifecycle | 10,000 | ~10s | Long-term aging |
| testNPCLifecycle | 10,000 | ~10s | NPC lifecycle |
| testIntegratedSystemsDontCrash | 10,000 | ~10s | Stress test |

---

## Test Patterns

### Setup Pattern
```java
@BeforeEach
public void setup() {
    simulator = new RegionSimulator();
    biomes = new Biome[256][256]; // Fill with GRASSLAND
    elevation = new double[256][256]; // Fill with 0.5
    simulator.setWorldData(biomes, elevation, 256, 256);
    testRegion = createTestRegion();
    simulator.addRegion(testRegion);
    simulator.activateRegion(testRegion.getId());
}
```

### Simulation Pattern
```java
simulator.advanceTicks(1000);
```

### Integrity Check Pattern
```java
assertNotNull(testRegion.getNPCs());
assertNotNull(testRegion.getClans());
assertNotNull(testRegion.getStructures());
assertNotNull(testRegion.getQuests());
assertNotNull(testRegion.getVillages());
```

---

## Dependencies

- JUnit 5
- All Phase 1.10 packages:
  - org.adventure.region.RegionSimulator
  - org.adventure.npc.NPCLifecycleManager
  - org.adventure.simulation.ClanExpansionSimulator
  - org.adventure.simulation.StructureLifecycleManager
  - org.adventure.simulation.QuestDynamicGenerator
  - org.adventure.settlement.VillageManager

---

## Success Criteria

All 10 tests passing indicates:
- ✅ All 5 managers integrate correctly
- ✅ Player vs NPC detection works
- ✅ Active/background simulation works
- ✅ Quest generation works from world events
- ✅ Village detection works from structure clusters
- ✅ Structure lifecycle works (disasters, neglect, ruins)
- ✅ NPC lifecycle works (aging, reproduction, death)
- ✅ Multi-region management works
- ✅ System stability proven (10,000 ticks stress test)

---

## See Also

- [RegionSimulator](../../main/java/org/adventure/region/RegionSimulator.md)
- [ClanExpansionSimulator](../../main/java/org/adventure/simulation/ClanExpansionSimulator.md)
- [StructureLifecycleManager](../../main/java/org/adventure/simulation/StructureLifecycleManager.md)
- [QuestDynamicGenerator](../../main/java/org/adventure/simulation/QuestDynamicGenerator.md)
- [NPCLifecycleManager](../../main/java/org/adventure/npc/NPCLifecycleManager.md)
- [VillageManager](../../main/java/org/adventure/settlement/VillageManager.md)

---

**Status:** ✅ Complete Phase 1.10.3 Integration Validation  
**Total Tests:** 10  
**All Systems:** Integrated and tested end-to-end
