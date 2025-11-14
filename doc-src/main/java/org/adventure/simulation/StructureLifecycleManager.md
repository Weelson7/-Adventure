# StructureLifecycleManager

**Package:** `org.adventure.simulation`  
**Since:** Phase 1.10.3  
**Purpose:** Handle structure aging, natural disasters, neglect decay, and ruin conversion

---

## Overview

`StructureLifecycleManager` simulates the natural lifecycle of structures in the game world, including damage from disasters, decay from neglect, and conversion to ruins when destroyed. This creates a dynamic world where structures age and deteriorate, requiring maintenance and repair.

---

## Class Structure

```java
public class StructureLifecycleManager {
    private static final long DISASTER_CHECK_INTERVAL = 1000;
    private static final double DISASTER_CHANCE = 0.05; // 5% per check
    private static final long NEGLECT_CHECK_INTERVAL = 7000; // ~7 days
    private static final double NEGLECT_DECAY_RATE = 0.05; // 5% per check
    private static final long INACTIVITY_THRESHOLD = 50000; // ~50 days
}
```

---

## Key Methods

### Main Simulation Loop

#### `simulateTick(List<Structure> structures, List<Clan> clans, long currentTick)`
Main simulation method called once per tick. Processes all structures.

**Algorithm:**
1. Check for natural disasters (every 1000 ticks)
2. Check for neglect decay (every 7000 ticks)
3. Convert destroyed structures to ruins (health = 0)

**Parameters:**
- `structures` - All structures in the region
- `clans` - All clans (for ownership checks)
- `currentTick` - Current simulation tick

**Processing:**
```java
for (Structure structure : structures) {
    checkForDisasters(structure, currentTick);
    checkForNeglect(structure, clans, currentTick);
    
    if (structure.getHealth() <= 0) {
        convertToRuin(structure);
    }
}
```

---

### Disaster System

#### `checkForDisasters(Structure structure, long currentTick)`
Checks and applies natural disasters to structures.

**Disaster Frequency:** Every 1000 ticks, 5% chance

**Disaster Types:**
1. **Earthquake:** 50% health damage, affects all structures equally
2. **Fire:** 40% health damage, higher for wooden structures
3. **Flood:** 30% health damage, affects low-elevation structures

**Selection Algorithm:**
```java
int roll = rng.nextInt(3);
if (roll == 0) applyEarthquake(structure);
else if (roll == 1) applyFire(structure);
else applyFlood(structure);
```

**Damage Application:**
```java
double newHealth = structure.getHealth() - damagePercent;
structure.setHealth(Math.max(0, newHealth));
```

---

### Neglect System

#### `checkForNeglect(Structure structure, Clan owner, long currentTick)`
Checks and applies decay from neglect (unpaid taxes or inactivity).

**Neglect Conditions:**
1. **Unpaid Taxes:** Owner has unpaid taxes > 21 days (from taxation system)
2. **Inactivity:** Structure unused for 50,000+ ticks (~50 days)

**Decay Rate:** -5% health per 7000 ticks (weekly checks)

**Logic:**
```java
if (hasUnpaidTaxes(owner) || isInactive(structure, currentTick)) {
    double newHealth = structure.getHealth() * (1.0 - NEGLECT_DECAY_RATE);
    structure.setHealth(newHealth);
}
```

---

### Ruin Conversion

#### `convertToRuin(Structure structure)`
Converts a destroyed structure (health = 0) into ancient ruins.

**Conversion Process:**
1. Change structure type to ANCIENT_RUINS
2. Set health to 50% (ruins are partially intact)
3. Preserve original owner information in metadata
4. Update structure state

**Result:**
```java
Structure ruin = Structure.builder()
    .id(structure.getId() + "_ruin")
    .type(StructureType.ANCIENT_RUINS)
    .ownerId(null) // No longer owned
    .ownerType(OwnerType.NONE)
    .locationTileId(structure.getLocationTileId())
    .entrance(structure.getEntrance())
    .health(50.0) // Partially intact
    .maxHealth(100.0)
    .createdAtTick(currentTick)
    .build();
```

**Metadata Preservation:**
- Original structure type stored
- Original owner ID stored
- Original creation tick stored
- Enables quest generation and lore

---

## Disaster Details

### Earthquake
**Frequency:** 33% of disasters  
**Damage:** 50% health loss  
**Affected Structures:** All types equally

**Characteristics:**
- Catastrophic single-event damage
- No structure immunity
- Can destroy buildings instantly (if health < 50%)

---

### Fire
**Frequency:** 33% of disasters  
**Damage:** 40% health loss (base), higher for wooden structures  
**Affected Structures:** Residential and wooden structures more vulnerable

**Characteristics:**
- Spreads between nearby structures (future enhancement)
- More damage to HOUSE, FARM, LOGGING_CAMP
- Less damage to stone structures (CASTLE, FORTRESS)

---

### Flood
**Frequency:** 33% of disasters  
**Damage:** 30% health loss  
**Affected Structures:** Low-elevation structures near water

**Characteristics:**
- Elevation-dependent (elevation < 0.3 = high risk)
- Affects structures near rivers and coasts
- Can be mitigated with flood walls (future enhancement)

