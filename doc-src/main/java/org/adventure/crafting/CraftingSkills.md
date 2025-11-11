# CraftingSkills.java - Progression Tracker

**Package:** `org.adventure.crafting`  
**Source:** [CraftingSkills.java](../../../../../src/main/java/org/adventure/crafting/CraftingSkills.java)  
**Phase:** MVP Phase 1.4 (Items & Crafting)

## Overview

`CraftingSkills` tracks a character's crafting progression across all eight categories (Smithing, Alchemy, Enchanting, etc.). It manages XP accumulation, specialization bonuses (+20% XP), and tier-based XP penalties (50% for below-tier recipes). This class is the bridge between the character system (Phase 1.3) and the crafting system (Phase 1.4).

From `docs/objects_crafting_legacy.md`:
> "Each character tracks crafting XP independently per category, with specializations providing bonus XP to accelerate mastery."

## Design Philosophy

### Per-Category Independence

**Design Decision**: Each category tracks XP independently

```java
private final Map<CraftingCategory, Integer> categoryXp;
```

**Why Not Shared XP Pool?**
- **Specialization Matters**: Smithing XP doesn't help Alchemy
- **Realistic**: Real-world crafting skills are separate
- **Progression Clarity**: Players see exactly what they've practiced
- **Balance**: Prevents "jack of all trades" meta

**Alternative Rejected**: Single "Crafting" XP → rejected as too simplistic

### The 2-Specialization System

**Core Rule**: Max 2 specializations per character

```java
private static final int MAX_SPECIALIZATIONS = 2;
private final Set<CraftingCategory> specializations;
```

**Rationale**:
- **Meaningful Choice**: Can't master everything
- **Build Diversity**: Different specialization combos = different playstyles
- **Economic Interdependence**: Need other crafters for non-specialized categories
- **+20% XP Bonus**: Significant advantage without being overpowered

**Specialization Formula**:
```
finalXP = baseXP × specializationMultiplier × qualityMultiplier × tierPenalty
specializationMultiplier = isSpecialized ? 1.2 : 1.0
```

**Example**:
```java
// Specialized Smithing
XP = 100 × 1.2 × 1.0 × 1.0 = 120 XP

// Non-specialized Smithing
XP = 100 × 1.0 × 1.0 × 1.0 = 100 XP

// Difference: 20% faster progression when specialized
```

### Below-Tier XP Penalty

**Problem**: Grinding low-tier recipes for XP

**Solution**: 50% XP penalty for crafting below your proficiency tier

```java
private static final float BELOW_TIER_XP_PENALTY = 0.5f;
```

**Example**:
```java
// Expert crafter (tier 4) crafting Novice recipe (tier 1)
baseXP = 100
tierDiff = 4 - 1 = 3 (below tier)
penalty = 0.5×
finalXP = 100 × 0.5 = 50 XP

// Expert crafter crafting Expert recipe (tier 4)
finalXP = 100 × 1.0 = 100 XP (no penalty)
```

**Design Goal**: Encourage crafting at-tier or above-tier recipes for optimal XP

## Class Structure

### Fields

```java
private static final int MAX_SPECIALIZATIONS = 2;
private static final float SPECIALIZATION_XP_BONUS = 0.2f;  // +20%
private static final float BELOW_TIER_XP_PENALTY = 0.5f;    // 50% penalty

private final String characterId;                            // Owner character
private final Map<CraftingCategory, Integer> categoryXp;     // XP per category
private final Set<CraftingCategory> specializations;         // Max 2
```

## Core Methods

### Getting XP and Proficiency

```java
public int getXp(CraftingCategory category) {
    return categoryXp.getOrDefault(category, 0);
}

public CraftingProficiency getProficiency(CraftingCategory category) {
    int xp = getXp(category);
    return CraftingProficiency.fromXp(xp);
}
```

**Usage**:
```java
CraftingSkills skills = new CraftingSkills("player_001");

int smithingXP = skills.getXp(CraftingCategory.SMITHING);  // 250
CraftingProficiency prof = skills.getProficiency(CraftingCategory.SMITHING);  // JOURNEYMAN
```

### Specialization Management

```java
public boolean addSpecialization(CraftingCategory category) {
    if (specializations.size() >= MAX_SPECIALIZATIONS) {
        return false;  // Already at max
    }
    return specializations.add(category);
}

public boolean removeSpecialization(CraftingCategory category) {
    return specializations.remove(category);
}

public boolean isSpecialized(CraftingCategory category) {
    return specializations.contains(category);
}
```

