# NPCLifecycleManager

**Package:** `org.adventure.npc`  
**Type:** Class (Manager)  
**Since:** Phase 1.10.1

---

## Overview

`NPCLifecycleManager` handles NPC lifecycle events over time:
- **Aging:** NPCs age 1 year per 10,000 ticks
- **Marriage:** Automatic marriage proposals between compatible NPCs
- **Reproduction:** Married couples have children based on fertility
- **Death:** Natural death for elderly NPCs (age 70+)
- **Inheritance:** Treasury and possessions pass to family

This manager integrates with `RegionSimulator` for tick-based simulation.

---

## Constants

```java
private static final int TICKS_PER_YEAR = 10000;          // 1 year = 10k ticks
private static final int REPRODUCTION_COOLDOWN = 5000;     // ~6 months between checks
private static final int MARRIAGE_AGE_MIN = 18;            // Minimum marriage age
private static final int DEATH_AGE_START = 70;             // Death chance starts at 70
private static final int MAX_NPCS_PER_HOUSE = 4;           // Housing capacity
```

---

## Key Methods

### `tick()`
Main simulation method called each game tick.

**Signature:**
```java
public void tick(
    long currentTick,
    List<NamedNPC> npcs,
    List<Structure> structures
)
```

**Algorithm:**
```
For each NPC:
  1. Update age (if 10k ticks passed since last birthday)
  2. Check marriage eligibility (if unmarried adult)
  3. Check reproduction (if married and fertile)
  4. Check death (if elderly)
  5. Update job (if child aged to 18)
```

---

### `updateAge()`
Ages NPCs based on ticks since birth.

**Signature:**
```java
private void updateAge(NamedNPC npc, long currentTick)
```

**Algorithm:**
```java
long ticksSinceBirth = currentTick - npc.getBirthTick();
int newAge = (int)(ticksSinceBirth / TICKS_PER_YEAR);

if (newAge != npc.getAge()) {
    npc.setAge(newAge);
    
    // Update job for children aging into adulthood
    if (newAge == 18 && npc.getJob() == NPCJob.CHILD) {
        assignAdultJob(npc);
    }
    
    // Update fertility
    npc.setFertility(calculateFertility(newAge, npc.getGender()));
}
```

**Effects:**
- Age increments at exact 10,000 tick intervals
- Children become adults at age 18 (job assigned)
- Fertility recalculated each birthday

---

### `attemptMarriage()`
Attempts to marry an unmarried adult NPC.

**Signature:**
```java
private void attemptMarriage(
    NamedNPC npc,
    List<NamedNPC> allNpcs,
    long currentTick
)
```

**Marriage Criteria:**
- Both NPCs age >= 18
- Both unmarried (spouseId == null)
- Same clan (clanId matches)
- Age difference <= 10 years
- Random chance: 10% per check (every 5,000 ticks)

**Algorithm:**
```
1. Find compatible partners:
   - Same clan
   - Age 18+
   - Unmarried
   - Age difference <= 10 years
2. If no candidates → return
3. Roll 10% chance (seeded RNG)
4. If successful:
   - Set spouseId for both NPCs
   - Move partner to NPC's home
   - Generate "Marriage" event (future)
```

**Example:**
```
NPC: Aldric, age 27, unmarried
Candidates: [Aria (age 25), Elara (age 30), Mira (age 40)]
Selected: Aria (age 25, closest to Aldric's age)
Result: Aldric.spouseId = Aria.id, Aria.spouseId = Aldric.id
```

---

### `attemptReproduction()`
Attempts to create a child for a married couple.

**Signature:**
```java
private void attemptReproduction(
    NamedNPC npc,
    List<NamedNPC> allNpcs,
    List<Structure> structures,
    long currentTick
)
```

**Reproduction Requirements:**
- NPC is married (spouseId != null)
- NPC has fertility > 0
- Cooldown elapsed (5,000 ticks since last check)
- Home has space (< 4 NPCs)
- Random chance based on fertility

**Algorithm:**
```
1. Check cooldown (5k ticks since last check)
2. Find home structure
3. Count occupants (NPCs in same home)
4. If home full (>= 4) → return
5. Roll chance: fertility / 100
   - Example: 80 fertility = 80% chance
6. If successful:
   - Generate child NPC (age 0, gender random)
   - Add child to parent's childrenIds
   - Add child to spouse's childrenIds
   - Add child to clan
   - Generate "Birth" event (future)
7. Update lastReproductionCheck
```

**Reproduction Rates:**
| Parent Age | Fertility | Annual Birth Chance* |
|------------|-----------|---------------------|
| 20-35 | 85-100 | ~85-100% (1-2 children) |
| 27 (peak) | 100 | ~100% (2 children) |
| 40 | 61 | ~61% (1 child) |
| 45 | 6 | ~6% (rare) |

*Per year (2 checks × fertility%)

---

### `attemptDeath()`
Checks if an elderly NPC dies of natural causes.

**Signature:**
```java
private void attemptDeath(
    NamedNPC npc,
    List<NamedNPC> allNpcs,
    long currentTick
)
```

**Death Probability:**
| Age | Death Chance (per check) |
|-----|-------------------------|
| 70-74 | 1% |
| 75-79 | 5% |
| 80-84 | 20% |
| 85-89 | 50% |
| 90-94 | 90% |
| 95+ | 100% |

**Algorithm:**
```
1. Calculate death chance based on age
2. Roll random (seeded)
3. If death occurs:
   - Handle inheritance:
     * Spouse becomes widowed (spouseId = null)
     * Children remain linked (childrenIds)
     * Treasury/possessions to spouse or children
   - Remove NPC from world
   - Generate "Death" event (future)
```

