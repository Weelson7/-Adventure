package org.adventure.region;

import org.adventure.npc.NPCLifecycleManager;
import org.adventure.simulation.ClanExpansionSimulator;
import org.adventure.simulation.StructureLifecycleManager;
import org.adventure.simulation.QuestDynamicGenerator;
import org.adventure.settlement.VillageManager;
import org.adventure.world.Biome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the tick-driven simulation of regions in the game world.
 * Handles active vs background simulation with different tick rates.
 * 
 * <p>Phase 1.10.3: Integrated with dynamic world simulation systems:
 * <ul>
 *   <li>NPC lifecycle (aging, marriage, reproduction, death)</li>
 *   <li>Clan expansion (NPC-driven building, diplomacy, war, trade)</li>
 *   <li>Structure lifecycle (disasters, neglect, conversion to ruins)</li>
 *   <li>Quest generation (dynamic from world events)</li>
 *   <li>Village detection and management</li>
 * </ul>
 */
public class RegionSimulator {
    private final Map<Integer, Region> regions;
    private final double tickLength;
    private final double activeTickRateMultiplier;
    private final double backgroundTickRateMultiplier;
    private long currentTick;
    
    // Phase 1.10.3: Simulation managers
    private final NPCLifecycleManager npcLifecycleManager;
    private final ClanExpansionSimulator clanExpansionSimulator;
    private final StructureLifecycleManager structureLifecycleManager;
    private final QuestDynamicGenerator questDynamicGenerator;
    private final VillageManager villageManager;
    
    // World data (needed for placement/pathfinding)
    private Biome[][] biomes;
    private double[][] elevation;
    private int worldWidth;
    private int worldHeight;

    // Default configuration from specs_summary.md
    public static final double DEFAULT_TICK_LENGTH = 1.0; // 1 second
    public static final double DEFAULT_ACTIVE_MULTIPLIER = 1.0;
    public static final double DEFAULT_BACKGROUND_MULTIPLIER = 1.0 / 60.0;

    public RegionSimulator() {
        this(DEFAULT_TICK_LENGTH, DEFAULT_ACTIVE_MULTIPLIER, DEFAULT_BACKGROUND_MULTIPLIER);
    }

    public RegionSimulator(double tickLength, double activeMultiplier, double backgroundMultiplier) {
        this.regions = new HashMap<>();
        this.tickLength = tickLength;
        this.activeTickRateMultiplier = activeMultiplier;
        this.backgroundTickRateMultiplier = backgroundMultiplier;
        this.currentTick = 0;
        
        // Phase 1.10.3: Initialize simulation managers
        this.npcLifecycleManager = new NPCLifecycleManager();
        this.clanExpansionSimulator = new ClanExpansionSimulator();
        this.structureLifecycleManager = new StructureLifecycleManager();
        this.questDynamicGenerator = new QuestDynamicGenerator();
        this.villageManager = new VillageManager();
    }
    
    /**
     * Set world data for simulation (needed for placement rules and pathfinding).
     * 
     * @param biomes World biome map
     * @param elevation World elevation map
     * @param worldWidth World width in tiles
     * @param worldHeight World height in tiles
     */
    public void setWorldData(Biome[][] biomes, double[][] elevation, int worldWidth, int worldHeight) {
        this.biomes = biomes;
        this.elevation = elevation;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        
        // Initialize clan expansion simulator with elevation data
        this.clanExpansionSimulator.setWorldData(elevation);
    }

    /**
     * Add a region to the simulator.
     */
    public void addRegion(Region region) {
        regions.put(region.getId(), region);
    }

    /**
     * Get a region by ID.
     */
    public Region getRegion(int id) {
        return regions.get(id);
    }

    /**
     * Get all regions.
     */
    public List<Region> getAllRegions() {
        return new ArrayList<>(regions.values());
    }

    /**
     * Activate a region (switch to full simulation).
     */
    public void activateRegion(int regionId) {
        Region region = regions.get(regionId);
        if (region != null && region.getState() == Region.RegionState.BACKGROUND) {
            region.setState(Region.RegionState.ACTIVE);
            // Resynchronize: apply accumulated changes from lastProcessedTick to currentTick
            resynchronizeRegion(region);
        }
    }

    /**
     * Deactivate a region (switch to simplified simulation).
     */
    public void deactivateRegion(int regionId) {
        Region region = regions.get(regionId);
        if (region != null && region.getState() == Region.RegionState.ACTIVE) {
            region.setState(Region.RegionState.BACKGROUND);
            region.setLastProcessedTick(currentTick);
        }
    }

