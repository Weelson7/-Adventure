# Crafting System - Complete Documentation Index

**Package:** `org.adventure.crafting`  
**Phase:** MVP Phase 1.4 (Items & Crafting)

## Overview

The crafting system enables characters to create items from raw materials, tools, and recipes. It features:
- **Skill Progression**: 5 proficiency tiers with XP-based advancement
- **Specialization**: Up to 2 specialized categories with +20% XP bonuses
- **Quality System**: 4 quality levels affecting durability and XP rewards
- **Failure Mechanics**: Proficiency-based success rates
- **Dynamic Recipes**: Builder pattern for flexible recipe creation

## Package Contents

### Core Systems
- **[CraftingSystem.md](CraftingSystem.md)** - Main orchestrator for crafting operations
- **[CraftingSkills.md](CraftingSkills.md)** - Character progression tracker
- **[CraftingRecipe.md](CraftingRecipe.md)** - Recipe definition and validation

### Enums & Configuration
- **[CraftingProficiency.md](CraftingProficiency.md)** - 5-tier skill levels (Novice → Master)
- **[CraftingCategory.md](CraftingCategory.md)** - 8 crafting specializations
- **[CraftingQuality.md](CraftingQuality.md)** - 4 quality tiers for crafted items

### Content Registry
- **[ItemRegistry.md](ItemRegistry.md)** - MVP item prototypes and recipes

## Quick Start

### Creating a Crafting Recipe
```java
CraftingRecipe ironSwordRecipe = new CraftingRecipe.Builder("recipe_iron_sword", "iron_sword")
    .category(CraftingCategory.SMITHING)
    .requiredProficiency(CraftingProficiency.APPRENTICE)
    .addMaterial("iron_ingot", 3)
    .addTool("forge")
    .baseSuccessChance(0.9f)
    .build();
```

### Crafting an Item
```java
CraftingSystem craftingSystem = new CraftingSystem();
CraftingSkills skills = new CraftingSkills();

CraftingResult result = craftingSystem.craft(
    ironSwordRecipe,
    skills,
    availableMaterials,
    availableTools
);

if (result.isSuccess()) {
    Item craftedItem = result.getItem();
    CraftingQuality quality = result.getQuality();
    System.out.println("Crafted " + quality + " quality item!");
}
```

### Tracking Progression
```java
// Add XP after successful craft
skills.addXp(CraftingCategory.SMITHING, 100);

// Check proficiency
CraftingProficiency smithing = skills.getProficiency(CraftingCategory.SMITHING);
System.out.println("Smithing level: " + smithing.getName());

// Specialize for bonus XP
skills.addSpecialization(CraftingCategory.SMITHING);  // +20% XP bonus
```

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      CraftingSystem                         │
│  - Orchestrates crafting operations                         │
│  - Validates materials, tools, proficiency                  │
│  - Determines success/failure and quality                   │
│  - Grants XP based on rarity × quality multipliers          │
└──────────────────┬──────────────────────────────────────────┘
                   │
         ┌─────────┴─────────┐
         │                   │
         ▼                   ▼
┌─────────────────┐  ┌──────────────────┐
│ CraftingRecipe  │  │ CraftingSkills   │
│  - Materials    │  │  - XP tracking   │
│  - Tools        │  │  - Proficiency   │
│  - Proficiency  │  │  - Specialization│
└─────────────────┘  └──────────────────┘
         │                   │
         └─────────┬─────────┘
                   │
         ┌─────────┴──────────┬─────────────────┐
         ▼                    ▼                 ▼
