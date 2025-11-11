package org.adventure.crafting;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * CraftingSkills tracks a character's crafting progression across all categories.
 * Handles XP gain, specializations, and proficiency calculations.
 */
public class CraftingSkills {
    private static final int MAX_SPECIALIZATIONS = 2;
    private static final float SPECIALIZATION_XP_BONUS = 0.2f; // +20% XP
    private static final float BELOW_TIER_XP_PENALTY = 0.5f; // 50% XP for recipes below tier
    
    private final String characterId;
    
    // XP per category
    private final Map<CraftingCategory, Integer> categoryXp;
    
    // Specializations (max 2)
    private final Set<CraftingCategory> specializations;
    
    public CraftingSkills(String characterId) {
        this.characterId = characterId;
        this.categoryXp = new HashMap<>();
        this.specializations = new HashSet<>();
        
        // Initialize all categories to 0 XP
        for (CraftingCategory category : CraftingCategory.values()) {
            categoryXp.put(category, 0);
        }
    }
    
    /**
     * Gets current XP for a category.
     */
    public int getXp(CraftingCategory category) {
        return categoryXp.getOrDefault(category, 0);
    }
    
    /**
     * Gets current proficiency level for a category.
     */
    public CraftingProficiency getProficiency(CraftingCategory category) {
        int xp = getXp(category);
        return CraftingProficiency.fromXp(xp);
    }
    
    /**
     * Checks if a category is specialized.
     */
    public boolean isSpecialized(CraftingCategory category) {
        return specializations.contains(category);
    }
    
    /**
     * Gets all specializations.
     */
    public Set<CraftingCategory> getSpecializations() {
        return new HashSet<>(specializations);
    }
    
    /**
     * Attempts to add a specialization.
     * Returns true if successful, false if already at max specializations.
     */
    public boolean addSpecialization(CraftingCategory category) {
        if (specializations.size() >= MAX_SPECIALIZATIONS) {
            return false;
        }
        return specializations.add(category);
    }
    
    /**
     * Removes a specialization.
     */
    public boolean removeSpecialization(CraftingCategory category) {
        return specializations.remove(category);
    }
    
    /**
     * Adds XP to a category with all modifiers applied.
     * 
     * @param category The crafting category
     * @param baseXp Base XP before modifiers
     * @param recipeProficiency The proficiency tier of the recipe
     * @param qualityBonus Quality multiplier (1.0 = standard, 1.5 = high quality, etc.)
     * @param toolBonus Tool quality multiplier (1.0 = basic, 1.2 = fine, etc.)
     * @return Actual XP gained after all modifiers
     */
    public int addXp(CraftingCategory category, int baseXp, 
                     CraftingProficiency recipeProficiency, 
                     float qualityBonus, float toolBonus) {
        
        float multiplier = 1.0f;
        
        // Apply specialization bonus
        if (isSpecialized(category)) {
            multiplier += SPECIALIZATION_XP_BONUS;
        }
        
        // Apply penalty for crafting below current tier
        CraftingProficiency currentProficiency = getProficiency(category);
        if (recipeProficiency.ordinal() < currentProficiency.ordinal()) {
            multiplier *= BELOW_TIER_XP_PENALTY;
        }
        
        // Apply quality and tool bonuses
        multiplier *= qualityBonus;
        multiplier *= toolBonus;
        
        int xpGained = Math.round(baseXp * multiplier);
        
        int currentXp = getXp(category);
        categoryXp.put(category, currentXp + xpGained);
        
        return xpGained;
    }
    
    /**
     * Simple addXp with just base XP (uses default modifiers).
     */
    public int addXp(CraftingCategory category, int baseXp) {
        return addXp(category, baseXp, CraftingProficiency.NOVICE, 1.0f, 1.0f);
    }
    
    /**
     * Gets progress toward next proficiency level as a percentage (0.0 to 1.0).
     */
    public float getProgressToNextLevel(CraftingCategory category) {
        int xp = getXp(category);
        CraftingProficiency current = CraftingProficiency.fromXp(xp);
        CraftingProficiency next = current.next();
        
        if (next == null) {
            return 1.0f; // Already at max
        }
        
        int xpInCurrentTier = xp - current.getMinXp();
        int xpRequiredForTier = next.getMinXp() - current.getMinXp();
        
        return (float) xpInCurrentTier / xpRequiredForTier;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CraftingSkills{characterId='").append(characterId).append("', ");
        sb.append("specializations=").append(specializations).append(", ");
        sb.append("proficiencies={");
        
        boolean first = true;
        for (CraftingCategory category : CraftingCategory.values()) {
            if (!first) sb.append(", ");
            first = false;
            sb.append(category.getId()).append("=").append(getProficiency(category));
        }
        
        sb.append("}}");
        return sb.toString();
    }
}
