# RegionSimulator Class Documentation

**Package:** `org.adventure.region`  
**File:** `src/main/java/org/adventure/region/RegionSimulator.java`  
**Purpose:** Orchestrates tick-driven simulation with active/background region multipliers

---

## Overview

The `RegionSimulator` class is the central engine for region-based simulation in the game world. It implements the tick-driven model specified in `docs/architecture_design.md` and `docs/specs_summary.md`, managing active regions (full simulation) and background regions (simplified simulation) with different tick rate multipliers.

### Key Responsibilities

- **Tick Management:** Advance simulation time in discrete ticks (default 1 second)
- **State Transitions:** Activate/deactivate regions based on player proximity
- **Differential Simulation:** Apply full simulation to active regions, simplified to background
- **Resynchronization:** Apply accumulated changes when regions activate

---

## Class Structure

### Constants

```java
public static final double DEFAULT_TICK_LENGTH = 1.0; // 1 second
public static final double DEFAULT_ACTIVE_MULTIPLIER = 1.0;
public static final double DEFAULT_BACKGROUND_MULTIPLIER = 1.0 / 60.0;
```

**Defaults from `specs_summary.md`:**
- **Tick length:** 1 second (configurable per deployment)
- **Active multiplier:** 1.0 (full simulation speed)
- **Background multiplier:** 1/60 (one update per 60 ticks)

---

## Fields

```java
private final Map<Integer, Region> regions;
private final double tickLength;
private final double activeTickRateMultiplier;
private final double backgroundTickRateMultiplier;
private long currentTick;
```

- **regions:** Map of region ID → Region object
- **tickLength:** Duration of one tick in seconds
- **activeTickRateMultiplier:** Simulation speed for active regions
- **backgroundTickRateMultiplier:** Simulation speed for background regions
- **currentTick:** Current simulation tick (monotonically increasing)

---

## Constructors

### Default Constructor

```java
public RegionSimulator()
```

Creates simulator with default configuration:
- Tick length: 1.0 second
- Active multiplier: 1.0
- Background multiplier: 1/60

**Example:**
```java
RegionSimulator sim = new RegionSimulator();
// Uses canonical defaults from specs_summary.md
```

### Custom Constructor

```java
public RegionSimulator(double tickLength, double activeMultiplier, double backgroundMultiplier)
```

Creates simulator with custom configuration.

**Parameters:**
- `tickLength` — Duration of one tick in seconds
- `activeMultiplier` — Simulation speed for active regions
- `backgroundMultiplier` — Simulation speed for background regions

**Example:**
```java
// 2-second ticks, 1.5x active speed, 0.5x background speed
RegionSimulator sim = new RegionSimulator(2.0, 1.5, 0.5);
```

---

## Public Methods

### Region Management

#### addRegion

```java
public void addRegion(Region region)
```

Add a region to the simulator.

**Parameters:**
- `region` — Region to add

**Example:**
```java
Region region = new Region(1, 256, 256, 64, 64);
sim.addRegion(region);
```

#### getRegion

```java
public Region getRegion(int id)
```

Get a region by ID.

**Parameters:**
- `id` — Region identifier

**Returns:** Region object, or `null` if not found

**Example:**
```java
Region region = sim.getRegion(1);
if (region != null) {
    System.out.println("Found region " + region.getId());
}
```

#### getAllRegions

```java
public List<Region> getAllRegions()
```

Get all regions in the simulator.

**Returns:** List of all regions

**Example:**
```java
for (Region region : sim.getAllRegions()) {
    System.out.println("Region " + region.getId() + " state: " + region.getState());
}
```

---

### State Transitions

#### activateRegion

```java
public void activateRegion(int regionId)
```

Activate a region (switch from BACKGROUND to ACTIVE simulation).

**Parameters:**
- `regionId` — ID of region to activate

**Behavior:**
1. Check if region is in BACKGROUND state
2. Set state to ACTIVE
3. Call `resynchronizeRegion()` to apply accumulated changes
4. Update `lastProcessedTick` to current tick

**Example:**
```java
// Player enters region 1
sim.activateRegion(1);

Region region = sim.getRegion(1);
assert region.getState() == Region.RegionState.ACTIVE;
```

