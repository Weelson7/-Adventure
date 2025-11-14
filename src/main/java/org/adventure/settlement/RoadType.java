package org.adventure.settlement;

/**
 * Types of roads that can be built/upgraded.
 * Roads can be upgraded as traffic increases:
 * DIRT (default) → STONE (traffic >= 50) → PAVED (traffic >= 80)
 * 
 * Design: BUILD_PHASE1.10.x.md → Phase 1.10.2
 */
public enum RoadType {
    /**
     * Basic dirt road, created by default.
     * Can be upgraded to STONE when traffic >= 50.
     */
    DIRT,
    
    /**
     * Stone-paved road, upgraded from DIRT.
     * Can be upgraded to PAVED when traffic >= 80.
     */
    STONE,
    
    /**
     * Fully paved road, highest tier.
     * No further upgrades available.
     */
    PAVED
}
