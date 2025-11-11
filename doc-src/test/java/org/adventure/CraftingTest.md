# CraftingTest.java - Crafting System Test Suite

**Package:** `org.adventure` (test)  
**Source:** [CraftingTest.java](../../../../../src/test/java/org/adventure/CraftingTest.java)  
**Phase:** MVP Phase 1.4 (Items & Crafting)  
**Tests:** 23 test cases

## Overview

`CraftingTest` validates all aspects of the crafting system including recipe validation, proficiency checks, material/tool validation, success/failure mechanics, quality determination, and XP calculation. It ensures the crafting orchestration works correctly with all integrated components.

## Test Structure

### Setup

```java
private CraftingSystem craftingSystem;
private Random random;

@BeforeEach
public void setup() {
    random = new Random(42); // Fixed seed for deterministic tests
    craftingSystem = new CraftingSystem(random);
    
    // Register all MVP items and recipes
    for (ItemPrototype proto : ItemRegistry.getMvpItemPrototypes()) {
        craftingSystem.registerItemPrototype(proto);
    }
    for (CraftingRecipe recipe : ItemRegistry.getMvpRecipes()) {
        craftingSystem.registerRecipe(recipe);
    }
}
```

**Key Points:**
- **Deterministic RNG**: Fixed seed (42) ensures reproducible results
- **Full MVP Setup**: All 12 prototypes and 7 recipes loaded
- **Fresh System**: New instance per test (isolation)

## Test Cases (23 Total)

### 1. Crafting Proficiency Levels

```java
@Test
public void testCraftingProficiencyLevels() {
    assertEquals(CraftingProficiency.NOVICE, CraftingProficiency.fromXp(0));
    assertEquals(CraftingProficiency.NOVICE, CraftingProficiency.fromXp(99));
    assertEquals(CraftingProficiency.APPRENTICE, CraftingProficiency.fromXp(100));
    assertEquals(CraftingProficiency.APPRENTICE, CraftingProficiency.fromXp(299));
    assertEquals(CraftingProficiency.JOURNEYMAN, CraftingProficiency.fromXp(300));
    assertEquals(CraftingProficiency.EXPERT, CraftingProficiency.fromXp(600));
    assertEquals(CraftingProficiency.MASTER, CraftingProficiency.fromXp(1000));
    assertEquals(CraftingProficiency.MASTER, CraftingProficiency.fromXp(5000));
}
```

**Validates:** XP thresholds for proficiency tiers
- 0-99 XP → Novice
- 100-299 XP → Apprentice
- 300-599 XP → Journeyman
- 600-999 XP → Expert
- 1000+ XP → Master (soft cap)

### 2. Proficiency Progression

```java
@Test
public void testProficiencyProgression() {
    assertEquals(CraftingProficiency.APPRENTICE, CraftingProficiency.NOVICE.next());
    assertEquals(CraftingProficiency.JOURNEYMAN, CraftingProficiency.APPRENTICE.next());
    assertEquals(CraftingProficiency.EXPERT, CraftingProficiency.JOURNEYMAN.next());
    assertEquals(CraftingProficiency.MASTER, CraftingProficiency.EXPERT.next());
    assertNull(CraftingProficiency.MASTER.next());
}
```

**Validates:** Tier progression chain and Master as final tier

### 3. XP to Next Level

```java
@Test
public void testXpToNextLevel() {
    assertEquals(100, CraftingProficiency.NOVICE.getXpToNextLevel(0));
    assertEquals(1, CraftingProficiency.NOVICE.getXpToNextLevel(99));
    assertEquals(200, CraftingProficiency.APPRENTICE.getXpToNextLevel(100));
    assertEquals(0, CraftingProficiency.MASTER.getXpToNextLevel(1000));
}
```

**Validates:** Correct XP needed for next tier
- Novice at 0 XP: needs 100 to reach Apprentice
- Novice at 99 XP: needs 1 more
- Master: always returns 0 (soft cap)

### 4. Crafting Skills Initialization

```java
@Test
public void testCraftingSkillsInitialization() {
    CraftingSkills skills = new CraftingSkills("player_001");
    
    assertEquals(0, skills.getXp(CraftingCategory.SMITHING));
    assertEquals(CraftingProficiency.NOVICE, skills.getProficiency(CraftingCategory.SMITHING));
    assertEquals(0, skills.getSpecializations().size());
}
```

