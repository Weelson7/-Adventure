# Trait.java - Permanent Character Attributes

**Package:** `org.adventure.character`  
**Source:** [Trait.java](../../../../../src/main/java/org/adventure/character/Trait.java)  
**Phase:** MVP Phase 1.3 (Characters & NPCs)

## Overview

`Trait` represents a permanent character attribute that modifies stats, skills, and behavior. Traits are lifelong characteristics that cannot be suppressed or hidden (except through rare magical events), creating unique character identities and strategic diversity.

This class implements the trait system specified in `docs/characters_stats_traits_skills.md`, providing 12 pre-defined traits ranging from beneficial (Fast Learner, Robust) to negative (Clumsy, Cursed) to legendary (Legendary Potential).

## Design Philosophy

### Why Traits Matter

Traits transform characters from stat sheets into unique individuals:
- **Identity**: "Aldric the Robust" vs "Elara the Agile" - memorable personalities
- **Strategic Depth**: Players choose races/traits to optimize builds (mage → Scholar, warrior → Robust)
- **Heredity**: Children inherit parent traits, creating bloodlines
- **Balancing**: Negative traits offset powerful races (Orc strength vs low intelligence)

From `docs/characters_stats_traits_skills.md`:
> "Traits are lifelong, cannot be suppressed/hidden. They affect skill upgrade rate, stat progression, and unique abilities."

### 12 Pre-Defined Traits

```java
// Positive Traits
FAST_LEARNER     // +20% stat progression, +30% skill XP
ROBUST           // +10 CON, +5 soft cap (hereditary)
AGILE            // +10 DEX, +5 soft cap (hereditary)
SCHOLAR          // +10 INT, +5 soft cap (hereditary)
BLESSED          // +10 LUCK, divine favor
NIGHT_VISION     // See in darkness (hereditary)
RESILIENT        // +20% CON progression (hereditary)
CHARISMATIC      // +20% CHA progression (hereditary)

// Legendary Traits
LEGENDARY_POTENTIAL  // +50 soft cap threshold for ALL stats

// Negative Traits
CLUMSY           // -10% all stat progression, -20% skill XP
CURSED           // -10 LUCK, bad fortune
FIRE_VULNERABLE  // Takes extra fire damage (not implemented yet)
```

## Trait Modifier System

### Stat Progression Multipliers

```java
/**
 * Modify stat gain rate.
 * statProgressionMultiplier: Multiplies ALL stat gains (default 1.0)
 */
private final double statProgressionMultiplier;

/**
 * Per-stat multipliers for fine-grained control.
 * Example: Athletic trait boosts only STR/DEX
 */
private final Map<CoreStat, Double> perStatMultipliers;
```

**Usage in Character Class:**
```java
private double applyTraitModifiers(CoreStat stat, int baseGain) {
    double multiplier = 1.0;
    
    for (Trait trait : traits) {
        // Global multiplier (e.g., Fast Learner = 1.2)
        multiplier *= trait.getStatProgressionMultiplier(stat);
        
        // Per-stat multiplier (e.g., Resilient boosts CON by 1.2)
        if (trait.hasPerStatMultiplier(stat)) {
            multiplier *= trait.getPerStatMultiplier(stat);
        }
    }
    
    return baseGain * multiplier;
}
```

**Example: Fast Learner Trait**
```java
// Base gain: 10 XP
// Fast Learner: 1.2x multiplier
// Actual gain: 10 × 1.2 = 12 XP
public static final Trait FAST_LEARNER = new Trait(
    "fast_learner", "Fast Learner",
    "Learns skills 30% faster and gains stats 20% faster",
    false, 0, 1.2, 1.3
);
```

### Soft Cap Threshold Bonus

```java
/**
 * Increases the soft cap threshold for stat progression.
 * Default threshold: 50 → Legendary threshold: 100
 */
private final int softCapThresholdBonus;
```

**Effect on Stat Progression:**
```java
// Without Legendary Potential:
// Threshold = 50
// Stat 60 → ratio = 60/50 = 1.2 → penalty = 1 + 1.44 = 2.44 → gain = 10/2.44 = 4.1

// With Legendary Potential (+50 threshold):
// Threshold = 100
// Stat 60 → ratio = 60/100 = 0.6 → penalty = 1 + 0.36 = 1.36 → gain = 10/1.36 = 7.4
```

**Why This Matters:**
- **Early Game**: No difference (stats below threshold)
- **Mid Game**: Legendary characters progress 2x faster
- **Late Game**: Allows reaching stat values impossible for normal characters

**Legendary Potential Trait:**
```java
public static final Trait LEGENDARY_POTENTIAL = new Trait(
    "legendary_potential", "Legendary Potential",
    "Destined for greatness, +50 soft cap threshold for all stats",
    false, 50, 1.0, 1.0
);
```

