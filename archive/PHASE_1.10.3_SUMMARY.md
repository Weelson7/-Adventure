# Phase 1.10.3 Summary — Dynamic World Simulation

**Phase:** MVP Phase 1.10.3  
**Status:** 🚧 **NOT YET STARTED** (Planning Document)  
**Target Start:** TBD  
**Prerequisites:** Phase 1.10.1 ✅ & Phase 1.10.2 ✅ COMPLETE  

---

## Overview

Phase 1.10.3 is the final phase of the "Living World" initiative (Phase 1.10.x). While Phases 1.10.1 and 1.10.2 successfully created a populated world with NPCs, settlements, and infrastructure, Phase 1.10.3 will make this world **truly dynamic**:

- **NPC-driven clan expansion** — AI clans grow, build, and compete
- **Player-controlled clans** — Players have full control, no automatic behavior
- **Structure lifecycle** — Buildings age, get damaged, and become ruins
- **Dynamic quest generation** — Quests emerge from world events
- **Living world simulation** — NPCs age, marry, reproduce, and die naturally

### Current State (as of November 14, 2025)

**✅ COMPLETE (Phases 1.10.1 & 1.10.2):**
- Named NPC system with homes, jobs, relationships
- NPCLifecycleManager (core aging/marriage/reproduction logic)
- PlayerNPCInteraction (player marriage & reproduction)
- Village/city detection and management
- Automatic road generation between structures
- Structure placement rules and validation
- Quest and prophecy generation at worldgen
- 547 tests passing

**🚧 NOT YET STARTED (Phase 1.10.3):**
- ClanExpansionSimulator (NPC vs player-led clans)
- StructureLifecycleManager (disasters, attacks, ruins)
- QuestDynamicGenerator (quests from events)
- RegionSimulator integration (TODO at line 118)
- Full simulation testing (10k+ ticks)

---

## Motivation: From Static to Dynamic

### The Problem
After Phase 1.10.2, the world is **populated but static**:
- ✅ NPCs exist with names, ages, jobs, homes
- ✅ Clans and settlements are seeded at worldgen
- ✅ Villages have roads and structure clusters
- ❌ **BUT:** Nothing changes over time unless a player acts
- ❌ **BUT:** NPC clans don't expand, build, or compete
- ❌ **BUT:** Structures never decay or get destroyed
- ❌ **BUT:** No new quests emerge from events

### The Vision
Phase 1.10.3 will create a **living, breathing world**:
- NPC clans autonomously expand their territories
- Structures age and can be destroyed (natural disasters, war, neglect)
- NPCs continue their lifecycle (aging, marriage, reproduction, death)
- Dynamic quests emerge from world events (ruins, conflicts, disasters)
- Players observe a world that evolves even when they're not directly involved

**Critical Design Constraint:**
- **NPC-led clans:** Follow predetermined AI rules (expansion, diplomacy, warfare)
- **Player-led clans:** Completely skip automatic simulation (player has full control)

---

## Design Specifications

### 1. ClanExpansionSimulator

**Purpose:** Simulate NPC-led clan growth and behavior (player-led clans bypass entirely)

**Key Features:**
- **Expansion Logic:** NPC clans build structures when treasury > 500 gold
  - 60% residential (HOUSE)
  - 30% commercial (SHOP, MARKET)
  - 10% special (TEMPLE, GUILD_HALL, BARRACKS)
- **War Logic:** Attack rivals when relationship < -50 AND military strength > 1.5x
- **Alliance Logic:** Propose alliances when relationship > 50 AND mutual enemies exist
- **Trade Logic:** Establish trade routes with neutral/friendly clans
- **Split Logic:** NPC clans split when size > 50 members (60/40 split)

**Player Clan Behavior:**
```java
private boolean isPlayerControlled(Clan clan) {
    return clan.getMembers().stream()
        .anyMatch(memberId -> playerRegistry.isPlayer(memberId));
}

public void simulateTick(...) {
    for (Clan clan : clans) {
        if (isPlayerControlled(clan)) {
            continue; // Skip ALL automatic AI behavior
        }
        // Only process NPC-led clans
        processNPCExpansion(clan, currentTick);
        processNPCDiplomacy(clan, clans, currentTick);
        processNPCWarfare(clan, clans, currentTick);
        checkForSplit(clan, currentTick);
    }
}
```

