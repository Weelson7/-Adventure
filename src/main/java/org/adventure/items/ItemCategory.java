package org.adventure.items;

/**
 * Categories for all item types in the game.
 * Determines how items behave and which systems interact with them.
 */
public enum ItemCategory {
    // Combat items
    WEAPON("weapon", "Weapons for combat"),
    ARMOR("armor", "Protective armor pieces"),
    SHIELD("shield", "Shields for defense"),
    
    // Tools and equipment
    TOOL("tool", "Tools for harvesting and crafting"),
    INSTRUMENT("instrument", "Musical and measuring instruments"),
    
    // Consumables
    CONSUMABLE("consumable", "Single-use or consumable items"),
    FOOD("food", "Food items for sustenance"),
    POTION("potion", "Magical potions and elixirs"),
    
    // Magic
    MAGIC_ITEM("magic_item", "Enchanted items with magical properties"),
    SPELL_FOCUS("spell_focus", "Focuses for casting spells"),
    RUNE("rune", "Magical runes for spellcrafting"),
    
    // Resources and materials
    MATERIAL("material", "Raw materials for crafting"),
    GEM("gem", "Precious gems and crystals"),
    
    // Storage and containers
    CONTAINER("container", "Bags, chests, and storage items"),
    
    // Knowledge items
    BOOK("book", "Books, manuals, and scrolls"),
    MAP("map", "Maps and navigation aids"),
    
    // Decorative and furniture
    FURNITURE("furniture", "Furniture and home items"),
    DECORATION("decoration", "Decorative items"),
    
    // Special items
    KEY("key", "Keys for locks"),
    ARTIFACT("artifact", "Legendary artifacts"),
    QUEST_ITEM("quest_item", "Special quest-related items"),
    
    // Misc
    MISC("misc", "Miscellaneous items");
    
    private final String id;
    private final String description;
    
    ItemCategory(String id, String description) {
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
