# EventPropagation.java

## Overview
`EventPropagation` implements the BFS (Breadth-First Search) algorithm for spreading stories and events across regions with exponential or linear decay. It manages the simulation of how narratives and occurrences spread through the game world.

## Package
`org.adventure.story`

## Purpose
- Propagate stories and events across regional boundaries
- Apply decay formulas (exponential or linear) to reduce spread probability with distance
- Prevent infinite propagation via max hops limit
- Handle cycles in region connectivity graphs
- Integrate with saturation management for cap enforcement

## Key Features

### BFS Algorithm
- Spreads from origin region to neighbors
- Tracks visited regions to prevent cycles
- Uses queue for level-order traversal
- Honors max hops distance limit

### Decay Models
- **Exponential Decay**: `decay(h) = exp(-k * h)` where k=0.8 (default)
- **Linear Decay**: `decay(h) = max(0, 1 - (k * h))` where k=0.15 (default)
- Exponential preferred for realistic propagation (rapid falloff)

### Saturation Integration
- Uses `SaturationManager` to calculate effective probabilities
- Formula: `effectiveP = baseP * decay(h) * saturationFactor(region)`
- Respects per-region caps (50 stories, 20 events)

### Visited Tracking
- Prevents re-visiting regions in same propagation pass
- Allows future propagation from different sources
- Enables multiple overlapping spreads

## Constructor

```java
public EventPropagation(SaturationManager saturationManager)
```

**Parameters**:
- `saturationManager`: Manager for tracking region saturation and calculating effective probabilities

**Example**:
```java
SaturationManager satMgr = new SaturationManager();
EventPropagation propagation = new EventPropagation(satMgr);
```

## Methods

### propagateStory
```java
public void propagateStory(
    Story story,
    String originRegionId,
    Map<String, List<String>> regionNeighbors,
    Random random
)
```

**Description**: Propagates a story from origin region to neighbors using BFS with exponential decay.

**Parameters**:
- `story` (Story): The story to propagate (mutated: hopCount, affectedRegions, lastProcessedTick)
- `originRegionId` (String): Starting region ID
- `regionNeighbors` (Map<String, List<String>>): Graph of region connectivity
- `random` (Random): Seeded RNG for deterministic propagation

**Behavior**:
1. Register story with saturation manager
2. Initialize BFS queue with origin (hopCount=0)
3. While queue not empty and hopCount < story.maxHops:
   - Dequeue current region
   - For each neighbor not yet visited:
     - Calculate decay: `exp(-0.8 * hopCount)`
     - Get saturation factor from manager
     - Compute effective probability: `baseP * decay * satFactor`
     - Roll random check
     - If successful and not at max hops:
       - Add story to region's affected set
       - Update story hopCount
       - Enqueue neighbor for next level
4. Update story lastProcessedTick

**Example**:
```java
Story legend = createLegend();
String originRegion = "region_1024";
Map<String, List<String>> neighbors = buildRegionGraph();
Random rng = new Random(12345);

propagation.propagateStory(legend, originRegion, neighbors, rng);

// Story now has:
// - affectedRegions populated
// - hopCount updated
// - lastProcessedTick set
```

**Side Effects**:
- Mutates `story` object (hopCount, affectedRegions, lastProcessedTick)
- Registers story with saturation manager
- Does NOT mutate regionNeighbors or random

---

### propagateEvent
```java
public void propagateEvent(
    Event event,
    String originRegionId,
    Map<String, List<String>> regionNeighbors,
    Random random
)
```

**Description**: Propagates an event from origin region using same BFS algorithm as stories.

**Parameters**: Same as `propagateStory`

**Behavior**: Identical to `propagateStory` but operates on Event objects instead.

**Example**:
```java
Event combat = createCombatEvent();
propagation.propagateEvent(combat, "region_2048", neighbors, rng);
```

**Note**: Events and stories use separate saturation caps (20 vs 50) but same propagation algorithm.

---

### calculateExponentialDecay
```java
public double calculateExponentialDecay(int hopCount)
```

**Description**: Calculates exponential decay factor for given distance.

**Parameters**:
- `hopCount` (int): Distance from origin (0 = origin)

**Returns**: `double` - Decay factor in [0.0, 1.0]

**Formula**: `exp(-k * hopCount)` where k = 0.8

