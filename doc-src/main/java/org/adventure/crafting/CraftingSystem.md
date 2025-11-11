# CraftingSystem.java - Crafting Orchestrator

**Package:** `org.adventure.crafting`  
**Source:** [CraftingSystem.java](../../../../../src/main/java/org/adventure/crafting/CraftingSystem.java)  
**Phase:** MVP Phase 1.4 (Items & Crafting)

## Overview

`CraftingSystem` is the main orchestrator that validates requirements, performs success/failure rolls, determines quality, consumes materials, creates items, and grants XP. It's the central hub that ties together recipes, skills, materials, and prototypes to execute the crafting process.

From `docs/objects_crafting_legacy.md`:
> "The CraftingSystem coordinates all crafting operations, from validation through item creation and XP rewards."

## Design Philosophy

### Separation of Concerns

**Design Decision**: CraftingSystem orchestrates, doesn't define

```
CraftingRecipe    → Defines WHAT can be crafted
CraftingSkills    → Tracks WHO can craft
CraftingSystem    → Executes HOW crafting happens
```

**Why?**
- **Single Responsibility**: Each class has one job
- **Testability**: Can test recipes without system, system without skills
- **Flexibility**: Swap out crafting logic without changing recipes

### Immutable Inputs, Mutable State

```java
public CraftingResult craft(
    CraftingRecipe recipe,          // Immutable: recipe definition
    CraftingSkills skills,           // Mutable: gains XP
    Map<String, Integer> materials,  // Mutable: consumed on success
    Set<String> availableTools       // Immutable: just checked
)
```

**Philosophy**: Input is validated, state is changed only on success

## Class Structure

### Core Fields

```java
private final Random random;                                      // RNG for success/quality rolls
private final Map<String, CraftingRecipe> recipes;                // Recipe registry
private final Map<String, ItemPrototype> itemPrototypes;          // Item prototype registry
```

**Minimal State**: Just registries and RNG

### CraftingResult Inner Class

```java
public static class CraftingResult {
    private final boolean success;
    private final String message;
    private final Item item;           // null if failed
    private final int xpGained;
    private final CraftingQuality quality;  // null if failed
    
    public static CraftingResult success(Item item, int xp, CraftingQuality quality) {
        return new CraftingResult(true, "Crafting successful!", item, xp, quality);
    }
    
    public static CraftingResult failure(String reason) {
        return new CraftingResult(false, reason, null, 0, null);
    }
}
```

**Pattern**: Result object encapsulates all outcomes

## The craft() Method - Step by Step

### Complete Flow

```java
public CraftingResult craft(
    CraftingRecipe recipe, 
    CraftingSkills skills, 
    Map<String, Integer> materials, 
    Set<String> availableTools
) {
    // STEP 1: Validate proficiency
    CraftingProficiency crafterProficiency = skills.getProficiency(recipe.getCategory());
    if (crafterProficiency.ordinal() < recipe.getMinProficiency().ordinal()) {
        return CraftingResult.failure("Insufficient proficiency. Requires: " + recipe.getMinProficiency().getName());
    }
    
    // STEP 2: Validate materials
    for (CraftingRecipe.MaterialRequirement req : recipe.getMaterials()) {
        int available = materials.getOrDefault(req.getPrototypeId(), 0);
        if (available < req.getQuantity()) {
            return CraftingResult.failure("Insufficient materials: " + req.getPrototypeId());
        }
    }
    
    // STEP 3: Validate tools
    for (String requiredTool : recipe.getRequiredToolPrototypeIds()) {
        if (!availableTools.contains(requiredTool)) {
            return CraftingResult.failure("Missing required tool: " + requiredTool);
        }
    }
    
    // STEP 4: Calculate success chance
    float failureChance = recipe.calculateFailureChance(crafterProficiency);
    boolean craftingSuccess = random.nextFloat() > failureChance;
    
    // STEP 5: Handle failure
    if (!craftingSuccess) {
        // Still gain XP on failure (proficiency-based)
        int failureXp = (int)(recipe.getBaseXp() * crafterProficiency.getFailureMultiplier());
        skills.addXp(recipe.getCategory(), failureXp, recipe.getMinProficiency(), 1.0f, 1.0f);
        return CraftingResult.failure("Crafting failed. Gained " + failureXp + " XP.");
    }
    
    // STEP 6: Determine quality on success
    CraftingQuality quality = rollQuality();
    
    // STEP 7: Consume materials
    for (CraftingRecipe.MaterialRequirement req : recipe.getMaterials()) {
        materials.compute(req.getPrototypeId(), (k, v) -> v - req.getQuantity());
    }
    
    // STEP 8: Create item
    ItemPrototype prototype = itemPrototypes.get(recipe.getOutputPrototypeId());
    if (prototype == null) {
        return CraftingResult.failure("Item prototype not found: " + recipe.getOutputPrototypeId());
    }
    Item item = Item.fromPrototype(prototype);
    
    // Apply quality to item
    item.setProperty("crafting_quality", quality.name());
    int qualityDurability = (int)(prototype.getMaxDurability() * quality.getDurabilityMultiplier());
    item.setProperty("max_durability_bonus", qualityDurability - prototype.getMaxDurability());
    
    // STEP 9: Calculate and grant XP
    float qualityMultiplier = quality.getXpMultiplier();
    float rarityMultiplier = prototype.getRarity().getXpMultiplier();
    int xpGained = skills.addXp(
        recipe.getCategory(), 
        recipe.getBaseXp(), 
        recipe.getMinProficiency(), 
        qualityMultiplier, 
        rarityMultiplier
    );
    
    return CraftingResult.success(item, xpGained, quality);
}
```

