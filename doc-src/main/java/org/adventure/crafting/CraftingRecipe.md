# CraftingRecipe.java - Recipe Definition System

**Package:** `org.adventure.crafting`  
**Source:** [CraftingRecipe.java](../../../../../src/main/java/org/adventure/crafting/CraftingRecipe.java)  
**Phase:** MVP Phase 1.4 (Items & Crafting)

## Overview

`CraftingRecipe` defines what materials, tools, and proficiency are required to craft an item. It uses the Builder pattern to handle complex recipe configurations and calculates failure chances based on the crafter's proficiency. This class is the blueprint that `CraftingSystem` executes.

From `docs/objects_crafting_legacy.md`:
> "Recipes specify material requirements, tool dependencies, minimum proficiency, and difficulty parameters for success calculation."

## Design Philosophy

### Recipes as Data, Not Code

**Design Decision**: Recipes are data objects, not hardcoded logic

```java
// ✅ GOOD: Recipe as data
CraftingRecipe recipe = new CraftingRecipe.Builder("recipe_iron_sword", "Iron Sword", CraftingCategory.SMITHING)
    .addMaterial("iron_ingot", 3)
    .addTool("steel_hammer")
    .minProficiency(CraftingProficiency.NOVICE)
    .build();

// ❌ BAD: Recipe as hardcoded method
public Item craftIronSword(Materials materials, Tools tools) {
    if (materials.get("iron_ingot") < 3) return null;
    if (!tools.has("steel_hammer")) return null;
    // ... hardcoded logic ...
}
```

**Advantages**:
- **Moddability**: Add recipes via JSON without recompiling
- **Inspectability**: Can query recipe requirements in UI
- **Testing**: Easy to create test recipes
- **Balance**: Designers tweak numbers without code changes

### Builder Pattern for Complexity

**Problem**: Recipes have many optional parameters

**Solution**: Fluent builder interface

```java
CraftingRecipe recipe = new CraftingRecipe.Builder(id, name, category)
    .description("Craft an iron sword")
    .addMaterial("iron_ingot", 3)
    .addMaterial("wood_plank", 1)
    .addTool("steel_hammer")
    .minProficiency(CraftingProficiency.NOVICE)
    .output("iron_sword", 1)
    .craftingTime(20)
    .baseXp(20)
    .baseDifficulty(0.2f)
    .build();
```

**Readability**: Recipe reads like a configuration file

## Class Structure

### Core Fields

```java
private final String id;                                // Unique recipe ID
private final String name;                              // Display name
private final CraftingCategory category;                // Which skill category
private final String description;                       // Flavor text

// Requirements
private final List<MaterialRequirement> materials;      // Materials needed
private final List<String> requiredToolPrototypeIds;    // Tools needed (not consumed)
private final CraftingProficiency minProficiency;       // Minimum skill level

// Outputs
private final String outputPrototypeId;                 // What item is produced
private final int outputQuantity;                       // How many (usually 1)

// Parameters
private final int craftingTimeTicks;                    // Time to craft
private final int baseXp;                               // Base XP reward
private final float baseDifficulty;                     // Base failure chance (0.0-1.0)
```

### Material Requirement Inner Class

```java
public static class MaterialRequirement {
    private final String prototypeId;    // Material item ID
    private final int quantity;          // How many needed
    
    public MaterialRequirement(String prototypeId, int quantity) {
        this.prototypeId = Objects.requireNonNull(prototypeId);
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        this.quantity = quantity;
    }
}
```

**Example**:
```java
new MaterialRequirement("iron_ingot", 3)  // Need 3 iron ingots
```

## Failure Chance Calculation

### The Formula

