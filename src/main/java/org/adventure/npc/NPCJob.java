package org.adventure.npc;

import org.adventure.structure.StructureType;

/**
 * Represents the occupation/job of an NPC.
 * Each job may require a specific workplace structure and generates production value.
 */
public enum NPCJob {
    // Residential (no workplace required)
    /** Child under 15 years old, no job */
    CHILD(null, 0),
    
    /** Unemployed adult */
    UNEMPLOYED(null, 0),
    
    // Production - Note: No dedicated production structures yet, will be added in Phase 2
    /** Farmer (no dedicated workplace yet) */
    FARMER(null, 50),
    
    /** Blacksmith (no dedicated workplace yet) */
    BLACKSMITH(null, 100),
    
    /** Miner (no dedicated workplace yet) */
    MINER(null, 40),
    
    /** Lumberjack (no dedicated workplace yet) */
    LUMBERJACK(null, 30),
    
    // Commercial
    /** Merchant running a shop */
    MERCHANT(StructureType.SHOP, 80),
    
    /** Innkeeper running an inn */
    INNKEEPER(StructureType.INN, 60),
    
    // Military
    /** Warrior stationed at barracks */
    WARRIOR(StructureType.BARRACKS, 20),
    
    /** Guard stationed at a watchtower */
    GUARD(StructureType.WATCHTOWER, 30),
    
    // Special
    /** Priest serving at a temple */
    PRIEST(StructureType.TEMPLE, 70),
    
    /** Wizard residing in a wizard tower */
    WIZARD(StructureType.WIZARD_TOWER, 90),
    
    /** Guild master leading a guild hall */
    GUILD_MASTER(StructureType.GUILD_HALL, 100);
    
    private final StructureType requiredWorkplace;
    private final int productionValue;
    
    /**
     * Creates an NPC job with workplace requirement and production value.
     * 
     * @param workplace The structure type required for this job (null if no workplace needed)
     * @param production Gold produced per 1000 ticks
     */
    NPCJob(StructureType workplace, int production) {
        this.requiredWorkplace = workplace;
        this.productionValue = production;
    }
    
    /**
     * Gets the required workplace structure type for this job.
     * 
     * @return Structure type required, or null if no workplace needed
     */
    public StructureType getRequiredWorkplace() {
        return requiredWorkplace;
    }
    
    /**
     * Gets the production value in gold per 1000 ticks.
     * 
     * @return Gold production rate
     */
    public int getProductionValue() {
        return productionValue;
    }
    
    /**
     * Checks if this job requires a workplace structure.
     * 
     * @return true if workplace required, false otherwise
     */
    public boolean requiresWorkplace() {
        return requiredWorkplace != null;
    }
}
