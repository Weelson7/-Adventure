package org.adventure.region;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a geographical region in the game world.
 * Regions can be active (full simulation) or background (simplified simulation).
 */
public class Region {
    private final int id;
    private final int centerX;
    private final int centerY;
    private final int width;
    private final int height;
    private long lastProcessedTick;
    private RegionState state;
    private final List<ResourceNode> resourceNodes;
    private int npcCount;
    private int schemaVersion = 1;

    public enum RegionState {
        ACTIVE,      // Full simulation (players nearby)
        BACKGROUND   // Simplified simulation (no players)
    }

    public Region(int id, int centerX, int centerY, int width, int height) {
        this.id = id;
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.height = height;
        this.lastProcessedTick = 0;
        this.state = RegionState.BACKGROUND;
        this.resourceNodes = new ArrayList<>();
        this.npcCount = 0;
    }

    /**
     * Add a resource node to this region.
     */
    public void addResourceNode(ResourceNode node) {
        resourceNodes.add(node);
    }

    /**
     * Get all resource nodes in this region.
     */
    public List<ResourceNode> getResourceNodes() {
        return resourceNodes;
    }

    /**
     * Update resource nodes with regeneration.
     */
    public void regenerateResources(long currentTick, double deltaTime) {
        for (ResourceNode node : resourceNodes) {
            node.regenerate(deltaTime);
        }
        this.lastProcessedTick = currentTick;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public long getLastProcessedTick() {
        return lastProcessedTick;
    }

    public void setLastProcessedTick(long tick) {
        this.lastProcessedTick = tick;
    }

    public RegionState getState() {
        return state;
    }

    public void setState(RegionState state) {
        this.state = state;
    }

    public int getNpcCount() {
        return npcCount;
    }

    public void setNpcCount(int count) {
        this.npcCount = count;
    }

    /**
     * Check if a world coordinate is within this region.
     */
    public boolean contains(int x, int y) {
        int minX = centerX - width / 2;
        int maxX = centerX + width / 2;
        int minY = centerY - height / 2;
        int maxY = centerY + height / 2;
        return x >= minX && x < maxX && y >= minY && y < maxY;
    }
    
    public int getSchemaVersion() {
        return schemaVersion;
    }
}