```java
public float calculateFailureChance(CraftingProficiency crafterProficiency) {
    // Can't craft if below minimum proficiency
    if (crafterProficiency.ordinal() < minProficiency.ordinal()) {
        return 1.0f;  // 100% failure
    }
    
    // Proficiency bonus: each level above minimum reduces failure
    int proficiencyDiff = crafterProficiency.ordinal() - minProficiency.ordinal();
    float proficiencyBonus = proficiencyDiff * 0.15f;  // 15% reduction per tier
    
    // Apply proficiency multiplier from enum
    float adjustedDifficulty = (baseDifficulty - proficiencyBonus) * crafterProficiency.getFailureMultiplier();
    
    return Math.max(0.0f, Math.min(1.0f, adjustedDifficulty));
}
```

### Step-by-Step Breakdown

**Step 1: Check Minimum Proficiency**
```java
if (crafterProficiency < minProficiency) {
    return 1.0f;  // Can't craft at all
}
```

**Step 2: Calculate Proficiency Bonus**
```java
int proficiencyDiff = crafterProf.ordinal() - minProf.ordinal();
float bonus = proficiencyDiff × 0.15f;

// Example: Expert crafting Apprentice recipe
// Expert (tier 4) - Apprentice (tier 2) = 2 tiers above
// Bonus = 2 × 0.15 = 0.30 (30% reduction)
```

**Step 3: Apply Proficiency Multiplier**
```java
float adjustedDifficulty = (baseDifficulty - bonus) × profMultiplier;

// Example: Apprentice recipe, Expert crafter
// baseDifficulty = 0.3 (30% base failure)
// bonus = 0.30 (from above)
// profMultiplier = 0.2 (Apprentice's failure multiplier)
// 
// adjustedDifficulty = (0.3 - 0.30) × 0.2 = 0.0 × 0.2 = 0.0
// Result: 0% failure chance!
```

**Step 4: Clamp to Valid Range**
```java
return Math.max(0.0f, Math.min(1.0f, adjustedDifficulty));
// Ensure result is between 0.0 and 1.0
```

### Example Calculations

**Example 1: At-Tier Crafting**
```java
Recipe: Iron Sword (Apprentice, 0.2 difficulty)
Crafter: Apprentice (tier 2)

proficiencyDiff = 2 - 2 = 0
bonus = 0 × 0.15 = 0.0
adjustedDiff = (0.2 - 0.0) × 0.4 = 0.08
failureChance = 8%
successChance = 92%
```

**Example 2: Above-Tier Crafting**
```java
Recipe: Iron Sword (Apprentice, 0.2 difficulty)
Crafter: Journeyman (tier 3)

proficiencyDiff = 3 - 2 = 1
bonus = 1 × 0.15 = 0.15
adjustedDiff = (0.2 - 0.15) × 0.3 = 0.015
failureChance = 1.5%
successChance = 98.5%
```

**Example 3: Master Crafting Low-Tier**
```java
Recipe: Iron Sword (Apprentice, 0.2 difficulty)
Crafter: Master (tier 5)

proficiencyDiff = 5 - 2 = 3
bonus = 3 × 0.15 = 0.45
adjustedDiff = (0.2 - 0.45) × 0.1 = -0.25 × 0.1 = -0.025
clamped = 0.0
failureChance = 0%
successChance = 100%
```

**Example 4: Below-Tier Crafting**
```java
Recipe: Expert Sword (Expert, 0.35 difficulty)
Crafter: Novice (tier 1)

proficiencyDiff = 1 - 4 = -3 (below minimum!)
return 1.0f
failureChance = 100%
successChance = 0%
```

## Builder Pattern

### Builder Fields and Defaults

```java
public static class Builder {
    // Required
    private String id;
    private String name;
    private CraftingCategory category;
    
    // Optional with defaults
    private String description = "";
    private List<MaterialRequirement> materials = new ArrayList<>();
    private List<String> requiredToolPrototypeIds = new ArrayList<>();
    private CraftingProficiency minProficiency = CraftingProficiency.NOVICE;
    private String outputPrototypeId;
    private int outputQuantity = 1;
    private int craftingTimeTicks = 10;
    private int baseXp = 10;
    private float baseDifficulty = 0.1f;
}
```

### Fluent Methods