### Detailed Breakdown

#### Step 1: Validate Proficiency

```java
CraftingProficiency crafterProficiency = skills.getProficiency(recipe.getCategory());
if (crafterProficiency.ordinal() < recipe.getMinProficiency().ordinal()) {
    return CraftingResult.failure("Insufficient proficiency. Requires: " + recipe.getMinProficiency().getName());
}
```

**Example:**
```
Recipe: Steel Axe (requires Apprentice)
Crafter: Novice proficiency

Check: NOVICE (0) < APPRENTICE (1)?  YES
Result: "Insufficient proficiency. Requires: Apprentice"
```

#### Step 2: Validate Materials

```java
for (CraftingRecipe.MaterialRequirement req : recipe.getMaterials()) {
    int available = materials.getOrDefault(req.getPrototypeId(), 0);
    if (available < req.getQuantity()) {
        return CraftingResult.failure("Insufficient materials: " + req.getPrototypeId());
    }
}
```

**Example:**
```
Recipe needs: 3× iron_ingot, 1× wood_plank
Materials: { "iron_ingot": 5, "wood_plank": 0 }

Check iron_ingot: 5 >= 3?  YES
Check wood_plank: 0 >= 1?  NO
Result: "Insufficient materials: wood_plank"
```

#### Step 3: Validate Tools

```java
for (String requiredTool : recipe.getRequiredToolPrototypeIds()) {
    if (!availableTools.contains(requiredTool)) {
        return CraftingResult.failure("Missing required tool: " + requiredTool);
    }
}
```

**Example:**
```
Recipe needs: steel_hammer
Available tools: { "iron_pickaxe" }

Check: availableTools.contains("steel_hammer")?  NO
Result: "Missing required tool: steel_hammer"
```

#### Step 4: Calculate Success Chance

```java
float failureChance = recipe.calculateFailureChance(crafterProficiency);
boolean craftingSuccess = random.nextFloat() > failureChance;
```

**Example:**
```
Recipe: Iron Sword (Apprentice, 0.2 difficulty)
Crafter: Journeyman

failureChance = recipe.calculateFailureChance(JOURNEYMAN)
              = 0.015 (1.5%, calculated in CraftingRecipe)

random.nextFloat() = 0.452
0.452 > 0.015?  YES
Result: craftingSuccess = true
```

#### Step 5: Handle Failure

```java
if (!craftingSuccess) {
    int failureXp = (int)(recipe.getBaseXp() * crafterProficiency.getFailureMultiplier());
    skills.addXp(recipe.getCategory(), failureXp, recipe.getMinProficiency(), 1.0f, 1.0f);
    return CraftingResult.failure("Crafting failed. Gained " + failureXp + " XP.");
}
```

**Example:**
```
Recipe: Iron Sword (baseXp = 20)
Crafter: Apprentice (failureMultiplier = 0.4)

failureXp = 20 × 0.4 = 8
skills.addXp(SMITHING, 8, ...)
Result: "Crafting failed. Gained 8 XP."
```

#### Step 6: Determine Quality

