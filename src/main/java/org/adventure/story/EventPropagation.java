package org.adventure.story;

import java.util.*;

/**
 * EventPropagation handles BFS-based event and story propagation across regions.
 * Uses deterministic seeded RNG, exponential decay, and saturation controls.
 */
public class EventPropagation {
    // Default constants from specs_summary.md
    private static final double DEFAULT_DECAY_K = 0.8;
    private static final double DEFAULT_BASE_PROBABILITY = 0.9;
    private static final int DEFAULT_MAX_HOPS = 6;
    private static final double MIN_PROPAGATION_THRESHOLD = 0.01;

    private final Random rng;
    private final double decayK;

    /**
     * Create a new EventPropagation with deterministic seeding.
     *
     * @param seed Seed for deterministic RNG
     */
    public EventPropagation(long seed) {
        this(seed, DEFAULT_DECAY_K);
    }

    /**
     * Create a new EventPropagation with custom decay parameter.
     *
     * @param seed Seed for deterministic RNG
     * @param decayK Decay constant for exponential falloff
     */
    public EventPropagation(long seed, double decayK) {
        this.rng = new Random(seed);
        this.decayK = decayK;
    }

    /**
     * Propagate an event using BFS algorithm with exponential decay.
     *
     * @param event Event to propagate
     * @param neighbors Map of tile IDs to their neighbor tile IDs
     * @param saturationManager Saturation manager for checking caps
     * @return Set of tile IDs where event successfully propagated
     */
    public Set<Integer> propagateEvent(Event event, Map<Integer, List<Integer>> neighbors, 
                                       SaturationManager saturationManager) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        if (neighbors == null) {
            neighbors = new HashMap<>(); // Empty map = isolated tile
        }

        Set<Integer> affectedTiles = new HashSet<>();
        Queue<PropagationNode> queue = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();

        // Initialize with origin
        queue.add(new PropagationNode(
            event.getOriginTileId(),
            event.getBaseProbability(),
            0
        ));
        visited.add(event.getOriginTileId());
        affectedTiles.add(event.getOriginTileId());

        // BFS propagation
        while (!queue.isEmpty()) {
            PropagationNode current = queue.poll();

            // Stop if max hops reached
            if (current.hopCount >= event.getMaxHops()) {
                continue;
            }

            // Get neighbors of current tile
            List<Integer> currentNeighbors = neighbors.getOrDefault(current.tileId, Collections.emptyList());

            for (int neighborId : currentNeighbors) {
                // Skip if already visited
                if (visited.contains(neighborId)) {
                    continue;
                }

                visited.add(neighborId);

                // Calculate decay for this hop
                int nextHopCount = current.hopCount + 1;
                double decay = calculateExponentialDecay(nextHopCount);
                
                // Calculate effective probability with saturation
                double connectionFactor = 1.0; // Default - can be enhanced with region connectivity
                double saturationFactor = saturationManager.getSaturationFactor(neighborId, event.getCategory());
                double effectiveProbability = current.probability * decay * connectionFactor * saturationFactor;

                // Check if propagation occurs
                if (effectiveProbability >= MIN_PROPAGATION_THRESHOLD && rng.nextDouble() < effectiveProbability) {
                    affectedTiles.add(neighborId);
                    
                    // Add to queue for further propagation
                    queue.add(new PropagationNode(
                        neighborId,
                        effectiveProbability,
                        nextHopCount
                    ));

                    // Register with saturation manager
                    saturationManager.registerEvent(neighborId, event.getCategory());
                }
            }
        }

        return affectedTiles;
    }

    /**
     * Propagate a story using BFS algorithm with exponential decay.
     * Similar to event propagation but with story-specific logic.
     *
     * @param story Story to propagate
     * @param neighbors Map of tile IDs to their neighbor tile IDs
     * @param saturationManager Saturation manager for checking caps
     * @return Set of tile IDs where story successfully propagated
     */
    public Set<Integer> propagateStory(Story story, Map<Integer, List<Integer>> neighbors,
                                       SaturationManager saturationManager) {
        if (story == null) {
            throw new IllegalArgumentException("Story cannot be null");
        }
        if (neighbors == null) {
            neighbors = new HashMap<>();
        }

        Set<Integer> affectedTiles = new HashSet<>();
        Queue<PropagationNode> queue = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();

        // Initialize with origin
        queue.add(new PropagationNode(
            story.getOriginTileId(),
            story.getBaseProbability(),
            0
        ));
        visited.add(story.getOriginTileId());
        affectedTiles.add(story.getOriginTileId());

        // BFS propagation
        while (!queue.isEmpty()) {
            PropagationNode current = queue.poll();

            // Stop if max hops reached
            if (current.hopCount >= story.getMaxHops()) {
                continue;
            }

            // Get neighbors of current tile
            List<Integer> currentNeighbors = neighbors.getOrDefault(current.tileId, Collections.emptyList());

            for (int neighborId : currentNeighbors) {
                if (visited.contains(neighborId)) {
                    continue;
                }

                visited.add(neighborId);

                // Calculate decay for this hop
                int nextHopCount = current.hopCount + 1;
                double decay = calculateExponentialDecay(nextHopCount);
                
                // Calculate effective probability with saturation
                double connectionFactor = 1.0;
                double saturationFactor = saturationManager.getStorySaturationFactor(neighborId, story.getStoryType());
                double effectiveProbability = current.probability * decay * connectionFactor * saturationFactor;

                // Check if propagation occurs
                if (effectiveProbability >= MIN_PROPAGATION_THRESHOLD && rng.nextDouble() < effectiveProbability) {
                    affectedTiles.add(neighborId);
                    
                    queue.add(new PropagationNode(
                        neighborId,
                        effectiveProbability,
                        nextHopCount
                    ));

                    saturationManager.registerStory(neighborId, story.getStoryType());
                }
            }
        }

        return affectedTiles;
    }

    /**
     * Calculate exponential decay: decay(h) = exp(-k * h)
     * where h is hop count and k is decay constant (default 0.8).
     *
     * @param hopCount Number of hops from origin
     * @return Decay factor [0.0, 1.0]
     */
    public double calculateExponentialDecay(int hopCount) {
        return Math.exp(-decayK * hopCount);
    }

    /**
     * Calculate linear decay: decay(h) = max(0, 1 - k*h)
     * Alternative decay function for gentler falloff.
     *
     * @param hopCount Number of hops from origin
     * @return Decay factor [0.0, 1.0]
     */
    public double calculateLinearDecay(int hopCount) {
        return Math.max(0.0, 1.0 - (decayK * hopCount));
    }

    /**
     * Internal class representing a node in the propagation BFS.
     */
    private static class PropagationNode {
        final int tileId;
        final double probability;
        final int hopCount;

        PropagationNode(int tileId, double probability, int hopCount) {
            this.tileId = tileId;
            this.probability = probability;
            this.hopCount = hopCount;
        }
    }
}