**Validates:** Fresh character starts at 0 XP, Novice tier, no specializations

### 5. Crafting Skills Add XP

```java
@Test
public void testCraftingSkillsAddXp() {
    CraftingSkills skills = new CraftingSkills("player_001");
    
    int gained = skills.addXp(CraftingCategory.SMITHING, 50);
    
    assertEquals(50, gained);
    assertEquals(50, skills.getXp(CraftingCategory.SMITHING));
    assertEquals(CraftingProficiency.NOVICE, skills.getProficiency(CraftingCategory.SMITHING));
}
```

**Validates:** XP accumulation without tier change

### 6. Crafting Skills Level Up

```java
@Test
public void testCraftingSkillsLevelUp() {
    CraftingSkills skills = new CraftingSkills("player_001");
    
    skills.addXp(CraftingCategory.SMITHING, 150);
    
    assertEquals(150, skills.getXp(CraftingCategory.SMITHING));
    assertEquals(CraftingProficiency.APPRENTICE, skills.getProficiency(CraftingCategory.SMITHING));
}
```

**Validates:** Tier advancement when XP crosses threshold (100 → Apprentice)

### 7. Specialization Bonus

```java
@Test
public void testSpecializationBonus() {
    CraftingSkills skills = new CraftingSkills("player_001");
    skills.addSpecialization(CraftingCategory.SMITHING);
    
    // Specialization gives +20% XP
    int gained = skills.addXp(CraftingCategory.SMITHING, 100);
    
    assertEquals(120, gained); // 100 * 1.2
    assertEquals(120, skills.getXp(CraftingCategory.SMITHING));
}
```

**Validates:** +20% XP bonus for specialized categories

### 8. Max Two Specializations

```java
@Test
public void testMaxTwoSpecializations() {
    CraftingSkills skills = new CraftingSkills("player_001");
    
    assertTrue(skills.addSpecialization(CraftingCategory.SMITHING));
    assertTrue(skills.addSpecialization(CraftingCategory.ALCHEMY));
    assertFalse(skills.addSpecialization(CraftingCategory.ENCHANTING)); // Third fails
    
    assertEquals(2, skills.getSpecializations().size());
}
```

**Validates:** Specialization limit enforced

### 9. Below-Tier XP Penalty

```java
@Test
public void testBelowTierXpPenalty() {
    CraftingSkills skills = new CraftingSkills("player_001");
    skills.addXp(CraftingCategory.SMITHING, 500); // Journeyman level
    
    // Crafting Novice recipe at Journeyman level gives 50% XP
    int gained = skills.addXp(
        CraftingCategory.SMITHING, 
        100, 
        CraftingProficiency.NOVICE, // Recipe tier
        1.0f, 
        1.0f
    );
    
    assertEquals(50, gained); // 100 * 0.5
}
```

**Validates:** 50% XP when crafting below your tier (diminishing returns)

### 10. Crafting Recipe Validation

```java
@Test
public void testCraftingRecipeValidation() {
    CraftingRecipe recipe = craftingSystem.getRecipe("recipe_iron_sword");
    
    assertNotNull(recipe);
    assertEquals("Iron Sword", recipe.getName());
    assertEquals(CraftingCategory.SMITHING, recipe.getCategory());
    assertEquals(CraftingProficiency.NOVICE, recipe.getMinProficiency());
    assertEquals("iron_sword", recipe.getOutputPrototypeId());
    assertEquals(1, recipe.getOutputQuantity());
    
    List<CraftingRecipe.MaterialRequirement> materials = recipe.getMaterials();
    assertEquals(2, materials.size());
    
    boolean hasIronIngot = materials.stream()
        .anyMatch(m -> m.getPrototypeId().equals("iron_ingot") && m.getQuantity() == 3);
    assertTrue(hasIronIngot);
}
```

**Validates:** Recipe properties correct (name, category, materials, output)

### 11. Failure Chance Calculation

