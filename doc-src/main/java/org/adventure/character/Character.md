# Character.java - Core Character Data Model

**Package:** `org.adventure.character`  
**Source:** [Character.java](../../../../../src/main/java/org/adventure/character/Character.java)  
**Phase:** MVP Phase 1.3 (Characters & NPCs)

## Overview

`Character` is the central data model representing both player characters and NPCs in the game world. It encapsulates all character-related data including core stats with soft-cap progression, permanent traits, learned skills, mana management, and inventory.

This class implements the character system specified in `docs/characters_stats_traits_skills.md` and provides the foundation for gameplay mechanics like combat, crafting, magic, and social interactions.

## Design Philosophy

### Why Characters Are Central

Characters drive the entire simulation:
- **Player Agency**: Players customize and control characters to achieve goals
- **NPC Autonomy**: NPCs act as independent agents with their own stats, skills, and behaviors
- **Stat Progression**: Soft-cap formula ensures meaningful growth without runaway power
- **Trait Diversity**: Permanent traits create unique character identities
- **Skill Mastery**: Players specialize through skill trees and proficiency tiers

From `docs/grand_plan.md` MVP matrix:
> "Characters are the primary interface between player intent and world simulation."

### 8 Core Stats: The Foundation of Ability

```java
public enum CoreStat {
    STRENGTH,      // Physical power, melee damage, carry capacity
    DEXTERITY,     // Agility, ranged accuracy, dodge chance
    INTELLIGENCE,  // Magic power, spell effectiveness, mana pool
    WISDOM,        // Perception, magic resistance, willpower
    CONSTITUTION,  // Health, stamina, poison/disease resistance
    CHARISMA,      // Social influence, leadership, trading
    PERCEPTION,    // Awareness, detection, initiative in combat
    LUCK           // Critical hits, rare drops, event outcomes
}
```

**Design Rationale:**
- **8 stats** provide sufficient differentiation without overwhelming complexity
- **Abbreviations** (STR, DEX, INT, etc.) for compact UI display
- **Descriptions** explain gameplay impact for player understanding
- **No stat dependencies** - each stat is independently valuable

## Soft-Cap Progression Formula

### The Problem: Runaway Stat Growth

Without limits, characters could grind stats infinitely, breaking game balance. Traditional hard caps feel arbitrary and frustrating.

### The Solution: Logistic Soft-Cap

```java
/**
 * Soft-cap formula for stat progression:
 * Δstat = baseGain / (1 + (currentStat / softCapThreshold)^2)
 * 
 * Example: currentStat=30, baseGain=10, threshold=50
 * → Δstat = 10 / (1 + (30/50)^2) = 10 / 1.36 ≈ 7.35
 */
public void addStatProgress(CoreStat stat, int baseGain) {
    int currentStat = stats.get(stat);
    
    // Apply trait modifiers to base gain
    double modifiedGain = applyTraitModifiers(stat, baseGain);
    
    // Calculate soft-cap threshold (default: 50, legendary: 100)
    int softCapThreshold = DEFAULT_SOFT_CAP_THRESHOLD;
    for (Trait trait : traits) {
        softCapThreshold += trait.getSoftCapThresholdBonus();
    }
    
    // Apply soft-cap formula
    double ratio = (double) currentStat / softCapThreshold;
    double actualGain = modifiedGain / (1.0 + (ratio * ratio));
    
    int newStat = Math.min(currentStat + (int) Math.round(actualGain), HARD_CAP);
    stats.put(stat, newStat);
}
```

**Key Properties:**
- **Early Game (stat < threshold)**: Near-linear growth, easy to improve
- **Mid Game (stat ≈ threshold)**: Diminishing returns kick in, ~50% efficiency
- **Late Game (stat > threshold)**: Exponential difficulty, requires dedication
- **Hard Cap (200)**: Absolute maximum for legendary characters

**Tunable Parameters:**
- `DEFAULT_SOFT_CAP_THRESHOLD = 50`: Baseline human capability
- `LEGENDARY_SOFT_CAP_THRESHOLD = 100`: Enhanced by "Legendary Potential" trait
- `HARD_CAP = 200`: Prevents overflow, ensures balance