**Inheritance Rules:**
- **Spouse exists:** Spouse inherits everything, becomes widowed
- **No spouse, children exist:** Split equally among children
- **No family:** Clan inherits (treasury to clan)

---

## Integration

### With RegionSimulator
```java
// In RegionSimulator.simulateTick()
public void simulateTick(Region region, long currentTick) {
    // Existing logic...
    
    // NPC lifecycle
    npcLifecycleManager.tick(
        currentTick,
        region.getNPCs(),
        region.getStructures()
    );
    
    // Other simulation...
}
```

### With WorldGen
```java
// At worldgen, NPCs start at tick 0
WorldGen gen = new WorldGen(256, 256);
gen.generate(12345L);
// All NPCs have birthTick = 0, age varies
// First tick() call at tick 1 starts lifecycle
```

---

## Lifecycle Flow Diagram

```
BIRTH (worldgen or reproduction)
  ↓
CHILDHOOD (age 0-17, job=CHILD)
  ↓ age 18
ADULTHOOD (age 18-60)
  ↓ compatible NPCs nearby
MARRIAGE (spouseId set)
  ↓ fertility > 0, home has space
REPRODUCTION (children born)
  ↓ age 60
ELDERHOOD (job=UNEMPLOYED, fertility=0)
  ↓ age 70+
DEATH (increasing probability)
  ↓
INHERITANCE (family inherits)
  ↓
REMOVAL (NPC removed from world)
```

---

## Tick Schedule

**Every Tick (1-10k):**
- Age updates (when 10k ticks pass)

**Every 5,000 Ticks (~6 months):**
- Marriage checks (unmarried adults)
- Reproduction checks (married couples)

**Every 1,000 Ticks (~1.2 months):**
- Death checks (elderly NPCs)

---

## Usage Examples

### Basic Simulation
```java
NPCLifecycleManager manager = new NPCLifecycleManager();
List<NamedNPC> npcs = worldGen.getNPCs();
List<Structure> structures = worldGen.getStructures();

for (long tick = 0; tick < 100000; tick++) {
    manager.tick(tick, npcs, structures);
    
    // Check for events
    if (tick % 10000 == 0) {
        System.out.println("Year " + (tick / 10000) + ": " + 
            npcs.size() + " NPCs");
    }
}
```

### Marriage Statistics
```java
long married = npcs.stream()
    .filter(npc -> npc.getSpouseId() != null)
    .count();

double marriageRate = (double)married / npcs.size();
System.out.println("Marriage rate: " + (marriageRate * 100) + "%");
```

### Birth/Death Tracking
```java
int initialPopulation = npcs.size();
manager.tick(currentTick, npcs, structures);
int newPopulation = npcs.size();

int births = Math.max(0, newPopulation - initialPopulation);
int deaths = Math.max(0, initialPopulation - newPopulation);

System.out.println("Births: " + births + ", Deaths: " + deaths);
```

---

## Testing

### Unit Tests
```java
@Test
public void testAging() {
    NamedNPC npc = createTestNPC(age=25, birthTick=0);
    
    // Simulate 10k ticks (1 year)
    manager.tick(10000L, List.of(npc), structures);
    
    assertEquals(26, npc.getAge());
}

@Test
public void testMarriage() {
    NamedNPC male = createTestNPC(age=27, gender=MALE);
    NamedNPC female = createTestNPC(age=25, gender=FEMALE);
    
    // Simulate until married (max 100k ticks)
    for (long tick = 0; tick < 100000; tick += 5000) {
        manager.tick(tick, List.of(male, female), structures);
        if (male.isMarried()) break;
    }
    
    assertTrue(male.isMarried());
    assertEquals(female.getId(), male.getSpouseId());
}

@Test
public void testReproduction() {
    NamedNPC parent1 = createMarriedNPC(age=27, fertility=100);
    NamedNPC parent2 = createMarriedNPC(age=25, fertility=100);
    List<NamedNPC> npcs = new ArrayList<>(List.of(parent1, parent2));
    
    // Simulate 1 year
    for (long tick = 0; tick < 10000; tick++) {
        manager.tick(tick, npcs, structures);
    }
    
    // Should have at least 1 child (high fertility)
    assertTrue(npcs.size() > 2);
}

@Test
public void testDeath() {
    NamedNPC elder = createTestNPC(age=95, birthTick=-950000);
    List<NamedNPC> npcs = new ArrayList<>(List.of(elder));
    
    // Death is 100% at age 95
    manager.tick(0L, npcs, structures);
    
    assertTrue(npcs.isEmpty()); // NPC removed
}
```

---

## Performance Considerations

**Optimization Tips:**
- Cache structure lookups (avoid repeated iteration)
- Use spatial indexing for home-finding
- Batch marriage checks (process every 5k ticks, not every tick)
- Skip player-controlled NPCs (isPlayer==true)

**Expected Load:**
- 500 NPCs: ~5ms per tick (acceptable)
- 5,000 NPCs: ~50ms per tick (may need optimization)
- 50,000 NPCs: ~500ms per tick (requires batching/async)

---

## Related Classes

- `NamedNPC` — Entity class for named NPCs
- `NPCGenerator` — Factory for creating NPCs
- `Gender` — Enum for NPC gender
- `NPCJob` — Enum for NPC occupations
- `RegionSimulator` — Calls NPCLifecycleManager.tick()
- `PlayerNPCInteraction` — Player marriage/reproduction
