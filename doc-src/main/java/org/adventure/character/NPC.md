# NPC.java - Non-Player Character System

**Package:** `org.adventure.character`  
**Source:** [NPC.java](../../../../../src/main/java/org/adventure/character/NPC.java)  
**Phase:** MVP Phase 1.3 (Characters & NPCs)

## Overview

`NPC` (Non-Player Character) extends `Character` with AI behavior, spawning logic, and world interaction. NPCs are spawned deterministically based on world seed, biome habitability, and regional features, creating a living, populated world.

## Design Philosophy

### Deterministic Spawning

```java
/**
 * Spawn NPCs based on world seed + biome + coordinates.
 * Same seed = same NPC population every time.
 */
public static NPC spawnNPC(long worldSeed, int x, int y, Biome biome) {
    Random rng = new Random(worldSeed + x * 1000L + y);
    
    // Biome-specific race weights
    Race race = selectRaceForBiome(biome, rng);
    BehaviorType behavior = selectBehaviorForBiome(biome, rng);
    
    String id = "npc_" + x + "_" + y;
    String name = generateName(race, rng);
    
    return new NPC(id, name, race, behavior, x, y, biome.name());
}
```

**Why Deterministic?**
- **Reproducibility**: Same seed = same world, same NPCs
- **Testing**: Predictable NPC spawns for unit tests
- **Multiplayer**: All players see same NPCs at same locations

### 6 Behavior Types

```java
public enum BehaviorType {
    PEACEFUL,     // Ignores player unless attacked (farmers, civilians)
    NEUTRAL,      // Defends itself when attacked (merchants, travelers)
    AGGRESSIVE,   // Attacks player on sight (bandits, monsters)
    TRADER,       // Offers trade services (merchants, smiths)
    QUEST_GIVER,  // Provides quests to player (elders, nobles)
    GUARD         // Patrols and enforces laws (town guards, soldiers)
}
```

## Biome-Specific Spawning

### Race Distribution by Biome

```java
private static Race selectRaceForBiome(Biome biome, Random rng) {
    return switch (biome) {
        case FOREST -> weightedRandom(rng, 
            Map.of(Race.ELF, 0.5, Race.HUMAN, 0.3, Race.HALFLING, 0.2));
        case MOUNTAIN -> weightedRandom(rng,
            Map.of(Race.DWARF, 0.6, Race.DRAGONBORN, 0.3, Race.GNOME, 0.1));
        case GRASSLAND -> weightedRandom(rng,
            Map.of(Race.HUMAN, 0.7, Race.HALF_ELF, 0.3));
        case DESERT -> weightedRandom(rng,
            Map.of(Race.HUMAN, 0.5, Race.ORC, 0.3, Race.DRAGONBORN, 0.2));
        case TUNDRA -> weightedRandom(rng,
            Map.of(Race.DWARF, 0.5, Race.ORC, 0.5));
        default -> Race.HUMAN;  // Fallback
    };
}
```

**Design Rationale:**
- **Forest → Elves**: Natural habitat for woodland elves
- **Mountain → Dwarves**: Mountain fortresses and mines
- **Grassland → Humans**: Agricultural heartlands
- **Desert → Nomads**: Harsh environment, adaptable races
- **Tundra → Hardy Races**: Only tough races survive

### Behavior Distribution by Biome

```java
private static BehaviorType selectBehaviorForBiome(Biome biome, Random rng) {
    return switch (biome) {
        case GRASSLAND, FOREST -> weightedRandom(rng,
            Map.of(PEACEFUL, 0.5, TRADER, 0.3, NEUTRAL, 0.2));
        case DESERT, TUNDRA -> weightedRandom(rng,
            Map.of(NEUTRAL, 0.4, AGGRESSIVE, 0.3, TRADER, 0.3));
        case MOUNTAIN -> weightedRandom(rng,
            Map.of(TRADER, 0.4, GUARD, 0.3, NEUTRAL, 0.3));
        case SWAMP, JUNGLE -> weightedRandom(rng,
            Map.of(AGGRESSIVE, 0.5, NEUTRAL, 0.3, PEACEFUL, 0.2));
        default -> NEUTRAL;
    };
}
```