**Example**:
```java
CraftingSkills skills = new CraftingSkills("player_001");

// Add first specialization
boolean success1 = skills.addSpecialization(CraftingCategory.SMITHING);
System.out.println(success1);  // true

// Add second specialization
boolean success2 = skills.addSpecialization(CraftingCategory.ENCHANTING);
System.out.println(success2);  // true

// Try to add third (fails)
boolean success3 = skills.addSpecialization(CraftingCategory.ALCHEMY);
System.out.println(success3);  // false (already have 2)

// Check specialization
boolean isSmithingSpecialized = skills.isSpecialized(CraftingCategory.SMITHING);
System.out.println(isSmithingSpecialized);  // true
```

### XP Gain with Full Modifiers

```java
public int addXp(CraftingCategory category, int baseXp, 
                 CraftingProficiency recipeProficiency, 
                 float qualityBonus, float toolBonus) {
    
    float multiplier = 1.0f;
    
    // Apply specialization bonus (+20%)
    if (isSpecialized(category)) {
        multiplier += SPECIALIZATION_XP_BONUS;
    }
    
    // Apply penalty for crafting below current tier (-50%)
    CraftingProficiency currentProficiency = getProficiency(category);
    if (recipeProficiency.ordinal() < currentProficiency.ordinal()) {
        multiplier *= BELOW_TIER_XP_PENALTY;
    }
    
    // Apply quality and tool bonuses
    multiplier *= qualityBonus;
    multiplier *= toolBonus;
    
    int xpGained = Math.round(baseXp * multiplier);
    
    int currentXp = getXp(category);
    categoryXp.put(category, currentXp + xpGained);
    
    return xpGained;
}
```

**Full XP Calculation Example**:
```java
// Specialized Smithing, Expert crafter, crafting Journeyman recipe, Masterwork quality
baseXP = 100
specialization = 1.2× (is specialized)
tierPenalty = 0.5× (Journeyman tier 3 < Expert tier 4, below tier)
quality = 2.0× (Masterwork)
toolBonus = 1.2× (Fine tools)

finalXP = 100 × 1.2 × 0.5 × 2.0 × 1.2 = 144 XP
```

**Breakdown**:
1. Base: 100 XP
2. Specialization: 100 × 1.2 = 120 XP
3. Tier penalty: 120 × 0.5 = 60 XP (below tier)
4. Quality: 60 × 2.0 = 120 XP
5. Tools: 120 × 1.2 = 144 XP

### Simplified XP Method

```java
public int addXp(CraftingCategory category, int baseXp) {
    return addXp(category, baseXp, CraftingProficiency.NOVICE, 1.0f, 1.0f);
}
```

**Usage for simple cases**:
```java
skills.addXp(CraftingCategory.SMITHING, 50);  // Just add 50 XP
```

### Progress Tracking

```java
public float getProgressToNextLevel(CraftingCategory category) {
    int xp = getXp(category);
    CraftingProficiency current = CraftingProficiency.fromXp(xp);
    CraftingProficiency next = current.next();
    
    if (next == null) {
        return 1.0f;  // Already at Master
    }
    
    int xpInCurrentTier = xp - current.getMinXp();
    int xpRequiredForTier = next.getMinXp() - current.getMinXp();
    
    return (float) xpInCurrentTier / xpRequiredForTier;
}
```

**Example**:
```java
// Player has 250 XP in Smithing
// Journeyman (300 XP) → Expert (600 XP) = 300 XP gap
// 250 XP in tier, need 300 total

int xp = 250;
CraftingProficiency current = CraftingProficiency.JOURNEYMAN;  // 300 XP threshold
CraftingProficiency next = CraftingProficiency.EXPERT;  // 600 XP threshold

int xpInTier = 250 - 300 = -50  // Wait, player is still Apprentice!
// Correct: 250 XP → Apprentice (100-299 range)

current = CraftingProficiency.APPRENTICE;  // 100 XP threshold
next = CraftingProficiency.JOURNEYMAN;  // 300 XP threshold

xpInTier = 250 - 100 = 150
xpNeeded = 300 - 100 = 200
progress = 150 / 200 = 0.75 (75% to Journeyman)
```

## Usage Examples

### Character Creation

```java
Character player = new Character("player_001", "Thorin", Race.DWARF);
CraftingSkills skills = new CraftingSkills(player.getId());

// Choose specializations
skills.addSpecialization(CraftingCategory.SMITHING);
skills.addSpecialization(CraftingCategory.ENCHANTING);

// Thorin is now a weapon smith who can enchant his own creations
```

