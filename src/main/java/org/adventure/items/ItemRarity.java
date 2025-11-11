package org.adventure.items;

/**
 * Item rarity affects value, crafting difficulty, and drop rates.
 * Rarer items have multipliers applied to base values and XP gains.
 */
public enum ItemRarity {
    COMMON("common", 1.0f, 1.0f),
    UNCOMMON("uncommon", 1.5f, 1.5f),
    RARE("rare", 2.0f, 2.0f),
    EPIC("epic", 2.5f, 2.5f),
    LEGENDARY("legendary", 3.0f, 3.0f),
    ARTIFACT("artifact", 5.0f, 5.0f);
    
    private final String id;
    private final float valueMultiplier;
    private final float xpMultiplier;
    
    ItemRarity(String id, float valueMultiplier, float xpMultiplier) {
        this.id = id;
        this.valueMultiplier = valueMultiplier;
        this.xpMultiplier = xpMultiplier;
    }
    
    public String getId() {
        return id;
    }
    
    public float getValueMultiplier() {
        return valueMultiplier;
    }
    
    public float getXpMultiplier() {
        return xpMultiplier;
    }
    
    @Override
    public String toString() {
        return id;
    }
}
