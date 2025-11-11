package org.adventure.crafting;

import org.adventure.items.Item;
import org.adventure.items.ItemPrototype;
import org.adventure.items.ItemRarity;

import java.util.*;

/**
 * CraftingSystem orchestrates the crafting process.
 * Handles recipe validation, success/failure rolls, XP gain, and item creation.
 */
public class CraftingSystem {
    
    private final Map<String, CraftingRecipe> recipes;
    private final Map<String, ItemPrototype> itemPrototypes;
    private final Random random;
    
    public CraftingSystem(Random random) {
        this.recipes = new HashMap<>();
        this.itemPrototypes = new HashMap<>();
        this.random = random;
    }
    
    /**
     * Registers a recipe.
     */
    public void registerRecipe(CraftingRecipe recipe) {
        recipes.put(recipe.getId(), recipe);
    }
    
    /**
     * Registers an item prototype.
     */
    public void registerItemPrototype(ItemPrototype prototype) {
        itemPrototypes.put(prototype.getId(), prototype);
    }
    
    /**
     * Gets a recipe by ID.
     */
    public CraftingRecipe getRecipe(String recipeId) {
        return recipes.get(recipeId);
    }
    
    /**
     * Gets an item prototype by ID.
     */
    public ItemPrototype getItemPrototype(String prototypeId) {
        return itemPrototypes.get(prototypeId);
    }
    
    /**
     * Gets all registered recipes.
     */
    public Collection<CraftingRecipe> getAllRecipes() {
        return new ArrayList<>(recipes.values());
    }
    
    /**
     * Attempts to craft an item using a recipe.
     * 
     * @param recipe The recipe to use
     * @param crafterSkills The crafter's skills
     * @param availableMaterials Materials available for crafting (will be consumed on success)
     * @param availableTools Tools available (must be present but not consumed)
     * @param toolQualityMultiplier Quality of tools (1.0 = basic, 1.2 = fine, 1.5 = masterwork)
     * @return CraftingResult with outcome details
     */
    public CraftingResult craft(CraftingRecipe recipe, 
                                CraftingSkills crafterSkills,
                                Map<String, Integer> availableMaterials,
                                Set<String> availableTools,
                                float toolQualityMultiplier) {
        
        // Validate recipe exists
        if (!recipes.containsValue(recipe)) {
            return CraftingResult.failure("Recipe not registered");
        }
        
        // Check proficiency
        CraftingProficiency crafterProf = crafterSkills.getProficiency(recipe.getCategory());
        if (crafterProf.ordinal() < recipe.getMinProficiency().ordinal()) {
            return CraftingResult.failure("Insufficient proficiency");
        }
        
        // Check materials
        for (CraftingRecipe.MaterialRequirement req : recipe.getMaterials()) {
            int available = availableMaterials.getOrDefault(req.getPrototypeId(), 0);
            if (available < req.getQuantity()) {
                return CraftingResult.failure("Insufficient materials: " + req.getPrototypeId());
            }
        }
        
        // Check tools
        for (String toolId : recipe.getRequiredToolPrototypeIds()) {
            if (!availableTools.contains(toolId)) {
                return CraftingResult.failure("Missing required tool: " + toolId);
            }
        }
        
        // Calculate success/failure
        float failureChance = recipe.calculateFailureChance(crafterProf);
        float roll = random.nextFloat();
        boolean success = roll > failureChance;
        
        // Determine quality on success
        CraftingQuality quality = CraftingQuality.STANDARD;
        if (success) {
            float qualityRoll = random.nextFloat();
            if (qualityRoll > 0.95f) {
                quality = CraftingQuality.MASTERWORK;
            } else if (qualityRoll > 0.8f) {
                quality = CraftingQuality.HIGH_QUALITY;
            }
        } else {
            // Failure - check if it's a flawed item or complete failure
            float flawedChance = 0.5f; // 50% chance to get flawed item on failure
            if (random.nextFloat() < flawedChance) {
                quality = CraftingQuality.FLAWED;
                success = true; // Created something, but flawed
            }
        }
        
        // Consume materials on attempt (regardless of outcome)
        for (CraftingRecipe.MaterialRequirement req : recipe.getMaterials()) {
            availableMaterials.merge(req.getPrototypeId(), -req.getQuantity(), Integer::sum);
        }
        
        Item craftedItem = null;
        int xpGained = 0;
        
        if (success) {
            // Create the item
            ItemPrototype outputPrototype = itemPrototypes.get(recipe.getOutputPrototypeId());
            if (outputPrototype == null) {
                return CraftingResult.failure("Output prototype not found: " + recipe.getOutputPrototypeId());
            }
            
            craftedItem = Item.fromPrototype(outputPrototype);
            
            // Apply quality modifiers to durability
            switch (quality) {
                case FLAWED:
                    craftedItem.damage(craftedItem.getMaxDurability() * 0.3f); // Start at 70% durability
                    break;
                case MASTERWORK:
                    // Could add bonus durability or other properties
                    craftedItem.setCustomProperty("quality", "masterwork");
                    break;
                case HIGH_QUALITY:
                    craftedItem.setCustomProperty("quality", "high_quality");
                    break;
                default:
                    break;
            }
            
            // Grant XP
            ItemRarity rarity = outputPrototype.getRarity();
            xpGained = crafterSkills.addXp(
                recipe.getCategory(),
                recipe.getBaseXp(),
                recipe.getMinProficiency(),
                quality.getXpMultiplier(),
                toolQualityMultiplier
            );
            
            // Apply rarity multiplier
            xpGained = Math.round(xpGained * rarity.getXpMultiplier());
        } else {
            // Complete failure - grant partial XP (25%)
            xpGained = crafterSkills.addXp(
                recipe.getCategory(),
                Math.round(recipe.getBaseXp() * 0.25f),
                recipe.getMinProficiency(),
                1.0f,
                toolQualityMultiplier
            );
        }
        
        return new CraftingResult(success, craftedItem, quality, xpGained, "");
    }
    
    /**
     * Simplified craft method with default tool quality.
     */
    public CraftingResult craft(CraftingRecipe recipe,
                                CraftingSkills crafterSkills,
                                Map<String, Integer> availableMaterials,
                                Set<String> availableTools) {
        return craft(recipe, crafterSkills, availableMaterials, availableTools, 1.0f);
    }
    
    /**
     * Result of a crafting attempt.
     */
    public static class CraftingResult {
        private final boolean success;
        private final Item item; // Null if complete failure
        private final CraftingQuality quality;
        private final int xpGained;
        private final String message;
        
        public CraftingResult(boolean success, Item item, CraftingQuality quality, 
                            int xpGained, String message) {
            this.success = success;
            this.item = item;
            this.quality = quality;
            this.xpGained = xpGained;
            this.message = message;
        }
        
        public static CraftingResult failure(String message) {
            return new CraftingResult(false, null, CraftingQuality.STANDARD, 0, message);
        }
        
        public boolean isSuccess() { return success; }
        public Item getItem() { return item; }
        public CraftingQuality getQuality() { return quality; }
        public int getXpGained() { return xpGained; }
        public String getMessage() { return message; }
        
        @Override
        public String toString() {
            if (!success) {
                return "CraftingResult{failure: " + message + "}";
            }
            return String.format("CraftingResult{success, quality=%s, xp=%d}", quality, xpGained);
        }
    }
}
