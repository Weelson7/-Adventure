# RoadGenerator

**Package:** `org.adventure.settlement`  
**Type:** Service Class (Stateful)

---

## Overview

`RoadGenerator` automatically generates road networks between structures using A* pathfinding. Roads connect structures within 10 tiles entrance-to-entrance, avoiding water and mountains, preferring flat terrain.

---

## Design Principles

1. **Automatic Connection**: Structures within 10 tiles are connected automatically
2. **Terrain-Aware Pathfinding**: Uses A* to find optimal paths avoiding obstacles
3. **Entrance-to-Entrance**: Roads connect structure entrances, not centers
4. **Permanent Network**: Roads are added to network and reused for future paths
5. **Elevation Cost**: Prefers flat terrain, penalizes steep changes

---

## Class Structure

```java
public class RoadGenerator {
    // Constants
    private static final int MAX_ROAD_DISTANCE = 10;
    private static final double WATER_THRESHOLD = 0.2;
    private static final double MOUNTAIN_THRESHOLD = 0.7;
    private static final double ELEVATION_COST_MULTIPLIER = 2.0;
    
    // State
    private final Map<String, RoadTile> roadNetwork;
    private final double[][] elevationMap;
    private final int worldWidth;
    private final int worldHeight;
}
```

---

## Key Methods

### Main Generation
- **`generateAutomaticRoads(structures, currentTick)`**: Generate roads for all structures
  - Connects all structures within 10 tiles to each other
  - Ensures entrances connect to roads if not adjacent
  - Returns list of newly created road tiles
  - Does NOT create duplicate roads

- **`connectToNearbyBuildings(structure, nearbyStructures, currentTick)`**: Connect one structure to nearby buildings
  - Connects given structure to each nearby structure within range
  - Returns list of newly created road tiles

- **`connectEntranceToRoad(entranceX, entranceY, structure, currentTick)`**: Connect entrance to nearest existing road
  - Finds nearest road in network
  - Creates path from entrance to that road
  - Returns list of road tiles in path

### Network Queries
- **`isEntranceAdjacentToRoad(entranceX, entranceY)`**: Check if entrance touches a road
  - Tests 4-directional adjacency (N/S/E/W)
  - Returns true if any neighbor is a road

- **`getAllRoads()`**: Get all roads in the network
  - Returns collection of all RoadTile objects
  - Read-only view of road network

### Pathfinding (A* Algorithm)
- **`findPath(startX, startY, endX, endY, currentTick)`**: Find optimal path between two points
  - Uses A* with Manhattan distance heuristic
  - Considers terrain cost (elevation changes)
  - Avoids water (< 0.2) and mountains (> 0.7)
  - Prefers existing roads (0.5x cost)
  - Returns list of RoadTile objects in path

---

## Terrain Rules

### Impassable Terrain
- **Water**: elevation < 0.2 → cost = infinity (blocked)
- **Mountains**: elevation > 0.7 → cost = infinity (blocked)

### Terrain Costs
- **Base Cost**: 1.0 per tile
- **Elevation Change**: `abs(elevation_change) * 2.0`
- **Existing Road**: Base cost × 0.5 (prefer reuse)

### Movement
- **4-directional**: North, South, East, West only (no diagonals)

---

## Road Generation Algorithm

### Phase 1: Entrance-to-Road Connection
For each structure:
1. Get entrance coordinates from structure
2. If entrance not adjacent to road:
   - Find nearest road tile
   - Create path from entrance to road

### Phase 2: Structure-to-Structure Connection
For each pair of structures:
1. Calculate entrance-to-entrance distance
2. If distance <= 10 tiles:
   - Create path between entrances using A*
   - Add new roads to network

### Phase 3: Path Reuse
- Existing roads are reused (not recreated)
- Network key: "x_y" string format
- New roads added to network immediately

---

## A* Pathfinding Details

### Data Structures
- **Open Set**: Priority queue ordered by f-score (g + h)
- **Closed Set**: Hash set of visited nodes
- **Node Map**: All discovered nodes by coordinate key

### Cost Functions
- **g(n)**: Cost from start to node n
- **h(n)**: Heuristic from node n to goal (Manhattan distance)
- **f(n)**: Total estimated cost (g + h)

### Path Reconstruction
1. Follow parent pointers from goal to start
2. Create RoadTile for each coordinate (or reuse existing)
3. Add new tiles to road network
4. Reverse path to start-to-goal order

---

## Usage Examples

### Generate All Roads
```java
RoadGenerator generator = new RoadGenerator(elevationMap);
List<RoadTile> newRoads = generator.generateAutomaticRoads(allStructures, currentTick);
```

### Connect Single Structure
```java
List<Structure> nearby = findNearbyStructures(structure, 10);
List<RoadTile> newRoads = generator.connectToNearbyBuildings(structure, nearby, currentTick);
```

### Check Entrance Access
```java
boolean hasRoadAccess = generator.isEntranceAdjacentToRoad(entranceX, entranceY);
```

### Get All Roads
```java
Collection<RoadTile> allRoads = generator.getAllRoads();
```

---

## Implementation Notes

### Simplified Terrain Model
- Phase 1.10.2 uses `double[][] elevationMap` placeholder
- Full terrain integration requires Tile class (future)
- Elevation thresholds are approximations

### Coordinate Parsing
- Location IDs assumed format: "x_y"
- Helper methods: `parseX()`, `parseY()`
- TODO: Replace with proper TileId parsing when available

### Performance Considerations
- Road network uses HashMap for O(1) lookups
- A* priority queue uses natural ordering by f-score
- Closed set prevents revisiting nodes

---

## Related Classes

- **RoadTile**: Individual road tile in the network
- **RoadType**: Road quality tiers (DIRT/STONE/PAVED)
- **Structure**: Buildings that roads connect
- **EntranceSide**: Structure entrance direction
- **VillageManager**: Uses roads for settlement detection

---

## Testing

**Test Classes**: Integration tests in Phase 1.10.2 validation  
**Coverage**: Core pathfinding and generation logic

### Test Scenarios
- Road generation between nearby structures
- Terrain obstacle avoidance (water, mountains)
- Entrance-to-road connection logic
- Path reuse and network management

---

## Design Decisions

1. **Why A* pathfinding?**: Optimal paths with terrain awareness; industry standard for grid-based games.

2. **Why 10-tile max distance?**: Balances connectivity vs. road sprawl; prevents absurdly long roads.

3. **Why no diagonal movement?**: Simpler pathfinding; matches common game grid movement.

4. **Why permanent roads?**: Simplifies logic in Phase 1.10.2; removal can be added later.

5. **Why terrain cost multiplier 2.0?**: Makes elevation changes significant but not prohibitive.

---

## Future Enhancements (Post-MVP)

1. **Dynamic Terrain**: Integrate with full Tile class and TerrainType
2. **Road Networks**: Track connected components, find alternate routes
3. **Bridge Building**: Allow crossing water/chasms with special roads
4. **Player Control**: Let players manually place/remove roads
5. **Traffic Simulation**: Calculate traffic based on actual NPC paths

---

## References

- Design: `BUILD_PHASE1.10.x.md` → Phase 1.10.2
- Algorithm: [A* Pathfinding](https://en.wikipedia.org/wiki/A*_search_algorithm)
- Related: `RoadTile.md`, `RoadType.md`, `Structure.md`, `EntranceSide.md`
