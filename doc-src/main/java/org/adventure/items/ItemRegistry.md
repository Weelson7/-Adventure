# ItemRegistry.java - MVP Content Definitions

**Package:** `org.adventure.items`  
**Source:** [ItemRegistry.java](../../../../../src/main/java/org/adventure/items/ItemRegistry.java)  
**Phase:** MVP Phase 1.4 (Items & Crafting)

## Overview

`ItemRegistry` provides the **MVP (Minimum Viable Product) content** for Phase 1.4, defining 12 item prototypes and 7 crafting recipes. This is the initial content set for testing the items and crafting systems before full content expansion in later phases.

From `docs/grand_plan.md`:
> "Phase 1.4 delivers a functional crafting system with enough content to demonstrate all features."

## Design Philosophy

### Static Registry Pattern

**Design Decision**: Static methods return lists

```java
public static List<ItemPrototype> getMvpItemPrototypes() { ... }
public static List<CraftingRecipe> getMvpRecipes() { ... }
```

**Why?**
- **Simplicity**: No instance needed
- **Testability**: Easy to load in test setup
- **Migration Path**: Can switch to JSON/database later without changing callers

**Future**: Load from `data/items.json` and `data/recipes.json`

### Complete but Minimal

**Philosophy**: Enough content to test all systems, not more

**Coverage:**
- ✅ Weapons (swords, axes)
- ✅ Tools (pickaxes, hammers)
- ✅ Armor (leather, chainmail)
- ✅ Materials (ores, ingots, leather, wood)
- ✅ Consumables (potions)
- ✅ Crafting progression (Novice → Apprentice)

**Missing (intentionally):**
- ❌ Advanced materials (enchanted essence)
- ❌ High-tier recipes (Expert/Master)
- ❌ Multiple weapon types (spears, bows)
- ❌ Complex crafting chains (ore → ingot → alloy → weapon)

## MVP Item Prototypes (12 Total)

### Weapons (2)

#### Iron Sword
```java
new ItemPrototype.Builder("iron_sword", "Iron Sword", ItemCategory.WEAPON)
    .description("A basic iron sword")
    .rarity(ItemRarity.COMMON)
    .maxDurability(100)
    .baseValue(50)
    .stackable(false)
    .build()
```

**Properties:**
- **Category**: WEAPON
- **Rarity**: COMMON (1.0× multipliers)
- **Durability**: 100 (typical weapon)
- **Value**: 50 gold
- **Stackable**: No (equipment)

#### Steel Axe
```java
new ItemPrototype.Builder("steel_axe", "Steel Axe", ItemCategory.WEAPON)
    .description("A sturdy steel axe")
    .rarity(ItemRarity.UNCOMMON)
    .maxDurability(120)
    .baseValue(80)
    .stackable(false)
    .build()
```

**Properties:**
- **Category**: WEAPON
- **Rarity**: UNCOMMON (1.5× multipliers)
- **Durability**: 120 (better than iron)
- **Value**: 80 gold

### Tools (2)

#### Iron Pickaxe
```java
new ItemPrototype.Builder("iron_pickaxe", "Iron Pickaxe", ItemCategory.TOOL)
    .description("Used for mining")
    .rarity(ItemRarity.COMMON)
    .maxDurability(80)
    .baseValue(40)
    .stackable(false)
    .build()
```

**Properties:**
- **Category**: TOOL
- **Durability**: 80 (less than weapons)
- **Value**: 40 gold

#### Steel Hammer
```java
new ItemPrototype.Builder("steel_hammer", "Steel Hammer", ItemCategory.TOOL)
    .description("Required for smithing")
    .rarity(ItemRarity.UNCOMMON)
    .maxDurability(150)
    .baseValue(60)
    .stackable(false)
    .build()
```

**Properties:**
- **Durability**: 150 (durable tool)
- **Value**: 60 gold
- **Used by**: Smithing recipes

### Armor (2)

#### Leather Armor
```java
new ItemPrototype.Builder("leather_armor", "Leather Armor", ItemCategory.ARMOR)
    .description("Basic leather protection")
    .rarity(ItemRarity.COMMON)
    .maxDurability(60)
    .baseValue(30)
    .stackable(false)
    .build()
```

**Properties:**
- **Durability**: 60 (lower than weapons)
- **Value**: 30 gold