```java
CraftingQuality quality = rollQuality();

private CraftingQuality rollQuality() {
    float roll = random.nextFloat();
    
    if (roll < 0.05f) {           // 5%
        return CraftingQuality.MASTERWORK;
    } else if (roll < 0.25f) {    // 20%
        return CraftingQuality.HIGH_QUALITY;
    } else if (roll < 0.85f) {    // 60%
        return CraftingQuality.STANDARD;
    } else {                      // 15%
        return CraftingQuality.FLAWED;
    }
}
```

**Example:**
```
random.nextFloat() = 0.18

0.18 < 0.05?  NO
0.18 < 0.25?  YES
Result: HIGH_QUALITY (20% chance bracket)
```

#### Step 7: Consume Materials

```java
for (CraftingRecipe.MaterialRequirement req : recipe.getMaterials()) {
    materials.compute(req.getPrototypeId(), (k, v) -> v - req.getQuantity());
}
```

**Example:**
```
Before: materials = { "iron_ingot": 10, "wood_plank": 5 }
Recipe needs: 3× iron_ingot, 1× wood_plank

materials.compute("iron_ingot", (k, v) -> 10 - 3) → 7
materials.compute("wood_plank", (k, v) -> 5 - 1) → 4

After: materials = { "iron_ingot": 7, "wood_plank": 4 }
```

**Note**: Materials are consumed ONLY on success

#### Step 8: Create Item

```java
ItemPrototype prototype = itemPrototypes.get(recipe.getOutputPrototypeId());
Item item = Item.fromPrototype(prototype);

item.setProperty("crafting_quality", quality.name());
int qualityDurability = (int)(prototype.getMaxDurability() * quality.getDurabilityMultiplier());
item.setProperty("max_durability_bonus", qualityDurability - prototype.getMaxDurability());
```

**Example:**
```
Prototype: iron_sword (maxDurability = 100)
Quality: HIGH_QUALITY (durabilityMultiplier = 1.2)

qualityDurability = 100 × 1.2 = 120
durabilityBonus = 120 - 100 = 20

Item properties:
- crafting_quality: "HIGH_QUALITY"
- max_durability_bonus: 20
- current_durability: 120 (starts at max)
```

#### Step 9: Calculate and Grant XP

```java
float qualityMultiplier = quality.getXpMultiplier();
float rarityMultiplier = prototype.getRarity().getXpMultiplier();
int xpGained = skills.addXp(
    recipe.getCategory(), 
    recipe.getBaseXp(), 
    recipe.getMinProficiency(), 
    qualityMultiplier, 
    rarityMultiplier
);
```

**Example:**
```
Recipe: Iron Sword (baseXp = 20, minProf = Apprentice)
Quality: HIGH_QUALITY (xpMultiplier = 1.5)
Prototype: iron_sword (COMMON rarity, rarityMultiplier = 1.0)
Crafter: Journeyman in SMITHING (not specialized)

xpGained = skills.addXp(SMITHING, 20, APPRENTICE, 1.5, 1.0)
         = 20 × 1.5 × 1.0 × 0.5 (below-tier penalty)
         = 15 XP

Result: CraftingResult.success(item, 15, HIGH_QUALITY)
```

## Quality Roll Distribution

```java
private CraftingQuality rollQuality() {
    float roll = random.nextFloat();  // 0.0 to 1.0
    
    if (roll < 0.05f) {           // 0.00 - 0.05 (5%)
        return CraftingQuality.MASTERWORK;
    } else if (roll < 0.25f) {    // 0.05 - 0.25 (20%)
        return CraftingQuality.HIGH_QUALITY;
    } else if (roll < 0.85f) {    // 0.25 - 0.85 (60%)
        return CraftingQuality.STANDARD;
    } else {                      // 0.85 - 1.00 (15%)
        return CraftingQuality.FLAWED;
    }
}
```

**Visual Distribution:**
```
MASTERWORK:    █████ (5%)
HIGH_QUALITY:  ████████████████████ (20%)
STANDARD:      ████████████████████████████████████████████████████████████ (60%)
FLAWED:        ███████████████ (15%)
```

## Usage Examples

### Basic Crafting (Iron Sword)