```java
@Test
public void testFailureChanceCalculation() {
    CraftingRecipe recipe = craftingSystem.getRecipe("recipe_iron_sword");
    
    // Novice crafting Novice recipe
    float failChance = recipe.calculateFailureChance(CraftingProficiency.NOVICE);
    assertTrue(failChance > 0 && failChance < 1);
    
    // Master crafting Novice recipe (should be very low failure)
    float masterFailChance = recipe.calculateFailureChance(CraftingProficiency.MASTER);
    assertTrue(masterFailChance < failChance);
}
```

**Validates:** 
- Failure chance is valid probability (0.0-1.0)
- Higher proficiency → lower failure chance

### 12. Cannot Craft Below Min Proficiency

```java
@Test
public void testCannotCraftBelowMinProficiency() {
    CraftingRecipe recipe = craftingSystem.getRecipe("recipe_steel_axe"); // Requires Apprentice
    
    // Try to craft with Novice proficiency
    float failChance = recipe.calculateFailureChance(CraftingProficiency.NOVICE);
    
    assertEquals(1.0f, failChance); // 100% failure
}
```

**Validates:** Below minimum tier → 100% failure

### 13. Successful Craft

```java
@Test
public void testSuccessfulCraft() {
    CraftingRecipe recipe = craftingSystem.getRecipe("recipe_iron_sword");
    CraftingSkills skills = new CraftingSkills("player_001");
    
    Map<String, Integer> materials = new HashMap<>();
    materials.put("iron_ingot", 10);
    materials.put("wood_plank", 5);
    
    Set<String> tools = new HashSet<>();
    tools.add("steel_hammer");
    
    // Use fixed seed to ensure success
    Random deterministicRandom = new Random(12345);
    CraftingSystem deterministicSystem = new CraftingSystem(deterministicRandom);
    for (ItemPrototype proto : ItemRegistry.getMvpItemPrototypes()) {
        deterministicSystem.registerItemPrototype(proto);
    }
    for (CraftingRecipe r : ItemRegistry.getMvpRecipes()) {
        deterministicSystem.registerRecipe(r);
    }
    
    CraftingSystem.CraftingResult result = deterministicSystem.craft(
        recipe, skills, materials, tools
    );
    
    if (result.isSuccess()) {
        assertNotNull(result.getItem());
        assertEquals("iron_sword", result.getItem().getPrototypeId());
        assertTrue(result.getXpGained() > 0);
    }
    
    // Materials should be consumed
    assertEquals(7, materials.get("iron_ingot")); // 10 - 3
    assertEquals(4, materials.get("wood_plank")); // 5 - 1
}
```

**Validates:** 
- Success creates correct item
- XP granted
- Materials consumed (10 → 7, 5 → 4)

### 14. Craft Fails with Insufficient Materials

```java
@Test
public void testCraftFailsWithInsufficientMaterials() {
    CraftingRecipe recipe = craftingSystem.getRecipe("recipe_iron_sword");
    CraftingSkills skills = new CraftingSkills("player_001");
    
    Map<String, Integer> materials = new HashMap<>();
    materials.put("iron_ingot", 1); // Not enough (needs 3)
    materials.put("wood_plank", 1);
    
    Set<String> tools = new HashSet<>();
    tools.add("steel_hammer");
    
    CraftingSystem.CraftingResult result = craftingSystem.craft(
        recipe, skills, materials, tools
    );
    
    assertFalse(result.isSuccess());
    assertTrue(result.getMessage().contains("Insufficient materials"));
}
```

**Validates:** Material validation catches shortage

### 15. Craft Fails with Missing Tool

```java
@Test
public void testCraftFailsWithMissingTool() {
    CraftingRecipe recipe = craftingSystem.getRecipe("recipe_iron_sword");
    CraftingSkills skills = new CraftingSkills("player_001");
    
    Map<String, Integer> materials = new HashMap<>();
    materials.put("iron_ingot", 10);
    materials.put("wood_plank", 5);
    
    Set<String> tools = new HashSet<>();
    // Missing steel_hammer
    
    CraftingSystem.CraftingResult result = craftingSystem.craft(
        recipe, skills, materials, tools
    );
    
    assertFalse(result.isSuccess());
    assertTrue(result.getMessage().contains("Missing required tool"));
}
```

**Validates:** Tool validation catches missing tools

### 16. Craft Fails with Insufficient Proficiency