**Decay Values**:
- hopCount=0: 1.000 (100% - origin)
- hopCount=1: 0.449 (44.9%)
- hopCount=2: 0.201 (20.1%)
- hopCount=3: 0.091 (9.1%)
- hopCount=4: 0.041 (4.1%)
- hopCount=5: 0.018 (1.8%)
- hopCount=6: 0.008 (0.8%)
- hopCount=7: 0.004 (0.4%)
- hopCount=8: 0.002 (0.2%)

**Example**:
```java
double decay1 = propagation.calculateExponentialDecay(1);
// decay1 ≈ 0.449

double decay3 = propagation.calculateExponentialDecay(3);
// decay3 ≈ 0.091
```

**Rationale**: Exponential decay creates realistic falloff - stories spread quickly nearby but rarely reach distant regions.

---

### calculateLinearDecay
```java
public double calculateLinearDecay(int hopCount)
```

**Description**: Calculates linear decay factor for given distance.

**Parameters**:
- `hopCount` (int): Distance from origin

**Returns**: `double` - Decay factor in [0.0, 1.0]

**Formula**: `max(0, 1 - (k * hopCount))` where k = 0.15

**Decay Values**:
- hopCount=0: 1.000 (100%)
- hopCount=1: 0.850 (85%)
- hopCount=2: 0.700 (70%)
- hopCount=3: 0.550 (55%)
- hopCount=4: 0.400 (40%)
- hopCount=5: 0.250 (25%)
- hopCount=6: 0.100 (10%)
- hopCount=7: 0.000 (0% - cutoff)

**Example**:
```java
double decay1 = propagation.calculateLinearDecay(1);
// decay1 = 0.85

double decay7 = propagation.calculateLinearDecay(7);
// decay7 = 0.0 (no propagation beyond hop 7)
```

**Note**: Currently not used in propagation (exponential is default). Available for future configuration.

## Inner Class: PropagationNode

```java
private static class PropagationNode {
    String regionId;
    int hopCount;
    
    PropagationNode(String regionId, int hopCount) {
        this.regionId = regionId;
        this.hopCount = hopCount;
    }
}
```

**Purpose**: Encapsulates region ID and hop count for BFS queue.

**Usage**: Internal to propagation algorithm, not exposed publicly.

## Propagation Algorithm Details

### BFS Queue Structure
```
Queue: [(origin, 0)]
Step 1: Process origin → enqueue neighbors at hop 1
Queue: [(neighbor1, 1), (neighbor2, 1), ...]
Step 2: Process hop 1 neighbors → enqueue hop 2 neighbors
Queue: [(neighbor3, 2), (neighbor4, 2), ...]
...continues until maxHops or queue empty
```

### Effective Probability Calculation
```java
double decay = calculateExponentialDecay(hopCount);
double saturationFactor = saturationManager.getSaturationFactor(
    neighborId, 
    isStory ? "STORY" : "EVENT"
);
double effectiveProb = baseProbability * decay * saturationFactor;

if (random.nextDouble() < effectiveProb) {
    // Propagate to this neighbor
}
```

### Visited Tracking
```java
Set<String> visited = new HashSet<>();
visited.add(originRegionId); // Mark origin as visited

// During BFS
if (!visited.contains(neighborId)) {
    visited.add(neighborId);
    // Process neighbor
}
```

## Configuration Constants

### DECAY_CONSTANT_EXPONENTIAL
```java
private static final double DECAY_CONSTANT_EXPONENTIAL = 0.8;
```

Higher values = faster decay. Default 0.8 gives rapid but reasonable falloff.

### DECAY_CONSTANT_LINEAR
```java
private static final double DECAY_CONSTANT_LINEAR = 0.15;
```

Controls slope of linear decay. Default 0.15 gives ~7 hops before cutoff.

## Usage Examples

### Basic Story Propagation
```java
// Setup
SaturationManager satMgr = new SaturationManager();
EventPropagation propagation = new EventPropagation(satMgr);

// Create story
Story rumor = new Story.Builder()
    .id("story_rumor_001")
    .storyType(StoryType.RUMOR)
    .baseProbability(0.75)
    .maxHops(6)
    .build();

// Define region graph
Map<String, List<String>> neighbors = new HashMap<>();
neighbors.put("region_1", List.of("region_2", "region_3"));
neighbors.put("region_2", List.of("region_1", "region_4"));
neighbors.put("region_3", List.of("region_1", "region_5"));
// ...

// Propagate
Random rng = new Random(12345);
propagation.propagateStory(rumor, "region_1", neighbors, rng);

// Check results
System.out.println("Affected regions: " + rumor.getAffectedRegions());
System.out.println("Max hop reached: " + rumor.getHopCount());
```