```java
CraftingSystem system = new CraftingSystem(new Random());

// Register prototypes and recipes
system.registerItemPrototype(ironIngotProto);
system.registerItemPrototype(woodPlankProto);
system.registerItemPrototype(ironSwordProto);
system.registerRecipe(ironSwordRecipe);

// Player setup
CraftingSkills skills = new CraftingSkills("player_001");
skills.addXp(CraftingCategory.SMITHING, 50);  // Some experience

Map<String, Integer> materials = new HashMap<>();
materials.put("iron_ingot", 5);
materials.put("wood_plank", 2);

Set<String> tools = new HashSet<>();
tools.add("steel_hammer");

// Attempt craft
CraftingResult result = system.craft(
    system.getRecipe("recipe_iron_sword"),
    skills,
    materials,
    tools
);

if (result.isSuccess()) {
    System.out.println("✓ Crafted: " + result.getItem().getPrototypeId());
    System.out.println("  Quality: " + result.getQuality().getName());
    System.out.println("  XP Gained: " + result.getXpGained());
    System.out.println("  Remaining Materials:");
    System.out.println("    - iron_ingot: " + materials.get("iron_ingot"));
    System.out.println("    - wood_plank: " + materials.get("wood_plank"));
} else {
    System.out.println("✗ " + result.getMessage());
}

// Output (example):
// ✓ Crafted: iron_sword
//   Quality: Standard
//   XP Gained: 20
//   Remaining Materials:
//     - iron_ingot: 2
//     - wood_plank: 1
```

### Handling All Failure Cases

```java
CraftingRecipe recipe = system.getRecipe("recipe_steel_axe");
CraftingSkills skills = new CraftingSkills("player_001");

Map<String, Integer> materials = new HashMap<>();
materials.put("steel_ingot", 1);  // Not enough (needs 4)
materials.put("wood_plank", 1);

Set<String> tools = new HashSet<>();
// No hammer

CraftingResult result = system.craft(recipe, skills, materials, tools);

// Failure 1: Insufficient proficiency
// → "Insufficient proficiency. Requires: Apprentice"

skills.addXp(CraftingCategory.SMITHING, 150);  // Now Apprentice
result = system.craft(recipe, skills, materials, tools);

// Failure 2: Insufficient materials
// → "Insufficient materials: steel_ingot"

materials.put("steel_ingot", 10);  // Fix materials
result = system.craft(recipe, skills, materials, tools);

// Failure 3: Missing tool
// → "Missing required tool: steel_hammer"

tools.add("steel_hammer");  // Fix tools
result = system.craft(recipe, skills, materials, tools);

// Now might succeed or fail due to RNG
if (result.isSuccess()) {
    System.out.println("Success!");
} else {
    // Failure 4: RNG failure
    // → "Crafting failed. Gained 8 XP."
}
```

### Batch Crafting

```java
CraftingRecipe arrowRecipe = system.getRecipe("recipe_arrows");  // Produces 10 arrows
CraftingSkills skills = new CraftingSkills("player_001");

Map<String, Integer> materials = new HashMap<>();
materials.put("iron_ingot", 20);   // Enough for 20 batches
materials.put("wood_plank", 40);
materials.put("feather", 60);

Set<String> tools = new HashSet<>();
// No tools required

int totalArrows = 0;
int attempts = 0;
int successes = 0;

while (materials.get("iron_ingot") >= 1 && 
       materials.get("wood_plank") >= 2 && 
       materials.get("feather") >= 3) {
    
    CraftingResult result = system.craft(arrowRecipe, skills, materials, tools);
    attempts++;
    
    if (result.isSuccess()) {
        successes++;
        totalArrows += result.getItem().getQuantity();  // 10 per batch
    }
}

System.out.println("Crafting Session:");
System.out.println("  Attempts: " + attempts);
System.out.println("  Successes: " + successes);
System.out.println("  Total Arrows: " + totalArrows);
System.out.println("  Success Rate: " + (100 * successes / attempts) + "%");

// Output (example):
// Crafting Session:
//   Attempts: 20
//   Successes: 18
//   Total Arrows: 180
//   Success Rate: 90%
```

## API Reference

### Constructor

```java
public CraftingSystem(Random random)
```

### Registration Methods

```java
void registerRecipe(CraftingRecipe recipe)
void registerItemPrototype(ItemPrototype prototype)
```

### Query Methods

```java
CraftingRecipe getRecipe(String recipeId)
ItemPrototype getItemPrototype(String prototypeId)
```

### Core Method

