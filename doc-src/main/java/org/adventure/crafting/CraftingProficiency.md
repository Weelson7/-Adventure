# CraftingProficiency.java - Skill Tier System

**Package:** `org.adventure.crafting`  
**Source:** [CraftingProficiency.java](../../../../../src/main/java/org/adventure/crafting/CraftingProficiency.java)  
**Phase:** MVP Phase 1.4 (Items & Crafting)

## Overview

`CraftingProficiency` defines five skill tiers representing a character's mastery in crafting. Each tier unlocks more complex recipes, improves success rates, and requires progressively more XP to achieve. This enum implements the soft-cap progression system specified in `docs/objects_crafting_legacy.md`.

## Design Philosophy

### Why 5 Tiers?

From design analysis:
- **Too Few** (3 tiers): Not enough progression depth
- **Too Many** (10+ tiers): Feels grindy, diminishing returns
- **Sweet Spot** (5 tiers): Clear milestones, achievable goals

**Tier Philosophy:**
1. **NOVICE**: Tutorial tier, learn basics
2. **APPRENTICE**: Competent, can craft useful items
3. **JOURNEYMAN**: Professional, reliable crafting
4. **EXPERT**: Master craftsman, complex recipes
5. **MASTER**: Legendary artisan, peak achievements

### Soft-Cap vs Hard-Cap

**Hard-Cap System** (rejected):
```
Level 5 = Max, no further progression
```

**Soft-Cap System** (implemented):
```
MASTER = 1000 XP threshold, but XP continues beyond
```

**Advantages:**
- **No Artificial Ceiling**: Players can always improve
- **Diminishing Returns**: Harder to progress, but possible
- **Prestige Tracking**: "I have 5000 Smithing XP" vs "I'm level 5"
- **Future Expansion**: Add GRANDMASTER tier without breaking saves

## Enum Values

### NOVICE (Tier 1)
```java
NOVICE(1, "Novice", 0, 0.5f)
```

**Characteristics:**
- **XP Required**: 0 (starting tier)
- **XP to Next**: 100 XP
- **Failure Multiplier**: 0.5× (50% penalty per tier below recipe requirement)

**Unlocks:**
- Basic recipes (crafting 101)
- Common material items
- Simple tools

**Example Recipes:**
- Wooden club (WEAPON, 5 damage)
- Cloth armor (ARMOR, 2 armor)
- Basic bandage (CONSUMABLE, +10 HP)

**Success Rates:**
- Novice recipe: 90%+ success
- Apprentice recipe: 70% success (1 tier below)
- Journeyman recipe: 50% success (2 tiers below)

### APPRENTICE (Tier 2)
```java
APPRENTICE(2, "Apprentice", 100, 0.4f)
```

**Characteristics:**
- **XP Required**: 100 XP
- **XP to Next**: 200 XP (300 total)
- **Failure Multiplier**: 0.4× (40% penalty per tier)

**Unlocks:**
- Intermediate recipes
- Uncommon rarity items
- Basic weapons/armor

**Example Recipes:**
- Iron sword (WEAPON, 10 damage)
- Leather armor (ARMOR, 5 armor)
- Health potion (POTION, +50 HP)

**Milestone**: First "real" crafting tier, items become useful

### JOURNEYMAN (Tier 3)
```java
JOURNEYMAN(3, "Journeyman", 300, 0.3f)
```

**Characteristics:**
- **XP Required**: 300 XP (cumulative)
- **XP to Next**: 300 XP (600 total)
- **Failure Multiplier**: 0.3× (30% penalty)

**Unlocks:**
- Advanced recipes
- Rare rarity items
- Multi-component crafting

**Example Recipes:**
- Steel axe (WEAPON, 15 damage)
- Plate armor (ARMOR, 15 armor)
- Superior potion (POTION, +100 HP)

**Milestone**: Professional tier, can make money from crafting

### EXPERT (Tier 4)
```java
EXPERT(4, "Expert", 600, 0.2f)
```

**Characteristics:**
- **XP Required**: 600 XP (cumulative)
- **XP to Next**: 400 XP (1000 total)
- **Failure Multiplier**: 0.2× (20% penalty)

**Unlocks:**
- Expert recipes
- Epic rarity items
- Enchanting/enhancement

**Example Recipes:**
- Enchanted sword (WEAPON, 25 damage + fire)
- Dragon scale armor (ARMOR, 30 armor)
- Major elixir (POTION, +200 HP + buff)