#### Iron Chainmail
```java
new ItemPrototype.Builder("iron_chainmail", "Iron Chainmail", ItemCategory.ARMOR)
    .description("Iron chain armor")
    .rarity(ItemRarity.UNCOMMON)
    .maxDurability(100)
    .baseValue(70)
    .stackable(false)
    .build()
```

**Properties:**
- **Durability**: 100 (sturdy armor)
- **Value**: 70 gold

### Crafting Materials (5)

#### Iron Ore
```java
new ItemPrototype.Builder("iron_ore", "Iron Ore", ItemCategory.CRAFTING_MATERIAL)
    .description("Raw iron ore")
    .rarity(ItemRarity.COMMON)
    .maxDurability(1)
    .baseValue(5)
    .stackable(true)
    .maxStackSize(100)
    .build()
```

**Properties:**
- **Stackable**: Yes (100 per stack)
- **Durability**: 1 (not used for materials)
- **Value**: 5 gold each

#### Iron Ingot
```java
new ItemPrototype.Builder("iron_ingot", "Iron Ingot", ItemCategory.CRAFTING_MATERIAL)
    .description("Refined iron")
    .rarity(ItemRarity.COMMON)
    .maxDurability(1)
    .baseValue(15)
    .stackable(true)
    .maxStackSize(100)
    .build()
```

**Properties:**
- **Value**: 15 gold (3× ore value = smelting adds value)

#### Steel Ingot
```java
new ItemPrototype.Builder("steel_ingot", "Steel Ingot", ItemCategory.CRAFTING_MATERIAL)
    .description("Strong steel ingot")
    .rarity(ItemRarity.UNCOMMON)
    .maxDurability(1)
    .baseValue(40)
    .stackable(true)
    .maxStackSize(100)
    .build()
```

**Properties:**
- **Rarity**: UNCOMMON
- **Value**: 40 gold (2.67× iron ingot)

#### Leather
```java
new ItemPrototype.Builder("leather", "Leather", ItemCategory.CRAFTING_MATERIAL)
    .description("Tanned leather")
    .rarity(ItemRarity.COMMON)
    .maxDurability(1)
    .baseValue(10)
    .stackable(true)
    .maxStackSize(100)
    .build()
```

#### Wood Plank
```java
new ItemPrototype.Builder("wood_plank", "Wood Plank", ItemCategory.CRAFTING_MATERIAL)
    .description("Wooden plank")
    .rarity(ItemRarity.COMMON)
    .maxDurability(1)
    .baseValue(2)
    .stackable(true)
    .maxStackSize(100)
    .build()
```

**Properties:**
- **Value**: 2 gold (cheapest material)

### Consumables (1)

#### Healing Potion
```java
new ItemPrototype.Builder("healing_potion", "Healing Potion", ItemCategory.CONSUMABLE)
    .description("Restores health")
    .rarity(ItemRarity.COMMON)
    .maxDurability(1)
    .baseValue(25)
    .stackable(true)
    .maxStackSize(20)
    .build()
```

**Properties:**
- **Stackable**: Yes (20 per stack, lower than materials)
- **Value**: 25 gold

## MVP Crafting Recipes (7 Total)

### Weapons (2)

#### Iron Sword Recipe
```java
new CraftingRecipe.Builder("recipe_iron_sword", "Iron Sword", CraftingCategory.SMITHING)
    .description("Craft a basic iron sword")
    .addMaterial("iron_ingot", 3)
    .addMaterial("wood_plank", 1)
    .addTool("steel_hammer")
    .minProficiency(CraftingProficiency.NOVICE)
    .output("iron_sword", 1)
    .craftingTime(20)
    .baseXp(20)
    .baseDifficulty(0.2f)
    .build()
```

**Requirements:**
- **Materials**: 3× iron_ingot (45g), 1× wood_plank (2g) = 47g total
- **Tools**: steel_hammer
- **Proficiency**: Novice
- **Output**: 1× iron_sword (50g value)
- **Profit**: 3g (6% margin)

**Economics**: Low-profit starter recipe

#### Steel Axe Recipe
```java
new CraftingRecipe.Builder("recipe_steel_axe", "Steel Axe", CraftingCategory.SMITHING)
    .description("Craft a steel axe")
    .addMaterial("steel_ingot", 4)
    .addMaterial("wood_plank", 2)
    .addTool("steel_hammer")
    .minProficiency(CraftingProficiency.APPRENTICE)
    .output("steel_axe", 1)
    .craftingTime(30)
    .baseXp(40)
    .baseDifficulty(0.3f)
    .build()
```