**File:** `src/main/java/org/adventure/simulation/ClanExpansionSimulator.java`  
**Lines:** ~400 (estimated)  
**Tests:** `src/test/java/org/adventure/ClanExpansionSimulatorTest.java` (~40 tests)

---

### 2. StructureLifecycleManager

**Purpose:** Handle structure aging, damage, and destruction

**Key Features:**
- **Natural Disasters:**
  - Every 1000 ticks: 5% chance of disaster per structure
  - Earthquake: 30-50% damage, 10% chance to destroy
  - Fire: 40-60% damage (wooden structures more vulnerable)
  - Flood: 20-30% damage (low-elevation structures)
  
- **Rival Clan Attacks:**
  - Check clan relationships (enemy if < -50 reputation)
  - Attack probability: `enemyMilitary / (ownMilitary + 1)`
  - Damage: 50-70% health reduction
  
- **Neglect/Abandonment:**
  - If owner has unpaid taxes > 21 days → health decays
  - Decay rate: -5% per 7 days
  - At 0 health: convert to ANCIENT_RUINS

- **Ruin Conversion:**
```java
private Structure convertToRuin(Structure original) {
    return new Structure.Builder()
        .id(original.getId() + "_ruin")
        .type(StructureType.ANCIENT_RUINS)
        .locationTileId(original.getLocationTileId())
        .health(0)
        .ownerId(null)
        .createdAtTick(currentTick)
        .build();
}
```

**File:** `src/main/java/org/adventure/simulation/StructureLifecycleManager.java`  
**Lines:** ~350 (estimated)  
**Tests:** `src/test/java/org/adventure/StructureLifecycleManagerTest.java` (~35 tests)

---

### 3. QuestDynamicGenerator

**Purpose:** Generate quests from world events

**Quest Templates:**

1. **Ruin Quest:**
   - Trigger: Structure becomes ANCIENT_RUINS
   - Quest: "Explore the ruins of [structure name]"
   - Objective: Reach ruin location
   - Reward: Loot (random items), story fragment

2. **Conflict Quest:**
   - Trigger: Two clans declare war (relationship drops below -50)
   - Quest: "Mediate dispute between [clan1] and [clan2]"
   - Objective: Talk to both clan leaders, choose outcome
   - Reward: Reputation with chosen clan

3. **Disaster Quest:**
   - Trigger: Natural disaster damages structures
   - Quest: "Help rebuild [village] after [disaster]"
   - Objective: Donate resources or labor
   - Reward: Village reputation, housing discount

**File:** `src/main/java/org/adventure/simulation/QuestDynamicGenerator.java`  
**Lines:** ~250 (estimated)  
**Tests:** `src/test/java/org/adventure/QuestDynamicGeneratorTest.java` (~30 tests)

---

### 4. RegionSimulator Integration

**Current State:**
```java
// RegionSimulator.java line 118
private void processActiveRegion(Region region) {
    double deltaTime = tickLength * activeTickRateMultiplier;
    region.regenerateResources(currentTick, deltaTime);
    // TODO: Process NPCs, events, structures (Phase 1.3+)
}
```

**Target State:**
```java
private void processActiveRegion(Region region) {
    double deltaTime = tickLength * activeTickRateMultiplier;
    region.regenerateResources(currentTick, deltaTime);
    
    // Phase 1.10.3: Dynamic world simulation
    npcLifecycleManager.simulateTick(
        region.getNPCs(),
        region.getStructures(),
        currentTick
    );
    
    clanExpansionSimulator.simulateTick(
        region.getClans(),
        region.getStructures(),
        region.getVillages(),
        currentTick
    );
    
    structureLifecycleManager.simulateTick(
        region.getStructures(),
        region.getClans(),
        currentTick
    );
    
    List<Quest> newQuests = questDynamicGenerator.generateQuestsFromEvents(
        region.getRecentEvents(),
        currentTick
    );
    region.addQuests(newQuests);
    
    List<Village> villages = villageManager.detectVillages(region.getStructures());
    region.setVillages(villages);
}
```