```java
public Builder addMaterial(String prototypeId, int quantity) {
    this.materials.add(new MaterialRequirement(prototypeId, quantity));
    return this;
}

public Builder addTool(String toolPrototypeId) {
    this.requiredToolPrototypeIds.add(toolPrototypeId);
    return this;
}

public Builder minProficiency(CraftingProficiency proficiency) {
    this.minProficiency = proficiency;
    return this;
}

public Builder output(String prototypeId, int quantity) {
    this.outputPrototypeId = prototypeId;
    this.outputQuantity = quantity;
    return this;
}
```

### Validation

```java
public CraftingRecipe build() {
    Objects.requireNonNull(id, "id cannot be null");
    Objects.requireNonNull(name, "name cannot be null");
    Objects.requireNonNull(category, "category cannot be null");
    Objects.requireNonNull(outputPrototypeId, "outputPrototypeId cannot be null");
    
    if (materials.isEmpty()) {
        throw new IllegalArgumentException("Recipe must have at least one material");
    }
    if (outputQuantity < 1) {
        throw new IllegalArgumentException("outputQuantity must be at least 1");
    }
    if (craftingTimeTicks < 1) {
        throw new IllegalArgumentException("craftingTimeTicks must be at least 1");
    }
    if (baseXp < 0) {
        throw new IllegalArgumentException("baseXp must be non-negative");
    }
    if (baseDifficulty < 0 || baseDifficulty > 1) {
        throw new IllegalArgumentException("baseDifficulty must be between 0 and 1");
    }
    
    return new CraftingRecipe(this);
}
```

## Usage Examples

### Simple Recipe (Iron Sword)

```java
CraftingRecipe ironSword = new CraftingRecipe.Builder(
    "recipe_iron_sword",
    "Iron Sword", 
    CraftingCategory.SMITHING
)
.description("Craft a basic iron sword")
.addMaterial("iron_ingot", 3)
.addMaterial("wood_plank", 1)
.addTool("steel_hammer")
.minProficiency(CraftingProficiency.NOVICE)
.output("iron_sword", 1)
.craftingTime(20)
.baseXp(20)
.baseDifficulty(0.2f)
.build();
```

### Complex Recipe (Enchanted Sword)

```java
CraftingRecipe enchantedSword = new CraftingRecipe.Builder(
    "recipe_enchanted_sword",
    "Enchanted Sword",
    CraftingCategory.ENCHANTING
)
.description("Imbue a sword with magical properties")
.addMaterial("iron_sword", 1)          // Needs crafted sword
.addMaterial("enchanted_essence", 3)    // Rare material
.addMaterial("mana_crystal", 1)         // Very rare
.addTool("enchanting_table")            // Special tool
.minProficiency(CraftingProficiency.EXPERT)  // High skill required
.output("enchanted_iron_sword", 1)
.craftingTime(60)                       // Takes longer
.baseXp(100)                            // High XP reward
.baseDifficulty(0.4f)                   // Difficult even for experts
.build();
```

### Bulk Recipe (Arrows)

```java
CraftingRecipe arrows = new CraftingRecipe.Builder(
    "recipe_arrows",
    "Arrows",
    CraftingCategory.SMITHING
)
.description("Craft a bundle of arrows")
.addMaterial("iron_ingot", 1)
.addMaterial("wood_plank", 2)
.addMaterial("feather", 3)
.minProficiency(CraftingProficiency.NOVICE)
.output("arrow", 10)                    // Produces 10 arrows
.craftingTime(15)
.baseXp(15)
.baseDifficulty(0.15f)
.build();
```

### Recipe Without Tools

```java
CraftingRecipe bandage = new CraftingRecipe.Builder(
    "recipe_bandage",
    "Bandage",
    CraftingCategory.ALCHEMY
)
.description("Make a simple bandage")
.addMaterial("cloth", 2)
.addMaterial("healing_herb", 1)
// No tools required
.minProficiency(CraftingProficiency.NOVICE)
.output("bandage", 1)
.craftingTime(5)
.baseXp(5)
.baseDifficulty(0.05f)
.build();
```

