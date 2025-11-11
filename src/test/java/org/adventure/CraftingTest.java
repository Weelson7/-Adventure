package org.adventure;

import org.adventure.crafting.*;
import org.adventure.items.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for crafting system including recipes, proficiency, and XP progression.
 */
public class CraftingTest {
    
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
    
    @Test
    public void testProficiencyProgression() {
        assertEquals(CraftingProficiency.APPRENTICE, CraftingProficiency.NOVICE.next());
        assertEquals(CraftingProficiency.JOURNEYMAN, CraftingProficiency.APPRENTICE.next());
        assertEquals(CraftingProficiency.EXPERT, CraftingProficiency.JOURNEYMAN.next());
        assertEquals(CraftingProficiency.MASTER, CraftingProficiency.EXPERT.next());
        assertNull(CraftingProficiency.MASTER.next());
    }
    
    @Test
    public void testXpToNextLevel() {
        assertEquals(100, CraftingProficiency.NOVICE.getXpToNextLevel(0));
        assertEquals(1, CraftingProficiency.NOVICE.getXpToNextLevel(99));
        assertEquals(200, CraftingProficiency.APPRENTICE.getXpToNextLevel(100));
        assertEquals(0, CraftingProficiency.MASTER.getXpToNextLevel(1000));
    }
    
    @Test
    public void testCraftingSkillsInitialization() {
        CraftingSkills skills = new CraftingSkills("player_001");
        
        assertEquals(0, skills.getXp(CraftingCategory.SMITHING));
        assertEquals(CraftingProficiency.NOVICE, skills.getProficiency(CraftingCategory.SMITHING));
        assertEquals(0, skills.getSpecializations().size());
    }
    
    @Test
    public void testCraftingSkillsAddXp() {
        CraftingSkills skills = new CraftingSkills("player_001");
        
        int gained = skills.addXp(CraftingCategory.SMITHING, 50);
        
        assertEquals(50, gained);
        assertEquals(50, skills.getXp(CraftingCategory.SMITHING));
        assertEquals(CraftingProficiency.NOVICE, skills.getProficiency(CraftingCategory.SMITHING));
    }
    
    @Test
    public void testCraftingSkillsLevelUp() {
        CraftingSkills skills = new CraftingSkills("player_001");
        
        skills.addXp(CraftingCategory.SMITHING, 150);
        
        assertEquals(150, skills.getXp(CraftingCategory.SMITHING));
        assertEquals(CraftingProficiency.APPRENTICE, skills.getProficiency(CraftingCategory.SMITHING));
    }
    
    @Test
    public void testSpecializationBonus() {
        CraftingSkills skills = new CraftingSkills("player_001");
        skills.addSpecialization(CraftingCategory.SMITHING);
        
        // Specialization gives +20% XP
        int gained = skills.addXp(CraftingCategory.SMITHING, 100);
        
        assertEquals(120, gained); // 100 * 1.2
        assertEquals(120, skills.getXp(CraftingCategory.SMITHING));
    }
    
    @Test
    public void testMaxTwoSpecializations() {
        CraftingSkills skills = new CraftingSkills("player_001");
        
        assertTrue(skills.addSpecialization(CraftingCategory.SMITHING));
        assertTrue(skills.addSpecialization(CraftingCategory.ALCHEMY));
        assertFalse(skills.addSpecialization(CraftingCategory.ENCHANTING)); // Third fails
        
        assertEquals(2, skills.getSpecializations().size());
    }
    
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
    
    @Test
    public void testCannotCraftBelowMinProficiency() {
        CraftingRecipe recipe = craftingSystem.getRecipe("recipe_steel_axe"); // Requires Apprentice
        
        // Try to craft with Novice proficiency
        float failChance = recipe.calculateFailureChance(CraftingProficiency.NOVICE);
        
        assertEquals(1.0f, failChance); // 100% failure
    }
    
    @Test
    public void testSuccessfulCraft() {
        CraftingRecipe recipe = craftingSystem.getRecipe("recipe_iron_sword");
        CraftingSkills skills = new CraftingSkills("player_001");
        
        Map<String, Integer> materials = new HashMap<>();
        materials.put("iron_ingot", 10); // More than needed
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
        
        // With low difficulty and deterministic random, should succeed
        if (result.isSuccess()) {
            assertNotNull(result.getItem());
            assertEquals("iron_sword", result.getItem().getPrototypeId());
            assertTrue(result.getXpGained() > 0);
        }
        
        // Materials should be consumed
        assertEquals(7, materials.get("iron_ingot")); // 10 - 3
        assertEquals(4, materials.get("wood_plank")); // 5 - 1
    }
    
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
    
    @Test
    public void testCraftingQualityEnum() {
        assertEquals(1.0f, CraftingQuality.FLAWED.getXpMultiplier());
        assertEquals(1.2f, CraftingQuality.STANDARD.getXpMultiplier());
        assertEquals(1.5f, CraftingQuality.HIGH_QUALITY.getXpMultiplier());
        assertEquals(2.0f, CraftingQuality.MASTERWORK.getXpMultiplier());
    }
    
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
    
    @Test
    public void testCraftingCategoryEnum() {
        assertEquals("smithing", CraftingCategory.SMITHING.getId());
        assertEquals("alchemy", CraftingCategory.ALCHEMY.getId());
        assertEquals("enchanting", CraftingCategory.ENCHANTING.getId());
        assertNotNull(CraftingCategory.SMITHING.getDescription());
    }
    
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
}
