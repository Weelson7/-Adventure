package org.adventure.structure;

/**
 * Types of errors that can occur during structure placement validation.
 * Used by StructurePlacementRules to provide detailed error messages.
 * 
 * Design: BUILD_PHASE1.10.x.md → Phase 1.10.2
 */
public enum PlacementErrorType {
    /**
     * Structure is too close to another structure.
     * Minimum spacing: 5 tiles from center to center.
     */
    TOO_CLOSE_TO_STRUCTURE,
    
    /**
     * Structure would block another structure's entrance.
     * Entrance tiles must remain clear (can be road, not structure).
     */
    BLOCKING_ENTRANCE,
    
    /**
     * Structure cannot be placed on an existing road tile.
     * Exception: entrance can face/touch road.
     */
    ON_ROAD,
    
    /**
     * Terrain is unsuitable for building.
     * Reasons: elevation too high (> 0.7), in water (< 0.2 unless special).
     */
    UNSUITABLE_TERRAIN,
    
    /**
     * Structure position is outside world bounds.
     */
    OUT_OF_BOUNDS
}