**Use Case:**
```java
// Activate regions within player proximity
int playerX = 256, playerY = 256;
int activationRadius = 100;

for (Region region : sim.getAllRegions()) {
    int dx = region.getCenterX() - playerX;
    int dy = region.getCenterY() - playerY;
    double dist = Math.sqrt(dx*dx + dy*dy);
    
    if (dist <= activationRadius) {
        sim.activateRegion(region.getId());
    }
}
```

#### deactivateRegion

```java
public void deactivateRegion(int regionId)
```

Deactivate a region (switch from ACTIVE to BACKGROUND simulation).

**Parameters:**
- `regionId` — ID of region to deactivate

**Behavior:**
1. Check if region is in ACTIVE state
2. Set state to BACKGROUND
3. Update `lastProcessedTick` to current tick

**Example:**
```java
// Player leaves region 1
sim.deactivateRegion(1);

Region region = sim.getRegion(1);
assert region.getState() == Region.RegionState.BACKGROUND;
```

---

### Simulation Advancement

#### tick

```java
public void tick()
```

Process one simulation tick for all regions.

**Behavior:**
1. Increment `currentTick`
2. For each active region: call `processActiveRegion()`
3. For each background region: call `processBackgroundRegion()`

**Example:**
```java
// Single tick
sim.tick();

// Main game loop
while (gameRunning) {
    sim.tick(); // Process one tick
    Thread.sleep((long)(sim.getTickLength() * 1000)); // Wait tick duration
}
```

#### advanceTicks

```java
public void advanceTicks(int numTicks)
```

Advance simulation by N ticks.

**Parameters:**
- `numTicks` — Number of ticks to process

**Example:**
```java
// Fast-forward 100 ticks
sim.advanceTicks(100);

assert sim.getCurrentTick() == 100;
```

**Use Case:**
```java
// Simulate 1 hour (3600 ticks at 1 second each)
sim.advanceTicks(3600);
```

---

### Internal Processing (Private Methods)

#### processActiveRegion

```java
private void processActiveRegion(Region region)
```

Process an active region with full simulation.

**Algorithm:**
```java
double deltaTime = tickLength * activeTickRateMultiplier;
region.regenerateResources(currentTick, deltaTime);
// TODO: Process NPCs, events, structures (Phase 1.3+)
```

**Performance:** O(n) where n = number of resource nodes in region

#### processBackgroundRegion

```java
private void processBackgroundRegion(Region region)
```

Process a background region with simplified simulation.

**Algorithm:**
```java
long ticksSinceLastProcess = currentTick - region.getLastProcessedTick();
double ticksPerBackgroundUpdate = 1.0 / backgroundTickRateMultiplier;

if (ticksSinceLastProcess >= ticksPerBackgroundUpdate) {
    double deltaTime = ticksSinceLastProcess * tickLength * backgroundTickRateMultiplier;
    region.regenerateResources(currentTick, deltaTime);
}
```

**Explanation:**
- Background regions only process every N ticks (default: 60)
- When processed, apply accumulated time with background multiplier

**Example Timeline (default config):**
- Tick 0-59: No processing
- Tick 60: Process with `deltaTime = 60 * 1.0 * (1/60) = 1.0 second`
- Tick 61-119: No processing
- Tick 120: Process with `deltaTime = 60 * 1.0 * (1/60) = 1.0 second`

#### resynchronizeRegion

```java
private void resynchronizeRegion(Region region)
```

Resynchronize a region when it becomes active.

**Algorithm:**
```java
long ticksElapsed = currentTick - region.getLastProcessedTick();
if (ticksElapsed > 0) {
    double deltaTime = ticksElapsed * tickLength * backgroundTickRateMultiplier;
    region.regenerateResources(currentTick, deltaTime);
}
region.setLastProcessedTick(currentTick);
```

**Explanation:**
- Apply all accumulated changes from `lastProcessedTick` to `currentTick`
- Use background multiplier (region was in background state)

**Example:**
```java
// Region last processed at tick 0, activated at tick 120
// Elapsed: 120 ticks
// Effective time: 120 * 1.0 * (1/60) = 2.0 seconds
// Apply 2 seconds of resource regeneration
```

---

## Getters

```java
public long getCurrentTick()
public double getTickLength()
public double getActiveTickRateMultiplier()
public double getBackgroundTickRateMultiplier()
public int getRegionCount()
public int getActiveRegionCount()
public int getBackgroundRegionCount()
```