### Trait Modifiers on Progression

Traits affect how quickly stats grow:

```java
private double applyTraitModifiers(CoreStat stat, int baseGain) {
    double multiplier = 1.0;
    
    for (Trait trait : traits) {
        multiplier *= trait.getStatProgressionMultiplier(stat);
    }
    
    return baseGain * multiplier;
}
```

**Examples:**
- **Robust** (+20% CON progression): `multiplier = 1.2`
- **Agile** (+20% DEX progression): `multiplier = 1.2`
- **Clumsy** (-10% all stats): `multiplier = 0.9`

## Mana System

### Derived from Intelligence

```java
/**
 * Update derived stats based on core stats and traits.
 * Mana pool computed as: maxMana = baseMana + (INT × manaPerStatPoint)
 */
private void updateDerivedStats() {
    int intelligence = stats.get(CoreStat.INTELLIGENCE);
    
    // Base mana: 10 + (INT × 2)
    maxMana = BASE_MANA + (intelligence * MANA_PER_STAT_POINT);
    
    // Mana regeneration: 1 + floor(INT / 10) per tick
    int manaRegen = 1 + (intelligence / 10);
    derivedStats.put("manaRegen", (double) manaRegen);
    
    // Health computed from CON
    int constitution = stats.get(CoreStat.CONSTITUTION);
    int maxHealth = 50 + (constitution * 5);
    derivedStats.put("maxHealth", (double) maxHealth);
}
```

**Design Notes:**
- **Base Mana (10)**: All characters can cast weak spells
- **Scaling (INT × 2)**: Magic users (INT 50+) get 100+ mana pool
- **Regen Rate**: 1 + floor(INT / 10) mana per game tick
- **No Mana** for non-casters is still valid (10 mana allows scrolls/potions)

## Traits System Integration

### Permanent Character Modifiers

```java
private final List<Trait> traits;

public void addTrait(Trait trait) {
    if (!traits.contains(trait)) {
        traits.add(trait);
        updateDerivedStats(); // Recalculate mana, health, etc.
    }
}
```

**Trait Categories:**
1. **Stat Boosters**: Robust (+CON), Agile (+DEX), Scholar (+INT)
2. **Progression Modifiers**: Fast Learner (+XP), Blessed (+luck)
3. **Abilities**: Night Vision, Fire Resistance
4. **Legendary**: Legendary Potential (+50 soft cap threshold)
5. **Negative**: Clumsy (-stats), Cursed (-luck)

**Hereditary Traits:**
- Passed from parent to child NPCs
- Examples: Robust, Agile, Night Vision, Resilient, Charismatic
- Non-hereditary: Fast Learner, Blessed, Legendary Potential

See [Trait.md](Trait.md) for full trait specifications.

## Skills System Integration

### Learned Abilities with XP

```java
private final List<Skill> skills;

public void addSkill(Skill skill) {
    if (!skills.contains(skill)) {
        skills.add(skill);
    }
}

public boolean canLearnSkill(Skill skill) {
    return skill.canLearn(skills);
}
```

**Skill Categories:**
- **Combat**: Sword Fighting, Archery, Shield Defense, Dual Wielding
- **Crafting**: Smithing, Alchemy, Enchanting, Carpentry
- **Magic**: Fire Magic, Ice Magic, Healing Magic, Rune Casting
- **Social**: Persuasion, Leadership, Intimidation
- **Survival**: Foraging, Tracking, Camping

**Skill Progression:**
- **5 Proficiency Tiers**: Novice → Apprentice → Journeyman → Expert → Master
- **XP Thresholds**: 0/100/400/1101/2700
- **Prerequisites**: Some skills require others (Dual Wielding requires Sword Fighting)
- **Forgetting**: Skills can be forgotten for retraining (50% XP penalty)

See [Skill.md](Skill.md) for full skill specifications.

## Race Integration

### Base Stats from Species

```java
private Race race;

public Character(String id, String name, Race race) {
    this.race = race;
    
    // Initialize stats from race base stats
    for (CoreStat stat : CoreStat.values()) {
        this.stats.put(stat, race.getBaseStat(stat));
    }
}
```