**Milestone**: Elite tier, respected master craftsman

### MASTER (Tier 5)
```java
MASTER(5, "Master", 1000, 0.1f)
```

**Characteristics:**
- **XP Required**: 1000 XP (cumulative)
- **XP to Next**: Infinite (soft cap)
- **Failure Multiplier**: 0.1× (10% penalty)

**Unlocks:**
- Master recipes
- Legendary/Artifact rarity items
- Unique crafting abilities

**Example Recipes:**
- Legendary blade (WEAPON, 50 damage + special)
- Artifact armor (ARMOR, 50 armor + set bonus)
- Philosopher's stone (REAGENT, transmutation)

**Milestone**: Peak achievement, can craft anything in game

**Beyond Master:**
```java
// XP continues beyond 1000, tracked for prestige
// Future: GRANDMASTER tier at 5000 XP?
```

## XP Progression Curve

### Tier Requirements
```
NOVICE      →  APPRENTICE:  100 XP  (Δ100)
APPRENTICE  →  JOURNEYMAN:  300 XP  (Δ200, +100%)
JOURNEYMAN  →  EXPERT:      600 XP  (Δ300, +50%)
EXPERT      →  MASTER:      1000 XP (Δ400, +33%)
MASTER      →  Beyond:      ∞      (soft cap)
```

**Pattern**: Each tier requires `+100 XP` more than the previous gap

### Time to Master (Estimated)

**Assumptions:**
- Average craft: 20 XP (Common sword, Normal quality, specialized)
- 1 craft per 5 minutes (gathering materials)

**Calculation:**
```
NOVICE → APPRENTICE:  100 XP = 5 crafts   = 25 minutes
APPRENTICE → JOURNEYMAN: 200 XP = 10 crafts  = 50 minutes
JOURNEYMAN → EXPERT:     300 XP = 15 crafts  = 75 minutes
EXPERT → MASTER:         400 XP = 20 crafts  = 100 minutes

Total: 1000 XP = 50 crafts = 250 minutes (~4 hours)
```

**Realistic Estimate**: 6-8 hours (accounting for material gathering, failures, exploration)

**Design Goal**: Master tier achievable in a weekend of focused play

## Failure Multiplier System

### How It Works

```java
float failureChance = proficiencyDiff * recipe.getFailureMultiplier();
float finalSuccessChance = recipe.getBaseSuccessChance() - failureChance;
```

**Example: Journeyman Recipe**
```java
// Player is Apprentice (tier 2), Recipe requires Journeyman (tier 3)
int proficiencyDiff = 3 - 2 = 1;
float failureMultiplier = 0.3f;  // Journeyman's multiplier
float baseSuccess = 0.9f;

float penalty = 1 * 0.3f = 0.3 (30% penalty)
float finalSuccess = 0.9f - 0.3f = 0.6 (60% success rate)
```

### Penalty Scaling

| Tier Difference | NOVICE (0.5×) | APPRENTICE (0.4×) | JOURNEYMAN (0.3×) | EXPERT (0.2×) | MASTER (0.1×) |
|----------------|---------------|-------------------|-------------------|---------------|---------------|
| 1 tier below   | -50%          | -40%              | -30%              | -20%          | -10%          |
| 2 tiers below  | -100% (fail)  | -80%              | -60%              | -40%          | -20%          |
| 3 tiers below  | -150% (fail)  | -120% (fail)      | -90%              | -60%          | -30%          |

**Design Insight**: Higher tier recipes have lower failure multipliers (more forgiving)

### Success Rate Examples

**Master Recipe (base 90% success, 0.1× multiplier):**
- At Master: 90% success
- 1 tier below (Expert): 90% - (1 × 10%) = 80%
- 2 tiers below (Journeyman): 90% - (2 × 10%) = 70%
- 3 tiers below (Apprentice): 90% - (3 × 10%) = 60%

**Novice Recipe (base 95% success, 0.5× multiplier):**
- At Novice: 95% success
- 1 tier above (Apprentice): 95% (no penalty for exceeding requirement)

**Design Decision**: Lower tier recipes punish under-leveled attempts more harshly

## API Reference

### Fields
```java
private final int tier;                     // Tier number (1-5)
private final String name;                  // Display name ("Novice")
private final int xpRequired;               // Cumulative XP threshold
private final float failureMultiplier;      // Penalty per tier below
```

### Methods
```java
int getTier()                               // Get tier number
String getName()                            // Get display name
int getXpRequired()                         // Get XP threshold
float getFailureMultiplier()                // Get failure penalty
```

