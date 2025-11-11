# CraftingQuality.java - Quality Tier System

**Package:** `org.adventure.crafting`  
**Source:** [CraftingQuality.java](../../../../../src/main/java/org/adventure/crafting/CraftingQuality.java)  
**Phase:** MVP Phase 1.4 (Items & Crafting)

## Overview

`CraftingQuality` defines four quality tiers for crafted items, determined randomly upon successful crafting. Quality affects the crafted item's durability, XP rewards, and potential market value. This system adds variance and excitement to crafting outcomes.

From `docs/objects_crafting_legacy.md`:
> "Crafted items have quality tiers affecting durability and XP rewards, encouraging players to seek high-quality results."

## Design Philosophy

### Why Quality Randomness?

**Problem**: Deterministic crafting becomes boring
- Same recipe always produces identical output
- No excitement in crafting process
- No distinction between crafters of same skill

**Solution**: Random quality roll on success
- Same recipe can produce different quality items
- Excitement: "Will I get Masterwork?"
- Skill expression: Master crafters get more attempts

### Quality vs Rarity

**Rarity** (ItemRarity): Template property, fixed
- COMMON Iron Sword template
- LEGENDARY Excalibur template

**Quality** (CraftingQuality): Instance property, rolled
- NORMAL quality iron sword
- MASTERWORK quality iron sword
- Both from same COMMON template

**Independence**: Quality and Rarity multiply
```java
// Legendary base, Masterwork quality
XP = baseXP × 5.0 (legendary) × 2.0 (masterwork) = 10× XP!
```

## Enum Values

### POOR (Tier 1)
```java
POOR(1, "Poor", 0.5f, 0.7f)
```

**Characteristics:**
- **XP Multiplier**: 0.5× (half XP)
- **Durability Multiplier**: 0.7× (30% less durable)
- **Drop Chance**: 15% (on successful craft)

**Description**: Substandard craftsmanship, functional but flawed

**Effects:**
- Sword: 200 max durability → 140 durability
- XP reward: 100 XP → 50 XP
- Visual: Tarnished, scratched appearance

**When It Happens:**
- Low-level crafter attempting at-tier recipe
- Bad RNG roll (15% chance)
- Crafting while fatigued/debuffed (future)

**Player Reaction**: "Ugh, poor quality again. Sell to vendor."

### NORMAL (Tier 2)
```java
NORMAL(2, "Normal", 1.0f, 1.0f)
```

**Characteristics:**
- **XP Multiplier**: 1.0× (baseline XP)
- **Durability Multiplier**: 1.0× (full durability)
- **Drop Chance**: 60% (on successful craft)

**Description**: Standard craftsmanship, meets specifications

**Effects:**
- Sword: 200 max durability (as designed)
- XP reward: 100 XP (full amount)
- Visual: Clean, standard appearance

**When It Happens:**
- Most common result (60% of crafts)
- Expected outcome for average crafter

**Player Reaction**: "Normal quality, good enough to use/sell."

### FINE (Tier 3)
```java
FINE(3, "Fine", 1.5f, 1.15f)
```

**Characteristics:**
- **XP Multiplier**: 1.5× (50% bonus XP)
- **Durability Multiplier**: 1.15× (15% more durable)
- **Drop Chance**: 20% (on successful craft)

**Description**: Above-average craftsmanship, attention to detail

**Effects:**
- Sword: 200 max durability → 230 durability
- XP reward: 100 XP → 150 XP
- Visual: Polished, refined appearance

**When It Happens:**
- Lucky roll (20% chance)
- High-skill crafter (no direct bonus, but more attempts)

**Player Reaction**: "Nice! Fine quality, I'll keep this."

### MASTERWORK (Tier 4)
```java
MASTERWORK(4, "Masterwork", 2.0f, 1.3f)
```

**Characteristics:**
- **XP Multiplier**: 2.0× (double XP!)
- **Durability Multiplier**: 1.3× (30% more durable)
- **Drop Chance**: 5% (on successful craft)

**Description**: Exceptional craftsmanship, peak quality

**Effects:**
- Sword: 200 max durability → 260 durability
- XP reward: 100 XP → 200 XP (double!)
- Visual: Gleaming, perfect appearance
- Potential: Higher resale value, prestige

