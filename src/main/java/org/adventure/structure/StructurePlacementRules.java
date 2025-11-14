package org.adventure.structure;

import org.adventure.settlement.RoadTile;
import java.util.*;

/**
 * Validates structure placement according to game rules.
 * 
 * Placement rules:
 * 1. Minimum 5-tile spacing between structures (center to center)
 * 2. Entrance clearance: 1 tile in front must be clear or road
 * 3. No building on road tiles (entrance can touch)
 * 4. Terrain validation: elevation < 0.7 (not mountains), elevation > 0.2 unless special (not water)
 * 5. Bounds checking: within world dimensions
 * 
 * Note: This implementation uses a simplified terrain model for Phase 1.10.2.
 * Full terrain integration will be added when Tile class is implemented.
 * 
 * Design: BUILD_PHASE1.10.x.md → Phase 1.10.2
 */
public class StructurePlacementRules {
    private static final int MIN_STRUCTURE_SPACING = 5;
    private static final double WATER_THRESHOLD = 0.2;
    private static final double MOUNTAIN_THRESHOLD = 0.7;
    
    private final double[][] elevationMap;
    private final int worldWidth;
    private final int worldHeight;
    
    public StructurePlacementRules(double[][] elevationMap) {
        this.elevationMap = elevationMap;
        this.worldHeight = elevationMap.length;
        this.worldWidth = elevationMap[0].length;
    }
    
    /**
     * Check if structure can be placed at location.
     * 
     * @param x Structure X coordinate
     * @param y Structure Y coordinate
     * @param type Structure type
     * @param entrance Entrance side
     * @param existingStructures All existing structures
     * @param roadTiles All existing road tiles
     * @return true if placement is valid
     */
    public boolean canPlaceStructure(int x, int y, StructureType type, EntranceSide entrance,
                                     List<Structure> existingStructures, 
                                     Map<String, RoadTile> roadTiles) {
        List<PlacementError> errors = validatePlacement(x, y, type, entrance, 
                existingStructures, roadTiles);
        return errors.isEmpty();
    }
    