**File:** `src/main/java/org/adventure/region/RegionSimulator.java` (MODIFY)  
**Changes:** Add manager fields, integrate into tick processing  
**Tests:** `src/test/java/org/adventure/RegionSimulatorTest.java` (UPDATE)

---

## Implementation Plan

### Week 1: Core Simulation Systems
**Day 1-2:** ClanExpansionSimulator
- Implement NPC expansion logic
- Add player clan detection and bypass
- Test NPC-led vs player-led behavior

**Day 3:** Complete NPCLifecycleManager Integration
- NPCLifecycleManager already exists with core logic
- Add death handling and inheritance
- Test full lifecycle (birth → marriage → reproduction → death)

**Day 4:** StructureLifecycleManager
- Implement disaster logic
- Add attack mechanics
- Test ruin conversion

**Day 5:** QuestDynamicGenerator
- Implement quest templates
- Add event-to-quest mapping
- Test quest chain generation

### Week 2: Integration & Testing
**Day 6:** RegionSimulator Integration
- Add manager fields to RegionSimulator
- Integrate into tick processing
- Test simulation order (NPCs → clans → structures → quests)

**Day 7:** Full Simulation Testing
- Run 10k+ tick simulations
- Verify performance (< 5 seconds for 1000 ticks)
- Test determinism
- Verify all quality gates

---

## Quality Gates

### Determinism
- [ ] Same seed produces identical simulation results (NPC lifecycle, clan expansion, disasters)
- [ ] Checksum tests pass for all simulated entities
- [ ] Random events use seeded RNG (no `Math.random()`)

### NPC Lifecycle
- [ ] NPCs age 1 year per 10,000 ticks
- [ ] Marriage proposals occur (compatible NPCs marry)
- [ ] Children born to married couples (fertility-based)
- [ ] Death occurs naturally (age 70+, increasing probability)
- [ ] Inheritance works (spouse/children inherit possessions)

### Clan Expansion
- [ ] NPC-led clans build structures when treasury > 500
- [ ] Player-led clans skip ALL automatic behavior
- [ ] Clans split at 50+ members (NPC-led only, 60/40 split)
- [ ] New structures follow StructurePlacementRules

### Structure Lifecycle
- [ ] Disasters occur at 5% rate per 1000 ticks
- [ ] Structures convert to ANCIENT_RUINS when health = 0
- [ ] Ruins persist and can be explored

### Dynamic Quests
- [ ] Ruin quests generated when structures destroyed
- [ ] Conflict quests generated during clan wars
- [ ] Disaster quests generated after natural disasters
- [ ] Quest chains work (completion unlocks follow-ups)

### Integration
- [ ] RegionSimulator processes all managers without errors
- [ ] Simulation order correct (NPCs → clans → structures → quests → villages)
- [ ] Performance: 1000 ticks complete in < 5 seconds
- [ ] All changes persist correctly

### Testing
- [ ] 120+ new tests added (40 ClanExpansion + 35 StructureLifecycle + 30 QuestDynamic + 15 Integration)
- [ ] 667+ total tests passing (547 current + 120 new)
- [ ] 70%+ line coverage for new classes
- [ ] All determinism tests pass

---

## Success Metrics

**Phase 1.10.3 Complete When:**
- [ ] 3 new simulation classes implemented and tested
- [ ] RegionSimulator fully integrated
- [ ] 120+ new tests passing
- [ ] 667+ total tests passing project-wide
- [ ] Full 10k-tick simulation runs without errors
- [ ] NPC populations grow naturally (births > deaths initially)
- [ ] At least one NPC clan expands and builds new structures
- [ ] At least one structure becomes a ruin (disaster or neglect)
- [ ] Dynamic quests generated from events
- [ ] Player clans confirmed to skip automatic behavior
- [ ] Performance targets met (< 5 seconds per 1000 ticks)

