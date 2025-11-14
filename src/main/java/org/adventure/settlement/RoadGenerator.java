package org.adventure.settlement;

import org.adventure.structure.Structure;
import org.adventure.structure.EntranceSide;
import java.util.*;

/**
 * Generates automatic road networks between structures.
 * Uses A* pathfinding to create terrain-aware roads.
 * Automatically connects structures within 10 tiles.
 * 
 * Road rules:
 * - Connect structures within 10 tiles entrance-to-entrance
 * - Avoid water (elevation < 0.2) and mountains (elevation > 0.7)
 * - Prefer flat terrain (lower elevation change)
 * - Connect entrances to nearby roads if not adjacent
 * - Roads are permanent (no removal)
 * 
 * Note: This implementation uses a simplified terrain model for Phase 1.10.2.
 * Full terrain integration will be added when Tile class is implemented.
 * 
 * Design: BUILD_PHASE1.10.x.md → Phase 1.10.2
 */
public class RoadGenerator {
    private static final int MAX_ROAD_DISTANCE = 10;
    private static final double WATER_THRESHOLD = 0.2;
    private static final double MOUNTAIN_THRESHOLD = 0.7;
    private static final double ELEVATION_COST_MULTIPLIER = 2.0;
    
    private final Map<String, RoadTile> roadNetwork = new HashMap<>();
    private final double[][] elevationMap;
    private final int worldWidth;
    private final int worldHeight;
    
    public RoadGenerator(double[][] elevationMap) {
        this.elevationMap = elevationMap;
        this.worldHeight = elevationMap.length;
        this.worldWidth = elevationMap[0].length;
    }
    
    /**
     * Generate automatic roads for all structures within range.
     * 
     * @param structures All structures to connect
     * @param currentTick Current game tick for road creation
     * @return List of newly created road tiles
     */
    public List<RoadTile> generateAutomaticRoads(List<Structure> structures, long currentTick) {
        List<RoadTile> newRoads = new ArrayList<>();
        
        if (structures == null || structures.isEmpty()) {
            return newRoads;
        }
        
        // Connect each structure to nearby structures
        for (int i = 0; i < structures.size(); i++) {
            Structure structure = structures.get(i);
            int[] entrance = getEntranceCoords(structure);
            
            // First ensure entrance connects to road if not adjacent
            if (!isEntranceAdjacentToRoad(entrance[0], entrance[1])) {
                List<RoadTile> entranceRoad = connectEntranceToRoad(
                        entrance[0], entrance[1], structure, currentTick);
                newRoads.addAll(entranceRoad);
            }
            
            // Then connect to nearby buildings
            for (int j = i + 1; j < structures.size(); j++) {
                Structure otherStructure = structures.get(j);
                int[] otherEntrance = getEntranceCoords(otherStructure);
                
                double distance = Math.sqrt(
                        Math.pow(entrance[0] - otherEntrance[0], 2) +
                        Math.pow(entrance[1] - otherEntrance[1], 2));
                
                if (distance <= MAX_ROAD_DISTANCE) {
                    List<RoadTile> path = findPath(
                            entrance[0], entrance[1], 
                            otherEntrance[0], otherEntrance[1], 
                            currentTick);
                    newRoads.addAll(path);
                }
            }
        }
        
        return newRoads;
    }
    
    /**
     * Connect structure to nearby buildings within range.
     * 
     * @param structure Structure to connect
     * @param nearbyStructures Nearby structures
     * @param currentTick Current game tick
     * @return List of newly created road tiles
     */
    public List<RoadTile> connectToNearbyBuildings(Structure structure, 
                                                   List<Structure> nearbyStructures, 
                                                   long currentTick) {
        List<RoadTile> newRoads = new ArrayList<>();
        int[] entrance = getEntranceCoords(structure);
        
        for (Structure nearby : nearbyStructures) {
            int[] otherEntrance = getEntranceCoords(nearby);
            
            double distance = Math.sqrt(
                    Math.pow(entrance[0] - otherEntrance[0], 2) +
                    Math.pow(entrance[1] - otherEntrance[1], 2));
            
            if (distance <= MAX_ROAD_DISTANCE) {
                List<RoadTile> path = findPath(
                        entrance[0], entrance[1], 
                        otherEntrance[0], otherEntrance[1], 
                        currentTick);
                newRoads.addAll(path);
            }
        }
        
        return newRoads;
    }
    
    /**
     * Connect entrance to nearby road if not adjacent.
     * 
     * @param entranceX Entrance X coordinate
     * @param entranceY Entrance Y coordinate
     * @param structure Structure owning the entrance
     * @param currentTick Current game tick
     * @return List of road tiles connecting entrance to road
     */
    public List<RoadTile> connectEntranceToRoad(int entranceX, int entranceY, 
                                                Structure structure, long currentTick) {
        List<RoadTile> newRoads = new ArrayList<>();
        
        // Find nearest road tile
        RoadTile nearestRoad = findNearestRoad(entranceX, entranceY);
        if (nearestRoad == null) {
            return newRoads;
        }
        
        // Create path to nearest road
        List<RoadTile> path = findPath(
                entranceX, entranceY, 
                nearestRoad.getX(), nearestRoad.getY(), 
                currentTick);
        
        return path;
    }
    