    /**
     * Validate structure placement and return all errors.
     * 
     * @param x Structure X coordinate
     * @param y Structure Y coordinate
     * @param type Structure type
     * @param entrance Entrance side
     * @param existingStructures All existing structures
     * @param roadTiles All existing road tiles (key: "x_y")
     * @return List of placement errors (empty if valid)
     */
    public List<PlacementError> validatePlacement(int x, int y, StructureType type, 
                                                  EntranceSide entrance,
                                                  List<Structure> existingStructures,
                                                  Map<String, RoadTile> roadTiles) {
        List<PlacementError> errors = new ArrayList<>();
        
        if (entrance == null) {
            entrance = EntranceSide.SOUTH;
        }
        
        // Rule 1: Bounds checking
        if (!isWithinBounds(x, y)) {
            errors.add(new PlacementError(
                    PlacementErrorType.OUT_OF_BOUNDS,
                    String.format("Structure at (%d, %d) is out of world bounds (%d x %d)",
                            x, y, worldWidth, worldHeight)));
            return errors; // Fatal error, no need to check further
        }
        
        // Rule 2: Terrain validation
        double elevation = elevationMap[y][x];
        
        // Check for mountains (too steep)
        if (elevation > MOUNTAIN_THRESHOLD) {
            errors.add(new PlacementError(
                    PlacementErrorType.UNSUITABLE_TERRAIN,
                    String.format("Structure at (%d, %d) on mountain terrain (elevation %.2f > %.2f)",
                            x, y, elevation, MOUNTAIN_THRESHOLD)));
        }
        
        // Check for water (unless special water structure)
        if (elevation < WATER_THRESHOLD && !isWaterStructure(type)) {
            errors.add(new PlacementError(
                    PlacementErrorType.UNSUITABLE_TERRAIN,
                    String.format("Structure at (%d, %d) in water (elevation %.2f < %.2f)",
                            x, y, elevation, WATER_THRESHOLD)));
        }
        
        // Rule 3: Check road tiles (structure can't be on road, but entrance can touch)
        String locationKey = x + "_" + y;
        if (roadTiles != null && roadTiles.containsKey(locationKey)) {
            errors.add(new PlacementError(
                    PlacementErrorType.ON_ROAD,
                    String.format("Structure at (%d, %d) would be placed on existing road",
                            x, y)));
        }
        
        // Rule 4: Minimum spacing from other structures
        if (existingStructures != null) {
            for (Structure existing : existingStructures) {
                int existingX = parseX(existing.getLocationTileId());
                int existingY = parseY(existing.getLocationTileId());
                
                double distance = Math.sqrt(
                        Math.pow(x - existingX, 2) + 
                        Math.pow(y - existingY, 2));
                
                if (distance < MIN_STRUCTURE_SPACING) {
                    errors.add(new PlacementError(
                            PlacementErrorType.TOO_CLOSE_TO_STRUCTURE,
                            String.format("Structure at (%d, %d) too close to existing structure '%s' " +
                                        "at (%d, %d) - distance %.1f < %d tiles",
                                    x, y, existing.getId(), existingX, existingY, 
                                    distance, MIN_STRUCTURE_SPACING)));
                }
            }
        }
        
        // Rule 5: Entrance clearance (1 tile in front must be clear or road)
        int[] entranceCoords = entrance.getEntranceCoords(x, y);
        int entranceX = entranceCoords[0];
        int entranceY = entranceCoords[1];
        
        if (isWithinBounds(entranceX, entranceY)) {
            String entranceKey = entranceX + "_" + entranceY;
            boolean isRoad = roadTiles != null && roadTiles.containsKey(entranceKey);
            boolean isBlocked = false;
            
            if (!isRoad && existingStructures != null) {
                // Check if entrance blocked by structure
                for (Structure existing : existingStructures) {
                    int existingX = parseX(existing.getLocationTileId());
                    int existingY = parseY(existing.getLocationTileId());
                    
                    if (existingX == entranceX && existingY == entranceY) {
                        isBlocked = true;
                        break;
                    }
                }
            }
            
            if (isBlocked) {
                errors.add(new PlacementError(
                        PlacementErrorType.BLOCKING_ENTRANCE,
                        String.format("Structure at (%d, %d) has entrance at (%d, %d) blocked " +
                                    "by existing structure",
                                x, y, entranceX, entranceY)));
            }
            
            // Also check entrance terrain
            double entranceElevation = elevationMap[entranceY][entranceX];
            if (entranceElevation < WATER_THRESHOLD) {
                errors.add(new PlacementError(
                        PlacementErrorType.BLOCKING_ENTRANCE,
                        String.format("Structure at (%d, %d) has entrance at (%d, %d) in water " +
                                    "(elevation %.2f < %.2f)",
                                x, y, entranceX, entranceY, entranceElevation, WATER_THRESHOLD)));
            }
            if (entranceElevation > MOUNTAIN_THRESHOLD) {
                errors.add(new PlacementError(
                        PlacementErrorType.BLOCKING_ENTRANCE,
                        String.format("Structure at (%d, %d) has entrance at (%d, %d) blocked " +
                                    "by mountain (elevation %.2f > %.2f)",
                                x, y, entranceX, entranceY, entranceElevation, MOUNTAIN_THRESHOLD)));
            }
        }
        
        return errors;
    }
    
    /**
     * Validate entrance clearance only.
     * 
     * @param x Structure X coordinate
     * @param y Structure Y coordinate
     * @param entrance Entrance side
     * @param roadTiles All road tiles
     * @return true if entrance is clear or connects to road
     */
    public boolean isEntranceClear(int x, int y, EntranceSide entrance, 
                                   Map<String, RoadTile> roadTiles) {
        if (entrance == null) {
            entrance = EntranceSide.SOUTH;
        }
        
        int[] entranceCoords = entrance.getEntranceCoords(x, y);
        int entranceX = entranceCoords[0];
        int entranceY = entranceCoords[1];
        
        if (!isWithinBounds(entranceX, entranceY)) {
            return false;
        }
        
        // Check if entrance connects to road (preferred)
        String entranceKey = entranceX + "_" + entranceY;
        if (roadTiles != null && roadTiles.containsKey(entranceKey)) {
            return true;
        }
        
        // Check terrain at entrance
        double elevation = elevationMap[entranceY][entranceX];
        return elevation >= WATER_THRESHOLD && elevation <= MOUNTAIN_THRESHOLD;
    }
    
    // Helper methods
    
    private boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < worldWidth && y >= 0 && y < worldHeight;
    }
    
    private boolean isWaterStructure(StructureType type) {
        // Docks, fishing huts, etc. can be in water
        return type == StructureType.DOCK || type == StructureType.FISHING_HUT;
    }
    
    private int parseX(String locationTileId) {
        try {
            String[] parts = locationTileId.split("_");
            return Integer.parseInt(parts[0]);
        } catch (Exception e) {
            return 0;
        }
    }
    
    private int parseY(String locationTileId) {
        try {
            String[] parts = locationTileId.split("_");
            return Integer.parseInt(parts[1]);
        } catch (Exception e) {
            return 0;
        }
    }
}