**Biome Personality:**
- **Grassland/Forest**: Civilized, peaceful NPCs (farmers, traders)
- **Desert/Tundra**: Harsh environments, more aggressive NPCs
- **Mountain**: Isolated communities, focus on trade and defense
- **Swamp/Jungle**: Dangerous, high aggression rate

## NPC Density Control

### Spawn Rate by Biome

```java
/**
 * Determine if an NPC should spawn at this location.
 * Uses Perlin noise for natural-looking clusters.
 */
public static boolean shouldSpawnNPC(long worldSeed, int x, int y, Biome biome) {
    double spawnDensity = switch (biome) {
        case GRASSLAND -> 0.05;  // 5% of tiles (high population)
        case FOREST -> 0.03;     // 3% (moderate)
        case MOUNTAIN -> 0.02;   // 2% (sparse)
        case DESERT -> 0.01;     // 1% (very sparse)
        case TUNDRA -> 0.005;    // 0.5% (extremely sparse)
        case OCEAN, LAKE -> 0.0; // No land NPCs in water
        default -> 0.02;
    };
    
    Random rng = new Random(worldSeed + x * 1000L + y);
    return rng.nextDouble() < spawnDensity;
}
```

**Why Variable Density?**
- **Realism**: Cities are crowded, deserts are empty
- **Performance**: Limit NPC count in low-population biomes
- **Gameplay**: NPCs are resources (traders, quest givers), should be findable

## Hereditary Traits in NPCs

### Parent-Child Trait Inheritance

```java
/**
 * Generate child NPC from two parents.
 * Inherits hereditary traits from parents (50% chance each).
 */
public static NPC generateOffspring(NPC parent1, NPC parent2, long seed, int x, int y) {
    Random rng = new Random(seed);
    
    // Choose race from parents (50/50)
    Race race = rng.nextBoolean() ? parent1.getRace() : parent2.getRace();
    
    // Inherit traits
    NPC child = new NPC("offspring_" + x + "_" + y, 
        generateName(race, rng), race, BehaviorType.PEACEFUL, x, y, parent1.biomeId);
    
    // 50% chance to inherit each hereditary trait
    for (Trait trait : parent1.getTraits()) {
        if (trait.isHereditary() && rng.nextDouble() < 0.5) {
            child.addTrait(trait);
        }
    }
    for (Trait trait : parent2.getTraits()) {
        if (trait.isHereditary() && rng.nextDouble() < 0.5 && !child.hasTrait(trait)) {
            child.addTrait(trait);
        }
    }
    
    return child;
}
```

**Genetic Diversity:**
- **Hereditary Traits**: Robust, Agile, Night Vision, Resilient, Charismatic
- **50% Inheritance**: Not guaranteed, prevents trait stacking
- **Both Parents**: Can inherit from either parent
- **Bloodlines**: Powerful families emerge over generations

## AI Behavior (Phase 1.3 Placeholder)

### Current State

```java
private int currentX;
private int currentY;
private int health;
private int maxHealth;

public void move(int dx, int dy) {
    currentX += dx;
    currentY += dy;
}

public void takeDamage(int damage) {
    health = Math.max(0, health - damage);
}

public boolean isAlive() {
    return health > 0;
}
```

**Status:** Basic state tracking only

### Future AI (Phase 1.8)

**Behavior Trees:**
```java
// Phase 1.8: Full AI implementation
switch (behaviorType) {
    case PEACEFUL -> runPeacefulAI();   // Flee from danger
    case NEUTRAL -> runNeutralAI();     // Defend when attacked
    case AGGRESSIVE -> runAggressiveAI(); // Hunt player
    case TRADER -> runTraderAI();       // Seek trade opportunities
    case QUEST_GIVER -> runQuestAI();   // Offer quests
    case GUARD -> runGuardAI();         // Patrol territory
}
```