┌──────────────────┐  ┌───────────────┐  ┌──────────────────┐
│CraftingProficiency│  │CraftingCategory│  │CraftingQuality   │
│  - NOVICE (0 XP) │  │  - SMITHING   │  │  - POOR (0.5× XP)│
│  - APPRENTICE    │  │  - ALCHEMY    │  │  - NORMAL (1.0×) │
│  - JOURNEYMAN    │  │  - ENCHANTING │  │  - FINE (1.5×)   │
│  - EXPERT        │  │  - COOKING    │  │  - MASTERWORK 2× │
│  - MASTER        │  │  - 8 total    │  └──────────────────┘
└──────────────────┘  └───────────────┘
```

## Key Formulas

### XP Gain Formula
```
finalXP = baseXP × rarityMultiplier × qualityMultiplier × specializationBonus × tierPenalty
```

**Components:**
- `baseXP`: Recipe base (10-50 XP)
- `rarityMultiplier`: 1.0× (Common) to 5.0× (Legendary)
- `qualityMultiplier`: 0.5× (Poor) to 2.0× (Masterwork)
- `specializationBonus`: 1.2× if specialized, else 1.0×
- `tierPenalty`: 0.5× if below required tier, else 1.0×

**Example:**
```java
// Legendary sword, Masterwork quality, specialized Smithing, at-tier
XP = 20 × 5.0 × 2.0 × 1.2 × 1.0 = 240 XP
```

### Success Chance Formula
```
finalChance = baseChance - (proficiencyDiff × failureMultiplier)
```

**Example:**
```java
// Recipe requires Journeyman (tier 3), player is Apprentice (tier 2)
baseChance = 0.9 (90%)
proficiencyDiff = 3 - 2 = 1
failureMultiplier = 0.2
finalChance = 0.9 - (1 × 0.2) = 0.7 (70% success)
```

### Quality Determination (on success)
```java
float roll = random.nextFloat();
if (roll < 0.05f) quality = MASTERWORK;      // 5%
else if (roll < 0.25f) quality = FINE;       // 20%
else if (roll < 0.85f) quality = NORMAL;     // 60%
else quality = POOR;                          // 15%
```

## Design Principles

### 1. Soft-Cap Progression
XP requirements increase exponentially, but there's no hard cap:
```
NOVICE      →  APPRENTICE:  100 XP
APPRENTICE  →  JOURNEYMAN:  200 XP (total 300)
JOURNEYMAN  →  EXPERT:      300 XP (total 600)
EXPERT      →  MASTER:      400 XP (total 1000)
MASTER      →  Beyond:      Infinite XP possible
```

### 2. Specialization Trade-offs
- **Max 2 specializations** per character
- **+20% XP** in specialized categories
- **Forces choices**: Can't master everything
- **Encourages trading**: Need other crafters for non-specialized items

### 3. Quality Over Quantity
- **Masterwork items**: 2× XP, +30% durability, prestige
- **Poor quality items**: 0.5× XP, -30% durability
- **Encourages patience**: Wait for good roll vs spam crafting

### 4. Failure as Progression
- **Consume materials even on failure** (stakes matter)
- **Still gain XP on failure** (reduced, but not zero)
- **Learn from mistakes**: Proficiency reduces failure rate

## MVP Content

### 12 Item Prototypes
- **Weapons**: Iron Sword, Steel Axe
- **Tools**: Iron Pickaxe
- **Armor**: Leather Armor, Plate Helmet
- **Materials**: Iron Ingot, Wood Plank, Leather Hide, Stone, Coal, Enchanted Essence, Dragon Scale

### 7 Crafting Recipes
1. **Iron Sword**: 3 iron ingots + forge (Apprentice Smithing)
2. **Steel Axe**: 2 iron ingots + coal + forge (Journeyman Smithing)
3. **Iron Pickaxe**: 3 iron ingots + 2 wood planks + anvil (Apprentice Smithing)
4. **Leather Armor**: 5 leather hides + workbench (Novice Leatherworking)
5. **Plate Helmet**: 4 iron ingots + forge + anvil (Expert Smithing)
6. **Health Potion**: 2 herbs + glass vial + alchemy table (Apprentice Alchemy)
7. **Enchanted Sword**: 1 iron sword + enchanted essence + enchanting table (Expert Enchanting)

## Integration Points

### Phase 1.3: Characters
```java
Character crafter = new Character("crafter_1", "Blacksmith", Race.DWARF);
CraftingSkills skills = crafter.getCraftingSkills();
```

### Phase 1.4: Items
```java
ItemPrototype swordProto = craftingSystem.getItemPrototype("iron_sword");
Item craftedSword = Item.fromPrototype(swordProto);
```

### Phase 1.5: Structures (future)
```java
Structure forge = region.getStructure("forge_01");
if (forge.isOperational()) {
    tools.add("forge");
}
```

### Phase 1.6: Economy (future)
```java
int materialCost = recipe.getMaterialValue();
int craftingFee = skills.getProficiency().getCraftingFee();
int totalCost = materialCost + craftingFee;
```

## Testing

Comprehensive test suite with 23 tests covering:
- Recipe validation (materials, tools, proficiency)
- Success/failure mechanics
- Quality determination
- XP progression (specialization, tier penalties)
- Edge cases (missing materials, invalid recipes)

See [CraftingTest.md](../../../../../test/java/org/adventure/CraftingTest.md) for details.

## Performance Considerations

### Memory
- **CraftingSkills**: ~200 bytes per character
- **CraftingRecipe**: ~150 bytes per recipe
- **100 recipes**: ~15 KB (negligible)

### CPU
- **Crafting operation**: ~0.1ms (material lookup, validation, RNG)
- **XP calculation**: Inline math, no allocations
- **No per-tick updates**: Event-driven only

## Known Limitations

1. **No Batch Crafting**: Must craft 1 item at a time
2. **No Crafting Queue**: Future: queue 10 swords to craft overnight
3. **No Tool Durability**: Tools don't wear out (yet)
4. **No Critical Success**: Future: 1% chance for double output
5. **No Material Refund**: Failed craft consumes all materials

## Future Enhancements

### Phase 1.5: Tool Durability
```java
Item forge = tools.get("forge");
forge.damage(10);  // Tools wear out with use
```

### Phase 1.6: Bulk Crafting
```java
CraftingResult result = craftingSystem.craftBatch(recipe, skills, materials, tools, 10);
// Craft 10 items with single operation
```

### Phase 1.7: Master Recipes
```java
// Unlock special recipes at Master tier
if (skills.getProficiency(CraftingCategory.SMITHING) == CraftingProficiency.MASTER) {
    unlo

ckRecipe("legendary_sword_of_destiny");
}
```

### Phase 2: Crafting Mini-game
```java
// Interactive crafting: time quality based on player input
CraftingResult result = craftingSystem.craftInteractive(recipe, skills, playerInputs);
```

## References

- **Design Docs**: `docs/objects_crafting_legacy.md`
- **Grand Plan**: `docs/grand_plan.md` → Phase 1.4
- **Data Models**: `docs/data_models.md` → Crafting Schema
- **Tests**: `src/test/java/org/adventure/CraftingTest.java`
- **Summary**: `archive/PHASE_1.4_SUMMARY.md`

---

**Last Updated:** Phase 1.4 Implementation (November 2025)  
**Status:** ✅ Complete - 7 recipes, 12 items, 23 tests passing