**8 Playable Races:**
- **Human**: Balanced (50 all stats), +Fast Learner trait
- **Elf**: High DEX/PER (60), low CON (40), +Night Vision
- **Dwarf**: High CON/STR (60), low CHA (40), +Robust
- **Orc**: High STR (65), low INT/CHA (35), +Intimidating
- **Halfling**: High LUCK/DEX (60), low STR (30), +Agile
- **Gnome**: High INT/WIS (60), low STR/CON (35), +Scholar
- **Half-Elf**: Balanced (52 all), versatile
- **Dragonborn**: High STR/CHA (60), +Fire Resistance

See [Race.md](Race.md) for full race specifications.

## Inventory System (Phase 1.4 Preview)

```java
private final List<String> inventoryItemIds;

public void addItem(String itemId) {
    inventoryItemIds.add(itemId);
}

public boolean hasItem(String itemId) {
    return inventoryItemIds.contains(itemId);
}
```

**Current Implementation:**
- Stores item IDs as strings
- No weight/capacity limits yet
- Full Item class integration in Phase 1.4 (Objects & Crafting)

**Future Enhancements:**
- Equipment slots (weapon, armor, accessories)
- Weight-based carry capacity (STR × 10 kg)
- Quick-access hotbar for consumables

## Society/Clan Membership

```java
private String societyId;

public void joinSociety(String societyId) {
    this.societyId = societyId;
}

public boolean isMemberOf(String societyId) {
    return this.societyId != null && this.societyId.equals(societyId);
}
```

**Integration with Phase 1.6:**
- Characters belong to clans, kingdoms, or factions
- Society membership affects reputation, trade, combat alliances
- NPCs prioritize society members in interactions

## Simulation Tick Tracking

```java
private long lastUpdatedTick;

public void updateTick(long currentTick) {
    this.lastUpdatedTick = currentTick;
    
    // Regenerate mana
    int manaRegen = (int) derivedStats.getOrDefault("manaRegen", 1.0);
    currentMana = Math.min(currentMana + manaRegen, maxMana);
}
```

**Tick System:**
- Game progresses in discrete time steps (ticks)
- Characters track last update for delta calculations
- Mana regenerates each tick based on INT
- Future: Health regen, hunger, fatigue

## API Reference

### Constructor

```java
public Character(String id, String name, Race race)
```

Creates a new character with base stats from race.

**Parameters:**
- `id`: Unique character identifier (e.g., "char_001")
- `name`: Character display name (e.g., "Aldric the Brave")
- `race`: Race/species (determines base stats and traits)

**Example:**
```java
Character player = new Character("player_1", "Elara", Race.ELF);
player.addTrait(Trait.FAST_LEARNER);
player.addSkill(Skill.ARCHERY);
```

### Stat Management

```java
public int getStat(CoreStat stat)
public void setStat(CoreStat stat, int value)
public void addStatProgress(CoreStat stat, int baseGain)
public int getSoftCapThreshold()
```

### Trait Management

```java
public void addTrait(Trait trait)
public boolean hasTrait(Trait trait)
public List<Trait> getTraits()
```

### Skill Management

```java
public void addSkill(Skill skill)
public boolean hasSkill(Skill skill)
public boolean canLearnSkill(Skill skill)
public List<Skill> getSkills()
```

### Mana Management

```java
public int getCurrentMana()
public int getMaxMana()
public boolean spendMana(int cost)
public void restoreMana(int amount)
```

### Inventory

```java
public void addItem(String itemId)
public boolean hasItem(String itemId)
public List<String> getInventory()
```

### Society

```java
public void joinSociety(String societyId)
public void leaveSociety()
public String getSocietyId()
public boolean isMemberOf(String societyId)
```

## Testing

See [CharacterTest.md](../../../../../test/java/org/adventure/CharacterTest.md) for comprehensive test coverage:
- 16 unit tests covering all core functionality
- Stat progression with soft-cap validation
- Trait/skill integration tests
- Mana system tests
- Race base stat initialization

## Design Decisions

### Why Store Stats as Map?

```java
private final Map<CoreStat, Integer> stats;
```