**Goal-Oriented Planning:**
- **Needs**: Hunger, thirst, sleep, safety
- **Goals**: Find food, rest, trade, socialize
- **Actions**: Move, attack, trade, talk, sleep

## Testing

See [NPCTest.md](../../../../../test/java/org/adventure/NPCTest.md):
- 18 unit tests covering all NPC functionality
- Deterministic spawning validation
- Biome-specific behavior checks
- Trait inheritance tests

**Status:** ✅ Complete - All tests passing

## API Reference

### Constructor

```java
public NPC(String id, String name, Race race, BehaviorType behaviorType,
           int spawnX, int spawnY, String biomeId)
```

### Static Factory Methods

```java
public static NPC spawnNPC(long worldSeed, int x, int y, Biome biome)
public static boolean shouldSpawnNPC(long worldSeed, int x, int y, Biome biome)
public static NPC generateOffspring(NPC parent1, NPC parent2, long seed, int x, int y)
```

### Position & Movement

```java
public int getSpawnX()
public int getSpawnY()
public int getCurrentX()
public int getCurrentY()
public void move(int dx, int dy)
```

### Health & Combat

```java
public int getHealth()
public int getMaxHealth()
public void takeDamage(int damage)
public void heal(int amount)
public boolean isAlive()
```

### Behavior

```java
public BehaviorType getBehaviorType()
public String getBiomeId()
```

## Integration with World Generation

```java
// Phase 1.1: World Generation
for (int x = 0; x < worldWidth; x++) {
    for (int y = 0; y < worldHeight; y++) {
        Biome biome = world.getBiome(x, y);
        
        if (NPC.shouldSpawnNPC(worldSeed, x, y, biome)) {
            NPC npc = NPC.spawnNPC(worldSeed, x, y, biome);
            world.addNPC(npc);
        }
    }
}
```

## Design Decisions

### Why Extend Character?

**Inheritance:**
```java
public class NPC extends Character {
    // NPCs ARE characters, just with AI
}
```

**Benefits:**
- Reuse all Character functionality (stats, traits, skills)
- NPCs can level up, learn skills, gain traits
- Unified serialization/persistence

### Why Deterministic Spawning?

**Alternative:** Random spawning based on time
```java
// BAD: Non-deterministic
NPC npc = new NPC(..., new Random().nextInt());
```

**Why Deterministic Is Better:**
- **Reproducibility**: Testing, debugging, multiplayer
- **World Consistency**: NPCs are part of world, not random
- **Balancing**: Can tune spawn rates without RNG chaos

### Why Biome-Specific Races?

**Alternative:** Random race everywhere
```java
// BAD: Dwarves in ocean, elves in desert
Race race = Race.values()[rng.nextInt(Race.values().length)];
```

**Why Biome Mapping Is Better:**
- **Realism**: Races adapt to environments
- **Lore**: Elves in forests, dwarves in mountains
- **Gameplay**: Players learn where to find specific races

## Future Enhancements

### Phase 1.6: Societies & Clans
- NPCs belong to clans, kingdoms, factions
- Reputation system affects NPC interactions
- NPC diplomacy and alliances

### Phase 1.7: Stories & Events
- NPCs trigger quests based on location/race
- Dynamic events spawn special NPCs (merchants, refugees)
- NPC-driven storytelling

### Phase 1.8: AI Behavior Trees
- Full goal-oriented AI
- Pathfinding and navigation
- Combat tactics and formations

### Phase 1.9: Persistence & Versioning
- NPC state saved/loaded from disk
- NPC aging and mortality
- Generational dynasties

## References

- **Design Docs**: `docs/characters_stats_traits_skills.md`
- **Related Classes**: [Character.md](Character.md), [Race.md](Race.md), [Trait.md](Trait.md)
- **Tests**: [NPCTest.md](../../../../../test/java/org/adventure/NPCTest.md)

---

**Last Updated:** Phase 1.3 Implementation (November 2025)  
**Status:** ✅ Complete - 18 tests passing, deterministic spawning implemented
