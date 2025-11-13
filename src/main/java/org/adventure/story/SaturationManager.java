package org.adventure.story;

import java.util.*;

/**
 * SaturationManager tracks and enforces per-region caps for stories and events.
 * Implements saturation controls with effective probability reduction.
 */
public class SaturationManager {
    // Default caps from specs_summary.md
    private static final int DEFAULT_MAX_STORIES_PER_REGION = 50;
    private static final int DEFAULT_MAX_EVENTS_PER_REGION = 20;
    private static final double SOFT_CAP_THRESHOLD = 0.8; // 80% of max triggers reduction

    private final int maxStoriesPerRegion;
    private final int maxEventsPerRegion;
    
    // Track counts per region and type
    private final Map<Integer, Map<StoryType, Integer>> storyCountsByRegion;
    private final Map<Integer, Map<EventCategory, Integer>> eventCountsByRegion;
    private final Map<Integer, Integer> totalStoriesPerRegion;
    private final Map<Integer, Integer> totalEventsPerRegion;

    /**
     * Create a SaturationManager with default caps.
     */
    public SaturationManager() {
        this(DEFAULT_MAX_STORIES_PER_REGION, DEFAULT_MAX_EVENTS_PER_REGION);
    }

    /**
     * Create a SaturationManager with custom caps.
     *
     * @param maxStoriesPerRegion Maximum active stories per region
     * @param maxEventsPerRegion Maximum active events per region
     */
    public SaturationManager(int maxStoriesPerRegion, int maxEventsPerRegion) {
        this.maxStoriesPerRegion = maxStoriesPerRegion;
        this.maxEventsPerRegion = maxEventsPerRegion;
        this.storyCountsByRegion = new HashMap<>();
        this.eventCountsByRegion = new HashMap<>();
        this.totalStoriesPerRegion = new HashMap<>();
        this.totalEventsPerRegion = new HashMap<>();
    }

    /**
     * Register a story in a region.
     *
     * @param regionId Region tile ID
     * @param storyType Type of story
     */
    public void registerStory(int regionId, StoryType storyType) {
        storyCountsByRegion.putIfAbsent(regionId, new HashMap<>());
        Map<StoryType, Integer> typeCounts = storyCountsByRegion.get(regionId);
        typeCounts.put(storyType, typeCounts.getOrDefault(storyType, 0) + 1);
        
        totalStoriesPerRegion.put(regionId, totalStoriesPerRegion.getOrDefault(regionId, 0) + 1);
    }

    /**
     * Register an event in a region.
     *
     * @param regionId Region tile ID
     * @param category Event category
     */
    public void registerEvent(int regionId, EventCategory category) {
        eventCountsByRegion.putIfAbsent(regionId, new HashMap<>());
        Map<EventCategory, Integer> categoryCounts = eventCountsByRegion.get(regionId);
        categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
        
        totalEventsPerRegion.put(regionId, totalEventsPerRegion.getOrDefault(regionId, 0) + 1);
    }

    /**
     * Unregister a story from a region (when resolved, archived, or discredited).
     *
     * @param regionId Region tile ID
     * @param storyType Type of story
     */
    public void unregisterStory(int regionId, StoryType storyType) {
        if (storyCountsByRegion.containsKey(regionId)) {
            Map<StoryType, Integer> typeCounts = storyCountsByRegion.get(regionId);
            if (typeCounts.containsKey(storyType)) {
                int count = typeCounts.get(storyType);
                if (count <= 1) {
                    typeCounts.remove(storyType);
                } else {
                    typeCounts.put(storyType, count - 1);
                }
                
                int total = totalStoriesPerRegion.getOrDefault(regionId, 0);
                totalStoriesPerRegion.put(regionId, Math.max(0, total - 1));
            }
        }
    }

    /**
     * Unregister an event from a region (when completed or cancelled).
     *
     * @param regionId Region tile ID
     * @param category Event category
     */
    public void unregisterEvent(int regionId, EventCategory category) {
        if (eventCountsByRegion.containsKey(regionId)) {
            Map<EventCategory, Integer> categoryCounts = eventCountsByRegion.get(regionId);
            if (categoryCounts.containsKey(category)) {
                int count = categoryCounts.get(category);
                if (count <= 1) {
                    categoryCounts.remove(category);
                } else {
                    categoryCounts.put(category, count - 1);
                }
                
                int total = totalEventsPerRegion.getOrDefault(regionId, 0);
                totalEventsPerRegion.put(regionId, Math.max(0, total - 1));
            }
        }
    }