**Example:**
```java
System.out.println("Current tick: " + sim.getCurrentTick());
System.out.println("Total regions: " + sim.getRegionCount());
System.out.println("Active: " + sim.getActiveRegionCount());
System.out.println("Background: " + sim.getBackgroundRegionCount());
```

---

## Usage Patterns

### Basic Setup and Simulation

```java
// 1. Create simulator
RegionSimulator sim = new RegionSimulator();

// 2. Create and add regions
Region region1 = new Region(1, 100, 100, 64, 64);
Region region2 = new Region(2, 200, 200, 64, 64);

sim.addRegion(region1);
sim.addRegion(region2);

// 3. Add resource nodes
ResourceNode wood1 = new ResourceNode(1, ResourceType.WOOD, 90, 90, 100.0, 5.0);
region1.addResourceNode(wood1);

// 4. Activate region with player
sim.activateRegion(1);

// 5. Run simulation
for (int i = 0; i < 100; i++) {
    sim.tick();
}
```

### Dynamic Region Activation

```java
// Activate regions near player, deactivate distant ones
void updateActiveRegions(RegionSimulator sim, int playerX, int playerY, int activationRadius) {
    for (Region region : sim.getAllRegions()) {
        double dist = distance(region.getCenterX(), region.getCenterY(), playerX, playerY);
        
        if (dist <= activationRadius) {
            sim.activateRegion(region.getId());
        } else {
            sim.deactivateRegion(region.getId());
        }
    }
}

// Call every tick or every N ticks
sim.tick();
updateActiveRegions(sim, player.getX(), player.getY(), 100);
```

### Performance Monitoring

```java
// Monitor simulation load
long startTick = sim.getCurrentTick();
long startTime = System.currentTimeMillis();

sim.advanceTicks(1000);

long endTime = System.currentTimeMillis();
long elapsed = endTime - startTime;

System.out.println("1000 ticks took " + elapsed + " ms");
System.out.println("Average: " + (elapsed / 1000.0) + " ms per tick");
```

---

## Mathematical Analysis

### Active Region Processing

**Time per tick:** `deltaTime = tickLength * activeMultiplier`

**Default:** `deltaTime = 1.0 * 1.0 = 1.0 second`

**Resource regeneration example:**
```java
// Tree with 50/100 wood, regenRate = 10
// After 1 active tick: 50 + 10*1*(1-50/100) = 55
```

### Background Region Processing

**Processing frequency:** Every `1 / backgroundMultiplier` ticks

**Default:** Every `1 / (1/60) = 60` ticks

**Effective time per processing:** `ticksSinceLastProcess * tickLength * backgroundMultiplier`

**Default:** `60 * 1.0 * (1/60) = 1.0 second`

**Resource regeneration example:**
```java
// Tree with 50/100 wood, regenRate = 10
// After 60 background ticks: 50 + 10*1*(1-50/100) = 55
```

**Observation:** Active and background regions regenerate at same _effective_ rate, but background updates are batched for performance.

### Resynchronization Example

```
Scenario:
- Region last processed at tick 0 (in background)
- Activated at tick 120
- Background multiplier: 1/60

Calculation:
- Ticks elapsed: 120 - 0 = 120
- Effective time: 120 * 1.0 * (1/60) = 2.0 seconds
- Apply 2 seconds of regeneration

Result:
- Tree with 50/100 wood, regenRate = 10
- After resync: 50 + 10*2*(1-50/100) = 50 + 10 = 60
```

---

## Design Rationale

### Why Tick-Driven Simulation?

**Problem:** Continuous time simulation is hard to synchronize in multiplayer  
**Solution:** Discrete ticks provide deterministic checkpoints

**Benefits:**
- Deterministic: same inputs → same outputs
- Networkable: broadcast tick deltas
- Debuggable: can replay tick sequences

### Why Different Tick Rates for Active/Background?

**Problem:** Full simulation of entire world is expensive  
**Solution:** Only simulate active regions in full detail

**Performance Savings:**
- 10 active + 100 background regions
- Without optimization: 110 full simulations per tick
- With optimization: 10 full + ~2 simplified = ~12 effective simulations

**Savings:** ~90% reduction in simulation cost

### Why Resynchronization?

**Problem:** Background regions fall behind current tick  
**Solution:** Apply accumulated changes when activated

**Correctness:** Ensures region state reflects elapsed time, even when not actively simulated

