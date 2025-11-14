package org.adventure.settlement;

/**
 * Represents the type/size classification of a settlement.
 */
public enum VillageType {
    /** Small settlement with 3-14 structures */
    VILLAGE,
    
    /** Medium settlement with 15-29 structures or has market */
    TOWN,
    
    /** Large settlement with 30+ structures or 20+ structures + 50+ NPCs + special building */
    CITY
}
