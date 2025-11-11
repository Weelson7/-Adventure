package org.adventure.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents a tectonic plate in the world generation system.
 * Plates define regions that move and collide to create elevation patterns.
 */
public class Plate {
    private final int id;
    private final int centerX;
    private final int centerY;
    private final double driftX;
    private final double driftY;
    private final PlateType type;
    private final List<TileCoord> tiles;

    public enum PlateType {
        OCEANIC,    // Lower base elevation, forms ocean basins
        CONTINENTAL // Higher base elevation, forms landmasses
    }

    public Plate(int id, int centerX, int centerY, double driftX, double driftY, PlateType type) {
        this.id = id;
        this.centerX = centerX;
        this.centerY = centerY;
        this.driftX = driftX;
        this.driftY = driftY;
        this.type = type;
        this.tiles = new ArrayList<>();
    }

    /**
     * Generate deterministic drift vector from seed.
     */
    public static Plate createRandomPlate(int id, int worldWidth, int worldHeight, long seed, Random rng) {
        int centerX = rng.nextInt(worldWidth);
        int centerY = rng.nextInt(worldHeight);
        
        // Drift vectors in range [-0.5, 0.5]
        double driftX = (rng.nextDouble() - 0.5);
        double driftY = (rng.nextDouble() - 0.5);
        
        // 70% continental, 30% oceanic (realistic distribution)
        PlateType type = rng.nextDouble() < 0.7 ? PlateType.CONTINENTAL : PlateType.OCEANIC;
        
        return new Plate(id, centerX, centerY, driftX, driftY, type);
    }

    public void addTile(int x, int y) {
        tiles.add(new TileCoord(x, y));
    }

    public int getId() {
        return id;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public double getDriftX() {
        return driftX;
    }

    public double getDriftY() {
        return driftY;
    }

    public PlateType getType() {
        return type;
    }

    public List<TileCoord> getTiles() {
        return tiles;
    }

    /**
     * Calculate collision intensity with another plate at boundary.
     */
    public double collisionIntensity(Plate other) {
        double dx = other.driftX - this.driftX;
        double dy = other.driftY - this.driftY;
        double relativeDrift = Math.sqrt(dx * dx + dy * dy);
        return (relativeDrift * relativeDrift) / 4.0;
    }

    /**
     * Check if plates are colliding (moving toward each other).
     */
    public boolean isColliding(Plate other) {
        // Dot product of drift vector with vector toward other plate
        double toOtherX = other.centerX - this.centerX;
        double toOtherY = other.centerY - this.centerY;
        double dotProduct = driftX * toOtherX + driftY * toOtherY;
        return dotProduct > 0;
    }

    /**
     * Simple tile coordinate holder.
     */
    public static class TileCoord {
        public final int x;
        public final int y;

        public TileCoord(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
