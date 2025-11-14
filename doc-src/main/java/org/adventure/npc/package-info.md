# org.adventure.npc

**Package:** `org.adventure.npc`  
**Since:** Phase 1.10.1

---

## Overview

The `npc` package provides the **Named NPC System** for !Adventure, enabling named characters with homes, jobs, marriages, and lifecycle events. This system creates a living, dynamic world populated by NPCs who age, marry, reproduce, and die over time.

---

## Key Features

### Named NPCs
- **Unique identities:** Each NPC has a name, age, gender, clan affiliation
- **Deterministic generation:** Same seed produces identical NPCs
- **Lifecycle tracking:** Birth, aging, marriage, reproduction, death

### Job System
- **17 job types:** From FARMER to WIZARD
- **Workplace requirements:** Jobs tied to structures (BLACKSMITH → FORGE)
- **Production values:** Jobs generate income for clans

### Marriage & Reproduction
- **Automatic marriages:** ~50% of adults marry at worldgen
- **Fertility system:** Age-based fertility (peak at 27)
- **Children:** Born to married couples, inherit home and clan

### Player Integration
- **Player-NPC marriage:** Players can marry compatible NPCs
- **Player-NPC reproduction:** Players can have children with spouses
- **Manual control:** Players control timing (no automatic lifecycle)

---

## Core Classes

### Entity Classes
- **`NamedNPC`** — Individual NPC entity with stats, home, job, family
- **`Gender`** — Enum for NPC gender (MALE, FEMALE)
- **`NPCJob`** — Enum for NPC occupations (17 types)

### Generator Classes
- **`NPCGenerator`** — Factory for creating NPCs at worldgen
  - Generates individual NPCs
  - Creates clan populations (age distribution, marriages)
  - Assigns jobs based on available structures

### Manager Classes
- **`NPCLifecycleManager`** — Manages NPC lifecycle events
  - Aging (1 year per 10k ticks)
  - Marriage proposals (10% chance per 5k ticks)
  - Reproduction (fertility-based)
  - Natural death (age 70+)

### Interaction Classes
- **`PlayerNPCInteraction`** — Player-NPC marriage and reproduction
  - Manual marriage system
  - Manual reproduction system
  - Trait inheritance (future)

---

## Architecture

### Data Flow

```
WORLDGEN (tick 0)
  ↓
ClanGenerator
  → Creates clans with member counts
  ↓
SettlementGenerator
  → Creates structures (HOUSE, FORGE, etc.)
  ↓
NPCGenerator
  → Generates NPCs for each clan
  → Assigns homes (max 4 per HOUSE)
  → Assigns jobs (based on workplaces)
  → Creates initial marriages (~50%)
  ↓
WorldGen.npcs (List<NamedNPC>)

SIMULATION (tick 1+)
  ↓
RegionSimulator.tick()
  ↓
NPCLifecycleManager.tick()
  → Ages NPCs (10k ticks = 1 year)
  → Marriage attempts (unmarried adults)
  → Reproduction attempts (married couples)
  → Death checks (elderly NPCs)
  ↓
WorldState updated (marriages, births, deaths)
```

### Determinism Strategy

All NPC generation and lifecycle events use **seeded random** for determinism:

```java
// WorldGen phase 12: Generate NPCs
Random rng = new Random(worldSeed ^ 0xNPC);

for (Clan clan : clans) {
    List<NamedNPC> clanNPCs = NPCGenerator.generateInitialClanPopulation(
        clan, clanStructures, 0L, rng);
    allNPCs.addAll(clanNPCs);
}

// Same seed → same NPCs (names, ages, jobs, marriages)
```

**No UUID.randomUUID()** — All IDs are hash-based:
```java
String id = "npc_" + clanId + "_" + Math.abs(hash);
```

---

## Job System

### Job Types and Workplaces

| Job | Workplace | Production (gold/1k ticks) | Notes |
|-----|-----------|----------------------------|-------|
| CHILD | None | 0 | Age 0-17 |
| UNEMPLOYED | None | 0 | No job assigned |
| FARMER | FARM | 50 | Food production |
| BLACKSMITH | FORGE | 100 | Tool/weapon crafting |
| MINER | MINE | 40 | Ore extraction |
| LUMBERJACK | LOGGING_CAMP | 30 | Wood harvesting |
| MERCHANT | SHOP | 80 | Trade goods |
| INNKEEPER | INN | 60 | Hospitality services |
| WARRIOR | BARRACKS | 20 | Military (costs gold) |
| GUARD | GUARD_TOWER | 30 | Security |
| PRIEST | TEMPLE | 70 | Religious services |
| WIZARD | WIZARD_TOWER | 90 | Magical services |
| GUILD_MASTER | GUILD_HALL | 100 | Guild management |

### Job Assignment Algorithm

```java
1. At worldgen:
   - List available workplaces (structures owned by clan)
   - For each adult NPC:
     * Get jobs matching available workplaces
     * Randomly assign from available jobs
     * If no workplaces → UNEMPLOYED
     
2. When child ages to 18:
   - Re-run job assignment
   - Assign first available job with workplace
   - Otherwise → UNEMPLOYED

3. When new structure built:
   - Reassign unemployed NPCs to new job
   - Priority: longest unemployed first
```

---

## Marriage System

### Initial Marriages (Worldgen)
```java
// In NPCGenerator.generateInitialClanPopulation()
createInitialMarriages(npcs, rng);

// Algorithm:
1. Filter unmarried adults (age 18-60)
2. Separate by gender
3. Shuffle both lists (seeded)
4. Marry all possible pairs (min(males, females))
5. Set spouseId for both NPCs
6. Move to same home

// Result: ~50% of adults married (depends on gender balance)
```

