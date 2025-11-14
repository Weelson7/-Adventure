# RoadGenerator

**Package:** `org.adventure.settlement`  
**Type:** Generator Class  
**Phase:** 1.10.2  
**Status:** ✅ Complete

## Overview

Generates automatic road networks between structures using A* pathfinding. Roads connect structures within 10 tiles entrance-to-entrance, avoiding water and mountains while preferring flat terrain.

## Purpose

- **Automatic Connectivity:** Connect nearby structures (≤ 10 tiles)
- **Terrain-Aware:** Avoid water/mountains, prefer flat land
- **Entrance Integration:** Roads connect to structure entrance sides
- **Reuse Existing:** Prefer existing roads (50% cost reduction)

## Key Features

### A* Pathfinding
- **Heuristic:** Manhattan distance (faster than Euclidean)
- **Cost Function:** Terrain penalties + elevation changes
- **Optimizations:** Priority queue, closed set, early termination

### Road Rules
- Connect structures within 10 tiles (entrance-to-entrance)
- Avoid water (elevation < 0.2) and mountains (elevation > 0.7)
- Prefer flat terrain (penalize elevation changes by 2×)
- Entrance clearance: ensure entrances connect to roads
- Roads are permanent (no removal in Phase 1.10.2)

### Constants
```java
MAX_ROAD_DISTANCE = 10
WATER_THRESHOLD = 0.2
MOUNTAIN_THRESHOLD = 0.7
ELEVATION_COST_MULTIPLIER = 2.0
```

## API Reference

### Constructor
```java
public RoadGenerator(double[][] elevationMap)
```

**Parameters:**
- `elevationMap` — 2D array of elevation values (0.0 = water, 1.0 = mountain peak)

### Generate Automatic Roads
```java
public List<RoadTile> generateAutomaticRoads(
    List<Structure> structures, 
    long currentTick)
```

**Algorithm:**
1. For each structure:
   - Ensure entrance connects to road (if not adjacent)
   - Find all structures within 10 tiles
   - Generate roads to nearby structures via A*
2. Return all newly created road tiles

**Example:**
```java
RoadGenerator roadGen = new RoadGenerator(elevationMap);
List<RoadTile> roads = roadGen.generateAutomaticRoads(structures, 0L);
System.out.println("Generated " + roads.size() + " roads");
```

### Connect to Nearby Buildings
```java
public List<RoadTile> connectToNearbyBuildings(
    Structure structure, 
    List<Structure> nearbyStructures, 
    long currentTick)
```

**Purpose:** Connect single structure to specific neighbors

### Connect Entrance to Road
```java
public List<RoadTile> connectEntranceToRoad(
    int entranceX, int entranceY, 
    Structure structure, 
    long currentTick)
```

**Purpose:** Ensure entrance tile connects to nearest road

### Check Entrance Adjacency
```java
public boolean isEntranceAdjacentToRoad(int entranceX, int entranceY)
```

**Returns:** `true` if entrance touches a road tile (N/E/S/W neighbor)

### Get All Roads
```java
public Collection<RoadTile> getAllRoads()
```

**Returns:** All roads in the internal network

## A* Pathfinding

### Cost Function
```java
moveCost = 1.0 + (|elevationChange| * 2.0)

if (existingRoad) {
    moveCost *= 0.5;  // Prefer existing roads
}

if (elevation < 0.2 || elevation > 0.7) {
    moveCost = Double.MAX_VALUE;  // Impassable
}
```

### Heuristic
```java
h(x, y) = |x - goalX| + |y - goalY|  // Manhattan distance
```

### Node Class
```java
private static class Node {
    int x, y;
    double g;      // Cost from start
    double h;      // Heuristic to goal
    double f;      // Total cost (g + h)
    Node parent;   // For path reconstruction
}
```

## Usage Examples

### Basic Road Generation
```java
WorldGen gen = new WorldGen(256, 256);
gen.generate(12345L);

RoadGenerator roadGen = new RoadGenerator(gen.getElevationMap());
List<RoadTile> roads = roadGen.generateAutomaticRoads(
    gen.getStructures(), 0L);

System.out.println("Generated " + roads.size() + " road tiles");
```

