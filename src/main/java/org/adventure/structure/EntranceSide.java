package org.adventure.structure;

/**
 * Represents the side of a structure where the entrance is located.
 * Used for placement validation and road generation.
 * The entrance tile must be clear (can be road, cannot be another structure).
 * 
 * Design: BUILD_PHASE1.10.x.md → Phase 1.10.2
 */
public enum EntranceSide {
    /**
     * Entrance on north side (top).
     * Entrance tile is at (x, y - 1) relative to structure center.
     */
    NORTH,
    
    /**
     * Entrance on east side (right).
     * Entrance tile is at (x + 1, y) relative to structure center.
     */
    EAST,
    
    /**
     * Entrance on south side (bottom).
     * Entrance tile is at (x, y + 1) relative to structure center.
     */
    SOUTH,
    
    /**
     * Entrance on west side (left).
     * Entrance tile is at (x - 1, y) relative to structure center.
     */
    WEST;
    
    /**
     * Get the offset coordinates for this entrance side.
     * 
     * @return [dx, dy] offset from structure center
     */
    public int[] getOffset() {
        switch (this) {
            case NORTH: return new int[]{0, -1};
            case EAST: return new int[]{1, 0};
            case SOUTH: return new int[]{0, 1};
            case WEST: return new int[]{-1, 0};
            default: throw new IllegalStateException("Unknown entrance side: " + this);
        }
    }
    
    /**
     * Get entrance coordinates given structure position.
     * 
     * @param structureX structure X coordinate
     * @param structureY structure Y coordinate
     * @return [entranceX, entranceY] coordinates
     */
    public int[] getEntranceCoords(int structureX, int structureY) {
        int[] offset = getOffset();
        return new int[]{structureX + offset[0], structureY + offset[1]};
    }
}