## API Reference

### Constructor (via Builder)

```java
CraftingRecipe recipe = new CraftingRecipe.Builder(String id, String name, CraftingCategory category)
    .description(String description)                    // Optional
    .addMaterial(String prototypeId, int quantity)      // Required (at least 1)
    .addTool(String toolPrototypeId)                    // Optional
    .minProficiency(CraftingProficiency proficiency)    // Optional (default NOVICE)
    .output(String prototypeId, int quantity)           // Required
    .craftingTime(int ticks)                            // Optional (default 10)
    .baseXp(int xp)                                     // Optional (default 10)
    .baseDifficulty(float difficulty)                   // Optional (default 0.1)
    .build();
```

### Getters

```java
String getId()
String getName()
CraftingCategory getCategory()
String getDescription()
List<MaterialRequirement> getMaterials()
List<String> getRequiredToolPrototypeIds()
CraftingProficiency getMinProficiency()
String getOutputPrototypeId()
int getOutputQuantity()
int getCraftingTimeTicks()
int getBaseXp()
float getBaseDifficulty()
```

### Calculation Methods

```java
float calculateFailureChance(CraftingProficiency crafterProficiency)
```

## Integration with Other Systems

### CraftingSystem
```java
public class CraftingSystem {
    public CraftingResult craft(CraftingRecipe recipe, CraftingSkills skills, ...) {
        // Validate materials
        for (MaterialRequirement req : recipe.getMaterials()) {
            if (!hasEnough(materials, req)) {
                return CraftingResult.failure("Insufficient " + req.getPrototypeId());
            }
        }
        
        // Check proficiency
        CraftingProficiency prof = skills.getProficiency(recipe.getCategory());
        if (prof.ordinal() < recipe.getMinProficiency().ordinal()) {
            return CraftingResult.failure("Requires " + recipe.getMinProficiency());
        }
        
        // Calculate success
        float failureChance = recipe.calculateFailureChance(prof);
        boolean success = random.nextFloat() > failureChance;
        
        // ... craft item ...
    }
}
```

### ItemRegistry
```java
public static List<CraftingRecipe> getMvpRecipes() {
    List<CraftingRecipe> recipes = new ArrayList<>();
    
    recipes.add(new CraftingRecipe.Builder(...)
        .addMaterial("iron_ingot", 3)
        .output("iron_sword", 1)
        .build());
    
    return recipes;
}
```

### UI - Recipe Book
```java
public void displayRecipe(CraftingRecipe recipe) {
    System.out.println("=== " + recipe.getName() + " ===");
    System.out.println(recipe.getDescription());
    System.out.println();
    
    System.out.println("Materials:");
    for (MaterialRequirement mat : recipe.getMaterials()) {
        System.out.println("  - " + mat.getQuantity() + "× " + mat.getPrototypeId());
    }
    
    if (!recipe.getRequiredToolPrototypeIds().isEmpty()) {
        System.out.println("\nTools:");
        for (String tool : recipe.getRequiredToolPrototypeIds()) {
            System.out.println("  - " + tool);
        }
    }
    
    System.out.println("\nRequires: " + recipe.getMinProficiency().getName());
    System.out.println("Produces: " + recipe.getOutputQuantity() + "× " + recipe.getOutputPrototypeId());
    System.out.println("Base XP: " + recipe.getBaseXp());
}

// Output:
// === Iron Sword ===
// Craft a basic iron sword
//
// Materials:
//   - 3× iron_ingot
//   - 1× wood_plank
//
// Tools:
//   - steel_hammer
//
// Requires: Novice
// Produces: 1× iron_sword
// Base XP: 20
```

## Performance Considerations

### Memory Footprint

**Per Recipe:**
- IDs and strings: ~200 bytes
- Materials list: ~50 bytes per material × 3 avg = 150 bytes
- Tools list: ~50 bytes per tool × 1 avg = 50 bytes
- Fields and overhead: ~100 bytes
- **Total**: ~500 bytes per recipe