---

## Quality Gates & Testing

### Tick Determinism

✅ **Same seed → same region states**

```java
RegionSimulator sim1 = new RegionSimulator();
RegionSimulator sim2 = new RegionSimulator();

// Add identical regions
Region r1 = new Region(1, 256, 256, 64, 64);
Region r2 = new Region(1, 256, 256, 64, 64);

ResourceNode n1 = new ResourceNode(1, ResourceType.WOOD, 240, 240, 100.0, 10.0);
ResourceNode n2 = new ResourceNode(1, ResourceType.WOOD, 240, 240, 100.0, 10.0);

n1.setCurrentQuantity(50.0);
n2.setCurrentQuantity(50.0);

r1.addResourceNode(n1);
r2.addResourceNode(n2);

sim1.addRegion(r1);
sim2.addRegion(r2);

// Same operations
sim1.advanceTicks(100);
sim2.advanceTicks(100);

// Same results
assert n1.getCurrentQuantity() == n2.getCurrentQuantity();
```

### Region State Preservation

✅ **Activation/deactivation preserves state**

```java
// Create region with resource
Region region = new Region(1, 256, 256, 64, 64);
ResourceNode node = new ResourceNode(1, ResourceType.WOOD, 240, 240, 100.0, 10.0);
node.setCurrentQuantity(50.0);
region.addResourceNode(node);

sim.addRegion(region);

// Activate, process, deactivate
sim.activateRegion(1);
sim.advanceTicks(10);
double quantityAfterActive = node.getCurrentQuantity();
sim.deactivateRegion(1);

// State preserved
assert node.getCurrentQuantity() == quantityAfterActive;
```

### Performance Target

✅ **Simulate 10 active + 50 background regions at 1 tick/second**

```java
// Stress test: 60 regions, 10 active
for (int i = 0; i < 60; i++) {
    Region region = new Region(i, i * 64, 0, 64, 64);
    // Add resource nodes...
    if (i < 10) {
        region.setState(Region.RegionState.ACTIVE);
    }
    sim.addRegion(region);
}

long start = System.currentTimeMillis();
sim.advanceTicks(100);
long elapsed = System.currentTimeMillis() - start;

// Should complete in < 100ms (1ms per tick budget)
assert elapsed < 100;
```

---

## Performance Considerations

### Computational Complexity

**Per tick:**
- Active regions: `O(A * N)` where A = active regions, N = avg nodes per region
- Background regions: `O(B / M)` where B = background regions, M = update frequency (60)

**Example (default config):**
- 10 active regions, 10 nodes each: 10 * 10 = 100 node updates
- 100 background regions, 10 nodes each: (100 * 10) / 60 ≈ 17 node updates
- **Total:** ~117 node updates per tick

**Comparison without optimization:**
- 110 regions * 10 nodes = 1100 node updates per tick
- **Speedup:** 9.4x

### Memory Footprint

- **Simulator overhead:** ~200 bytes (tick counter, config)
- **Region map:** ~32 bytes per region
- **Total:** 200 + (numRegions * 32) + (numRegions * regionSize)

**Example:** 1000 regions = ~200 KB overhead

### Optimization Opportunities

- **Spatial partitioning:** Grid or quadtree for fast region lookup
- **Batch updates:** Process multiple background regions in parallel
- **Lazy evaluation:** Only process background regions on access

---

## Future Enhancements (Post-MVP)

### Phase 1.3+ Additions

- **NPC simulation:** Move NPCs, update AI, handle combat
- **Event processing:** Trigger and propagate events
- **Structure updates:** Apply damage, ownership changes, taxation

### Advanced Features

- **Multi-threaded simulation:** Parallelize active region processing
- **Variable tick rates:** Per-region or per-feature tick rates
- **LOD system:** More than 2 levels (active, background) → add "distant" tier

---

## Related Documentation

- **Region.md** — Region structure and state management
- **ResourceNode.md** — Resource regeneration mechanics
- **architecture_design.md** — Tick model and simulation strategy
- **specs_summary.md** — Canonical defaults and tick rates

---

## Version History

- **v0.1.0 (Nov 2025):** Initial implementation for Phase 1.2
  - Tick-driven simulation model
  - Active/background region multipliers
  - Region activation/deactivation
  - Resynchronization on activation
  - Resource node regeneration
