package org.adventure.crafting;

/**
 * Quality levels for crafted items.
 * Higher quality items grant more XP and have better properties.
 */
public enum CraftingQuality {
    FLAWED("flawed", 1.0f, 0.7f),           // Failed craft but got something
    STANDARD("standard", 1.2f, 1.0f),        // Normal successful craft
    HIGH_QUALITY("high_quality", 1.5f, 1.0f), // Better than normal
    MASTERWORK("masterwork", 2.0f, 1.1f);    // Exceptional craft
    
    private final String id;
    private final float xpMultiplier;
    private final float durabilityMultiplier;
    
    CraftingQuality(String id, float xpMultiplier, float durabilityMultiplier) {
        this.id = id;
        this.xpMultiplier = xpMultiplier;
        this.durabilityMultiplier = durabilityMultiplier;
    }
    
    public String getId() {
        return id;
    }
    
    public float getXpMultiplier() {
        return xpMultiplier;
    }
    
    public float getDurabilityMultiplier() {
        return durabilityMultiplier;
    }
    
    @Override
    public String toString() {
        return id;
    }
}