    /**
     * Resynchronize a region when it becomes active.
     * Apply accumulated resource regeneration from lastProcessedTick to currentTick.
     */
    private void resynchronizeRegion(Region region) {
        long ticksElapsed = currentTick - region.getLastProcessedTick();
        if (ticksElapsed > 0) {
            // Apply background tick rate multiplier for elapsed time
            double deltaTime = ticksElapsed * tickLength * backgroundTickRateMultiplier;
            region.regenerateResources(currentTick, deltaTime);
        }
        region.setLastProcessedTick(currentTick);
    }

    /**
     * Process one simulation tick for all regions.
     * Active regions use activeTickRateMultiplier, background regions use backgroundTickRateMultiplier.
     */
    public void tick() {
        currentTick++;

        for (Region region : regions.values()) {
            if (region.getState() == Region.RegionState.ACTIVE) {
                // Full simulation for active regions
                processActiveRegion(region);
            } else {
                // Simplified simulation for background regions (every 60 ticks by default)
                processBackgroundRegion(region);
            }
        }
    }

    /**
     * Process an active region with full simulation.
     * 
     * <p>Phase 1.10.3: Integrated dynamic world simulation:
     * <ol>
     *   <li>Resource regeneration (existing)</li>
     *   <li>NPC lifecycle (aging, marriage, reproduction, death)</li>
     *   <li>Clan expansion (NPC-led only, player clans skip)</li>
     *   <li>Structure lifecycle (disasters, neglect, ruin conversion)</li>
     *   <li>Dynamic quest generation (from world events)</li>
     *   <li>Village detection and promotion</li>
     * </ol>
     */
    private void processActiveRegion(Region region) {
        double deltaTime = tickLength * activeTickRateMultiplier;
        region.regenerateResources(currentTick, deltaTime);
        
        // Phase 1.10.3: Dynamic world simulation
        if (biomes != null && elevation != null) {
            // 1. NPC lifecycle (aging, marriage, reproduction, death)
            npcLifecycleManager.simulateTick(
                region.getNPCs(),
                region.getStructures(),
                currentTick
            );
            
            // 2. Clan expansion (NPC-led clans only)
            clanExpansionSimulator.simulateTick(
                region.getClans(),
                region.getNPCs(),
                region.getStructures(),
                region.getRoads(),
                biomes,
                elevation,
                worldWidth,
                worldHeight,
                currentTick
            );
            
            // 3. Structure lifecycle (disasters, neglect, ruins)
            structureLifecycleManager.simulateTick(
                region.getStructures(),
                region.getClans(),
                currentTick
            );
            
            // 4. Dynamic quest generation (from world events)
            var newQuests = questDynamicGenerator.generateQuestsFromEvents(
                region.getStructures(),
                region.getClans(),
                region.getStories(),
                currentTick
            );
            region.addQuests(newQuests);
            
            // 5. Village detection and management
            var villages = villageManager.detectVillages(region.getStructures());
            region.setVillages(villages);
        }
    }

    /**
     * Process a background region with simplified simulation.
     * Only process every N ticks based on backgroundTickRateMultiplier.
     */
    private void processBackgroundRegion(Region region) {
        // Calculate if this tick should process the background region
        long ticksSinceLastProcess = currentTick - region.getLastProcessedTick();
        double ticksPerBackgroundUpdate = 1.0 / backgroundTickRateMultiplier;

        if (ticksSinceLastProcess >= ticksPerBackgroundUpdate) {
            double deltaTime = ticksSinceLastProcess * tickLength * backgroundTickRateMultiplier;
            region.regenerateResources(currentTick, deltaTime);
        }
    }

    /**
     * Advance simulation by N ticks.
     */
    public void advanceTicks(int numTicks) {
        for (int i = 0; i < numTicks; i++) {
            tick();
        }
    }

    // Getters
    public long getCurrentTick() {
        return currentTick;
    }

    public double getTickLength() {
        return tickLength;
    }

    public double getActiveTickRateMultiplier() {
        return activeTickRateMultiplier;
    }

    public double getBackgroundTickRateMultiplier() {
        return backgroundTickRateMultiplier;
    }

    public int getRegionCount() {
        return regions.size();
    }

    public int getActiveRegionCount() {
        return (int) regions.values().stream()
                .filter(r -> r.getState() == Region.RegionState.ACTIVE)
                .count();
    }

    public int getBackgroundRegionCount() {
        return (int) regions.values().stream()
                .filter(r -> r.getState() == Region.RegionState.BACKGROUND)
                .count();
    }
}
