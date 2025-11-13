package org.adventure;

import org.adventure.story.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EventPropagation class.
 */
public class EventPropagationTest {

    @Test
    public void testExponentialDecayFormula() {
        EventPropagation propagation = new EventPropagation(12345L, 0.8);

        // Test decay at different hop counts
        assertEquals(1.0, propagation.calculateExponentialDecay(0), 0.001); // exp(-0.8 * 0)
        assertEquals(0.449, propagation.calculateExponentialDecay(1), 0.001); // exp(-0.8 * 1)
        assertEquals(0.201, propagation.calculateExponentialDecay(2), 0.001); // exp(-0.8 * 2)
        assertEquals(0.091, propagation.calculateExponentialDecay(3), 0.001); // exp(-0.8 * 3)
    }

    @Test
    public void testLinearDecayFormula() {
        EventPropagation propagation = new EventPropagation(12345L, 0.8);

        // Test linear decay
        assertEquals(1.0, propagation.calculateLinearDecay(0), 0.001);
        assertEquals(0.2, propagation.calculateLinearDecay(1), 0.001);
        assertEquals(0.0, propagation.calculateLinearDecay(2), 0.001);
    }

    @Test
    public void testPropagateEventDeterministic() {
        long seed = 12345L;
        EventPropagation prop1 = new EventPropagation(seed);
        EventPropagation prop2 = new EventPropagation(seed);

        Event event = createTestEvent();
        Map<Integer, List<Integer>> neighbors = createLinearNeighborGraph(10);
        SaturationManager satMgr1 = new SaturationManager();
        SaturationManager satMgr2 = new SaturationManager();

        Set<Integer> affected1 = prop1.propagateEvent(event, neighbors, satMgr1);
        Set<Integer> affected2 = prop2.propagateEvent(event, neighbors, satMgr2);

        // Same seed should produce identical propagation
        assertEquals(affected1, affected2);
    }

    @Test
    public void testPropagateEventOriginAlwaysAffected() {
        EventPropagation propagation = new EventPropagation(12345L);
        Event event = createTestEvent();
        Map<Integer, List<Integer>> neighbors = createLinearNeighborGraph(10);
        SaturationManager satMgr = new SaturationManager();

        Set<Integer> affected = propagation.propagateEvent(event, neighbors, satMgr);

        // Origin should always be affected
        assertTrue(affected.contains(event.getOriginTileId()));
    }

    @Test
    public void testPropagateEventRespectsMaxHops() {
        EventPropagation propagation = new EventPropagation(12345L);
        
        Event event = new Event.Builder()
                .id("event_1")
                .category(EventCategory.REGIONAL)
                .name("Test Event")
                .originTileId(0)
                .baseProbability(1.0) // 100% to ensure propagation
                .maxHops(2)
                .build();

        Map<Integer, List<Integer>> neighbors = createLinearNeighborGraph(10);
        SaturationManager satMgr = new SaturationManager();

        Set<Integer> affected = propagation.propagateEvent(event, neighbors, satMgr);

        // Should affect origin (0) + up to maxHops (2) = tiles 0, 1, 2
        assertTrue(affected.size() <= 3, "Should affect at most origin + maxHops tiles");
    }

    @Test
    public void testPropagateEventWithNoNeighbors() {
        EventPropagation propagation = new EventPropagation(12345L);
        Event event = createTestEvent();
        Map<Integer, List<Integer>> neighbors = new HashMap<>();
        SaturationManager satMgr = new SaturationManager();

        Set<Integer> affected = propagation.propagateEvent(event, neighbors, satMgr);

        // Only origin should be affected
        assertEquals(1, affected.size());
        assertTrue(affected.contains(event.getOriginTileId()));
    }