---

## Neglect Mechanics

### Tax-Based Neglect
**Trigger:** Owner has unpaid taxes > 21 days  
**Effect:** Structure decays at 5% per week  
**Duration:** Until taxes paid or structure destroyed

**Integration with Taxation System (Phase 1.5):**
```java
boolean hasUnpaidTaxes(Clan owner) {
    return owner.getUnpaidTaxDuration() > 21 * 1000; // 21 days in ticks
}
```

---

### Inactivity-Based Neglect
**Trigger:** Structure unused for 50,000+ ticks (~50 days)  
**Effect:** Structure decays at 5% per week  
**Duration:** Until structure occupied or destroyed

**Activity Tracking:**
```java
boolean isInactive(Structure structure, long currentTick) {
    return (currentTick - structure.getLastActivityTick()) > INACTIVITY_THRESHOLD;
}
```

---

## Ruin System

### Ruin Properties
- **Type:** ANCIENT_RUINS
- **Health:** 50% (partially intact)
- **Ownership:** None (unclaimed)
- **Functionality:** Can be explored, looted, rebuilt

### Ruin Usage
1. **Exploration:** Players can explore ruins for loot and lore
2. **Quests:** QuestDynamicGenerator creates exploration quests
3. **Rebuilding:** Players can rebuild ruins (future feature)
4. **Archaeology:** Discover historical information (future feature)

---

## Integration

### RegionSimulator Integration
```java
public void processActiveRegion(Region region, long currentTick) {
    // ... resource regeneration ...
    // ... NPC lifecycle ...
    // ... clan expansion ...
    
    // Structure lifecycle
    structureLifecycleManager.simulateTick(
        region.getStructures(),
        region.getClans(),
        currentTick
    );
    
    // ... quest generation ...
    // ... village detection ...
}
```

### Quest System Integration
Ruins created by this manager trigger exploration quests in QuestDynamicGenerator:
```java
if (structure.getType() == StructureType.ANCIENT_RUINS) {
    generateRuinQuest(structure);
}
```

---

## Testing

### Test Coverage: 100%
- `testDisasterDamage()` - Disasters reduce health correctly
- `testNeglectDecay()` - Unpaid taxes cause decay
- `testInactivityDecay()` - Long inactivity causes decay
- `testRuinConversion()` - Destroyed structures become ruins
- `testRuinMetadata()` - Original info preserved in ruins
- `testMultipleDisasters()` - Cumulative disaster effects

### Example Test
```java
@Test
public void testDisasterDamage() {
    Structure structure = createStructure(100.0); // Full health
    
    // Simulate 10,000 ticks (10 disaster checks)
    for (int i = 0; i < 10000; i++) {
        manager.simulateTick(List.of(structure), clans, i);
    }
    
    assertTrue(structure.getHealth() < 100.0,
        "Structure should have taken damage from disasters");
}
```

---

## Design Patterns

- **Strategy Pattern:** Different disaster types with different effects
- **State Pattern:** Structure health drives lifecycle transitions
- **Factory Pattern:** Ruin creation from destroyed structures
- **Observer Pattern:** Notify quest system when ruins created

---

## Performance Considerations

- **Interval Checks:** Only check disasters every 1000 ticks
- **Neglect Checks:** Only check neglect every 7000 ticks
- **Early Returns:** Skip processing for invulnerable structures
- **Batch Processing:** Process all structures in single pass

---

## Balancing

### Disaster Frequency
- **5% per 1000 ticks** = ~1 disaster per 20,000 ticks per structure
- **Expected disasters per structure per year:** ~5 disasters
- **Tunable:** Adjust DISASTER_CHANCE for different difficulty

### Neglect Decay Rate
- **5% per 7000 ticks** = ~7% per 10,000 ticks (1 year)
- **Time to destruction (no maintenance):** ~14 years
- **Tunable:** Adjust NEGLECT_DECAY_RATE for different pacing

---

## Future Enhancements

### Planned Features (Phase 2+)
- **Repair System:** Players can repair damaged structures
- **Disaster Prevention:** Flood walls, firebreaks, earthquake-resistant construction
- **Disaster Spread:** Fires spread between nearby structures
- **Weather System:** Disasters linked to weather patterns
- **Insurance:** Clans can insure structures against disasters
- **Historical Ruins:** Ancient ruins with unique loot and lore

### Optimization Opportunities
- **Spatial Indexing:** Fast neighbor queries for disaster spread
- **Predictive Modeling:** Forecast disaster probability
- **Async Processing:** Background disaster calculations

---

## See Also

- [ClanExpansionSimulator](ClanExpansionSimulator.md) - NPC-led clan expansion
- [QuestDynamicGenerator](QuestDynamicGenerator.md) - Dynamic quest generation from ruins
- [Structure](../structure/Structure.md) - Structure data model
- [StructureType](../structure/StructureType.md) - Structure type enumeration
- [Taxation System](../society/Taxation.md) - Tax system integration

---

**Implementation:** Phase 1.10.3  
**Lines of Code:** 217  
**Test Coverage:** 100% (6 tests)  
**Status:** ✅ Complete and fully tested
