package org.adventure.world;

import java.util.*;

/**
 * River represents a flowing water body from source to ocean.
 * Uses priority-queue downhill flow algorithm (Dijkstra-like) for pathfinding.
 */
public class River {
    private final int id;
    private final List<Tile> path;
    private final TileCoord source;
    private final TileCoord terminus;
    private final boolean isLake;  // True if terminated in closed basin

    public River(int id, TileCoord source, TileCoord terminus, List<Tile> path, boolean isLake) {
        this.id = id;
        this.source = source;
        this.terminus = terminus;
        this.path = new ArrayList<>(path);
        this.isLake = isLake;
    }

    /**
     * Tile coordinate wrapper for river path.
     */
    public static class Tile {
        public final int x;
        public final int y;
        public final double elevation;

        public Tile(int x, int y, double elevation) {
            this.x = x;
            this.y = y;
            this.elevation = elevation;
        }
    }

    /**
     * Simple coordinate class for river endpoints.
     */
    public static class TileCoord {
        public final int x;
        public final int y;

        public TileCoord(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TileCoord coord = (TileCoord) o;
            return x == coord.x && y == coord.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    /**
     * Generate rivers for a world using priority-queue downhill flow.
     * 
     * @param elevation Elevation map
     * @param seed Random seed for determinism
     * @param width World width
     * @param height World height
     * @param numRivers Number of rivers to generate
     * @return List of generated rivers
     */
    public static List<River> generateRivers(double[][] elevation, long seed, int width, int height, int numRivers) {
        List<River> rivers = new ArrayList<>();
        Random rng = new Random(seed);
        Set<TileCoord> occupiedTiles = new HashSet<>();
        
        // Source elevation threshold - rivers start in highlands
        final double SOURCE_ELEVATION_THRESHOLD = 0.6;
        final double OCEAN_THRESHOLD = 0.2;
        final int MAX_PATH_LENGTH = Math.min(width, height) * 2;
        
        // Find potential river sources
        List<TileCoord> potentialSources = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (elevation[x][y] >= SOURCE_ELEVATION_THRESHOLD && elevation[x][y] < 0.95) {
                    potentialSources.add(new TileCoord(x, y));
                }
            }
        }
        
        // Shuffle sources for variety
        Collections.shuffle(potentialSources, rng);
        
        // Generate rivers from sources
        int riverCount = 0;
        for (TileCoord sourceCoord : potentialSources) {
            if (riverCount >= numRivers) break;
            
            // Skip if already part of a river
            if (occupiedTiles.contains(sourceCoord)) continue;
            
            // Pathfind from source to ocean
            List<Tile> path = findRiverPath(elevation, sourceCoord, width, height, 
                                            occupiedTiles, MAX_PATH_LENGTH, OCEAN_THRESHOLD, seed + riverCount);
            
            if (path != null && path.size() > 5) {  // Minimum river length
                Tile lastTile = path.get(path.size() - 1);
                boolean isLake = lastTile.elevation >= OCEAN_THRESHOLD;
                
                TileCoord terminus = new TileCoord(lastTile.x, lastTile.y);
                River river = new River(riverCount, sourceCoord, terminus, path, isLake);
                rivers.add(river);
                
                // Mark tiles as occupied
                for (Tile tile : path) {
                    occupiedTiles.add(new TileCoord(tile.x, tile.y));
                }
                
                riverCount++;
            }
        }
        
        return rivers;
    }