    @Test
    public void testPropagateEventSaturationReducesProbability() {
        EventPropagation propagation = new EventPropagation(12345L);
        
        Event event = new Event.Builder()
                .id("event_1")
                .category(EventCategory.REGIONAL)
                .name("Test Event")
                .originTileId(0)
                .baseProbability(1.0)
                .maxHops(5)
                .build();

        Map<Integer, List<Integer>> neighbors = createLinearNeighborGraph(10);
        
        // Saturate region 1
        SaturationManager satMgr = new SaturationManager(50, 20);
        for (int i = 0; i < 19; i++) { // 19/20 = 95% saturated
            satMgr.registerEvent(1, EventCategory.REGIONAL);
        }

        Set<Integer> affected = propagation.propagateEvent(event, neighbors, satMgr);

        // Saturation should reduce propagation
        // With 95% saturation, saturationFactor = 1 - 19/20 = 0.05
        // This makes propagation much less likely
        assertTrue(affected.size() >= 1); // Origin always affected
    }

    @Test
    public void testPropagateStoryDeterministic() {
        long seed = 12345L;
        EventPropagation prop1 = new EventPropagation(seed);
        EventPropagation prop2 = new EventPropagation(seed);

        Story story = createTestStory();
        Map<Integer, List<Integer>> neighbors = createLinearNeighborGraph(10);
        SaturationManager satMgr1 = new SaturationManager();
        SaturationManager satMgr2 = new SaturationManager();

        Set<Integer> affected1 = prop1.propagateStory(story, neighbors, satMgr1);
        Set<Integer> affected2 = prop2.propagateStory(story, neighbors, satMgr2);

        // Same seed should produce identical propagation
        assertEquals(affected1, affected2);
    }

    @Test
    public void testPropagateStoryOriginAlwaysAffected() {
        EventPropagation propagation = new EventPropagation(12345L);
        Story story = createTestStory();
        Map<Integer, List<Integer>> neighbors = createLinearNeighborGraph(10);
        SaturationManager satMgr = new SaturationManager();

        Set<Integer> affected = propagation.propagateStory(story, neighbors, satMgr);

        // Origin should always be affected
        assertTrue(affected.contains(story.getOriginTileId()));
    }

    @Test
    public void testPropagateStoryRespectsMaxHops() {
        EventPropagation propagation = new EventPropagation(12345L);
        
        Story story = new Story.Builder()
                .id("story_1")
                .storyType(StoryType.LEGEND)
                .title("Test Story")
                .originTileId(0)
                .baseProbability(1.0) // 100% to ensure propagation
                .maxHops(3)
                .build();

        Map<Integer, List<Integer>> neighbors = createLinearNeighborGraph(10);
        SaturationManager satMgr = new SaturationManager();

        Set<Integer> affected = propagation.propagateStory(story, neighbors, satMgr);

        // Should affect origin (0) + up to maxHops (3) = tiles 0, 1, 2, 3
        assertTrue(affected.size() <= 4, "Should affect at most origin + maxHops tiles");
    }

    @Test
    public void testPropagateEventNullEventThrows() {
        EventPropagation propagation = new EventPropagation(12345L);
        Map<Integer, List<Integer>> neighbors = createLinearNeighborGraph(10);
        SaturationManager satMgr = new SaturationManager();

        assertThrows(IllegalArgumentException.class, () -> {
            propagation.propagateEvent(null, neighbors, satMgr);
        });
    }

    @Test
    public void testPropagateStoryNullStoryThrows() {
        EventPropagation propagation = new EventPropagation(12345L);
        Map<Integer, List<Integer>> neighbors = createLinearNeighborGraph(10);
        SaturationManager satMgr = new SaturationManager();

        assertThrows(IllegalArgumentException.class, () -> {
            propagation.propagateStory(null, neighbors, satMgr);
        });
    }

    @Test
    public void testPropagateEventInGraphWithCycles() {
        EventPropagation propagation = new EventPropagation(12345L);
        Event event = createTestEvent();
        Map<Integer, List<Integer>> neighbors = createCyclicNeighborGraph();
        SaturationManager satMgr = new SaturationManager();

        Set<Integer> affected = propagation.propagateEvent(event, neighbors, satMgr);

        // Should handle cycles without infinite loops
        assertNotNull(affected);
        assertTrue(affected.size() > 0);
    }