    /**
     * Get saturation factor for events in a region.
     * Formula: max(0, 1 - (currentCount / maxCap))
     *
     * @param regionId Region tile ID
     * @param category Event category
     * @return Saturation factor [0.0, 1.0]
     */
    public double getSaturationFactor(int regionId, EventCategory category) {
        int currentCount = totalEventsPerRegion.getOrDefault(regionId, 0);
        return calculateSaturationFactor(currentCount, maxEventsPerRegion);
    }

    /**
     * Get saturation factor for stories in a region.
     * Formula: max(0, 1 - (currentCount / maxCap))
     *
     * @param regionId Region tile ID
     * @param storyType Story type
     * @return Saturation factor [0.0, 1.0]
     */
    public double getStorySaturationFactor(int regionId, StoryType storyType) {
        int currentCount = totalStoriesPerRegion.getOrDefault(regionId, 0);
        return calculateSaturationFactor(currentCount, maxStoriesPerRegion);
    }

    /**
     * Calculate saturation factor based on current count and max cap.
     * Formula: max(0, 1 - (currentCount / maxCap))
     *
     * @param currentCount Current number of items
     * @param maxCap Maximum allowed items
     * @return Saturation factor [0.0, 1.0]
     */
    private double calculateSaturationFactor(int currentCount, int maxCap) {
        if (maxCap <= 0) {
            return 0.0;
        }
        return Math.max(0.0, 1.0 - ((double) currentCount / maxCap));
    }

    /**
     * Check if a region is at or above the soft cap for stories.
     *
     * @param regionId Region tile ID
     * @return True if at or above soft cap (80% of max)
     */
    public boolean isStoryCapReached(int regionId) {
        int currentCount = totalStoriesPerRegion.getOrDefault(regionId, 0);
        return currentCount >= (maxStoriesPerRegion * SOFT_CAP_THRESHOLD);
    }

    /**
     * Check if a region is at or above the soft cap for events.
     *
     * @param regionId Region tile ID
     * @return True if at or above soft cap (80% of max)
     */
    public boolean isEventCapReached(int regionId) {
        int currentCount = totalEventsPerRegion.getOrDefault(regionId, 0);
        return currentCount >= (maxEventsPerRegion * SOFT_CAP_THRESHOLD);
    }

    /**
     * Get current story count in a region.
     *
     * @param regionId Region tile ID
     * @return Current number of active stories
     */
    public int getStoryCount(int regionId) {
        return totalStoriesPerRegion.getOrDefault(regionId, 0);
    }

    /**
     * Get current event count in a region.
     *
     * @param regionId Region tile ID
     * @return Current number of active events
     */
    public int getEventCount(int regionId) {
        return totalEventsPerRegion.getOrDefault(regionId, 0);
    }

    /**
     * Get count of specific story type in a region.
     *
     * @param regionId Region tile ID
     * @param storyType Type of story
     * @return Count of that story type
     */
    public int getStoryTypeCount(int regionId, StoryType storyType) {
        if (!storyCountsByRegion.containsKey(regionId)) {
            return 0;
        }
        return storyCountsByRegion.get(regionId).getOrDefault(storyType, 0);
    }

    /**
     * Get count of specific event category in a region.
     *
     * @param regionId Region tile ID
     * @param category Event category
     * @return Count of that event category
     */
    public int getEventCategoryCount(int regionId, EventCategory category) {
        if (!eventCountsByRegion.containsKey(regionId)) {
            return 0;
        }
        return eventCountsByRegion.get(regionId).getOrDefault(category, 0);
    }

    /**
     * Reset all counts (for testing or world reset).
     */
    public void reset() {
        storyCountsByRegion.clear();
        eventCountsByRegion.clear();
        totalStoriesPerRegion.clear();
        totalEventsPerRegion.clear();
    }
}