    /**
     * Find river path using priority-queue downhill flow (Dijkstra-like).
     * 
     * @param elevation Elevation map
     * @param source Starting coordinate
     * @param width World width
     * @param height World height
     * @param occupied Already occupied tiles (existing rivers)
     * @param maxLength Maximum path length
     * @param oceanThreshold Elevation threshold for ocean
     * @param seed Random seed for plateau tie-breaking
     * @return List of tiles forming river path, or null if failed
     */
    private static List<Tile> findRiverPath(double[][] elevation, TileCoord source, 
                                            int width, int height, Set<TileCoord> occupied,
                                            int maxLength, double oceanThreshold, long seed) {
        // Priority queue: lower elevation = higher priority (use priorityElev for tie-breaking)
        PriorityQueue<SearchNode> queue = new PriorityQueue<>(Comparator.comparingDouble(n -> n.priorityElev));
        Map<TileCoord, SearchNode> visited = new HashMap<>();
        
        SearchNode start = new SearchNode(source.x, source.y, elevation[source.x][source.y], null);
        queue.add(start);
        visited.put(source, start);
        
        Random tieBreaker = new Random(seed);
        
        // Safety limit: if we've explored too many nodes, give up (prevents infinite exploration)
        final int MAX_EXPLORED_NODES = Math.min(maxLength * 4, width * height / 4);
        int exploredCount = 0;
        
        while (!queue.isEmpty()) {
            exploredCount++;
            
            // Safety check: prevent excessive exploration
            if (exploredCount > MAX_EXPLORED_NODES) {
                return null;  // Give up on this river
            }
            SearchNode current = queue.poll();
            
            // Check termination conditions
            // 1. Reached ocean
            if (current.elevation < oceanThreshold) {
                return reconstructPath(current);
            }
            
            // 2. Max length exceeded (create lake)
            if (getPathLength(current) >= maxLength) {
                return reconstructPath(current);
            }
            
            // Explore 4-connected neighbors
            int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            List<SearchNode> neighbors = new ArrayList<>();
            
            for (int[] dir : directions) {
                int nx = current.x + dir[0];
                int ny = current.y + dir[1];
                
                // Bounds check
                if (nx < 0 || nx >= width || ny < 0 || ny >= height) continue;
                
                TileCoord neighborCoord = new TileCoord(nx, ny);
                
                // Skip if already visited or occupied by another river
                if (visited.containsKey(neighborCoord) || occupied.contains(neighborCoord)) continue;
                
                double neighborElev = elevation[nx][ny];
                
                // CRITICAL: Only explore downhill or plateau neighbors (not uphill)
                // Allow tiny tolerance for numerical precision
                if (neighborElev > current.elevation + 0.001) continue;
                
                // Add tiny deterministic noise for plateau tie-breaking (for priority queue only)
                // Noise is 10x smaller than tolerance to avoid affecting flow direction
                double noise = (tieBreaker.nextDouble() - 0.5) * 0.0001;
                double noisyElev = neighborElev + noise;
                
                // Store original elevation in node (not noisy)
                SearchNode neighbor = new SearchNode(nx, ny, neighborElev, current, noisyElev);
                neighbors.add(neighbor);
            }
            
            // Add neighbors to queue
            for (SearchNode neighbor : neighbors) {
                TileCoord neighborCoord = new TileCoord(neighbor.x, neighbor.y);
                if (!visited.containsKey(neighborCoord)) {
                    visited.put(neighborCoord, neighbor);
                    queue.add(neighbor);
                }
            }
        }
        
        // No path found (closed basin without reaching max length)
        return null;
    }

    /**
     * Reconstruct path from search node back to source.
     * Uses original (non-noisy) elevations from SearchNode.
     */
    private static List<Tile> reconstructPath(SearchNode end) {
        List<Tile> path = new ArrayList<>();
        SearchNode current = end;
        
        while (current != null) {
            // Use the original elevation (not priorityElev which has noise)
            path.add(new Tile(current.x, current.y, current.elevation));
            current = current.parent;
        }
        
        Collections.reverse(path);
        return path;
    }

    /**
     * Get path length from node back to source.
     */
    private static int getPathLength(SearchNode node) {
        int length = 0;
        SearchNode current = node;
        while (current != null) {
            length++;
            current = current.parent;
        }
        return length;
    }

    /**
     * Search node for pathfinding algorithm.
     */
    private static class SearchNode {
        final int x;
        final int y;
        final double elevation;  // Original elevation (stored in path)
        final double priorityElev;  // Noisy elevation (for priority queue ordering)
        final SearchNode parent;

        SearchNode(int x, int y, double elevation, SearchNode parent) {
            this(x, y, elevation, parent, elevation);
        }

        SearchNode(int x, int y, double elevation, SearchNode parent, double priorityElev) {
            this.x = x;
            this.y = y;
            this.elevation = elevation;
            this.priorityElev = priorityElev;
            this.parent = parent;
        }
    }

    /**
     * Validate that river flows downhill (no uphill segments).
     * 
     * @return true if all segments flow downhill or flat
     */
    public boolean isValidDownhill() {
        if (path.size() < 2) return true;
        
        for (int i = 1; i < path.size(); i++) {
            Tile prev = path.get(i - 1);
            Tile curr = path.get(i);
            
            // Allow small uphill due to noise (tolerance 0.002)
            if (curr.elevation > prev.elevation + 0.002) {
                return false;
            }
        }
        
        return true;
    }

    // Getters
    public int getId() {
        return id;
    }

    public List<Tile> getPath() {
        return new ArrayList<>(path);
    }

    public TileCoord getSource() {
        return source;
    }

    public TileCoord getTerminus() {
        return terminus;
    }

    public boolean isLake() {
        return isLake;
    }

    public int getLength() {
        return path.size();
    }
}