**Milestone Achievement:**
When Phase 1.10.3 is complete, the game will have a **fully dynamic, living world** that evolves independently of player actions, while still allowing players full control over their own clans and characters.

---

## Dependencies

**Prerequisite Phases:**
- ✅ Phase 1.10.1: Named NPC system, worldgen initial conditions (COMPLETE)
- ✅ Phase 1.10.2: Village formation, roads, structure placement (COMPLETE)

**Required Systems:**
- ✅ NPCLifecycleManager (exists, needs integration)
- ✅ VillageManager (exists)
- ✅ StructurePlacementRules (exists)
- ✅ RegionSimulator (exists, needs Phase 1.10.3 integration)
- ✅ Quest & Prophecy systems (exist)

**Blocking Issues:**
- None. All prerequisites are complete.

---

## Technical Debt & Future Work

**Known Limitations:**
1. **Player Registry:** Need formal player registry system to identify player-controlled clans
2. **Event System:** Dynamic quests need robust event tracking (partially exists via story system)
3. **Save/Load:** Full simulation state needs persistence support
4. **Performance:** Large worlds (1024x1024) may need optimization

**Post-Phase 1.10.3:**
- **Phase 1.11:** Advanced diplomacy (treaties, espionage, trade negotiations)
- **Phase 1.12:** Magic system integration (magical events, prophecy fulfillment)
- **Phase 2.x:** Advanced AI (strategic planning, emergent behavior)

---

## Related Documentation

**Design Docs:**
- [Societies & Clans](../docs/societies_clans_kingdoms.md)
- [Structures & Ownership](../docs/structures_ownership.md)
- [Stories & Events](../docs/stories_events.md)
- [World Generation](../docs/world_generation.md)

**Build Guides:**
- [Phase 1.10.x Build Guide](../BUILD_PHASE1.10.x.md) — Living World overview
- [Main Build Guide](../BUILD_PHASE1.md) — Phase 1 overview
- [Gameplay Build Guide](../BUILD-GAMEPLAY.md) — UI development
- [Phase 2 Build Guide](../BUILD_PHASE2.md) — Advanced systems

**Previous Phase Summaries:**
- [Phase 1.10.1 Summary](PHASE_1.10.1_SUMMARY.md) — Named NPC system ✅
- [Phase 1.10.2 Summary](PHASE_1.10.2_SUMMARY.md) — Village formation ✅ *(TO BE CREATED)*
- [Phase 1.6 Summary](PHASE_1.6_SUMMARY.md) — Societies implementation
- [Phase 1.5 Summary](PHASE_1.5_SUMMARY.md) — Structures implementation
- [Phase 1.7 Summary](PHASE_1.7_SUMMARY.md) — Stories implementation

---

## File Checklist

**New Files (Phase 1.10.3):**
```
src/main/java/org/adventure/simulation/ (NEW PACKAGE)
├── ClanExpansionSimulator.java (~400 lines) ❌
├── StructureLifecycleManager.java (~350 lines) ❌
└── QuestDynamicGenerator.java (~250 lines) ❌

src/test/java/org/adventure/simulation/ (NEW PACKAGE)
├── ClanExpansionSimulatorTest.java (~40 tests) ❌
├── StructureLifecycleManagerTest.java (~35 tests) ❌
└── QuestDynamicGeneratorTest.java (~30 tests) ❌
```

**Modified Files:**
```
src/main/java/org/adventure/region/RegionSimulator.java (ADD integration) ❌
src/test/java/org/adventure/region/RegionSimulatorTest.java (UPDATE tests) ❌
```

**Total Estimated Impact:**
- **New Lines:** ~1,000 production code + ~1,500 test code
- **New Tests:** 120+ tests
- **Modified Files:** 2 files
- **New Packages:** 1 package (`simulation`)

---

**Status:** 🚧 **NOT YET STARTED**  
**Ready to Begin:** ✅ All prerequisites complete  
**Estimated Duration:** 2 weeks (10 working days)  
**Complexity:** HIGH (simulation systems, integration, extensive testing)

---

**END OF PHASE_1.10.3_SUMMARY.md**