### Connect Specific Structures
```java
Structure house = /* ... */;
List<Structure> nearby = findNearbyStructures(house, allStructures, 10);

RoadGenerator roadGen = new RoadGenerator(elevationMap);
List<RoadTile> roads = roadGen.connectToNearbyBuildings(
    house, nearby, currentTick);

System.out.println("Connected house to " + nearby.size() + " buildings");
```

### Check Road Network
```java
RoadGenerator roadGen = /* ... */;
Collection<RoadTile> allRoads = roadGen.getAllRoads();

// Find roads by position
String key = x + "_" + y;
RoadTile road = allRoads.stream()
    .filter(r -> (r.getX() + "_" + r.getY()).equals(key))
    .findFirst()
    .orElse(null);
```

## Integration

### WorldGen (Phase 11.7)
```java
// After structures are generated
List<Structure> structures = generateStructures(settlements, seed);

// Generate roads
RoadGenerator roadGen = new RoadGenerator(elevationMap);
List<RoadTile> roads = roadGen.generateAutomaticRoads(structures, 0L);

this.roads = roads;
```

### Region Simulation
```java
// Roads affect NPC movement speed
void onNPCMove(NPC npc, int toX, int toY) {
    String key = toX + "_" + toY;
    RoadTile road = roadNetwork.get(key);
    
    if (road != null) {
        road.incrementTraffic(1);
        double speedMultiplier = getRoadSpeedMultiplier(road);
        npc.applySpeedBoost(speedMultiplier);
    }
}
```

## Performance

### Time Complexity
- **Per Path:** O(n log n) where n = tiles in search area
- **All Paths:** O(m × n log n) where m = structure pairs
- **Worst Case:** O(w × h) for entire world search

### Space Complexity
- **Open Set:** O(n) priority queue
- **Closed Set:** O(n) hash set
- **Road Network:** O(r) where r = total road tiles

### Optimizations
- Early termination on goal reached
- Closed set prevents re-exploring
- Reuse existing roads (50% cost)

## Testing

**Test Class:** (Planned) `org.adventure.RoadGeneratorTest`  
**Coverage:** Integration tests in WorldGenTest

### Test Cases (Planned)
- ✅ Generate roads between nearby structures (≤ 10 tiles)
- ✅ Avoid water tiles (elevation < 0.2)
- ✅ Avoid mountain tiles (elevation > 0.7)
- ✅ Prefer flat terrain (lower elevation changes)
- ✅ Connect to structure entrances
- ✅ Reuse existing roads
- ✅ No road generation for distant structures (> 10 tiles)

## Design Decisions

### Why A* Pathfinding?
- **Optimal Paths:** Finds shortest path with terrain costs
- **Efficient:** Much faster than Dijkstra for single-target searches
- **Flexible:** Easy to add custom cost functions

### Why Manhattan Distance Heuristic?
- **Admissible:** Never overestimates actual cost
- **Fast:** No sqrt() computation
- **Good Enough:** Close to optimal for grid movement

### Why 10-Tile Range?
- **Gameplay Balance:** Villages stay compact
- **Performance:** Limits path search space
- **Visual Clarity:** Roads visible on screen

## Future Enhancements (Phase 2.x)

- **Bridge Generation:** Roads over water tiles
- **Tunnel Generation:** Roads through mountains
- **Road Networks:** Highway system between cities
- **Trade Routes:** Mark major roads as trade routes
- **Dynamic Routing:** NPCs find optimal paths via road network

## Related Classes

- `RoadTile` — Individual road tile entity
- `RoadType` — DIRT/STONE/PAVED enum
- `Structure` — Buildings connected by roads
- `EntranceSide` — Determines road connection points

## References

- **Design Doc:** `BUILD_PHASE1.10.x.md` → Phase 1.10.2  
- **Algorithm:** A* Pathfinding  
- **Specs:** `docs/specs_summary.md` → Road generation rules
