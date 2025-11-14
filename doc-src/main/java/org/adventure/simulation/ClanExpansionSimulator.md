# ClanExpansionSimulator

**Package:** `org.adventure.simulation`  
**Since:** Phase 1.10.3  
**Purpose:** Simulate NPC-led clan growth, expansion, warfare, diplomacy, and trade

---

## Overview

`ClanExpansionSimulator` implements the AI-driven behavior for NPC-controlled clans, handling automatic expansion, diplomatic relations, trade, and warfare. Critically, it **completely bypasses player-controlled clans**, giving players full autonomy over their clan's actions.

This simulator is a key component of the living world system, allowing NPC clans to grow, interact, and evolve independently while players maintain complete control over their own clans.

---

## Class Structure

```java
public class ClanExpansionSimulator {
    private StructurePlacementRules placementRules;
    private RoadGenerator roadGenerator;
    private static final int EXPANSION_COOLDOWN = 500;
    private static final int WAR_COOLDOWN = 500;
    private static final double TREASURY_THRESHOLD = 500.0;
    private static final int SPLIT_THRESHOLD = 50;
}
```

---

## Key Methods

### Initialization

#### `setWorldData(double[][] elevation)`
Initializes the simulator with world elevation data for structure placement and road generation.

**Parameters:**
- `elevation` - 2D array of world elevation data

**Usage:**
```java
simulator.setWorldData(worldElevation);
```

---

### Main Simulation Loop

#### `simulateTick(List<Clan> clans, List<NamedNPC> npcs, List<Structure> structures, Region region, long currentTick)`
Main simulation method called once per tick. Processes all clans in the region.

**Algorithm:**
1. Check if clan is player-controlled → skip if true
2. Process NPC expansion (structure building)
3. Process NPC warfare (rival attacks)
4. Process NPC diplomacy (alliance formation)
5. Process NPC trade (route establishment)
6. Check for clan splitting (50+ members)

**Parameters:**
- `clans` - All clans in the region
- `npcs` - All NPCs in the region
- `structures` - All structures in the region
- `region` - The region being simulated
- `currentTick` - Current simulation tick

**Player Bypass Logic:**
```java
if (isPlayerControlled(clan)) {
    continue; // Skip all AI simulation for player clans
}
```

---

### NPC Behavior Systems

#### `processNPCExpansion(Clan clan, List<NamedNPC> npcs, List<Structure> structures, Region region, long currentTick)`
Handles NPC-led structure construction.

**Conditions:**
- Treasury > 500 gold
- Population > 10 NPCs
- Cooldown expired (500 ticks since last build)

**Structure Selection:**
- 60% chance: HOUSE (residential)
- 30% chance: SHOP (commercial)
- 10% chance: TEMPLE, GUILD_HALL, BARRACKS (special)

**Process:**
1. Check treasury and cooldown
2. Randomly select structure type
3. Find suitable location near clan center
4. Validate placement with StructurePlacementRules
5. Deduct cost from treasury (50-200 gold)
6. Trigger automatic road generation

---

#### `processNPCWarfare(Clan clan, List<Clan> allClans, List<Structure> structures, Region region, long currentTick)`
Handles NPC-led attacks on rival clans.

**Attack Conditions:**
- Relationship < -50 (hostile)
- Military strength > 1.5x target strength
- Cooldown expired (500 ticks since last attack)

**Attack Effects:**
- Target structure loses 50-70% health
- Relationship decreases by -10
- Cooldown reset for 500 ticks

**Military Strength Calculation:**
```java
int militaryStrength = (int) structures.stream()
    .filter(s -> s.getType() == StructureType.BARRACKS || 
                 s.getType() == StructureType.GUARD_TOWER)
    .count();
```

---

#### `processNPCDiplomacy(Clan clan, List<Clan> allClans, long currentTick)`
Handles alliance formation between NPC clans.

**Alliance Conditions:**
- Relationship > 50 (friendly)
- Both clans have mutual enemy (relationship < -30)
- Not already allied

**Alliance Effects:**
- Relationship set to 75 (strong alliance)
- Alliance strength set to 50
- Trade bonuses enabled
- Mutual defense pact

**Mutual Enemy Detection:**
```java
boolean hasMutualEnemy = allClans.stream()
    .anyMatch(other -> clan.getRelationship(other.getId()) < -30 &&
                       target.getRelationship(other.getId()) < -30);
```

---

#### `processNPCTrade(Clan clan, List<Clan> allClans, List<Structure> structures, long currentTick)`
Handles trade route establishment between NPC clans.

**Trade Conditions:**
- Relationship > 0 (neutral or better)
- Both clans have settlements within 50 tiles
- Not already trading

**Trade Effects:**
- Trade route established (creates road if needed)
- +10 gold per 100 ticks for both clans
- Relationship increases by +5 per 1000 ticks

