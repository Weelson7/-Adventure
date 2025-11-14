package org.adventure.region;

import org.adventure.npc.NamedNPC;
import org.adventure.society.Clan;
import org.adventure.structure.Structure;
import org.adventure.settlement.Village;
import org.adventure.settlement.RoadTile;
import org.adventure.quest.Quest;
import org.adventure.story.Story;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a geographical region in the game world.
 * Regions can be active (full simulation) or background (simplified simulation).
 * 
 * <p>Phase 1.10.3: Extended to support dynamic world simulation with NPCs, clans,
 * structures, villages, roads, quests, and stories.
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
    
    // Phase 1.10.3: Dynamic world state
    private final List<NamedNPC> npcs;
    private final List<Clan> clans;
    private final List<Structure> structures;
    private final List<Village> villages;
    private final List<RoadTile> roads;
    private final List<Quest> quests;
    private final List<Story> stories;

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
        this.npcs = new ArrayList<>();
        this.clans = new ArrayList<>();
        this.structures = new ArrayList<>();
        this.villages = new ArrayList<>();
        this.roads = new ArrayList<>();
        this.quests = new ArrayList<>();
        this.stories = new ArrayList<>();
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
    
    // Phase 1.10.3: Getters and setters for dynamic world state
    
    public List<NamedNPC> getNPCs() {
        return npcs;
    }
    
    public List<Clan> getClans() {
        return clans;
    }
    
    public List<Structure> getStructures() {
        return structures;
    }
    
    public List<Village> getVillages() {
        return villages;
    }
    
    public void setVillages(List<Village> villages) {
        this.villages.clear();
        this.villages.addAll(villages);
    }
    
    public List<RoadTile> getRoads() {
        return roads;
    }
    
    public List<Quest> getQuests() {
        return quests;
    }
    
    public void addQuests(List<Quest> newQuests) {
        this.quests.addAll(newQuests);
    }
    
    public List<Story> getStories() {
        return stories;
    }
    
    public List<Story> getRecentEvents() {
        // For now, return all active stories as "recent events"
        // In a full implementation, this would filter by tick proximity
        return new ArrayList<>(stories);
    }
}