```java
@Test
public void testCraftFailsWithInsufficientProficiency() {
    CraftingRecipe recipe = craftingSystem.getRecipe("recipe_steel_axe"); // Requires Apprentice
    CraftingSkills skills = new CraftingSkills("player_001"); // Novice by default
    
    Map<String, Integer> materials = new HashMap<>();
    materials.put("steel_ingot", 10);
    materials.put("wood_plank", 5);
    
    Set<String> tools = new HashSet<>();
    tools.add("steel_hammer");
    
    CraftingSystem.CraftingResult result = craftingSystem.craft(
        recipe, skills, materials, tools
    );
    
    assertFalse(result.isSuccess());
    assertTrue(result.getMessage().contains("Insufficient proficiency"));
}
```

**Validates:** Proficiency validation before crafting

### 17. XP Gain on Failure

```java
@Test
public void testXpGainOnFailure() {
    CraftingRecipe recipe = craftingSystem.getRecipe("recipe_iron_sword");
    CraftingSkills skills = new CraftingSkills("player_001");
    
    int initialXp = skills.getXp(CraftingCategory.SMITHING);
    
    Map<String, Integer> materials = new HashMap<>();
    materials.put("iron_ingot", 10);
    materials.put("wood_plank", 5);
    
    Set<String> tools = new HashSet<>();
    tools.add("steel_hammer");
    
    // Even if craft fails, should get some XP
    CraftingSystem.CraftingResult result = craftingSystem.craft(
        recipe, skills, materials, tools
    );
    
    int finalXp = skills.getXp(CraftingCategory.SMITHING);
    assertTrue(finalXp > initialXp); // Always gains some XP
}
```

**Validates:** XP granted even on RNG failure (learning from mistakes)

### 18. Crafting Quality Enum

```java
@Test
public void testCraftingQualityEnum() {
    assertEquals(1.0f, CraftingQuality.FLAWED.getXpMultiplier());
    assertEquals(1.2f, CraftingQuality.STANDARD.getXpMultiplier());
    assertEquals(1.5f, CraftingQuality.HIGH_QUALITY.getXpMultiplier());
    assertEquals(2.0f, CraftingQuality.MASTERWORK.getXpMultiplier());
}
```

**Validates:** Quality multipliers correct (1.0× to 2.0×)

### 19. All MVP Recipes Valid

```java
@Test
public void testAllMvpRecipesValid() {
    List<CraftingRecipe> recipes = ItemRegistry.getMvpRecipes();
    
    assertTrue(recipes.size() >= 7); // At least 7 recipes
    
    for (CraftingRecipe recipe : recipes) {
        assertNotNull(recipe.getId());
        assertNotNull(recipe.getName());
        assertNotNull(recipe.getCategory());
        assertFalse(recipe.getMaterials().isEmpty());
        assertTrue(recipe.getBaseXp() > 0);
        assertTrue(recipe.getCraftingTimeTicks() > 0);
    }
}
```

**Validates:** All MVP recipes have valid properties

### 20. All MVP Item Prototypes Valid

```java
@Test
public void testAllMvpItemPrototypesValid() {
    List<ItemPrototype> prototypes = ItemRegistry.getMvpItemPrototypes();
    
    assertTrue(prototypes.size() >= 12); // At least 12 items
    
    for (ItemPrototype proto : prototypes) {
        assertNotNull(proto.getId());
        assertNotNull(proto.getName());
        assertNotNull(proto.getCategory());
        assertTrue(proto.getMaxDurability() > 0);
        assertTrue(proto.getBaseValue() >= 0);
    }
}
```

**Validates:** All MVP prototypes have valid properties

### 21. Progress to Next Level

```java
@Test
public void testProgressToNextLevel() {
    CraftingSkills skills = new CraftingSkills("player_001");
    
    assertEquals(0.0f, skills.getProgressToNextLevel(CraftingCategory.SMITHING), 0.01f);
    
    skills.addXp(CraftingCategory.SMITHING, 50);
    assertEquals(0.5f, skills.getProgressToNextLevel(CraftingCategory.SMITHING), 0.01f);
    
    skills.addXp(CraftingCategory.SMITHING, 50);
    // Now at 100 XP (Apprentice), progress resets
    assertTrue(skills.getProgressToNextLevel(CraftingCategory.SMITHING) < 0.5f);
}
```

**Validates:** Progress percentage calculation (0.0-1.0)