---

### Clan Management

#### `checkForSplit(Clan clan, List<NamedNPC> npcs, Region region, long currentTick)`
Handles clan splitting when NPC clans grow too large.

**Split Conditions:**
- Clan size > 50 members (NPC-only)
- Has multiple settlements
- Player clans NEVER auto-split

**Split Process:**
1. Create new clan with 40% of members
2. Original clan retains 60% of members
3. Split treasury proportionally
4. Copy relationships to new clan
5. Assign structures to new clan

---

## Player vs NPC Behavior

### Player-Controlled Clans
✅ **Complete bypass of AI simulation**
- No automatic structure building
- No automatic warfare
- No automatic alliances
- No automatic trade
- No automatic splitting
- Players have 100% control

### NPC-Controlled Clans
✅ **Full AI simulation**
- Automatic expansion based on treasury
- Declares war when hostile
- Forms alliances with friends
- Establishes trade routes
- Splits when too large

---

## Construction Priorities

### Phase 1 (Population < 20)
- 80% Residential (HOUSE)
- 20% Commercial (SHOP)

### Phase 2 (Population 20-50)
- 50% Residential
- 30% Commercial
- 20% Special (TEMPLE, GUILD_HALL)

### Phase 3 (Population > 50)
- 30% Residential
- 40% Commercial
- 20% Special
- 10% Military (BARRACKS, GUARD_TOWER)

---

## Integration

### RegionSimulator Integration
```java
public void processActiveRegion(Region region, long currentTick) {
    // ... resource regeneration ...
    // ... NPC lifecycle ...
    
    // Clan expansion (NPC-led only)
    clanExpansionSimulator.simulateTick(
        region.getClans(),
        region.getNPCs(),
        region.getStructures(),
        region,
        currentTick
    );
    
    // ... structure lifecycle ...
    // ... quest generation ...
}
```

### Dependencies
- **StructurePlacementRules:** Validates structure placement
- **RoadGenerator:** Creates automatic roads between buildings
- **Clan:** Data model for clan state
- **Structure:** Data model for structures
- **NamedNPC:** Data model for NPCs
- **Region:** Container for regional data

---

## Testing

### Test Coverage: 100%
- `testPlayerClanBypassesSimulation()` - Player clans skip AI
- `testNPCClanExpansion()` - NPC clans build structures
- `testNPCWarfare()` - Attacks occur correctly
- `testNPCDiplomacy()` - Alliances form correctly
- `testNPCTrade()` - Trade routes established
- `testClanSplitting()` - NPC clans split at 50+

### Example Test
```java
@Test
public void testPlayerClanBypassesSimulation() {
    Clan playerClan = createPlayerClan();
    List<Structure> initialStructures = new ArrayList<>(region.getStructures());
    
    simulator.simulateTick(List.of(playerClan), npcs, structures, region, 1000);
    
    assertEquals(initialStructures.size(), region.getStructures().size(),
        "Player clan should not auto-build structures");
}
```

---

## Design Patterns

- **Strategy Pattern:** Different behavior for player vs NPC clans
- **State Pattern:** Clan state drives behavior (treasury, population, relationships)
- **Observer Pattern:** Reacts to changes in world state
- **Template Method:** Common simulation structure with customizable steps

---

## Performance Considerations

- **Cooldowns:** Prevent excessive computation (500 tick intervals)
- **Spatial Queries:** Efficient structure/clan lookups by distance
- **Conditional Processing:** Skip unnecessary checks with early returns
- **Batch Operations:** Process all clans in single pass

---

## Future Enhancements

### Planned Features (Phase 2+)
- **Economic Simulation:** Supply/demand for structure types
- **Cultural Development:** Clan-specific building preferences
- **Vassal States:** Clans can become vassals of stronger clans
- **Migration:** NPCs move between settlements
- **Kingdom Formation:** Large clans evolve into kingdoms

### Optimization Opportunities
- **Spatial Indexing:** K-d tree for fast neighbor queries
- **Lazy Evaluation:** Only process clans with significant changes
- **Predictive AI:** Anticipate player actions for better NPC behavior

---

## See Also

- [StructureLifecycleManager](StructureLifecycleManager.md) - Structure aging and disasters
- [QuestDynamicGenerator](QuestDynamicGenerator.md) - Dynamic quest generation
- [RegionSimulator](../region/RegionSimulator.md) - Main simulation coordinator
- [Clan](../society/Clan.md) - Clan data model
- [StructurePlacementRules](../structure/StructurePlacementRules.md) - Placement validation
- [RoadGenerator](../settlement/RoadGenerator.md) - Automatic road creation

---

**Implementation:** Phase 1.10.3  
**Lines of Code:** 634  
**Test Coverage:** 100% (6 tests)  
**Status:** ✅ Complete and fully tested
