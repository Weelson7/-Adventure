# Region Class Documentation

**Package:** `org.adventure.region`  
**File:** `src/main/java/org/adventure/region/Region.java`  
**Purpose:** Represents a geographical region in the game world with active/background simulation states

---

## Overview

The `Region` class represents a geographical subdivision of the game world. Regions are the fundamental unit for simulation optimization, allowing the game to apply full simulation to player-active areas while using simplified simulation for distant regions.

### Key Concepts

- **Active vs Background States:** Regions can be in ACTIVE (full simulation) or BACKGROUND (simplified) state
- **Resource Management:** Each region tracks its resource nodes for regeneration
- **Tick Tracking:** `lastProcessedTick` enables deterministic resynchronization
- **Spatial Boundaries:** Regions have defined spatial extents for containment checks

---

## Class Structure

### Enums

#### RegionState

```java
public enum RegionState {
    ACTIVE,      // Full simulation (players nearby)
    BACKGROUND   // Simplified simulation (no players)
}
```

**ACTIVE:** Region is actively simulated every tick. Used when players are present.
**BACKGROUND:** Region uses simplified simulation at reduced tick rate (default 1/60).

---

## Fields

### Core Properties

```java
private final int id;
private final int centerX;
private final int centerY;
private final int width;
private final int height;
```

- **id:** Unique identifier for this region
- **centerX, centerY:** World coordinates of region center
- **width, height:** Region dimensions in tiles

### Simulation State

```java
private long lastProcessedTick;
private RegionState state;
```

- **lastProcessedTick:** Last tick when this region was processed (for resynchronization)
- **state:** Current simulation state (ACTIVE or BACKGROUND)

### Game Objects

```java
private final List<ResourceNode> resourceNodes;
private int npcCount;
```

- **resourceNodes:** All resource nodes (trees, ore, crops) in this region
- **npcCount:** Number of NPCs currently in this region

---

## Constructor

```java
public Region(int id, int centerX, int centerY, int width, int height)
```

**Parameters:**
- `id` — Unique region identifier
- `centerX` — X coordinate of region center
- `centerY` — Y coordinate of region center
- `width` — Region width in tiles
- `height` — Region height in tiles

**Initial State:**
- `lastProcessedTick = 0`
- `state = BACKGROUND`
- `resourceNodes = empty list`
- `npcCount = 0`

**Example:**
```java
// Create 64x64 region centered at (256, 256)
Region region = new Region(1, 256, 256, 64, 64);
```

---

## Public Methods

### Resource Management

#### addResourceNode

```java
public void addResourceNode(ResourceNode node)
```

Add a resource node to this region.

**Parameters:**
- `node` — ResourceNode to add

**Example:**
```java
ResourceNode woodNode = new ResourceNode(1, ResourceNode.ResourceType.WOOD, 240, 240, 100.0, 5.0);
region.addResourceNode(woodNode);
```

#### getResourceNodes

```java
public List<ResourceNode> getResourceNodes()
```

Get all resource nodes in this region.

**Returns:** Unmodifiable list of resource nodes

**Example:**
```java
for (ResourceNode node : region.getResourceNodes()) {
    System.out.println("Node: " + node.getType() + " @ (" + node.getX() + ", " + node.getY() + ")");
}
```

#### regenerateResources

```java
public void regenerateResources(long currentTick, double deltaTime)
```

Update all resource nodes with regeneration based on elapsed time.

**Parameters:**
- `currentTick` — Current simulation tick
- `deltaTime` — Time elapsed in seconds

**Side Effects:**
- Calls `node.regenerate(deltaTime)` on all nodes
- Updates `lastProcessedTick` to `currentTick`

**Example:**
```java
// Regenerate resources for 2 seconds at tick 100
region.regenerateResources(100, 2.0);
```

**Formula:** See `ResourceNode.regenerate()` for regeneration formula

---

### Spatial Queries

#### contains

```java
public boolean contains(int x, int y)
```

Check if a world coordinate is within this region's boundaries.

**Parameters:**
- `x` — World X coordinate
- `y` — World Y coordinate

**Returns:** `true` if point is within region bounds

**Algorithm:**
```java
int minX = centerX - width / 2;
int maxX = centerX + width / 2;
int minY = centerY - height / 2;
int maxY = centerY + height / 2;
return x >= minX && x < maxX && y >= minY && y < maxY;
```

**Example:**
```java
Region region = new Region(1, 256, 256, 64, 64);
// Region spans [224, 288) in both dimensions

boolean inside = region.contains(256, 256);  // true (center)
boolean outside = region.contains(300, 300); // false (outside)
```

---

### State Management

#### getState / setState

```java
public RegionState getState()
public void setState(RegionState state)
```

Get or set the current simulation state.

**Example:**
```java
region.setState(Region.RegionState.ACTIVE);
boolean isActive = (region.getState() == Region.RegionState.ACTIVE);
```

#### getLastProcessedTick / setLastProcessedTick

```java
public long getLastProcessedTick()
public void setLastProcessedTick(long tick)
```

Get or set the last processed tick for resynchronization.

**Example:**
```java
long lastTick = region.getLastProcessedTick();
region.setLastProcessedTick(currentTick);
```

---

### NPC Management

#### getNpcCount / setNpcCount

```java
public int getNpcCount()
public void setNpcCount(int count)
```

Get or set the number of NPCs in this region.

**Example:**
```java
region.setNpcCount(50);
int npcs = region.getNpcCount();
```

---

## Getters

```java
public int getId()
public int getCenterX()
public int getCenterY()
public int getWidth()
public int getHeight()
```

All properties have standard getters for read access.