### Crafting with XP Gain

```java
CraftingSystem craftingSystem = new CraftingSystem();
CraftingRecipe recipe = craftingSystem.getRecipe("recipe_iron_sword");
CraftingSkills skills = player.getCraftingSkills();

// Check if can craft
CraftingProficiency required = recipe.getMinProficiency();
CraftingProficiency actual = skills.getProficiency(recipe.getCategory());

if (actual.ordinal() >= required.ordinal()) {
    // Craft item
    CraftingResult result = craftingSystem.craft(recipe, skills, materials, tools);
    
    if (result.isSuccess()) {
        System.out.println("Crafted " + result.getQuality() + " quality sword!");
        System.out.println("Gained " + result.getXpGained() + " XP");
        
        // Check for level up
        CraftingProficiency newProf = skills.getProficiency(recipe.getCategory());
        if (newProf != actual) {
            System.out.println("Level up! Now " + newProf.getName());
        }
    }
}
```

### UI Progress Display

```java
public void displayCraftingSkills(CraftingSkills skills) {
    System.out.println("=== Crafting Skills ===");
    
    for (CraftingCategory category : CraftingCategory.values()) {
        CraftingProficiency prof = skills.getProficiency(category);
        int xp = skills.getXp(category);
        float progress = skills.getProgressToNextLevel(category);
        boolean specialized = skills.isSpecialized(category);
        
        String marker = specialized ? "[★]" : "[ ]";
        String bar = createProgressBar(progress, 20);
        
        System.out.println(String.format("%s %s: %s (%d XP) %s",
            marker,
            category.getName(),
            prof.getName(),
            xp,
            bar
        ));
    }
}

private String createProgressBar(float progress, int width) {
    int filled = (int) (progress * width);
    return "[" + "█".repeat(filled) + "░".repeat(width - filled) + "]";
}

// Output:
// === Crafting Skills ===
// [★] Smithing: Expert (720 XP) [████████████████░░░░]
// [★] Enchanting: Journeyman (450 XP) [███████░░░░░░░░░░░░░░]
// [ ] Alchemy: Apprentice (150 XP) [██████████░░░░░░░░░░]
// [ ] Cooking: Novice (50 XP) [██████████░░░░░░░░░░]
// ...
```

### Specialization Choice UI

```java
public void offerSpecialization(Character player, CraftingCategory category) {
    CraftingSkills skills = player.getCraftingSkills();
    
    if (skills.getSpecializations().size() >= 2) {
        System.out.println("You already have 2 specializations!");
        System.out.println("Current: " + skills.getSpecializations());
        return;
    }
    
    if (skills.isSpecialized(category)) {
        System.out.println("You're already specialized in " + category.getName());
        return;
    }
    
    // Offer choice
    System.out.println("Specialize in " + category.getName() + "?");
    System.out.println("Benefits: +20% XP in this category");
    System.out.println("Cost: Can only have 2 specializations total");
    
    // Player chooses yes
    boolean success = skills.addSpecialization(category);
    if (success) {
        System.out.println("You are now specialized in " + category.getName() + "!");
    }
}
```

## Integration with Other Systems

### Character (Phase 1.3)
```java
public class Character {
    private CraftingSkills craftingSkills;
    
    public Character(String id, String name, Race race) {
        this.craftingSkills = new CraftingSkills(id);
    }
    
    public CraftingSkills getCraftingSkills() {
        return craftingSkills;
    }
}
```

### CraftingSystem (Phase 1.4)
```java
public CraftingResult craft(CraftingRecipe recipe, CraftingSkills skills, ...) {
    // Check proficiency
    CraftingProficiency prof = skills.getProficiency(recipe.getCategory());
    
    // ... craft item ...
    
    // Grant XP
    ItemRarity rarity = prototype.getRarity();
    int xpGained = skills.addXp(
        recipe.getCategory(),
        recipe.getBaseXp(),
        recipe.getMinProficiency(),
        result.getQuality().getXpMultiplier(),
        toolQualityMultiplier
    );
    
    // Apply rarity multiplier
    xpGained = Math.round(xpGained * rarity.getXpMultiplier());
    
    return result;
}
```

### Persistence (Phase 1.8)
```java
// Save crafting skills
{
  "characterId": "player_001",
  "categoryXp": {
    "SMITHING": 720,
    "ENCHANTING": 450,
    "ALCHEMY": 150
  },
  "specializations": ["SMITHING", "ENCHANTING"]
}
```

## Performance Considerations

### Memory Footprint