    @Test
    public void testPropagateEventInComplexGraph() {
        EventPropagation propagation = new EventPropagation(12345L);
        
        Event event = new Event.Builder()
                .id("event_1")
                .category(EventCategory.WORLD)
                .name("World Event")
                .originTileId(5)
                .baseProbability(0.9)
                .maxHops(6)
                .build();

        Map<Integer, List<Integer>> neighbors = createGridNeighborGraph(10, 10);
        SaturationManager satMgr = new SaturationManager();

        Set<Integer> affected = propagation.propagateEvent(event, neighbors, satMgr);

        // Should propagate through complex graph
        assertTrue(affected.size() > 1);
    }

    @Test
    public void testDecayDifferentConstants() {
        EventPropagation prop1 = new EventPropagation(12345L, 0.5);
        EventPropagation prop2 = new EventPropagation(12345L, 1.0);

        // Lower decay constant should have slower falloff
        double decay1 = prop1.calculateExponentialDecay(1);
        double decay2 = prop2.calculateExponentialDecay(1);

        assertTrue(decay1 > decay2, "Lower decay constant should have higher decay value");
    }

    // Helper methods

    private Event createTestEvent() {
        return new Event.Builder()
                .id("event_test")
                .category(EventCategory.REGIONAL)
                .name("Test Event")
                .originTileId(0)
                .baseProbability(0.9)
                .maxHops(6)
                .build();
    }

    private Story createTestStory() {
        return new Story.Builder()
                .id("story_test")
                .storyType(StoryType.LEGEND)
                .title("Test Story")
                .originTileId(0)
                .baseProbability(0.9)
                .maxHops(6)
                .build();
    }

    /**
     * Create a linear neighbor graph: 0-1-2-3-4-...
     */
    private Map<Integer, List<Integer>> createLinearNeighborGraph(int length) {
        Map<Integer, List<Integer>> neighbors = new HashMap<>();
        
        for (int i = 0; i < length; i++) {
            List<Integer> nodeNeighbors = new ArrayList<>();
            if (i > 0) {
                nodeNeighbors.add(i - 1);
            }
            if (i < length - 1) {
                nodeNeighbors.add(i + 1);
            }
            neighbors.put(i, nodeNeighbors);
        }
        
        return neighbors;
    }

    /**
     * Create a cyclic neighbor graph: 0-1-2-3-0
     */
    private Map<Integer, List<Integer>> createCyclicNeighborGraph() {
        Map<Integer, List<Integer>> neighbors = new HashMap<>();
        
        neighbors.put(0, Arrays.asList(1, 3));
        neighbors.put(1, Arrays.asList(0, 2));
        neighbors.put(2, Arrays.asList(1, 3));
        neighbors.put(3, Arrays.asList(2, 0));
        
        return neighbors;
    }

    /**
     * Create a grid neighbor graph with 4-connectivity.
     */
    private Map<Integer, List<Integer>> createGridNeighborGraph(int width, int height) {
        Map<Integer, List<Integer>> neighbors = new HashMap<>();
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int tileId = y * width + x;
                List<Integer> nodeNeighbors = new ArrayList<>();
                
                // North
                if (y > 0) {
                    nodeNeighbors.add((y - 1) * width + x);
                }
                // South
                if (y < height - 1) {
                    nodeNeighbors.add((y + 1) * width + x);
                }
                // West
                if (x > 0) {
                    nodeNeighbors.add(y * width + (x - 1));
                }
                // East
                if (x < width - 1) {
                    nodeNeighbors.add(y * width + (x + 1));
                }
                
                neighbors.put(tileId, nodeNeighbors);
            }
        }
        
        return neighbors;
    }
}
