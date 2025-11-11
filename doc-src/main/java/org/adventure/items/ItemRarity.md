# ItemRarity.java - Rarity Tier System

**Package:** `org.adventure.items`  
**Source:** [ItemRarity.java](../../../../../src/main/java/org/adventure/items/ItemRarity.java)  
**Phase:** MVP Phase 1.4 (Items & Crafting)

## Overview

`ItemRarity` defines six tiers of item rarity that affect drop rates, crafting difficulty, value multipliers, and visual presentation. This enum is central to the loot economy and progression systems, creating aspirational goals for players ("I want that legendary sword!") while maintaining balance.

From `docs/economy_resources.md`:
> "Rarity tiers create vertical progression within item categories, with higher tiers providing exponentially greater rewards at exponentially lower drop rates."

## Design Philosophy

### Why Rarity Matters

**Problem**: Without rarity, all iron swords are identical → no excitement, no progression

**Solution**: Rarity creates **hierarchies within categories**:
- Common iron sword: 10 damage, drops often
- Rare iron sword: 15 damage (+50%), drops 10× less
- Legendary iron sword: 25 damage (+150%), drops 100× less

**Psychological Effect**: "Epic" drop → dopamine hit → player retention

### The Six-Tier System

From MMO design best practices:
- **COMMON** (Grey): Vendor trash, filler loot
- **UNCOMMON** (Green): Slight upgrade, early-game gear
- **RARE** (Blue): Mid-game goal, noticeable power spike
- **EPIC** (Purple): Late-game chase items
- **LEGENDARY** (Orange): Build-defining items
- **ARTIFACT** (Red): Ultra-rare, prestige items