### Progression Methods
```java
static CraftingProficiency fromXp(int xp)   // Get proficiency for XP amount
CraftingProficiency next()                  // Get next tier (null if MASTER)
int getXpToNextLevel(int currentXp)         // XP needed for next tier
```

## Usage Examples

### Determining Proficiency from XP

```java
int playerXP = 250;
CraftingProficiency proficiency = CraftingProficiency.fromXp(playerXP);
System.out.println(proficiency.getName());  // "Journeyman"

// Edge cases
CraftingProficiency.fromXp(0);    // NOVICE
CraftingProficiency.fromXp(100);  // APPRENTICE (exactly at threshold)
CraftingProficiency.fromXp(99);   // NOVICE (1 XP short)
CraftingProficiency.fromXp(5000); // MASTER (beyond soft cap)
```

### Checking Next Tier

```java
CraftingProficiency current = CraftingProficiency.JOURNEYMAN;
CraftingProficiency next = current.next();

if (next != null) {
    int xpNeeded = next.getXpRequired() - playerCurrentXP;
    System.out.println("Need " + xpNeeded + " more XP for " + next.getName());
    // "Need 300 more XP for Expert"
}
```

### Progress Bar Display

```java
public void displayProgress(int currentXp) {
    CraftingProficiency current = CraftingProficiency.fromXp(currentXp);
    CraftingProficiency next = current.next();
    
    if (next == null) {
        System.out.println("[MASTER] Max tier reached!");
        return;
    }
    
    int xpIntoTier = currentXp - current.getXpRequired();
    int xpNeededForNext = next.getXpRequired() - current.getXpRequired();
    float progress = (float) xpIntoTier / xpNeededForNext;
    
    String bar = "█".repeat((int)(progress * 20)) + "░".repeat(20 - (int)(progress * 20));
    System.out.println("[" + current.getName() + "] [" + bar + "] " + 
                       (int)(progress * 100) + "% (" + xpIntoTier + "/" + xpNeededForNext + ")");
    // "[Journeyman] [████████████░░░░░░░░] 60% (180/300)"
}
```

### Recipe Validation

```java
public boolean canCraft(CraftingRecipe recipe, CraftingSkills skills) {
    CraftingProficiency required = recipe.getRequiredProficiency();
    CraftingProficiency actual = skills.getProficiency(recipe.getCategory());
    
    if (actual.getTier() < required.getTier()) {
        System.out.println("Requires " + required.getName() + " proficiency!");
        return false;
    }
    return true;
}
```

### Failure Chance Calculation

```java
public float calculateSuccessChance(CraftingRecipe recipe, CraftingSkills skills) {
    CraftingProficiency required = recipe.getRequiredProficiency();
    CraftingProficiency actual = skills.getProficiency(recipe.getCategory());
    
    int tierDiff = Math.max(0, required.getTier() - actual.getTier());
    float penalty = tierDiff * required.getFailureMultiplier();
    
    return Math.max(0.0f, recipe.getBaseSuccessChance() - penalty);
}

// Example
float chance = calculateSuccessChance(masterRecipe, apprenticeSkills);
// Recipe: MASTER (tier 5), Player: APPRENTICE (tier 2)
// Diff: 5 - 2 = 3 tiers
// Penalty: 3 × 0.1 = 0.3
// Success: 0.9 - 0.3 = 0.6 (60%)
```

## Integration with Other Systems

### CraftingSkills
```java
public class CraftingSkills {
    private Map<CraftingCategory, Integer> xpMap;
    
    public CraftingProficiency getProficiency(CraftingCategory category) {
        int xp = xpMap.getOrDefault(category, 0);
        return CraftingProficiency.fromXp(xp);
    }
}
```

### CraftingRecipe
```java
public class CraftingRecipe {
    private CraftingProficiency requiredProficiency;
    
    public float calculateFailureChance(CraftingProficiency actualProficiency) {
        int tierDiff = Math.max(0, requiredProficiency.getTier() - actualProficiency.getTier());
        return tierDiff * requiredProficiency.getFailureMultiplier();
    }
}
```

### CraftingSystem
```java
public CraftingResult craft(CraftingRecipe recipe, CraftingSkills skills) {
    CraftingProficiency required = recipe.getRequiredProficiency();
    CraftingProficiency actual = skills.getProficiency(recipe.getCategory());
    
    // Calculate success chance
    float successChance = 1.0f - recipe.calculateFailureChance(actual);
    
    // Roll for success
    if (random.nextFloat() < successChance) {
        return CraftingResult.success(...);
    } else {
        return CraftingResult.failure(...);
    }
}
```