```java
CraftingResult craft(
    CraftingRecipe recipe,
    CraftingSkills skills,
    Map<String, Integer> materials,
    Set<String> availableTools
)
```

### CraftingResult Methods

```java
boolean isSuccess()
String getMessage()
Item getItem()              // null if failed
int getXpGained()
CraftingQuality getQuality()  // null if failed

static CraftingResult success(Item item, int xp, CraftingQuality quality)
static CraftingResult failure(String reason)
```

## Integration with Other Systems

### Character Inventory

```java
public class Character {
    private Map<String, Integer> inventory;  // materials
    private Set<String> equippedTools;       // tools
    private CraftingSkills craftingSkills;
    
    public CraftingResult attemptCraft(CraftingSystem system, CraftingRecipe recipe) {
        return system.craft(recipe, craftingSkills, inventory, equippedTools);
    }
}
```

### UI - Crafting Menu

```java
public void displayCraftingMenu(CraftingSystem system, Character player) {
    List<CraftingRecipe> availableRecipes = system.getAllRecipes().stream()
        .filter(r -> canCraft(player, r))
        .collect(Collectors.toList());
    
    for (int i = 0; i < availableRecipes.size(); i++) {
        CraftingRecipe recipe = availableRecipes.get(i);
        System.out.println((i+1) + ". " + recipe.getName());
        
        // Show success chance
        float failChance = recipe.calculateFailureChance(
            player.getCraftingSkills().getProficiency(recipe.getCategory())
        );
        System.out.println("   Success: " + (int)((1 - failChance) * 100) + "%");
    }
    
    // Player selects recipe
    int choice = getUserInput();
    CraftingRecipe chosen = availableRecipes.get(choice - 1);
    
    CraftingResult result = player.attemptCraft(system, chosen);
    
    if (result.isSuccess()) {
        System.out.println("✓ Crafted " + result.getQuality().getName() + " " + result.getItem().getPrototypeId());
        player.addToInventory(result.getItem());
    } else {
        System.out.println("✗ " + result.getMessage());
    }
}
```

### Economic System

```java
public class CraftingEconomy {
    private CraftingSystem system;
    
    public int calculateCraftingCost(CraftingRecipe recipe) {
        int materialCost = 0;
        
        for (MaterialRequirement req : recipe.getMaterials()) {
            ItemPrototype proto = system.getItemPrototype(req.getPrototypeId());
            materialCost += proto.getBaseValue() * req.getQuantity();
        }
        
        return materialCost;
    }
    
    public int calculateCraftingProfit(CraftingRecipe recipe, CraftingQuality quality) {
        ItemPrototype output = system.getItemPrototype(recipe.getOutputPrototypeId());
        int outputValue = (int)(output.getBaseValue() * quality.getDurabilityMultiplier());
        int materialCost = calculateCraftingCost(recipe);
        
        return (outputValue - materialCost) * recipe.getOutputQuantity();
    }
}
```

## Performance Considerations

### Time Complexity

```
craft() breakdown:
- Proficiency check: O(1)
- Material validation: O(m) where m = number of materials
- Tool validation: O(t) where t = number of tools
- Success roll: O(1)
- Quality roll: O(1)
- Material consumption: O(m)
- Item creation: O(1)
- XP calculation: O(1)

Total: O(m + t) ≈ O(5) typical = O(1) practical
```

### Memory Usage

```
CraftingSystem:
- recipes: 100 recipes × 500 bytes = 50 KB
- itemPrototypes: 200 items × 400 bytes = 80 KB
- Random: 48 bytes
- Overhead: ~10 KB

Total: ~140 KB per instance
```

**Recommendation**: Single global instance

## Design Decisions

### Why Random Passed to Constructor?

**Alternative**: Create Random internally

```java
// Alternative (rejected)
public CraftingSystem() {
    this.random = new Random();
}

// Current (chosen)
public CraftingSystem(Random random) {
    this.random = random;
}
```

**Advantages:**
- **Testing**: Pass seeded Random for deterministic tests
- **Coordination**: Share RNG across systems (performance)
- **Control**: Can swap RNG implementation (e.g., SecureRandom)

### Why Consume Materials on Map Instead of Inventory?

**Design Decision**: craft() operates on generic Map

```java
// Alternative (rejected)
public CraftingResult craft(Recipe recipe, Inventory inventory) {
    inventory.removeItems(...);  // Tightly coupled
}

// Current (chosen)
public CraftingResult craft(Recipe recipe, Map<String, Integer> materials) {
    materials.compute(...);  // Loosely coupled
}
```

