package org.adventure.structure;

/**
 * Enumeration of room categories within structures.
 * Each category provides different gameplay effects.
 * 
 * @see Room
 */
public enum RoomCategory {
    /** Living quarters for residents */
    LIVING_QUARTERS("Provides rest and stat recovery"),
    
    /** Storage for items and resources */
    STORAGE("Increases inventory capacity"),
    
    /** Training facilities for skill development */
    TRAINING("Provides skill XP bonuses"),
    
    /** Crafting workshops */
    CRAFTING("Enables crafting and provides proficiency bonuses"),
    
    /** Magical chambers for spells and rituals */
    MAGICAL("Mana regeneration and spell research"),
    
    /** Treasury for wealth storage */
    TREASURY("Secure storage for valuables"),
    
    /** Defensive structures */
    DEFENSIVE("Provides protection and military bonuses"),
    
    /** Kitchen and dining areas */
    DINING("Provides food buffs and social bonuses"),
    
    /** Library or study */
    LIBRARY("Provides intelligence and wisdom bonuses"),
    
    /** Throne room or meeting hall */
    HALL("Enables clan meetings and diplomacy");
    
    private final String effect;
    
    RoomCategory(String effect) {
        this.effect = effect;
    }
    
    public String getEffect() {
        return effect;
    }
}