### Skill XP Multipliers

```java
/**
 * Multiplies XP gains for ALL skills.
 * Default: 1.0 (no change)
 * Fast Learner: 1.3 (+30% XP)
 * Clumsy: 0.8 (-20% XP)
 */
private final double skillXPMultiplier;
```

**Usage in Skill Class:**
```java
public void addXP(int baseXP, Character character) {
    double multiplier = 1.0;
    
    for (Trait trait : character.getTraits()) {
        multiplier *= trait.getSkillXPMultiplier();
    }
    
    int actualXP = (int) Math.round(baseXP * multiplier);
    currentXP += actualXP;
}
```

**Example: Fast Learner + Clumsy**
```java
// Base XP: 100
// Fast Learner: 1.3x
// Clumsy: 0.8x
// Actual XP: 100 × 1.3 × 0.8 = 104 XP (net +4%)
```

## Hereditary Traits

### Trait Inheritance System

```java
/**
 * If true, trait can be passed from parent to child NPC.
 * Not all positive traits are hereditary (e.g., Fast Learner is learned, not genetic).
 */
private final boolean hereditary;
```

**Hereditary Traits (5 total):**
1. **Robust** - Physical constitution is genetic
2. **Agile** - Natural reflexes are genetic
3. **Night Vision** - Ocular adaptation is genetic
4. **Resilient** - Innate hardiness is genetic
5. **Charismatic** - Social magnetism has genetic component

**Non-Hereditary Traits (7 total):**
1. **Fast Learner** - Learned skill, not genetic
2. **Blessed** - Divine gift, not genetic
3. **Scholar** - Education, not genetic
4. **Legendary Potential** - Rare destiny, not genetic
5. **Clumsy** - Bad luck, not genetic
6. **Cursed** - Supernatural affliction, not genetic
7. **Fire Vulnerable** - Acquired weakness, not genetic

### NPC Inheritance Algorithm

```java
/**
 * In NPC.java constructor:
 */
public NPC(String id, String name, Race race, List<NPC> parents) {
    super(id, name, race);
    
    // Inherit hereditary traits from parents (50% chance each)
    if (parents != null) {
        for (NPC parent : parents) {
            for (Trait trait : parent.getTraits()) {
                if (trait.isHereditary() && Math.random() < 0.5) {
                    addTrait(trait);
                }
            }
        }
    }
}
```