**Requirements:**
- **Materials**: 4× steel_ingot (160g), 2× wood_plank (4g) = 164g total
- **Proficiency**: Apprentice (progression gate)
- **Output**: 1× steel_axe (80g value)
- **Loss**: -84g (demonstration of unprofitable craft)

**Design Note**: Intentionally unprofitable to show not all crafting is for profit

### Tools (2)

#### Iron Pickaxe Recipe
```java
new CraftingRecipe.Builder("recipe_iron_pickaxe", "Iron Pickaxe", CraftingCategory.SMITHING)
    .description("Craft an iron pickaxe")
    .addMaterial("iron_ingot", 3)
    .addMaterial("wood_plank", 2)
    .addTool("steel_hammer")
    .minProficiency(CraftingProficiency.NOVICE)
    .output("iron_pickaxe", 1)
    .craftingTime(20)
    .baseXp(20)
    .baseDifficulty(0.2f)
    .build()
```

**Requirements:**
- **Materials**: 3× iron_ingot (45g), 2× wood_plank (4g) = 49g total
- **Output**: 1× iron_pickaxe (40g value)
- **Loss**: -9g (tool investment)

#### Steel Hammer Recipe
```java
new CraftingRecipe.Builder("recipe_steel_hammer", "Steel Hammer", CraftingCategory.SMITHING)
    .description("Craft a steel hammer")
    .addMaterial("steel_ingot", 3)
    .addMaterial("wood_plank", 1)
    .addTool("steel_hammer")
    .minProficiency(CraftingProficiency.APPRENTICE)
    .output("steel_hammer", 1)
    .craftingTime(25)
    .baseXp(35)
    .baseDifficulty(0.25f)
    .build()
```

**Bootstrapping Problem**: Requires steel_hammer to craft steel_hammer!

**Solution**: First hammer must be found/bought, then can craft replacements

### Armor (2)

#### Leather Armor Recipe
```java
new CraftingRecipe.Builder("recipe_leather_armor", "Leather Armor", CraftingCategory.SMITHING)
    .description("Craft leather armor")
    .addMaterial("leather", 5)
    .minProficiency(CraftingProficiency.NOVICE)
    .output("leather_armor", 1)
    .craftingTime(15)
    .baseXp(15)
    .baseDifficulty(0.15f)
    .build()
```

**Requirements:**
- **Materials**: 5× leather (50g)
- **Tools**: None (can craft with bare hands)
- **Output**: 1× leather_armor (30g value)
- **Loss**: -20g

**Design**: Simple armor, no tools required

#### Iron Chainmail Recipe
```java
new CraftingRecipe.Builder("recipe_iron_chainmail", "Iron Chainmail", CraftingCategory.SMITHING)
    .description("Craft iron chainmail")
    .addMaterial("iron_ingot", 8)
    .addTool("steel_hammer")
    .minProficiency(CraftingProficiency.APPRENTICE)
    .output("iron_chainmail", 1)
    .craftingTime(40)
    .baseXp(50)
    .baseDifficulty(0.35f)
    .build()
```

**Requirements:**
- **Materials**: 8× iron_ingot (120g)
- **Output**: 1× iron_chainmail (70g value)
- **Loss**: -50g (investment in protection)

### Consumables (1)

#### Healing Potion Recipe
```java
new CraftingRecipe.Builder("recipe_healing_potion", "Healing Potion", CraftingCategory.ALCHEMY)
    .description("Brew a healing potion")
    .addMaterial("healing_herb", 2)
    .addMaterial("water_flask", 1)
    .minProficiency(CraftingProficiency.NOVICE)
    .output("healing_potion", 1)
    .craftingTime(10)
    .baseXp(10)
    .baseDifficulty(0.1f)
    .build()
```

**Note**: Requires `healing_herb` and `water_flask` which are NOT in MVP prototypes

**Design Issue**: Incomplete recipe (demonstrates future content)

## Economic Analysis

### Material Costs vs Output Values

| Recipe | Material Cost | Output Value | Profit/Loss | Margin |
|--------|---------------|--------------|-------------|--------|
| Iron Sword | 47g | 50g | +3g | +6% |
| Steel Axe | 164g | 80g | -84g | -51% |
| Iron Pickaxe | 49g | 40g | -9g | -18% |
| Steel Hammer | 120g+hammer | 60g | Loss | N/A |
| Leather Armor | 50g | 30g | -20g | -40% |
| Iron Chainmail | 120g | 70g | -50g | -42% |
| Healing Potion | ??? | 25g | ??? | ??? |