### Dynamic Marriages (Simulation)
```java
// In NPCLifecycleManager.attemptMarriage()
// Every 5,000 ticks per unmarried adult:
1. Find compatible partners:
   - Same clan
   - Age difference <= 10 years
   - Unmarried
2. Roll 10% chance
3. If successful → marry random compatible partner
```

---

## Reproduction System

### Fertility Formula
```java
int calculateFertility(int age, Gender gender) {
    if (age < 18 || age > 45) return 0;
    
    int peak = (age >= 20 && age <= 35) ? 100 : 60;
    int decline = Math.abs(27 - age) * 3;
    
    return Math.max(0, peak - decline);
}
```

**Fertility Curve:**
```
100 |        ╱‾‾‾‾‾‾‾‾‾‾‾‾╲
    |      ╱                ╲
 60 |    ╱                    ╲
    |  ╱                        ╲
  0 |_|___________________________|_
    18  20  25  27  30  35  40  45
```

### Reproduction Algorithm
```java
// Every 5,000 ticks per married NPC:
1. Check fertility > 0
2. Check home has space (< 4 occupants)
3. Roll chance: fertility / 100
4. If successful:
   - Create child (age 0, random gender)
   - Add to parents' childrenIds
   - Add to clan
   - Generate "Birth" event

// Annual birth rate (2 checks per year):
// - 100 fertility = ~100% (1-2 children/year)
// - 50 fertility = ~50% (~1 child/2 years)
```

---

## Death System

### Death Probability

```java
double getDeathChance(int age) {
    if (age < 70) return 0.0;
    if (age >= 95) return 1.0;
    if (age >= 90) return 0.9;
    if (age >= 85) return 0.5;
    if (age >= 80) return 0.2;
    if (age >= 75) return 0.05;
    return 0.01;
}
```

**Life Expectancy:**
- Median: ~80 years (50% die by 85)
- Maximum: ~95 years (100% die by 95)

### Inheritance
```
Death occurs:
  ↓
If spouse exists:
  → Spouse becomes widowed (spouseId = null)
  → Spouse inherits treasury/possessions
Else if children exist:
  → Split treasury equally among children
Else:
  → Clan inherits (treasury → clan)
  ↓
NPC removed from world
```

---

## Integration Points

### With WorldGen
```java
// Phase 12: Generate Named NPCs
private void generateNamedNPCs(long seed) {
    Map<String, List<Structure>> clanStructures = 
        buildClanStructureMap();
    
    this.npcs = new ArrayList<>();
    Random rng = new Random(seed ^ 0xNPC);
    
    for (Clan clan : clans) {
        List<Structure> structures = 
            clanStructures.get(clan.getId());
        List<NamedNPC> clanNpcs = 
            NPCGenerator.generateInitialClanPopulation(
                clan, structures, 0L, rng);
        this.npcs.addAll(clanNpcs);
    }
}
```

### With RegionSimulator
```java
public void simulateTick(Region region, long currentTick) {
    // Update resources...
    
    // NPC lifecycle
    npcLifecycleManager.tick(
        currentTick,
        region.getNPCs(),
        region.getStructures()
    );
    
    // Clan expansion...
}
```

---

## Testing Strategy

### Determinism Tests
- Same seed → same NPC count, names, ages, genders, jobs
- Same seed → same initial marriages
- Same seed → same lifecycle events (marriages, births, deaths)

### Distribution Tests
- Age distribution: 20% children, 50% adults, 30% elders (±10%)
- Marriage rate: 30-70% of adults married
- Gender balance: 45-55% male/female (probabilistic)

### Integration Tests
- All NPCs have valid homes
- All jobs have valid workplaces
- No orphaned references (spouseId points to existing NPC)

---

## Performance Considerations

**Expected Load:**
- Small world (3-5 clans): ~50-100 NPCs
- Medium world (10-20 clans): ~200-500 NPCs
- Large world (50+ clans): ~1,000-2,000 NPCs

**Optimization Tips:**
- Cache structure lookups (avoid repeated iteration)
- Use spatial indexing for home-finding
- Batch lifecycle checks (every 5k ticks, not every tick)
- Skip player-controlled NPCs (bypass automatic lifecycle)

---

## Future Enhancements

### Phase 1.10.3+
- **Reputation system:** Relationship tracking for marriages
- **Trait inheritance:** Children inherit player stats/traits
- **NPC personalities:** Temperament affects behavior
- **Player-NPC dialogue:** Conversation system
- **Quest generation:** NPCs create dynamic quests

### Post-MVP
- **Polygamy support:** Multiple spouses (cultural variants)
- **NPC schedules:** Daily routines (work, sleep, socialize)
- **NPC skills:** Skill progression over time
- **NPC inventory:** Personal possessions, equipment

---

## Related Packages

- `org.adventure.world` — ClanGenerator, WorldGen
- `org.adventure.character` — Character stats/traits (for player NPCs)
- `org.adventure.structures` — Structure types, ownership
- `org.adventure.region` — RegionSimulator integration
- `org.adventure.story` — Event generation (births, deaths, marriages)

---

## Documentation Files

- `Gender.md` — Binary gender enum
- `NamedNPC.md` — NPC entity class
- `NPCJob.md` — Job enum and mechanics
- `NPCGenerator.md` — NPC factory class
- `NPCLifecycleManager.md` — Lifecycle simulation
- `PlayerNPCInteraction.md` — Player-NPC interactions