**Advantages:**
- **Extensibility**: Easy to add new stats without refactoring
- **Generic Algorithms**: Can iterate over all stats programmatically
- **Modding Support**: Custom stats via configuration

**Alternative Considered:**
- Individual fields (`private int strength;`) - rejected for rigidity

### Why Separate Derived Stats?

```java
private final Map<String, Double> derivedStats;
```

**Rationale:**
- **Computed Values**: Mana, health, regen rates calculated from core stats
- **Caching**: Avoid recomputing every frame, update only when stats change
- **Flexibility**: Store any derived value (critical chance, dodge rate, etc.)

### Why List<String> for Inventory?

**Current Phase (1.3):**
- Simple string IDs sufficient for placeholder implementation
- Defers Item class complexity to Phase 1.4

**Future Migration:**
```java
// Phase 1.4: Replace with proper Item objects
private final List<Item> inventory;
```

## Integration with Other Systems

### World Generation (Phase 1.1)
- NPCs spawn in regions based on biome habitability
- Race distribution tied to geographical features

### Regional Simulation (Phase 1.2)
- Characters interact with resource nodes
- Settlement growth depends on character population

### Objects & Crafting (Phase 1.4)
- Characters craft items using skills (Smithing, Alchemy)
- Equipment modifies stats and abilities

### Societies & Clans (Phase 1.6)
- Characters form hierarchies (leaders, members)
- Reputation systems affect NPC interactions

### Stories & Events (Phase 1.7)
- Character stats/traits trigger story branches
- Skill checks determine quest outcomes

## Performance Considerations

### Memory Footprint

**Per Character:**
- `Map<CoreStat, Integer>`: ~64 bytes (8 stats × 8 bytes)
- `List<Trait>`: ~40 bytes (avg 5 traits × 8 bytes reference)
- `List<Skill>`: ~80 bytes (avg 10 skills × 8 bytes reference)
- **Total**: ~200 bytes per character

**World Scale:**
- 1,000 characters = 200 KB
- 10,000 characters = 2 MB
- 100,000 characters = 20 MB (acceptable)

### Update Frequency

```java
public void updateTick(long currentTick) {
    // Only update mana regen, skip if no combat/magic
    if (currentMana < maxMana) {
        currentMana = Math.min(currentMana + manaRegen, maxMana);
    }
}
```

**Optimization:**
- Only active characters need per-tick updates
- Sleeping NPCs can update on longer intervals (every 10 ticks)
- Spatial partitioning: Only update characters near player

## Future Enhancements

### Phase 1.4: Full Item Integration
- Equipment slots with stat bonuses
- Weight-based carry capacity
- Item durability and repair

### Phase 1.5: Combat System
- Attack/defense calculations from stats
- Critical hits using LUCK stat
- Status effects (poisoned, stunned, buffed)

### Phase 1.6: Reputation System
- Per-society reputation scores
- Trait effects on reputation (Charismatic +10%)
- Fame/infamy mechanics

### Phase 1.8: AI Behavior Trees
- NPC decision-making based on stats/traits
- Personality archetypes (brave vs cautious)
- Goal-oriented action planning

## Known Limitations

1. **No Health System Yet**: Planned for Phase 1.5 (Combat)
2. **Simple Inventory**: String IDs, no weight limits
3. **No Equipment Slots**: Armor/weapon slots in Phase 1.4
4. **No Status Effects**: Buffs/debuffs in Phase 1.5
5. **No Age/Mortality**: Aging system in Phase 1.9 (Persistence)

## References

- **Design Docs**: `docs/characters_stats_traits_skills.md`
- **Data Models**: `docs/data_models.md` → Character Schema
- **Grand Plan**: `docs/grand_plan.md` → Phase 1.3 Requirements
- **Related Classes**: [Trait.md](Trait.md), [Skill.md](Skill.md), [Race.md](Race.md), [NPC.md](NPC.md)
- **Tests**: [CharacterTest.md](../../../../../test/java/org/adventure/CharacterTest.md)

---

**Last Updated:** Phase 1.3 Implementation (November 2025)  
**Status:** ✅ Complete - 16 tests passing, ready for Phase 1.4 integration