### Event Propagation with Saturation
```java
// Register multiple events to approach saturation
for (int i = 0; i < 15; i++) {
    Event event = createEvent("event_" + i);
    satMgr.registerEvent("region_A", event);
}

// Now propagate new event
Event newEvent = createEvent("event_new");
propagation.propagateEvent(newEvent, "region_A", neighbors, rng);

// Propagation probability reduced by saturation factor
// effectiveP = 0.8 * decay * (1 - 15/20) = 0.8 * decay * 0.25
```

### Custom Decay Testing
```java
// Test decay values
for (int hop = 0; hop <= 8; hop++) {
    double expDecay = propagation.calculateExponentialDecay(hop);
    double linDecay = propagation.calculateLinearDecay(hop);
    System.out.printf("Hop %d: exp=%.3f, lin=%.3f%n", hop, expDecay, linDecay);
}
```

## Design Decisions

### BFS vs DFS
- BFS chosen for level-order spreading (natural propagation model)
- DFS would create unrealistic "tentacle" patterns
- BFS ensures stories spread evenly in all directions

### Exponential vs Linear Decay
- Exponential more realistic (information decay in real world)
- Linear easier to understand but less natural
- Default exponential, linear available for testing/comparison

### Decay Constant (k=0.8)
- Tuned for 6-hop max distance
- At 6 hops: exp(-0.8*6) ≈ 0.8% chance
- Prevents stories from spanning entire world
- Balances realism with gameplay reach

### Saturation Integration
- Prevents region spam (max 50 stories, 20 events)
- Graceful degradation as caps approached
- Soft cap at 80% (warnings), hard cap at 100%

### Visited Tracking Per Propagation
- Each propagation call has own visited set
- Allows multiple stories to overlap regions
- Prevents infinite loops in cyclic graphs

## Testing

### Test Coverage
15 tests in `EventPropagationTest.java`:
- BFS order verification
- Exponential decay formula validation
- Linear decay formula validation
- Max hops enforcement
- Cycle handling
- Saturation factor integration
- Story and event propagation
- Visited tracking
- Empty neighbor handling

### Determinism Validation
Same seed + same inputs → same propagation results.

### Edge Cases
- No neighbors (origin only)
- Cyclic graphs (A→B→C→A)
- Max hops reached (stops spreading)
- 100% saturation (no propagation)

## Performance Considerations

### Time Complexity
- O(R + E) where R = regions, E = region connections (edges)
- BFS visits each region at most once per propagation
- Decay calculation: O(1)
- Saturation lookup: O(1) with HashMap

### Space Complexity
- O(R) for visited set
- O(R) for BFS queue (worst case: all regions in queue)
- O(1) additional working memory

### Optimization Opportunities
- Cache decay values (only 7-8 unique values per propagation)
- Batch propagation (multiple stories at once)
- Parallel propagation (Phase 2)

## Integration Points

### SaturationManager
- Required dependency, injected via constructor
- Consulted for every propagation decision
- Tracks story/event counts per region

### Story/Event Classes
- Mutated by propagation (hopCount, affectedRegions, lastProcessedTick)
- Read for parameters (baseProbability, maxHops)

### Region System
- Requires region neighbor graph
- Graph structure: Map<String, List<String>>
- Does not mutate region data directly

## Future Enhancements

### Phase 2
- Configurable decay model (exponential, linear, custom)
- Multi-source propagation (multiple origins)
- Directional propagation (rivers, roads boost spread)
- Event-driven propagation (triggered by player actions)
- Propagation history tracking

### Advanced Features
- 3D propagation (altitude affects spread)
- Terrain-based resistance (mountains slow spread)
- Faction boundaries (stories don't cross borders)
- Propagation visualization (animation, heatmaps)

## Related Classes
- `Story`: Data model for narrative propagation
- `Event`: Data model for event propagation
- `SaturationManager`: Enforces region caps, calculates saturation factors
- `Region`: Provides neighbor connectivity graph

## References
- Design: `docs/stories_events.md` → Event Propagation
- Specs: `docs/specs_summary.md` → Propagation Formulas (k=0.8, maxHops=6)
- Summary: `archive/PHASE_1.7_SUMMARY.md`
- Source: `src/main/java/org/adventure/story/EventPropagation.java`