**Observations:**
- Only Iron Sword is profitable from materials
- Tools/armor are investments, not profit sources
- Steel recipes all unprofitable (advanced materials expensive)

## Missing Content (Intentional)

### Prototypes Not Included
- `healing_herb` (required by healing_potion recipe)
- `water_flask` (required by healing_potion recipe)
- Advanced weapons (spears, bows, daggers)
- Enchanted items
- Quest items
- Special materials (gems, magical essence)

### Recipes Not Included
- Smelting (ore → ingot)
- Advanced weapons (Expert/Master tier)
- Enchanting recipes
- Cooking recipes
- Advanced alchemy

**Reason**: Phase 1.4 focuses on **system implementation**, not content breadth

## Usage Examples

### Loading MVP Content

```java
CraftingSystem system = new CraftingSystem(new Random());

// Load all prototypes
for (ItemPrototype proto : ItemRegistry.getMvpItemPrototypes()) {
    system.registerItemPrototype(proto);
}

// Load all recipes
for (CraftingRecipe recipe : ItemRegistry.getMvpRecipes()) {
    system.registerRecipe(recipe);
}

System.out.println("Loaded " + ItemRegistry.getMvpItemPrototypes().size() + " prototypes");
System.out.println("Loaded " + ItemRegistry.getMvpRecipes().size() + " recipes");

// Output:
// Loaded 12 prototypes
// Loaded 7 recipes
```

### Querying MVP Content

```java
List<ItemPrototype> weapons = ItemRegistry.getMvpItemPrototypes().stream()
    .filter(p -> p.getCategory() == ItemCategory.WEAPON)
    .collect(Collectors.toList());

System.out.println("MVP Weapons: " + weapons.size());
for (ItemPrototype weapon : weapons) {
    System.out.println("  - " + weapon.getName() + " (" + weapon.getRarity().getName() + ")");
}

// Output:
// MVP Weapons: 2
//   - Iron Sword (Common)
//   - Steel Axe (Uncommon)
```

### Finding Recipes by Category

```java
List<CraftingRecipe> smithingRecipes = ItemRegistry.getMvpRecipes().stream()
    .filter(r -> r.getCategory() == CraftingCategory.SMITHING)
    .collect(Collectors.toList());

System.out.println("Smithing Recipes: " + smithingRecipes.size());
// Output: Smithing Recipes: 6
```

## API Reference

```java
public static List<ItemPrototype> getMvpItemPrototypes()
public static List<CraftingRecipe> getMvpRecipes()
```

**Returns**: Immutable lists of MVP content

## Future Enhancements

### Phase 1.5: Complete MVP Content
```java
// Add missing materials
prototypes.add(new ItemPrototype.Builder("healing_herb", ...)
prototypes.add(new ItemPrototype.Builder("water_flask", ...)

// Fix healing potion recipe
```

### Phase 1.6: Smelting Recipes
```java
recipes.add(new CraftingRecipe.Builder("recipe_iron_ingot", ...)
    .addMaterial("iron_ore", 2)
    .addTool("furnace")
    .output("iron_ingot", 1)
    .build());
```

### Phase 2: JSON-Based Content
```json
{
  "id": "iron_sword",
  "name": "Iron Sword",
  "category": "WEAPON",
  "rarity": "COMMON",
  "maxDurability": 100,
  "baseValue": 50,
  "stackable": false
}
```

```java
public static List<ItemPrototype> loadFromJson(String path) {
    // Read data/items.json
    // Parse and build prototypes
}
```

## References

- **Design Docs**: `docs/objects_crafting_legacy.md` → MVP Content
- **Grand Plan**: `docs/grand_plan.md` → Phase 1.4 Content Goals
- **Related Classes**:
  - [ItemPrototype.md](ItemPrototype.md)
  - [CraftingRecipe.md](../crafting/CraftingRecipe.md)
  - [CraftingSystem.md](../crafting/CraftingSystem.md)
- **Tests**: [CraftingTest.md](../../../../../test/java/org/adventure/CraftingTest.md) → MVP content validation

---

**Last Updated:** Phase 1.4 Implementation (November 2025)  
**Status:** ✅ Complete - 12 prototypes, 7 recipes (1 incomplete), economic analysis
