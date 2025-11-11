package org.adventure.region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the tick-driven simulation of regions in the game world.
 * Handles active vs background simulation with different tick rates.
 */
public class RegionSimulator {
    private final Map<Integer, Region> regions;
    private final double tickLength;
    private final double activeTickRateMultiplier;
    private final double backgroundTickRateMultiplier;
    private long currentTick;

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
     */
    private void processActiveRegion(Region region) {
        double deltaTime = tickLength * activeTickRateMultiplier;
        region.regenerateResources(currentTick, deltaTime);
        // TODO: Process NPCs, events, structures (Phase 1.3+)
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