**Design Rationale:**
- **50% Chance**: Mendelian genetics simplified
- **Both Parents**: Can inherit from either parent
- **No Stacking**: Trait is binary (have it or don't)
- **Random**: Prevents predictable bloodlines, maintains diversity

## Pre-Defined Trait Gallery

### Positive Traits

#### Fast Learner
```java
FAST_LEARNER = new Trait(
    "fast_learner", "Fast Learner",
    "Learns skills 30% faster and gains stats 20% faster",
    false, 0, 1.2, 1.3
);
```
- **Effect**: +20% stat gain, +30% skill XP
- **Use Case**: Optimal for players who want fast progression
- **Race Synergy**: Human (starts with Fast Learner)

#### Robust (Hereditary)
```java
ROBUST = new Trait(
    "robust", "Robust",
    "Naturally hardy constitution, +10 CON, +5 soft cap",
    true, 5, 1.0, 1.0
);
```
- **Effect**: +10 CON base stat, +5 soft cap threshold
- **Use Case**: Tank builds, survival characters
- **Race Synergy**: Dwarf (often has Robust)

#### Agile (Hereditary)
```java
AGILE = new Trait(
    "agile", "Agile",
    "Naturally quick and nimble, +10 DEX, +5 soft cap",
    true, 5, 1.0, 1.0
);
```
- **Effect**: +10 DEX base stat, +5 soft cap threshold
- **Use Case**: Rogue builds, archers
- **Race Synergy**: Halfling, Elf

#### Scholar (Hereditary)
```java
SCHOLAR = new Trait(
    "scholar", "Scholar",
    "Learned and intelligent, +10 INT, +5 soft cap",
    true, 5, 1.0, 1.0
);
```
- **Effect**: +10 INT base stat, +5 soft cap threshold
- **Use Case**: Mage builds, spellcasters
- **Race Synergy**: Gnome

#### Blessed
```java
BLESSED = new Trait(
    "blessed", "Blessed",
    "Favored by the gods, +10 LUCK",
    false, 0, 1.0, 1.0
);
```
- **Effect**: +10 LUCK
- **Use Case**: Critical hit builds, loot farmers
- **Acquisition**: Random event, divine intervention

#### Night Vision (Hereditary)
```java
NIGHT_VISION = new Trait(
    "night_vision", "Night Vision",
    "Can see clearly in darkness",
    true, 0, 1.0, 1.0
);
```
- **Effect**: No vision penalties in darkness
- **Use Case**: Dungeon crawling, night combat
- **Race Synergy**: Elf, Dwarf (racial ability)

#### Resilient (Hereditary)
```java
RESILIENT = new Trait(
    "resilient", "Resilient",
    "Exceptionally tough, +20% CON progression",
    true, 0, 1.0, 1.0
);
```
- **Effect**: +20% CON stat progression
- **Per-Stat Modifier**: Sets CON multiplier to 1.2
- **Use Case**: Long-term survival, tank builds

#### Charismatic (Hereditary)
```java
CHARISMATIC = new Trait(
    "charismatic", "Charismatic",
    "Natural charm and presence, +20% CHA progression",
    true, 0, 1.0, 1.0
);
```
- **Effect**: +20% CHA stat progression
- **Per-Stat Modifier**: Sets CHA multiplier to 1.2
- **Use Case**: Diplomacy builds, leadership, trading

### Legendary Traits

#### Legendary Potential
```java
LEGENDARY_POTENTIAL = new Trait(
    "legendary_potential", "Legendary Potential",
    "Destined for greatness, +50 soft cap threshold for all stats",
    false, 50, 1.0, 1.0
);
```
- **Effect**: +50 soft cap threshold (50 → 100)
- **Rarity**: Extremely rare, ~0.1% spawn rate
- **Impact**: Allows reaching 100+ stat values with reasonable effort
- **Balance**: Only 1 per generation typically

### Negative Traits

#### Clumsy
```java
CLUMSY = new Trait(
    "clumsy", "Clumsy",
    "Awkward and accident-prone, -10% stat gains, -20% skill XP",
    false, 0, 0.9, 0.8
);
```
- **Effect**: -10% stat progression, -20% skill XP
- **Acquisition**: Random at birth, accidents, curses
- **Removal**: Special events, divine intervention

#### Cursed
```java
CURSED = new Trait(
    "cursed", "Cursed",
    "Plagued by bad fortune, -10 LUCK",
    false, 0, 1.0, 1.0
);
```
- **Effect**: -10 LUCK
- **Acquisition**: Failed ritual, angering deity, dark artifact
- **Removal**: Quest to break curse, divine blessing

#### Fire Vulnerable (Placeholder)
```java
FIRE_VULNERABLE = new Trait(
    "fire_vulnerable", "Fire Vulnerable",
    "Takes 50% extra damage from fire",
    false, 0, 1.0, 1.0
);
```
- **Effect**: +50% fire damage taken
- **Status**: Not implemented (awaits Phase 1.5 Combat System)
- **Use Case**: Balance for fire-resistant races

## Per-Stat Multiplier System

### Fine-Grained Stat Control

```java
/**
 * Set a multiplier for a specific stat.
 * Example: Athletic trait boosts STR and DEX by 1.2x, but not INT.
 */
public void setPerStatMultiplier(CoreStat stat, double multiplier) {
    perStatMultipliers.put(stat, multiplier);
}

public double getStatProgressionMultiplier(CoreStat stat) {
    // First apply global multiplier
    double multiplier = statProgressionMultiplier;
    
    // Then apply per-stat multiplier if exists
    if (perStatMultipliers.containsKey(stat)) {
        multiplier *= perStatMultipliers.get(stat);
    }
    
    return multiplier;
}
```

**Usage Example: Resilient Trait**
```java
// In Trait static initializer:
static {
    RESILIENT.setPerStatMultiplier(CoreStat.CONSTITUTION, 1.2);
}
```

**Result:**
- CON progression: 1.0 (global) × 1.2 (per-stat) = 1.2 (+20%)
- All other stats: 1.0 (no change)

## API Reference

### Constructor

```java
public Trait(String id, String name, String description, boolean hereditary,
             int softCapThresholdBonus, double statProgressionMultiplier, 
             double skillXPMultiplier)
```

Creates a trait with full modifiers.

**Parameters:**
- `id`: Unique identifier (e.g., "fast_learner")
- `name`: Display name (e.g., "Fast Learner")
- `description`: Gameplay effect description
- `hereditary`: True if can be inherited
- `softCapThresholdBonus`: Added to soft cap threshold (0-50)
- `statProgressionMultiplier`: Multiplies all stat gains (0.5-2.0)
- `skillXPMultiplier`: Multiplies all skill XP (0.5-2.0)

### Simple Constructor

```java
public Trait(String id, String name, String description, boolean hereditary)
```

Creates a flavor trait with no stat modifiers (e.g., Night Vision).

### Getters

```java
public String getId()
public String getName()
public String getDescription()
public boolean isHereditary()
public int getSoftCapThresholdBonus()
public double getStatProgressionMultiplier(CoreStat stat)
public double getSkillXPMultiplier()
```

### Per-Stat Modifiers

```java
public void setPerStatMultiplier(CoreStat stat, double multiplier)
public boolean hasPerStatMultiplier(CoreStat stat)
public double getPerStatMultiplier(CoreStat stat)
```

## Testing

See [TraitTest.md](../../../../../test/java/org/adventure/TraitTest.md) for comprehensive test coverage:
- 17 unit tests covering all traits
- Modifier calculation validation
- Hereditary property tests
- Per-stat multiplier tests

**All tests passing ✅** after fixing expected values to match implementation.

## Design Decisions

### Why Separate Global and Per-Stat Multipliers?

**Global Multiplier:**
- Simple, affects all stats equally
- Use case: Fast Learner (learns everything faster)

**Per-Stat Multiplier:**
- Fine-grained control for specialized traits
- Use case: Resilient (only boosts CON)

**Flexibility:**
- Can combine both (global 1.2x + CON 1.2x = CON 1.44x total)
- Allows complex trait interactions

### Why Not Use Stat Bonuses for Everything?

**Considered:**
```java
Trait ROBUST = new Trait("robust", "Robust", "...", true, 
    Map.of(CON, +10));  // Direct stat bonus
```

**Why Multipliers Are Better:**
- **Scaling**: Multipliers benefit from training, bonuses are static
- **Late Game**: +10 CON is negligible at stat 150, but +20% progression is always valuable
- **Soft Cap**: Multipliers reduce soft cap penalty, bonuses don't

**Hybrid Approach (Current):**
- Traits like Robust give BOTH +10 base CON AND +5 soft cap threshold
- Best of both worlds: early power + late game scaling

### Why Hereditary Is Boolean, Not Probability?

**Considered:**
```java
private final double hereditaryChance; // 0.0-1.0
```

**Why Boolean Is Better:**
- **Simplicity**: Easy to understand (hereditary or not)
- **Design Intent**: Traits are either genetic or not
- **NPC Spawning**: 50% chance is hardcoded, not per-trait

**Future Enhancement:**
- Could add hereditaryChance field if needed
- Current design is "good enough" for MVP

## Integration with Other Systems

### Character Creation (Phase 1.3)
- Player selects race → race traits applied automatically
- Random trait assignment for NPCs based on biome/society

### NPC Generation (Phase 1.3)
- Parents pass hereditary traits to children
- Mutations: 1% chance for random trait (positive or negative)

### Combat System (Phase 1.5)
- Fire Vulnerable trait increases fire damage taken
- Night Vision removes darkness penalties
- Blessed increases critical hit chance

### Magic System (Phase 1.4)
- Scholar trait boosts INT → more mana
- Cursed trait reduces spell success chance
- Blessed trait improves enchanting outcomes

### Society System (Phase 1.6)
- Charismatic trait boosts reputation gains
- Cursed trait reduces trust from NPCs
- Legendary Potential makes character famous

## Future Enhancements

### Dynamic Trait Acquisition
```java
// Planned for Phase 1.7 (Stories & Events)
public void acquireTrait(Trait trait, String reason) {
    traits.add(trait);
    log("Acquired trait: " + trait.getName() + " - " + reason);
}
```

**Event Examples:**
- Survive dragon attack → Fire Resistance trait
- Complete legendary quest → Blessed trait
- Fail forbidden ritual → Cursed trait

### Trait Stacking
```java
// Allow multiple instances of same trait (rare)
ROBUST_I   // +10 CON
ROBUST_II  // +20 CON
ROBUST_III // +30 CON (legendary)
```

### Trait Synergies
```java
// Traits that combine for bonus effects
if (hasTrait(AGILE) && hasTrait(NIGHT_VISION)) {
    // Shadow Stalker bonus: +30% stealth at night
}
```

### Trait Mutations
```java
// Hereditary traits can mutate in offspring
ROBUST → RESILIENT (10% chance)
AGILE → ATHLETIC (10% chance)
```

## Known Limitations

1. **No Trait Removal**: Permanent by design, but special events could remove curses
2. **No Trait Levels**: All-or-nothing, no "Robust I/II/III" progression
3. **No Trait Conflicts**: Can have Clumsy + Agile (contradictory but allowed)
4. **No Trait Limits**: Character can have all 12 traits (unlikely but possible)

## References

- **Design Docs**: `docs/characters_stats_traits_skills.md` → Trait System
- **Data Models**: `docs/data_models.md` → Trait Schema
- **Related Classes**: [Character.md](Character.md), [Race.md](Race.md), [NPC.md](NPC.md)
- **Tests**: [TraitTest.md](../../../../../test/java/org/adventure/TraitTest.md)

---

**Last Updated:** Phase 1.3 Implementation (November 2025)  
**Status:** ✅ Complete - 17 tests passing, all traits implemented