**Per CraftingSkills instance:**
- Character ID: 36 bytes
- Category XP Map: 8 entries × 20 bytes = 160 bytes
- Specializations Set: 2 entries × 20 bytes = 40 bytes
- **Total**: ~236 bytes per character

**Scalability:**
- 1,000 characters: 236 KB
- 10,000 characters: 2.36 MB (acceptable)

### XP Calculation Performance

```java
// addXp() complexity: O(1)
- HashMap lookup: O(1)
- Enum ordinal comparison: O(1)
- Float arithmetic: ~10 CPU cycles
- HashMap put: O(1)

// Total: ~50 CPU cycles per XP gain
```

**No per-tick updates**: XP only calculated on crafting actions

## Design Decisions

### Why Cap Specializations at 2?

**Alternatives Considered:**
- **1 specialization**: Too restrictive, no hybrid builds
- **3 specializations**: Too permissive, defeats purpose
- **Unlimited**: Everyone masters everything → no trade economy

**2 Specializations Wins:**
- Allows meaningful hybrids (Smithing + Enchanting)
- Forces strategic choice (can't do everything)
- Creates interdependence (need others for 3rd+ categories)
- Replayability (different combos = different playstyles)

### Why +20% XP Bonus (Not +50% or +10%)?

**Playtesting Analysis:**
- **+10%**: Not impactful enough (1000 XP in 50 crafts vs 45 crafts)
- **+20%**: Noticeable advantage (1000 XP in 50 crafts vs 42 crafts)
- **+50%**: Too strong, non-specialized becomes worthless

**Balance Goal**: Specialized progression ~15-20% faster

### Why 50% Penalty for Below-Tier Recipes?

**Problem**: Grinding Novice recipes for XP at Expert tier

**Solution**: Harsh penalty discourages low-tier spam

**Example:**
```
Expert crafting Novice recipe:
100 XP × 0.5 = 50 XP (half reward)

Expert crafting Expert recipe:
100 XP × 1.0 = 100 XP (full reward)
```

**Design Goal**: Encourage at-tier or above-tier crafting

### Why Independent XP Per Category?

**Alternative**: Shared "Crafting" XP pool

**Independent Wins Because:**
- **Realism**: Smithing skill ≠ Alchemy skill
- **Specialization Matters**: Can't "fake" expertise
- **Clear Progression**: See exactly what you've practiced
- **Balance**: Prevents master-of-all meta

## Known Limitations

1. **No Specialization Respec**: Once chosen, permanent (future: costly respec)
2. **No Temporary Specialization**: Can't specialize for one quest
3. **No Specialization Tiers**: Novice/Expert specialization same bonus
4. **Fixed Bonus**: +20% for all specializations (no category-specific bonuses)

## Future Enhancements

### Phase 1.6: Specialization Respec
```java
public boolean resetSpecializations(int goldCost) {
    if (player.getGold() >= goldCost) {
        player.spendGold(goldCost);
        specializations.clear();
        return true;
    }
    return false;
}
```

### Phase 1.7: Temporary Buffs
```java
// Crafting in guild hall = +10% XP for 1 hour
public void addTemporaryXpBonus(CraftingCategory category, float bonus, long durationTicks) {
    temporaryBonuses.put(category, new Bonus(bonus, durationTicks));
}
```

### Phase 2: Mastery Titles
```java
// Master in category unlocks title
if (getProficiency(CraftingCategory.SMITHING) == CraftingProficiency.MASTER) {
    player.unlockTitle("Master Smith");
}
```

## Testing

See [CraftingSkillsTest.md](../../../../../test/java/org/adventure/CraftingSkillsTest.md) for test coverage:
- XP tracking per category
- Specialization management (max 2)
- XP bonuses (+20% specialized)
- Tier penalties (50% below tier)
- Progress calculations
- Edge cases (negative XP, max specializations)

## References

- **Design Docs**: `docs/objects_crafting_legacy.md` → Progression System
- **Data Models**: `docs/data_models.md` → CraftingSkills Schema
- **Grand Plan**: `docs/grand_plan.md` → Phase 1.4 Character Integration
- **Related Classes**: [CraftingProficiency.md](CraftingProficiency.md), [CraftingCategory.md](CraftingCategory.md)
- **System**: [CraftingSystem.md](CraftingSystem.md)
- **Tests**: [CraftingTest.md](../../../../../test/java/org/adventure/CraftingTest.md)

---

**Last Updated:** Phase 1.4 Implementation (November 2025)  
**Status:** ✅ Complete - XP tracking, specialization system, tested
