package org.adventure.crafting;

/**
 * Categories of crafting skills that can be specialized.
 * Characters can specialize in up to 2 categories (as per design docs).
 */
public enum CraftingCategory {
    SMITHING("smithing", "Weapons, armor, and metal items"),
    ALCHEMY("alchemy", "Potions, elixirs, and chemical compounds"),
    ENCHANTING("enchanting", "Magical enhancements and imbuing"),
    CARPENTRY("carpentry", "Furniture, wooden tools, and structures"),
    TAILORING("tailoring", "Clothing, armor, and cloth items"),
    COOKING("cooking", "Food preparation and recipes"),
    ENGINEERING("engineering", "Mechanical devices and tools"),
    JEWELCRAFTING("jewelcrafting", "Rings, amulets, and precious items");
    
    private final String id;
    private final String description;
    
    CraftingCategory(String id, String description) {
        this.id = id;
        this.description = description;
    }
    
    public String getId() {
        return id;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return id;
    }
}