**Advantages:**
- **Flexibility**: Works with any material source (inventory, warehouse, shared storage)
- **Testability**: Easy to create test materials (just HashMap)
- **Decoupling**: CraftingSystem doesn't know about Inventory implementation

### Why XP Granted Even on Failure?

**Design Decision**: Always gain experience, even when failing

**Rationale:**
- **Player Retention**: Failures feel less punishing
- **Progression**: Players always make progress
- **Realism**: Learn from mistakes
- **Balance**: Failure XP reduced by proficiency multiplier

**Formula:**
```
Failure XP = baseXp × proficiencyFailureMultiplier

Novice fails: 20 × 0.5 = 10 XP
Master fails: 20 × 0.1 = 2 XP (masters learn less from easy mistakes)
```

### Why Quality Roll Doesn't Consider Proficiency?

**Design Decision**: Quality is pure RNG

**Alternative Rejected**: Higher proficiency → better quality chance

**Rationale:**
- **Simplicity**: Easier to understand ("quality is luck")
- **Drama**: Masters can still roll Flawed (frustrating but memorable)
- **Balance**: Proficiency already affects success rate
- **Future**: Can add proficiency-based quality bonus in Phase 1.6

## Known Limitations

1. **No Partial Material Refund**: Failure consumes nothing (unrealistic for some crafts)
2. **Binary Success**: No "partial success" (damaged item but usable)
3. **Fixed Quality Distribution**: Can't adjust probabilities per recipe/proficiency
4. **No Batch Crafting**: Must call craft() multiple times
5. **No Crafting Time**: Instant completion (craftingTimeTicks unused)

## Future Enhancements

### Phase 1.5: Crafting Time System
```java
public CraftingResult startCraft(CraftingRecipe recipe, ...) {
    // Returns CraftingInProgress with completion time
}

public CraftingResult completeCraft(CraftingInProgress progress) {
    // Finishes after craftingTimeTicks elapsed
}
```

### Phase 1.6: Proficiency-Influenced Quality
```java
private CraftingQuality rollQuality(CraftingProficiency proficiency) {
    float roll = random.nextFloat();
    
    // Master has +10% to roll Masterwork (5% → 15%)
    float masterworkChance = 0.05f + (proficiency.ordinal() * 0.025f);
    
    if (roll < masterworkChance) {
        return CraftingQuality.MASTERWORK;
    }
    // ... rest of distribution
}
```

### Phase 2: Batch Crafting
```java
public BatchCraftingResult craftMultiple(
    CraftingRecipe recipe,
    int quantity,
    ...
) {
    // Craft multiple items efficiently
    // Early-stop if materials run out
}
```

### Phase 2: Partial Failures
```java
public CraftingResult craft(...) {
    // ...
    if (!craftingSuccess) {
        // Refund 50% of materials
        for (MaterialRequirement req : recipe.getMaterials()) {
            int refund = req.getQuantity() / 2;
            materials.compute(req.getPrototypeId(), (k, v) -> v + refund);
        }
        // ...
    }
}
```

## Testing

See [CraftingTest.md](../../../../../test/java/org/adventure/CraftingTest.md) for test coverage:
- Registration (recipes, prototypes)
- Validation (proficiency, materials, tools)
- Success/failure mechanics
- Quality determination
- XP calculation
- Material consumption
- Integration scenarios

## References

- **Design Docs**: `docs/objects_crafting_legacy.md` → Crafting System Architecture
- **Data Models**: `docs/data_models.md` → CraftingSystem Schema
- **Grand Plan**: `docs/grand_plan.md` → Phase 1.4 Crafting
- **Related Classes**: 
  - [CraftingRecipe.md](CraftingRecipe.md) - Recipe definitions
  - [CraftingSkills.md](CraftingSkills.md) - XP and proficiency tracking
  - [ItemRegistry.md](ItemRegistry.md) - Recipe and prototype registration
  - [Item.md](../items/Item.md) - Crafted item instances
- **Tests**: [CraftingTest.md](../../../../../test/java/org/adventure/CraftingTest.md)

---

**Last Updated:** Phase 1.4 Implementation (November 2025)  
**Status:** ✅ Complete - Orchestration, validation, RNG, XP system, 23 tests passing