---

## Usage Patterns

### Basic Region Setup

```java
// Create region
Region region = new Region(1, 256, 256, 64, 64);

// Add resource nodes
ResourceNode wood = new ResourceNode(1, ResourceNode.ResourceType.WOOD, 240, 240, 100.0, 5.0);
ResourceNode ore = new ResourceNode(2, ResourceNode.ResourceType.ORE, 260, 260, 100.0, 0.0);
region.addResourceNode(wood);
region.addResourceNode(ore);

// Set NPCs
region.setNpcCount(20);
```

### Region Activation

```java
// Start in background state
Region region = new Region(1, 256, 256, 64, 64);
assert region.getState() == Region.RegionState.BACKGROUND;

// Activate when player enters
region.setState(Region.RegionState.ACTIVE);

// Deactivate when player leaves
region.setState(Region.RegionState.BACKGROUND);
region.setLastProcessedTick(currentTick);
```

### Resynchronization Pattern

```java
// Region in background state from tick 0 to 120
region.setLastProcessedTick(0);
region.setState(Region.RegionState.BACKGROUND);

// Simulate 120 ticks pass (handled by RegionSimulator)

// Activate at tick 120
long ticksElapsed = 120 - region.getLastProcessedTick(); // 120
double deltaTime = ticksElapsed * tickLength * backgroundMultiplier;
region.regenerateResources(120, deltaTime);
region.setState(Region.RegionState.ACTIVE);
```

---

## Design Rationale

### Why Region-Based Simulation?

**Performance:** Full simulation of entire world is prohibitively expensive
**Solution:** Only simulate active regions at full rate; use simplified simulation for background

**Player Experience:** Players only interact with nearby areas
**Solution:** Full simulation where players are; background simulation elsewhere

### Why lastProcessedTick?

**Problem:** Background regions need deterministic resynchronization
**Solution:** Track last processed tick, apply accumulated changes on activation

**Example:** If region processed at tick 0, activated at tick 120 with 1/60 background rate:
- Effective elapsed time: 120 * (1/60) = 2 seconds
- Apply 2 seconds of resource regeneration

---

## Integration with RegionSimulator

The `Region` class is designed to work with `RegionSimulator`:

```java
RegionSimulator sim = new RegionSimulator();
Region region = new Region(1, 256, 256, 64, 64);
sim.addRegion(region);

// RegionSimulator handles:
// - Tick processing (active vs background rates)
// - State transitions (activation/deactivation)
// - Resynchronization on activation
sim.tick();
```

See `RegionSimulator.md` for full simulation details.

---

## Quality Gates & Testing

### Determinism

✅ **Same initial state + same inputs → same final state**

```java
Region r1 = new Region(1, 256, 256, 64, 64);
Region r2 = new Region(1, 256, 256, 64, 64);

// Add identical resource nodes
r1.addResourceNode(new ResourceNode(1, ResourceType.WOOD, 240, 240, 100.0, 5.0));
r2.addResourceNode(new ResourceNode(1, ResourceType.WOOD, 240, 240, 100.0, 5.0));

// Same regeneration
r1.regenerateResources(10, 2.0);
r2.regenerateResources(10, 2.0);

// Same results
assert r1.getResourceNodes().get(0).getCurrentQuantity() ==
       r2.getResourceNodes().get(0).getCurrentQuantity();
```

### State Preservation

✅ **Region state transitions preserve data**

```java
// Background → Active → Background should preserve state
region.setState(RegionState.BACKGROUND);
long tickBefore = region.getLastProcessedTick();

region.setState(RegionState.ACTIVE);
region.setState(RegionState.BACKGROUND);

// State preserved
assert region.getResourceNodes().size() > 0;
assert region.getNpcCount() == expectedCount;
```

### Boundary Correctness

✅ **Containment checks are mathematically correct**

```java
Region region = new Region(1, 256, 256, 64, 64);
// Spans [224, 288) in both dimensions

assert region.contains(224, 224);  // Min corner (inclusive)
assert region.contains(287, 287);  // Max corner - 1
assert !region.contains(288, 288); // Max corner (exclusive)
```

---

## Performance Considerations

### Memory Footprint

- **Overhead per region:** ~200 bytes (id, center, dimensions, state, tick)
- **Resource nodes:** ~100 bytes each
- **Total:** ~200 + (numNodes * 100) bytes

**Example:** 1000 regions with 10 nodes each = ~1.2 MB

### Computational Cost

- **contains():** O(1) — 4 comparisons
- **regenerateResources():** O(n) where n = number of resource nodes
- **State transitions:** O(1)

**Optimization:** Resource regeneration dominates cost; batch updates for background regions

---

## Future Enhancements (Post-MVP)

### Phase 1.3+ Additions

- **NPC tracking:** Full NPC objects (not just counts)
- **Structure tracking:** Buildings and ownership in region
- **Event queues:** Per-region event processing
- **Weather/hazards:** Regional environmental effects

### Potential Optimizations

- **Spatial indexing:** Quad-tree for faster containment queries
- **Chunk subdivision:** Divide regions into smaller chunks for finer control
- **LOD system:** Multiple simulation detail levels (not just active/background)

---

## Related Documentation

- **RegionSimulator.md** — Tick-driven simulation orchestration
- **ResourceNode.md** — Resource regeneration mechanics
- **architecture_design.md** — High-level region simulation strategy
- **specs_summary.md** — Tick rates, multipliers, canonical defaults

---

## Version History

- **v0.1.0 (Nov 2025):** Initial implementation for Phase 1.2
  - Basic region structure
  - Active/background state system
  - Resource node management
  - Spatial containment queries