## Performance Considerations

### Enum Performance
- **Memory**: 5 enum constants × ~60 bytes = ~300 bytes
- **Lookup**: `O(1)` by ordinal
- **fromXp()**: `O(n)` linear scan (5 tiers, negligible)

### Optimization: fromXp() Lookup
```java
// Current: Linear scan
public static CraftingProficiency fromXp(int xp) {
    for (CraftingProficiency prof : values()) {
        if (xp < prof.xpRequired) {
            return prof;
        }
    }
    return MASTER;
}

// Future: Binary search (unnecessary for 5 tiers)
```

## Design Decisions

### Why Decreasing Failure Multipliers?

```java
NOVICE:      0.5× (harsh penalty)
APPRENTICE:  0.4×
JOURNEYMAN:  0.3×
EXPERT:      0.2×
MASTER:      0.1× (forgiving penalty)
```

**Rationale**: Higher tier recipes are **more complex**, so being under-leveled is **more forgiving**

**Example:**
- Novice recipe, 1 tier below: -50% success (harsh)
- Master recipe, 1 tier below: -10% success (forgiving)

**Design Philosophy**: "Master recipes require mastery to **optimize**, but experts can still **attempt** them"

### Why 1000 XP for Master?

**Playtesting Goals:**
- Achievable in 1 weekend (dedicated play)
- Not trivial (requires ~50 crafts)
- Allows specialization (2 categories → ~8 hours total)

**Alternative Considered:**
- 5000 XP → rejected (too grindy)
- 500 XP → rejected (too easy)

### Why Soft Cap Instead of Hard Cap?

**Advantages:**
1. **No Artificial Limit**: Players feel unrestricted
2. **Prestige Tracking**: "I have 5000 XP" vs "I'm max level"
3. **Future Expansion**: Add GRANDMASTER without breaking saves
4. **Diminishing Returns**: Natural slow-down without frustration

**Implementation:**
```java
public CraftingProficiency next() {
    if (this == MASTER) return null;  // Soft cap
    return values()[ordinal() + 1];
}
```

## Known Limitations

1. **No Sub-Tiers**: No "Journeyman 1, 2, 3" progression
2. **No Tier Decay**: Skills don't rust from disuse
3. **No Tier Bonuses**: Tiers only unlock recipes, no stat bonuses
4. **Fixed XP Curve**: Can't adjust difficulty per server

## Future Enhancements

### Phase 1.6: Grandmaster Tier
```java
GRANDMASTER(6, "Grandmaster", 5000, 0.05f)
```

### Phase 1.7: Tier Bonuses
```java
public float getCraftingSpeedBonus() {
    return switch (this) {
        case NOVICE -> 1.0f;
        case APPRENTICE -> 0.95f;  // 5% faster
        case JOURNEYMAN -> 0.90f;  // 10% faster
        case EXPERT -> 0.85f;
        case MASTER -> 0.80f;  // 20% faster
    };
}
```

### Phase 2: Prestige System
```java
// Reset to Novice, keep XP as "prestige points"
public void prestige(CraftingSkills skills) {
    int currentXp = skills.getXp(category);
    skills.resetXp(category);
    skills.addPrestigePoints(category, currentXp / 1000);
    // Prestige bonuses: +1% quality per prestige point
}
```

## Testing

See [CraftingProficiencyTest.md](../../../../../test/java/org/adventure/CraftingProficiencyTest.md) for test coverage:
- All 5 tiers defined correctly
- XP thresholds (0, 100, 300, 600, 1000)
- fromXp() lookup (edge cases)
- next() progression
- getXpToNextLevel() calculations
- Failure multiplier scaling

## References

- **Design Docs**: `docs/objects_crafting_legacy.md` → Progression System
- **Data Models**: `docs/data_models.md` → CraftingSkills Schema
- **Grand Plan**: `docs/grand_plan.md` → Phase 1.4 Crafting
- **Related Classes**: [CraftingSkills.md](CraftingSkills.md), [CraftingRecipe.md](CraftingRecipe.md)
- **Tests**: [CraftingTest.md](../../../../../test/java/org/adventure/CraftingTest.md)

---

**Last Updated:** Phase 1.4 Implementation (November 2025)  
**Status:** ✅ Complete - 5 tiers, soft-cap progression, tested
