package org.adventure.structure;

/**
 * Enumeration of all structure types in the game.
 * Organized by primary function and upgrade paths.
 * 
 * @see Structure
 */
public enum StructureType {
    // Residential Structures
    HOUSE("Residential", "Basic dwelling for individuals or small families"),
    MANOR("Residential", "Large estate with multiple rooms"),
    APARTMENT("Residential", "Multi-unit residential building"),
    CASTLE("Residential", "Fortified noble residence with defensive capabilities"),
    
    // Military Structures
    BARRACKS("Military", "Housing for soldiers and guards"),
    FORTRESS("Military", "Heavily fortified military stronghold"),
    WATCHTOWER("Military", "Defensive tower for surveillance"),
    ARMORY("Military", "Storage and maintenance facility for weapons"),
    
    // Commercial Structures
    SHOP("Commercial", "Small retail establishment"),
    MARKET("Commercial", "Large trading area with multiple vendors"),
    WAREHOUSE("Commercial", "Storage facility for goods"),
    INN("Commercial", "Lodging and dining establishment"),
    
    // Magical Structures
    WIZARD_TOWER("Magical", "Tall spire for magical study and practice"),
    ENCHANTED_LIBRARY("Magical", "Repository of magical knowledge"),
    RITUAL_CHAMBER("Magical", "Dedicated space for magical rituals"),
    
    // Ruins & Dungeons
    ANCIENT_RUINS("Ruins", "Remnants of ancient civilization"),
    CRYPT("Ruins", "Underground burial chamber"),
    LABYRINTH("Ruins", "Complex maze-like dungeon"),
    SUBMERGED_CITY("Ruins", "Underwater ruins of lost civilization"),
    
    // Special Structures
    TEMPLE("Special", "Religious or spiritual structure"),
    GUILD_HALL("Special", "Headquarters for guilds or organizations"),
    TRAINING_CENTER("Special", "Facility for skill development"),
    LEGENDARY_STRUCTURE("Special", "Unique structure with story significance");
    
    private final String category;
    private final String description;
    
    StructureType(String category, String description) {
        this.category = category;
        this.description = description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if this structure type is residential.
     */
    public boolean isResidential() {
        return "Residential".equals(category);
    }
    
    /**
     * Check if this structure type is military.
     */
    public boolean isMilitary() {
        return "Military".equals(category);
    }
    
    /**
     * Check if this structure type is commercial.
     */
    public boolean isCommercial() {
        return "Commercial".equals(category);
    }
    
    /**
     * Check if this structure type is magical.
     */
    public boolean isMagical() {
        return "Magical".equals(category);
    }
    
    /**
     * Check if this structure type is ruins/dungeon.
     */
    public boolean isRuins() {
        return "Ruins".equals(category);
    }
    
    /**
     * Check if this structure type is special.
     */
    public boolean isSpecial() {
        return "Special".equals(category);
    }
}