**Scalability:**
- 100 recipes: 50 KB
- 1,000 recipes: 500 KB (acceptable)

### Calculation Performance

```java
calculateFailureChance() complexity: O(1)
- Ordinal comparisons: O(1)
- Float arithmetic: ~10 CPU cycles
- Total: ~30 CPU cycles per calculation
```

## Design Decisions

### Why MaterialRequirement Inner Class?

**Alternative**: Just use `Map<String, Integer>`

```java
// Alternative (rejected)
private Map<String, Integer> materials;  // prototypeId → quantity

// Current (chosen)
private List<MaterialRequirement> materials;
```

**Advantages of Inner Class:**
- **Type Safety**: Can't accidentally put negative quantities
- **Validation**: Quantity checked at construction
- **Clarity**: `new MaterialRequirement("iron", 3)` vs `materials.put("iron", 3)`
- **Future**: Can add material-specific modifiers (quality requirements, etc.)

### Why Separate Tools from Materials?

**Design Decision**: Tools required but not consumed

```java
private List<MaterialRequirement> materials;          // Consumed on craft
private List<String> requiredToolPrototypeIds;        // Required but not consumed
```

**Rationale:**
- **Realism**: Hammers don't disappear after use
- **Economics**: Tools are investments, materials are consumables
- **Progression**: Getting tools unlocks recipe access
- **Future**: Tool durability (degrade but don't disappear)

**Alternative Rejected**: Combine into single list with "consumable" flag

### Why Float for baseDifficulty (0.0-1.0)?

**Alternative**: Integer difficulty (1-100)

**Float Wins:**
- **Precision**: 0.15 vs 15 (clearer as probability)
- **Standard**: Probabilities conventionally 0.0-1.0
- **Math**: Easier to multiply with other probabilities

### Why baseDifficulty Instead of baseSuccessChance?

**Design**: Store failure (difficulty), not success

**Rationale:**
- **Intuitive**: "This recipe is 20% difficult" vs "80% easy"
- **Design Language**: Game designers think in difficulty
- **Calculation**: Directly relates to proficiency penalties

## Known Limitations

1. **No Material Quality**: Can't require "Fine Iron Ingot" vs "Normal"
2. **No Alternative Materials**: Can't use "3 iron OR 2 steel"
3. **No Conditional Requirements**: Can't say "if specialized, need less materials"
4. **Fixed Output**: Always produces same quantity (no range)

## Future Enhancements

### Phase 1.6: Material Quality Requirements
```java
.addMaterial("iron_ingot", 3, CraftingQuality.HIGH_QUALITY)  // Require quality materials
```

### Phase 1.7: Alternative Materials
```java
.addMaterialChoice()
    .option("iron_ingot", 3)
    .option("steel_ingot", 2)  // Either 3 iron OR 2 steel
    .build()
```

### Phase 2: Variable Outputs
```java
.output("arrow", 8, 12)  // Produces 8-12 arrows based on quality roll
```

## Testing

See [CraftingRecipeTest.md](../../../../../test/java/org/adventure/CraftingRecipeTest.md) for test coverage:
- Builder validation (missing fields)
- Material requirements
- Tool requirements
- Failure chance calculations
- Edge cases (no tools, bulk output)

## References

- **Design Docs**: `docs/objects_crafting_legacy.md` → Recipe System
- **Data Models**: `docs/data_models.md` → CraftingRecipe Schema
- **Grand Plan**: `docs/grand_plan.md` → Phase 1.4 Recipes
- **Related Classes**: [CraftingSystem.md](CraftingSystem.md), [ItemRegistry.md](ItemRegistry.md)
- **Tests**: [CraftingTest.md](../../../../../test/java/org/adventure/CraftingTest.md)

---

**Last Updated:** Phase 1.4 Implementation (November 2025)  
**Status:** ✅ Complete - Builder pattern, failure calculation, 7 MVP recipes