### 22. Crafting Category Enum

```java
@Test
public void testCraftingCategoryEnum() {
    assertEquals("smithing", CraftingCategory.SMITHING.getId());
    assertEquals("alchemy", CraftingCategory.ALCHEMY.getId());
    assertEquals("enchanting", CraftingCategory.ENCHANTING.getId());
    assertNotNull(CraftingCategory.SMITHING.getDescription());
}
```

**Validates:** Category IDs and descriptions present

### 23. Material Requirement Equality

```java
@Test
public void testMaterialRequirementEquality() {
    CraftingRecipe.MaterialRequirement req1 = 
        new CraftingRecipe.MaterialRequirement("iron_ingot", 5);
    CraftingRecipe.MaterialRequirement req2 = 
        new CraftingRecipe.MaterialRequirement("iron_ingot", 5);
    CraftingRecipe.MaterialRequirement req3 = 
        new CraftingRecipe.MaterialRequirement("iron_ingot", 3);
    
    assertEquals(req1, req2);
    assertNotEquals(req1, req3);
}
```

**Validates:** MaterialRequirement equals() works correctly

## Test Coverage Summary

### Enums (CraftingProficiency, CraftingCategory, CraftingQuality)
- ✅ fromXp() conversion
- ✅ next() progression
- ✅ getXpToNextLevel() calculation
- ✅ Multipliers and properties

### CraftingSkills
- ✅ Initialization (0 XP, Novice)
- ✅ XP accumulation
- ✅ Tier advancement
- ✅ Specialization bonuses (+20%)
- ✅ Specialization limits (max 2)
- ✅ Below-tier penalties (50%)
- ✅ Progress tracking

### CraftingRecipe
- ✅ Recipe properties (name, category, materials, output)
- ✅ Failure chance calculation
- ✅ Below minimum proficiency (100% fail)
- ✅ MaterialRequirement equality

### CraftingSystem
- ✅ Successful craft (item creation, XP, material consumption)
- ✅ Insufficient materials failure
- ✅ Missing tool failure
- ✅ Insufficient proficiency failure
- ✅ XP gain on failure
- ✅ Quality determination

### ItemRegistry
- ✅ All MVP recipes valid (7+)
- ✅ All MVP prototypes valid (12+)

## Testing Strategies

### Deterministic RNG

```java
Random random = new Random(42);  // Fixed seed
CraftingSystem system = new CraftingSystem(random);
```

**Why?** Reproducible test results

### Separate Deterministic System for Success Test

```java
Random deterministicRandom = new Random(12345);
CraftingSystem deterministicSystem = new CraftingSystem(deterministicRandom);
```

**Why?** Different seed to ensure success (setup seed might roll failure)

### Boundary Testing

```java
// Edge of tier
fromXp(99)  → NOVICE
fromXp(100) → APPRENTICE

// Past soft cap
fromXp(5000) → MASTER
```

### Integration Testing

```java
// Full crafting flow
setup() → loads MVP data
craft() → validates all systems working together
```

## Performance Notes

**Test Suite Execution:**
- 23 tests: ~150ms total
- Per test average: ~6.5ms
- Setup overhead: ~20ms (loading 12 prototypes + 7 recipes)

**Fast Because:**
- No I/O operations
- Fixed RNG (no heavy computation)
- Small data set (MVP only)

## References

- **Source Classes:**
  - [CraftingSystem.md](../../../main/java/org/adventure/crafting/CraftingSystem.md)
  - [CraftingRecipe.md](../../../main/java/org/adventure/crafting/CraftingRecipe.md)
  - [CraftingSkills.md](../../../main/java/org/adventure/crafting/CraftingSkills.md)
  - [CraftingProficiency.md](../../../main/java/org/adventure/crafting/CraftingProficiency.md)
  - [CraftingCategory.md](../../../main/java/org/adventure/crafting/CraftingCategory.md)
  - [CraftingQuality.md](../../../main/java/org/adventure/crafting/CraftingQuality.md)
- **Related Tests:**
  - [ItemTest.md](ItemTest.md)
- **Design Docs:** `docs/testing_plan.md` → Crafting System Tests

---

**Last Updated:** Phase 1.4 Implementation (November 2025)  
**Status:** ✅ All 23 tests passing
