package org.adventure.crafting;

import java.util.*;

/**
 * CraftingRecipe defines a recipe for crafting an item.
 * Includes material requirements, tool requirements, proficiency requirements,
 * and parameters for success rate calculation.
 */
public class CraftingRecipe {
    private final String id;
    private final String name;
    private final CraftingCategory category;
    private final String description;
    
    // What's required to craft
    private final List<MaterialRequirement> materials;
    private final List<String> requiredToolPrototypeIds; // Tool prototypes needed
    private final CraftingProficiency minProficiency;
    
    // What's produced
    private final String outputPrototypeId;
    private final int outputQuantity;
    
    // Crafting parameters
    private final int craftingTimeTicks; // Time required to craft
    private final int baseXp; // Base XP before modifiers
    private final float baseDifficulty; // Base failure chance (0.0 to 1.0)
    
    private CraftingRecipe(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.category = builder.category;
        this.description = builder.description;
        this.materials = new ArrayList<>(builder.materials);
        this.requiredToolPrototypeIds = new ArrayList<>(builder.requiredToolPrototypeIds);
        this.minProficiency = builder.minProficiency;
        this.outputPrototypeId = builder.outputPrototypeId;
        this.outputQuantity = builder.outputQuantity;
        this.craftingTimeTicks = builder.craftingTimeTicks;
        this.baseXp = builder.baseXp;
        this.baseDifficulty = builder.baseDifficulty;
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public CraftingCategory getCategory() { return category; }
    public String getDescription() { return description; }
    public List<MaterialRequirement> getMaterials() { return new ArrayList<>(materials); }
    public List<String> getRequiredToolPrototypeIds() { return new ArrayList<>(requiredToolPrototypeIds); }
    public CraftingProficiency getMinProficiency() { return minProficiency; }
    public String getOutputPrototypeId() { return outputPrototypeId; }
    public int getOutputQuantity() { return outputQuantity; }
    public int getCraftingTimeTicks() { return craftingTimeTicks; }
    public int getBaseXp() { return baseXp; }
    public float getBaseDifficulty() { return baseDifficulty; }
    
    /**
     * Calculates failure chance based on proficiency and difficulty.
     * Formula: max(0, (baseDifficulty - proficiencyBonus) * proficiencyMultiplier)
     * 
     * @param crafterProficiency The crafter's proficiency level
     * @return Failure probability (0.0 to 1.0)
     */
    public float calculateFailureChance(CraftingProficiency crafterProficiency) {
        // Can't craft if below minimum proficiency
        if (crafterProficiency.ordinal() < minProficiency.ordinal()) {
            return 1.0f; // 100% failure
        }
        
        // Proficiency bonus: each level above minimum reduces failure
        int proficiencyDiff = crafterProficiency.ordinal() - minProficiency.ordinal();
        float proficiencyBonus = proficiencyDiff * 0.15f; // 15% reduction per tier above min
        
        // Apply proficiency multiplier from enum
        float adjustedDifficulty = (baseDifficulty - proficiencyBonus) * crafterProficiency.getFailureMultiplier();
        
        return Math.max(0.0f, Math.min(1.0f, adjustedDifficulty));
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CraftingRecipe that = (CraftingRecipe) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("CraftingRecipe{id='%s', name='%s', category=%s, minProf=%s}",
                id, name, category, minProficiency);
    }
    
    /**
     * Represents a material requirement for a recipe.
     */
    public static class MaterialRequirement {
        private final String prototypeId;
        private final int quantity;
        
        public MaterialRequirement(String prototypeId, int quantity) {
            this.prototypeId = Objects.requireNonNull(prototypeId);
            if (quantity < 1) {
                throw new IllegalArgumentException("Quantity must be at least 1");
            }
            this.quantity = quantity;
        }
        
        public String getPrototypeId() { return prototypeId; }
        public int getQuantity() { return quantity; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MaterialRequirement that = (MaterialRequirement) o;
            return quantity == that.quantity && Objects.equals(prototypeId, that.prototypeId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(prototypeId, quantity);
        }
        
        @Override
        public String toString() {
            return String.format("%dx %s", quantity, prototypeId);
        }
    }
    
    // Builder pattern
    public static class Builder {
        private String id;
        private String name;
        private CraftingCategory category;
        private String description = "";
        private List<MaterialRequirement> materials = new ArrayList<>();
        private List<String> requiredToolPrototypeIds = new ArrayList<>();
        private CraftingProficiency minProficiency = CraftingProficiency.NOVICE;
        private String outputPrototypeId;
        private int outputQuantity = 1;
        private int craftingTimeTicks = 10;
        private int baseXp = 10;
        private float baseDifficulty = 0.1f;
        
        public Builder(String id, String name, CraftingCategory category) {
            this.id = id;
            this.name = name;
            this.category = category;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
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
        
        public Builder craftingTime(int ticks) {
            this.craftingTimeTicks = ticks;
            return this;
        }
        
        public Builder baseXp(int xp) {
            this.baseXp = xp;
            return this;
        }
        
        public Builder baseDifficulty(float difficulty) {
            this.baseDifficulty = difficulty;
            return this;
        }
        
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
    }
}
