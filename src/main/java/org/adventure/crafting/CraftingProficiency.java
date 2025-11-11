package org.adventure.crafting;

/**
 * Crafting proficiency levels determine which recipes can be crafted
 * and affect success rates and XP gain.
 * 
 * Progression follows logarithmic scaling as defined in design docs.
 */
public enum CraftingProficiency {
    NOVICE(0, 99, "novice", 1.0f),
    APPRENTICE(100, 299, "apprentice", 0.9f),
    JOURNEYMAN(300, 599, "journeyman", 0.8f),
    EXPERT(600, 999, "expert", 0.7f),
    MASTER(1000, Integer.MAX_VALUE, "master", 0.6f);
    
    private final int minXp;
    private final int maxXp;
    private final String id;
    private final float failureMultiplier; // Higher proficiency = lower failure chance
    
    CraftingProficiency(int minXp, int maxXp, String id, float failureMultiplier) {
        this.minXp = minXp;
        this.maxXp = maxXp;
        this.id = id;
        this.failureMultiplier = failureMultiplier;
    }
    
    public int getMinXp() {
        return minXp;
    }
    
    public int getMaxXp() {
        return maxXp;
    }
    
    public String getId() {
        return id;
    }
    
    public float getFailureMultiplier() {
        return failureMultiplier;
    }
    
    /**
     * Gets the proficiency level for a given XP amount.
     */
    public static CraftingProficiency fromXp(int xp) {
        if (xp < APPRENTICE.minXp) return NOVICE;
        if (xp < JOURNEYMAN.minXp) return APPRENTICE;
        if (xp < EXPERT.minXp) return JOURNEYMAN;
        if (xp < MASTER.minXp) return EXPERT;
        return MASTER;
    }
    
    /**
     * Returns the next proficiency level, or null if already at max.
     */
    public CraftingProficiency next() {
        switch (this) {
            case NOVICE: return APPRENTICE;
            case APPRENTICE: return JOURNEYMAN;
            case JOURNEYMAN: return EXPERT;
            case EXPERT: return MASTER;
            case MASTER: return null;
            default: return null;
        }
    }
    
    /**
     * Gets XP required to reach the next level.
     * Returns 0 if already at max level.
     */
    public int getXpToNextLevel(int currentXp) {
        CraftingProficiency next = next();
        if (next == null) return 0;
        return Math.max(0, next.minXp - currentXp);
    }
    
    @Override
    public String toString() {
        return id;
    }
}