    /**
     * Check if entrance is adjacent to a road tile.
     * 
     * @param entranceX Entrance X coordinate
     * @param entranceY Entrance Y coordinate
     * @return true if entrance touches a road
     */
    public boolean isEntranceAdjacentToRoad(int entranceX, int entranceY) {
        int[][] neighbors = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        
        for (int[] dir : neighbors) {
            int nx = entranceX + dir[0];
            int ny = entranceY + dir[1];
            String key = nx + "_" + ny;
            
            if (roadNetwork.containsKey(key)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get all roads in the network.
     * 
     * @return Collection of all road tiles
     */
    public Collection<RoadTile> getAllRoads() {
        return roadNetwork.values();
    }
    
    // A* pathfinding implementation
    
    private List<RoadTile> findPath(int startX, int startY, int endX, int endY, long currentTick) {
        List<RoadTile> path = new ArrayList<>();
        
        // Skip if already connected or out of bounds
        if (!isValid(startX, startY) || !isValid(endX, endY)) {
            return path;
        }
        
        // A* data structures
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        Map<String, Node> allNodes = new HashMap<>();
        Set<String> closedSet = new HashSet<>();
        
        Node startNode = new Node(startX, startY, 0, heuristic(startX, startY, endX, endY));
        openSet.add(startNode);
        allNodes.put(startX + "_" + startY, startNode);
        
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            String currentKey = current.x + "_" + current.y;
            
            if (current.x == endX && current.y == endY) {
                // Reconstruct path
                return reconstructPath(current, currentTick);
            }
            
            closedSet.add(currentKey);
            
            for (int[] dir : directions) {
                int nx = current.x + dir[0];
                int ny = current.y + dir[1];
                String neighborKey = nx + "_" + ny;
                
                if (!isValid(nx, ny) || closedSet.contains(neighborKey)) {
                    continue;
                }
                
                double moveCost = getMoveCost(current.x, current.y, nx, ny);
                if (moveCost == Double.MAX_VALUE) {
                    continue; // Impassable terrain
                }
                
                double tentativeG = current.g + moveCost;
                
                Node neighbor = allNodes.get(neighborKey);
                if (neighbor == null) {
                    neighbor = new Node(nx, ny, tentativeG, heuristic(nx, ny, endX, endY));
                    neighbor.parent = current;
                    allNodes.put(neighborKey, neighbor);
                    openSet.add(neighbor);
                } else if (tentativeG < neighbor.g) {
                    neighbor.g = tentativeG;
                    neighbor.f = tentativeG + neighbor.h;
                    neighbor.parent = current;
                    openSet.remove(neighbor);
                    openSet.add(neighbor);
                }
            }
        }
        
        return path; // No path found
    }
    
    private List<RoadTile> reconstructPath(Node endNode, long currentTick) {
        List<RoadTile> path = new ArrayList<>();
        Node current = endNode;
        
        while (current != null) {
            String key = current.x + "_" + current.y;
            
            // Reuse existing road or create new one
            RoadTile road = roadNetwork.get(key);
            if (road == null) {
                road = new RoadTile.Builder()
                        .position(current.x, current.y)
                        .type(RoadType.DIRT)
                        .createdTick(currentTick)
                        .trafficLevel(0)
                        .isAutoGenerated(true)
                        .build();
                roadNetwork.put(key, road);
                path.add(road);
            }
            
            current = current.parent;
        }
        
        Collections.reverse(path);
        return path;
    }
    
    private double getMoveCost(int fromX, int fromY, int toX, int toY) {
        double elevation = elevationMap[toY][toX];
        
        // Block impassable terrain
        if (elevation < WATER_THRESHOLD || elevation > MOUNTAIN_THRESHOLD) {
            return Double.MAX_VALUE;
        }
        
        // Base cost
        double cost = 1.0;
        
        // Add elevation change cost
        double fromElevation = elevationMap[fromY][fromX];
        double elevationChange = Math.abs(elevation - fromElevation);
        cost += elevationChange * ELEVATION_COST_MULTIPLIER;
        
        // Prefer existing roads (lower cost)
        String key = toX + "_" + toY;
        if (roadNetwork.containsKey(key)) {
            cost *= 0.5;
        }
        
        return cost;
    }
    
    private double heuristic(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2); // Manhattan distance
    }
    
    private boolean isValid(int x, int y) {
        return x >= 0 && x < worldWidth && y >= 0 && y < worldHeight;
    }
    
    private RoadTile findNearestRoad(int x, int y) {
        RoadTile nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (RoadTile road : roadNetwork.values()) {
            double distance = Math.sqrt(
                    Math.pow(x - road.getX(), 2) + 
                    Math.pow(y - road.getY(), 2));
            
            if (distance < minDistance) {
                minDistance = distance;
                nearest = road;
            }
        }
        
        return nearest;
    }
    
    private int[] getEntranceCoords(Structure structure) {
        int structureX = parseX(structure.getLocationTileId());
        int structureY = parseY(structure.getLocationTileId());
        EntranceSide entrance = structure.getEntrance();
        
        if (entrance == null) {
            entrance = EntranceSide.SOUTH;
        }
        
        return entrance.getEntranceCoords(structureX, structureY);
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
    
    // A* node class
    private static class Node {
        int x, y;
        double g; // Cost from start
        double h; // Heuristic to goal
        double f; // Total cost (g + h)
        Node parent;
        
        Node(int x, int y, double g, double h) {
            this.x = x;
            this.y = y;
            this.g = g;
            this.h = h;
            this.f = g + h;
        }
    }
}