**When It Happens:**
- Rare roll (5% chance)
- "Crit" of crafting system

**Player Reaction**: "MASTERWORK! This is going in my collection!"

**Special Uses:**
- Signature weapons (keep forever)
- Guild recruitment (show off your Masterwork)
- High-value trading
- Enchanting base (don't waste enchants on poor quality)

## Quality Distribution

### Probability on Success

```java
float roll = random.nextFloat();  // 0.0 to 1.0

if (roll < 0.05f) {
    quality = MASTERWORK;      // 5%
} else if (roll < 0.25f) {
    quality = FINE;            // 20% (0.05 to 0.25)
} else if (roll < 0.85f) {
    quality = NORMAL;          // 60% (0.25 to 0.85)
} else {
    quality = POOR;            // 15% (0.85 to 1.0)
}
```

### Quality Per 100 Successful Crafts

| Quality | Probability | Expected Count |
|---------|-------------|----------------|
| Poor | 15% | 15 |
| Normal | 60% | 60 |
| Fine | 20% | 20 |
| Masterwork | 5% | 5 |

**Total**: 100 crafts → 5 Masterwork, 20 Fine, 60 Normal, 15 Poor

### Expected Value

**Average XP Multiplier:**
```
(0.15 × 0.5) + (0.60 × 1.0) + (0.20 × 1.5) + (0.05 × 2.0)
= 0.075 + 0.6 + 0.3 + 0.1
= 1.075×
```

**Interpretation**: On average, quality adds ~7.5% bonus XP over pure "Normal" rolls

**Average Durability Multiplier:**
```
(0.15 × 0.7) + (0.60 × 1.0) + (0.20 × 1.15) + (0.05 × 1.3)
= 0.105 + 0.6 + 0.23 + 0.065
= 1.0×
```

**Interpretation**: Average durability is approximately baseline

## Multiplier System

### XP Multiplier

```java
private final float xpMultiplier;

public float getXpMultiplier() {
    return xpMultiplier;
}
```

**Formula**:
```
finalXP = baseXP × rarityMultiplier × qualityMultiplier × specializationBonus
```

**Example: Masterwork Legendary Sword**
```java
baseXP = 20
rarity = ItemRarity.LEGENDARY (5.0×)
quality = CraftingQuality.MASTERWORK (2.0×)
specialization = 1.2× (if specialized)

finalXP = 20 × 5.0 × 2.0 × 1.2 = 240 XP
```

**Without Masterwork**:
```java
finalXP = 20 × 5.0 × 1.0 × 1.2 = 120 XP
// Masterwork doubles the XP!
```

### Durability Multiplier

```java
private final float durabilityMultiplier;

public float getDurabilityMultiplier() {
    return durabilityMultiplier;
}
```

**Application**:
```java
// In CraftingSystem.craft()
Item craftedItem = Item.fromPrototype(prototype);
float baseDurability = prototype.getMaxDurability();
float adjustedDurability = baseDurability * quality.getDurabilityMultiplier();
craftedItem.setMaxDurability(adjustedDurability);
```

**Example: Iron Sword Durability**
```java
baseMaxDurability = 200.0f

POOR:       200 × 0.7  = 140 durability (-30%)
NORMAL:     200 × 1.0  = 200 durability (baseline)
FINE:       200 × 1.15 = 230 durability (+15%)
MASTERWORK: 200 × 1.3  = 260 durability (+30%)
```

**Gameplay Impact**:
- **Masterwork sword**: Lasts 85% longer than Poor quality (260 / 140 = 1.86×)
- **Encourages seeking quality**: Better items last longer
- **Economy**: Masterwork items worth more (durability = value)

## API Reference

### Fields
```java
private final int tier;                     // Quality tier (1-4)
private final String name;                  // Display name ("Masterwork")
private final float xpMultiplier;           // XP scaling (0.5-2.0×)
private final float durabilityMultiplier;   // Durability scaling (0.7-1.3×)
```

### Methods
```java
int getTier()                               // Get tier number
String getName()                            // Get display name
float getXpMultiplier()                     // Get XP multiplier
float getDurabilityMultiplier()             // Get durability multiplier
```

### Static Methods
```java
static CraftingQuality fromTier(int tier)   // Get quality by tier
static CraftingQuality[] values()           // Get all qualities
```

## Usage Examples

### Quality Roll (in CraftingSystem)

```java
public CraftingResult craft(CraftingRecipe recipe, CraftingSkills skills, ...) {
    // ... validation and success roll ...
    
    if (!isSuccess) {
        return CraftingResult.failure("Crafting failed!");
    }
    
    // Determine quality
    float qualityRoll = random.nextFloat();
    CraftingQuality quality;
    
    if (qualityRoll < 0.05f) {
        quality = CraftingQuality.MASTERWORK;
    } else if (qualityRoll < 0.25f) {
        quality = CraftingQuality.FINE;
    } else if (qualityRoll < 0.85f) {
        quality = CraftingQuality.NORMAL;
    } else {
        quality = CraftingQuality.POOR;
    }
    
    // Create item with quality modifiers
    Item item = createItemWithQuality(recipe, quality);
    
    return CraftingResult.success(item, quality);
}
```

### Applying Quality to Item

```java
private Item createItemWithQuality(CraftingRecipe recipe, CraftingQuality quality) {
    ItemPrototype proto = getPrototype(recipe.getResultItemId());
    Item item = Item.fromPrototype(proto);
    
    // Apply durability modifier
    float baseDurability = proto.getMaxDurability();
    float adjustedDurability = baseDurability * quality.getDurabilityMultiplier();
    // Note: Would need Item.setMaxDurability() method
    
    // Store quality in custom properties
    item.setCustomProperty("quality", quality.getName());
    item.setCustomProperty("quality_tier", quality.getTier());
    
    return item;
}
```

### Calculating XP with Quality

```java
private int calculateXP(CraftingRecipe recipe, CraftingQuality quality, boolean isSpecialized) {
    ItemPrototype proto = getPrototype(recipe.getResultItemId());
    
    int baseXP = 20;
    float rarityMult = proto.getRarity().getXpMultiplier();
    float qualityMult = quality.getXpMultiplier();
    float specializationMult = isSpecialized ? 1.2f : 1.0f;
    
    return (int) (baseXP * rarityMult * qualityMult * specializationMult);
}
```

### UI Display

```java
public void displayCraftResult(CraftingResult result) {
    if (result.isSuccess()) {
        Item item = result.getItem();
        CraftingQuality quality = result.getQuality();
        
        String color = switch (quality) {
            case POOR -> "\u001B[31m";        // Red
            case NORMAL -> "\u001B[37m";      // White
            case FINE -> "\u001B[36m";        // Cyan
            case MASTERWORK -> "\u001B[33m";  // Gold
        };
        
        System.out.println("Crafted: " + color + quality.getName() + " quality " + 
                           item.getPrototypeId() + "\u001B[0m");
        // "Crafted: [Gold]Masterwork quality iron_sword[Reset]"
    }
}
```

## Integration with Other Systems

### CraftingSystem
```java
public class CraftingSystem {
    public CraftingResult craft(...) {
        // Success roll
        if (random.nextFloat() < successChance) {
            // Determine quality
            CraftingQuality quality = rollQuality();
            
            // Grant XP
            int xp = calculateXP(recipe, quality, isSpecialized);
            skills.addXp(recipe.getCategory(), xp);
            
            return CraftingResult.success(item, quality);
        }
    }
}
```

### Item (future enhancement)
```java
public class Item {
    private CraftingQuality quality;  // Store quality directly
    
    public Item(ItemPrototype proto, CraftingQuality quality) {
        this.maxDurability = proto.getMaxDurability() * quality.getDurabilityMultiplier();
        this.quality = quality;
    }
}
```

### Economy (future Phase 1.6)
```java
public int getMarketValue(Item item) {
    ItemPrototype proto = getPrototype(item);
    int baseValue = proto.getFinalValue();
    
    // Quality affects market value
    CraftingQuality quality = item.getQuality();
    float qualityBonus = switch (quality) {
        case POOR -> 0.7f;
        case NORMAL -> 1.0f;
        case FINE -> 1.3f;
        case MASTERWORK -> 2.0f;
    };
    
    return (int) (baseValue * qualityBonus);
}
```

## Performance Considerations

### Enum Performance
- **Memory**: 4 enum constants × ~60 bytes = ~240 bytes
- **Lookup**: `O(1)` by ordinal
- **Random roll**: ~0.01ms (single RNG call)

### Quality Roll Optimization
```java
// Current: Multiple comparisons
if (roll < 0.05f) { MASTERWORK }
else if (roll < 0.25f) { FINE }
// ...

// Alternative: Lookup table (unnecessary for 4 values)
```

## Design Decisions

### Why These Specific Probabilities?

```
Poor: 15%
Normal: 60%
Fine: 20%
Masterwork: 5%
```

**Rationale**:
- **Normal dominant** (60%): Most crafts should be usable
- **Fine uncommon** (20%): Occasional reward, not rare
- **Masterwork rare** (5%): 1 in 20, special event
- **Poor punishment** (15%): Not too common, but happens

**Playtesting Goals**:
- Masterwork: "Yes!" moment every ~20 crafts
- Fine: Nice surprise, not shocking
- Poor: Disappointment, but not rage-quit

### Why No "Exceptional" or "Legendary" Quality?

**Alternative**: 6 quality tiers (add Exceptional, Legendary)

**Rejected Because**:
- Diminishing returns: Players can't distinguish 6 tiers
- Dilutes Masterwork prestige
- Probability distribution becomes awkward
- 4 tiers is industry standard (WoW: Poor/Normal/Excellent/Masterwork)

### Why Durability AND XP Multipliers?

**Alternative**: Only XP multiplier

**Both Wins Because**:
- **XP**: Rewards crafter immediately
- **Durability**: Rewards user long-term
- **Synergy**: High-quality items feel better in both crafting and use

### Why Quality Determined on Success, Not Before?

**Alternative**: Pre-determine quality, then roll success

**Current Design Wins**:
- Success is primary goal, quality is bonus
- No "failed to craft Masterwork" disappointment
- Cleaner UX: Success → "What quality?"

## Known Limitations

1. **No Quality Influence**: Can't improve quality chance with higher skill
2. **Fixed Probabilities**: Can't adjust quality rates per recipe
3. **No Critical Failures**: Poor is worst case, no "destroyed materials" tier
4. **No Quality Upgrades**: Can't improve Poor → Normal after crafting

## Future Enhancements

### Phase 1.5: Quality Influence
```java
// Master crafters get better quality rolls
float qualityBonus = proficiency == CraftingProficiency.MASTER ? 0.1f : 0.0f;
if (roll < 0.05f + qualityBonus) { MASTERWORK }  // 15% at Master
```

### Phase 1.6: Quality Reforging
```java
// Upgrade quality with materials
public boolean upgradeQuality(Item item, Materials materials) {
    if (item.getQuality() == CraftingQuality.POOR) {
        // Consume materials, try to upgrade to Normal
        if (random.nextFloat() < 0.5f) {
            item.setQuality(CraftingQuality.NORMAL);
            return true;
        }
    }
    return false;
}
```

### Phase 2: Crafting Focus System
```java
// Trade success chance for quality chance
public CraftingResult craftWithFocus(CraftingRecipe recipe) {
    float successChance = baseChance * 0.8f;  // -20% success
    float masterworkChance = 0.05f * 2.0f;    // Double Masterwork chance
    // Risk/reward trade-off
}
```

## Testing

See [CraftingQualityTest.md](../../../../../test/java/org/adventure/CraftingQualityTest.md) for test coverage:
- All 4 quality tiers defined
- Probability distribution (5%/20%/60%/15%)
- XP multipliers (0.5×-2.0×)
- Durability multipliers (0.7×-1.3×)
- Quality roll determinism (seeded RNG)
- Edge cases (multiple rolls)

## References

- **Design Docs**: `docs/objects_crafting_legacy.md` → Quality System
- **Data Models**: `docs/data_models.md` → CraftingResult Schema
- **Grand Plan**: `docs/grand_plan.md` → Phase 1.4 Crafting Polish
- **Related Classes**: [CraftingSystem.md](CraftingSystem.md), [Item.md](../items/Item.md)
- **Tests**: [CraftingTest.md](../../../../../test/java/org/adventure/CraftingTest.md)

---

**Last Updated:** Phase 1.4 Implementation (November 2025)  
**Status:** ✅ Complete - 4 tiers, tested quality distribution