**Why Not More Tiers?**
- 7+ tiers → diminishing returns (players can't distinguish "mythic" from "legendary")
- 5 tiers → missing "artifact" aspirational tier
- **6 tiers** → proven standard (WoW, Diablo, etc.)

## Enum Values

### COMMON (Tier 1)
```java
COMMON(1, "Common", 1.0f, 1.0f, 0.60f)
```

**Characteristics:**
- **Value Multiplier**: 1.0× (baseline)
- **XP Multiplier**: 1.0× (baseline crafting XP)
- **Drop Chance**: 60% (more than half of all drops)

**Examples:**
- Rusty iron sword
- Worn leather armor
- Basic health potion
- Common iron ingots

**Usage:**
- Starter gear
- Vendor fodder (sell for gold)
- Bulk crafting materials
- Throwaway items in dangerous areas

**Color Code**: Grey (UI rendering)

### UNCOMMON (Tier 2)
```java
UNCOMMON(2, "Uncommon", 1.5f, 1.5f, 0.25f)
```

**Characteristics:**
- **Value Multiplier**: 1.5× (50% more expensive)
- **XP Multiplier**: 1.5× (50% more crafting XP)
- **Drop Chance**: 25% (1 in 4 drops)

**Examples:**
- Sharpened iron sword
- Reinforced leather armor
- Greater health potion
- Quality materials

**Usage:**
- Early-game upgrades
- First crafting milestones
- Apprentice crafter rewards
- Basic enchantable items

**Color Code**: Green (UI rendering)

### RARE (Tier 3)
```java
RARE(3, "Rare", 2.0f, 2.0f, 0.10f)
```

**Characteristics:**
- **Value Multiplier**: 2.0× (double baseline)
- **XP Multiplier**: 2.0× (double crafting XP)
- **Drop Chance**: 10% (1 in 10 drops)

**Examples:**
- Steel longsword
- Plate armor
- Superior health potion
- Rare gemstones

**Usage:**
- Mid-game power spike
- Journeyman crafter output
- Dungeon boss rewards
- First "named" items

**Color Code**: Blue (UI rendering)

### EPIC (Tier 4)
```java
EPIC(4, "Epic", 3.0f, 3.0f, 0.04f)
```

**Characteristics:**
- **Value Multiplier**: 3.0× (triple baseline)
- **XP Multiplier**: 3.0× (triple crafting XP)
- **Drop Chance**: 4% (1 in 25 drops)

**Examples:**
- Enchanted steel sword
- Dragon scale armor
- Major healing elixir
- Enchanted materials

**Usage:**
- Late-game chase items
- Expert crafter masterpieces
- Raid boss drops
- Build-enabling items

**Color Code**: Purple (UI rendering)

### LEGENDARY (Tier 5)
```java
LEGENDARY(5, "Legendary", 5.0f, 5.0f, 0.01f)
```

**Characteristics:**
- **Value Multiplier**: 5.0× (quintuple baseline)
- **XP Multiplier**: 5.0× (quintuple crafting XP)
- **Drop Chance**: 1% (1 in 100 drops)

**Examples:**
- Excalibur (named legendary sword)
- Aegis of the Gods (legendary shield)
- Philosopher's Stone (legendary reagent)
- Ancient artifact materials

**Usage:**
- Build-defining items
- Master crafter peak achievements
- World boss drops
- Story quest rewards
- Prestige goals

**Color Code**: Orange (UI rendering)

### ARTIFACT (Tier 6)
```java
ARTIFACT(6, "Artifact", 5.0f, 5.0f, 0.001f)
```

**Characteristics:**
- **Value Multiplier**: 5.0× (same as legendary for balance)
- **XP Multiplier**: 5.0× (same as legendary)
- **Drop Chance**: 0.1% (1 in 1000 drops)

**Examples:**
- One Ring (unique artifact)
- Thor's Hammer (single instance in world)
- Crown of Kings (legendary questline reward)
- World-First achievements

**Usage:**
- Ultra-rare prestige items
- Single-instance items (only 1 exists in world)
- Impossible crafting challenges
- Seasonal event rewards
- Developer gifts / GM tools

**Color Code**: Red/Gold (UI rendering)

**Special Rule**: Artifacts may be **unique** (only 1 per server)

## Multiplier System

### Value Multiplier

```java
private final float valueMultiplier;
```

**Formula**: `finalValue = baseValue × rarity.valueMultiplier`

**Example: Iron Sword Value Scaling**
```java
ItemPrototype ironSwordProto = new ItemPrototype.Builder(...)
    .baseValue(50)  // Base value
    .build();

// Common iron sword
.rarity(ItemRarity.COMMON)
// Value: 50 × 1.0 = 50 gold

// Rare iron sword
.rarity(ItemRarity.RARE)
// Value: 50 × 2.0 = 100 gold

// Legendary iron sword
.rarity(ItemRarity.LEGENDARY)
// Value: 50 × 5.0 = 250 gold
```

**Rationale**: Higher rarity → rarer drops → higher market value

### XP Multiplier

```java
private final float xpMultiplier;
```

**Formula**: `finalXP = baseXP × rarity.xpMultiplier × qualityMultiplier`

**Example: Crafting XP Scaling**
```java
// Craft common iron sword
baseXP = 10
rarity = ItemRarity.COMMON (1.0×)
finalXP = 10 × 1.0 = 10 XP

// Craft legendary iron sword
baseXP = 10
rarity = ItemRarity.LEGENDARY (5.0×)
finalXP = 10 × 5.0 = 50 XP (5× more XP!)
```

**Rationale**: Harder to craft (rarer materials) → more XP reward

**Integration with Crafting Quality**:
```java
// Legendary sword crafted at Masterwork quality
baseXP = 10
rarity = ItemRarity.LEGENDARY (5.0×)
quality = CraftingQuality.MASTERWORK (2.0×)
finalXP = 10 × 5.0 × 2.0 = 100 XP
```

### Drop Chance

```java
private final float dropChance;
```

**Formula**: Random roll (0.0-1.0) compared against cumulative drop chances

**Loot Table Example:**
```java
float roll = random.nextFloat();  // 0.0 to 1.0

if (roll < ItemRarity.ARTIFACT.dropChance) {        // 0.1% chance
    return generateArtifact();
} else if (roll < ItemRarity.LEGENDARY.dropChance) { // 1% chance
    return generateLegendary();
} else if (roll < ItemRarity.EPIC.dropChance) {      // 4% chance
    return generateEpic();
} else if (roll < ItemRarity.RARE.dropChance) {      // 10% chance
    return generateRare();
} else if (roll < ItemRarity.UNCOMMON.dropChance) {  // 25% chance
    return generateUncommon();
} else {                                              // 60% chance
    return generateCommon();
}
```

**Drop Rate Distribution:**
| Rarity | Drop Chance | Drops per 1000 |
|--------|-------------|----------------|
| Common | 60.0% | 600 |
| Uncommon | 25.0% | 250 |
| Rare | 10.0% | 100 |
| Epic | 4.0% | 40 |
| Legendary | 1.0% | 10 |
| Artifact | 0.1% | 1 |

**Magic Find Modifiers (future):**
```java
// Player has +50% magic find
float modifiedChance = baseChance * (1.0f + magicFind);
// Legendary: 1% × 1.5 = 1.5% (50% more legendaries)
```

## API Reference

### Fields
```java
private final int id;                   // Numeric ID (1-6)
private final String name;              // Display name ("Common")
private final float valueMultiplier;    // Value scaling (1.0-5.0×)
private final float xpMultiplier;       // XP scaling (1.0-5.0×)
private final float dropChance;         // Drop probability (0.001-0.60)
```

### Methods
```java
int getId()                             // Get tier number
String getName()                        // Get display name
float getValueMultiplier()              // Get value scaling
float getXpMultiplier()                 // Get XP scaling
float getDropChance()                   // Get drop rate
```

### Static Methods
```java
static ItemRarity fromId(int id)        // Get rarity by ID (throws if invalid)
static ItemRarity[] values()            // Get all rarities (standard enum)
```

## Usage Examples

### Creating Items with Rarity

```java
// Common iron sword
ItemPrototype commonSword = new ItemPrototype.Builder("iron_sword", "Iron Sword", ItemCategory.WEAPON)
    .rarity(ItemRarity.COMMON)
    .baseValue(50)
    .property("damage", 10)
    .build();

// Legendary iron sword (same base, different rarity)
ItemPrototype legendarySword = new ItemPrototype.Builder("legendary_iron_sword", "Excalibur", ItemCategory.WEAPON)
    .rarity(ItemRarity.LEGENDARY)
    .baseValue(50)  // Still 50 base
    .property("damage", 25)  // +150% damage
    .property("special_ability", "holy_smite")
    .build();

// Actual value: 50 × 5.0 = 250 gold
```

### Loot Generation

```java
public Item generateLoot(Enemy enemy) {
    float roll = random.nextFloat();
    
    // Boss enemies have better drop rates
    if (enemy.isBoss()) {
        roll *= 0.5f;  // Double drop chance for rare+
    }
    
    // Determine rarity
    ItemRarity rarity;
    if (roll < ItemRarity.ARTIFACT.dropChance) {
        rarity = ItemRarity.ARTIFACT;
    } else if (roll < ItemRarity.LEGENDARY.dropChance) {
        rarity = ItemRarity.LEGENDARY;
    } else if (roll < ItemRarity.EPIC.dropChance) {
        rarity = ItemRarity.EPIC;
    } else if (roll < ItemRarity.RARE.dropChance) {
        rarity = ItemRarity.RARE;
    } else if (roll < ItemRarity.UNCOMMON.dropChance) {
        rarity = ItemRarity.UNCOMMON;
    } else {
        rarity = ItemRarity.COMMON;
    }
    
    // Generate item of determined rarity
    ItemPrototype proto = getRandomPrototypeWithRarity(rarity);
    return Item.fromPrototype(proto);
}
```

### Crafting with Rarity

```java
public CraftingResult craft(CraftingRecipe recipe, CraftingSkills skills) {
    // Check if materials meet rarity requirements
    ItemRarity materialRarity = getMaterialRarity(recipe);
    
    if (materialRarity == ItemRarity.LEGENDARY) {
        // Legendary crafting requires Master proficiency
        if (skills.getProficiency() != CraftingProficiency.MASTER) {
            return CraftingResult.failure("Requires Master proficiency for Legendary crafting");
        }
    }
    
    // Craft item
    Item result = craftItem(recipe);
    
    // Grant XP based on rarity
    ItemPrototype proto = getPrototype(result);
    float xp = 10 * proto.getRarity().getXpMultiplier();
    skills.addXp(xp);
    
    return CraftingResult.success(result);
}
```

### UI Color Coding

```java
public String getColorCode(ItemRarity rarity) {
    return switch (rarity) {
        case COMMON -> "\u001B[37m";      // Grey
        case UNCOMMON -> "\u001B[32m";    // Green
        case RARE -> "\u001B[34m";        // Blue
        case EPIC -> "\u001B[35m";        // Purple
        case LEGENDARY -> "\u001B[33m";   // Orange (yellow)
        case ARTIFACT -> "\u001B[31m";    // Red
    };
}

public void displayItem(Item item) {
    ItemPrototype proto = getPrototype(item);
    String color = getColorCode(proto.getRarity());
    String reset = "\u001B[0m";
    
    System.out.println(color + proto.getName() + reset);
    // Output: [Colored]Excalibur[Reset]
}
```

## Integration with Other Systems

### ItemPrototype
```java
private final ItemRarity rarity;

public int getFinalValue() {
    return (int) (baseValue * rarity.getValueMultiplier());
}
```

### Crafting System
```java
// In CraftingSystem.craft()
ItemPrototype proto = getPrototype(recipe.getResultItemId());
float xp = BASE_XP * proto.getRarity().getXpMultiplier() * quality.getXpMultiplier();
skills.addXp(xp);
```

### Economy System (future Phase 1.6)
```java
// Vendor buy prices
public int getVendorBuyPrice(Item item) {
    ItemPrototype proto = getPrototype(item);
    int basePrice = proto.getFinalValue();  // Already has rarity multiplier
    return (int) (basePrice * 0.5f);  // Vendors pay 50%
}
```

## Performance Considerations

### Enum Performance
- **Memory**: 6 enum constants × ~60 bytes = ~360 bytes (negligible)
- **Lookup**: `O(1)` by ordinal
- **Comparison**: Reference equality (fastest)

### Multiplier Calculations
```java
// ❌ SLOW: Recalculate every access
public int getValue() {
    return (int) (baseValue * rarity.getValueMultiplier());
}

// ✅ FAST: Cache on creation (future optimization)
private final int cachedValue;
cachedValue = (int) (baseValue * rarity.getValueMultiplier());
```

## Design Decisions

### Why Same Multiplier for Legendary and Artifact?

```java
LEGENDARY(5, "Legendary", 5.0f, 5.0f, 0.01f)
ARTIFACT(6, "Artifact", 5.0f, 5.0f, 0.001f)
```

**Question**: Why not 10× for artifacts?

**Answer**: Artifact rarity is about **prestige and uniqueness**, not power creep

**Balance Philosophy**:
- Artifacts are **cosmetically distinct** (red color, unique names)
- Artifacts may be **server-unique** (only 1 exists)
- Artifacts have **story significance** (historyReferenceId)
- Power is capped at Legendary tier to prevent inflation

**Alternative Rejected**: 10× multiplier → game-breaking items, economy collapse

### Why Float for Multipliers?

```java
private final float valueMultiplier;
```

**Advantages**:
- **Precision**: Can do 1.5× (Uncommon), not just integers
- **Future**: Formula-based scaling (e.g., 1.25^tier)
- **Modding**: Custom multipliers (1.75×, 2.3×)

**Alternative Considered**: Integer percentages (150% = 1.5×) → rejected for less clear code

### Why Decreasing Drop Chances?

**Exponential Decay**: Each tier is ~2.5× rarer than previous

```
Common:    60.0%  (baseline)
Uncommon:  25.0%  (÷2.4)
Rare:      10.0%  (÷2.5)
Epic:       4.0%  (÷2.5)
Legendary:  1.0%  (÷4.0)
Artifact:   0.1%  (÷10.0)
```

**Rationale**:
- **Excitement**: Rare drops feel special
- **Economy**: Scarcity maintains value
- **Progression**: Legendary items are goals, not guarantees

## Known Limitations

1. **No Rarity Requirements**: Can craft Legendary with Novice proficiency (relies on recipe design)
2. **No Rarity Downgrade**: Can't "un-enchant" to lower rarity
3. **No Dynamic Rarity**: Rarity fixed at prototype creation
4. **No Rarity Modifiers**: Can't have "+10% rarity chance" buffs (yet)

## Future Enhancements

### Phase 1.6: Magic Find System
```java
// Player buff increases drop chances
float magicFind = player.getMagicFindBonus();  // 0.5 = +50%
float modifiedChance = rarity.dropChance * (1.0f + magicFind);
```

### Phase 1.7: Rarity Upgrade System
```java
// Upgrade Rare → Epic with special materials
Item rareItem = ...;
if (hasUpgradeMaterials() && random.nextFloat() < 0.1f) {
    // 10% success chance
    upgradeItemRarity(rareItem, ItemRarity.EPIC);
}
```

### Phase 2: Custom Rarity Colors
```java
// Modders define custom rarities
ItemRarity.registerCustom("Mythic", 10.0f, 10.0f, 0.0001f, "#FF00FF");
```

## Testing

See [ItemRarityTest.md](../../../../../test/java/org/adventure/ItemRarityTest.md) for test coverage:
- All 6 rarity tiers defined
- Multiplier scaling (1.0× to 5.0×)
- Drop chance probabilities
- Value/XP calculations
- Enum lookup by ID

## References

- **Design Docs**: `docs/economy_resources.md` → Rarity System
- **Data Models**: `docs/data_models.md` → ItemPrototype Schema
- **Grand Plan**: `docs/grand_plan.md` → Phase 1.4 Economy
- **Related Classes**: [ItemPrototype.md](ItemPrototype.md), [Item.md](Item.md)
- **Crafting**: [../crafting/CraftingSystem.md](../crafting/CraftingSystem.md)

---

**Last Updated:** Phase 1.4 Implementation (November 2025)  
**Status:** ✅ Complete - 6 tiers, integrated with crafting/loot systems
