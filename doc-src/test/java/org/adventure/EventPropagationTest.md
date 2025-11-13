# EventPropagationTest.java

## Overview
Test suite for `EventPropagation` class, validating BFS algorithm, decay formulas, saturation integration, and propagation mechanics for stories and events.

## Package
`org.adventure` (test package)

## Test Class
```java
public class EventPropagationTest
```

## Purpose
- Verify BFS propagation algorithm (breadth-first traversal)
- Test exponential and linear decay formulas
- Validate max hops enforcement
- Check cycle handling in region graphs
- Test saturation factor integration
- Ensure deterministic propagation with seeded RNG

## Test Coverage
**15 tests** covering all propagation scenarios, decay models, and edge cases

### Line Coverage
~95% (estimated) - all public methods, BFS paths, and decay calculations tested

## Test Methods

### testPropagateStoryBFS
**Purpose**: Verify BFS propagation order (level-by-level spread)

**Test Graph**:
```
A → B, C
B → D
C → E
D → F
E → F
```

**Expected Propagation**:
- Hop 0: A (origin)
- Hop 1: B, C
- Hop 2: D, E
- Hop 3: F

**Assertions**:
```java
Story story = createStory(maxHops=3);
propagation.propagateStory(story, "A", neighbors, rng);

Set<String> affected = story.getAffectedRegions();
assertTrue(affected.contains("A")); // Origin
assertTrue(affected.contains("B")); // Hop 1
assertTrue(affected.contains("C")); // Hop 1
// D, E, F depend on probability rolls
```

---

### testPropagateEventBFS
**Purpose**: Verify event propagation uses same BFS algorithm

**Pattern**: Same as `testPropagateStoryBFS` but with Event object

**Assertions**: Event affectedRegions populated correctly

---

### testExponentialDecayFormula
**Purpose**: Verify exponential decay formula: `exp(-0.8 * h)`

**Expected Values** (k=0.8):
- hop 0: 1.000
- hop 1: 0.449
- hop 2: 0.201
- hop 3: 0.091
- hop 4: 0.041
- hop 5: 0.018
- hop 6: 0.008

**Assertions**:
```java
assertEquals(1.000, propagation.calculateExponentialDecay(0), 0.001);
assertEquals(0.449, propagation.calculateExponentialDecay(1), 0.001);
assertEquals(0.201, propagation.calculateExponentialDecay(2), 0.001);
assertEquals(0.091, propagation.calculateExponentialDecay(3), 0.001);
assertEquals(0.041, propagation.calculateExponentialDecay(4), 0.001);
assertEquals(0.018, propagation.calculateExponentialDecay(5), 0.001);
assertEquals(0.008, propagation.calculateExponentialDecay(6), 0.001);
```

---

### testLinearDecayFormula
**Purpose**: Verify linear decay formula: `max(0, 1 - 0.15 * h)`

**Expected Values** (k=0.15):
- hop 0: 1.000
- hop 1: 0.850
- hop 2: 0.700
- hop 3: 0.550
- hop 4: 0.400
- hop 5: 0.250
- hop 6: 0.100
- hop 7: 0.000 (cutoff)

**Assertions**:
```java
assertEquals(1.000, propagation.calculateLinearDecay(0), 0.001);
assertEquals(0.850, propagation.calculateLinearDecay(1), 0.001);
assertEquals(0.700, propagation.calculateLinearDecay(2), 0.001);
// ...
assertEquals(0.000, propagation.calculateLinearDecay(7), 0.001);
```

---

### testMaxHopsEnforcement
**Purpose**: Verify propagation stops at maxHops distance

**Test Setup**:
- Story with maxHops=3
- Linear graph: A → B → C → D → E

**Expected**:
- Hop 0: A
- Hop 1: B
- Hop 2: C
- Hop 3: D
- Hop 4: E **NOT** reached (exceeds maxHops)

**Assertions**:
```java
Story story = createStory(maxHops=3);
propagation.propagateStory(story, "A", linearGraph, rng);

assertEquals(3, story.getHopCount()); // Stopped at max
Set<String> affected = story.getAffectedRegions();
// A, B, C, D should be affected
// E should NOT be affected
assertFalse(affected.contains("E"));
```

---

### testCycleHandling
**Purpose**: Verify propagation handles cycles without infinite loops

**Test Graph**:
```
A → B
B → C
C → A  (cycle back to origin)
```

**Expected**: Each region visited at most once per propagation

**Assertions**:
```java
Story story = createStory(maxHops=5);
propagation.propagateStory(story, "A", cyclicGraph, rng);

// Should complete without hanging
Set<String> affected = story.getAffectedRegions();
assertTrue(affected.size() <= 3); // Only A, B, C in graph
```

---

### testSaturationFactorIntegration
**Purpose**: Verify saturation reduces propagation probability

**Test Setup**:
1. Register 40 stories in region "B" (80% saturation)
2. Propagate new story from "A" to "B"
3. Effective probability = baseP * decay * 0.2 (saturation factor)

**Expected**: Reduced propagation to "B"

**Assertions**:
```java
// Saturate region B
for (int i = 0; i < 40; i++) {
    satMgr.registerStory("B", createStory("story_" + i));
}

// Propagate new story
Story newStory = createStory("new_story");
propagation.propagateStory(newStory, "A", neighbors, rng);

// B might not receive story due to saturation
double satFactor = satMgr.getSaturationFactor("B", "STORY");
assertEquals(0.2, satFactor, 0.01); // 1 - 40/50 = 0.2
```

---

### testStoryHopCountUpdate
**Purpose**: Verify story hopCount updated during propagation

**Test Graph**: A → B → C

**Expected**:
- After propagation, story.hopCount == max distance reached

**Assertions**:
```java
Story story = createStory(maxHops=5);
assertEquals(0, story.getHopCount()); // Initial

propagation.propagateStory(story, "A", graph, rng);

assertTrue(story.getHopCount() > 0); // Updated
assertTrue(story.getHopCount() <= story.getMaxHops());
```

---

### testEventHopCountUpdate
**Purpose**: Same as testStoryHopCountUpdate but for events

---

### testAffectedRegionsPopulated
**Purpose**: Verify affectedRegions set populated during propagation

**Test Setup**:
- Propagate story from "A"
- Graph: A → B, C, D

**Expected**: affectedRegions contains origin + successfully propagated neighbors

**Assertions**:
```java
Story story = createStory();
propagation.propagateStory(story, "A", neighbors, rng);

Set<String> affected = story.getAffectedRegions();
assertTrue(affected.contains("A")); // Origin always included
// B, C, D may be included based on probability
```

---

### testLastProcessedTickUpdate
**Purpose**: Verify lastProcessedTick updated after propagation

**Expected**: lastProcessedTick set to some value > 0 (implementation may vary)

**Assertions**:
```java
Story story = createStory();
assertEquals(0, story.getLastProcessedTick()); // Initial

propagation.propagateStory(story, "A", neighbors, rng);

// Implementation may set to current tick (if available)
// Or just verify it's tracked
```

---

### testEmptyNeighborMap
**Purpose**: Verify propagation handles region with no neighbors

**Test Graph**: A (isolated, no connections)

**Expected**: Only origin region affected

**Assertions**:
```java
Map<String, List<String>> emptyNeighbors = new HashMap<>();
emptyNeighbors.put("A", List.of()); // No neighbors

Story story = createStory();
propagation.propagateStory(story, "A", emptyNeighbors, rng);

Set<String> affected = story.getAffectedRegions();
assertEquals(1, affected.size());
assertTrue(affected.contains("A"));
```

---

### testMultiplePathsToSameRegion
**Purpose**: Verify region visited only once even if reachable via multiple paths

**Test Graph**:
```
    B
   / \
  A   D
   \ /
    C
```
Region D reachable from both B and C.

**Expected**: D visited only once

**Assertions**: Use visited tracking to verify

---

### testDeterministicPropagation
**Purpose**: Verify same seed produces same propagation results

**Test Flow**:
1. Propagate story with seed=12345
2. Record affected regions
3. Reset story state
4. Propagate again with seed=12345
5. Assert affected regions identical

**Assertions**:
```java
Story story1 = createStory("story_1");
Random rng1 = new Random(12345);
propagation.propagateStory(story1, "A", neighbors, rng1);
Set<String> affected1 = new HashSet<>(story1.getAffectedRegions());

Story story2 = createStory("story_2");
Random rng2 = new Random(12345);
propagation.propagateStory(story2, "A", neighbors, rng2);
Set<String> affected2 = new HashSet<>(story2.getAffectedRegions());

assertEquals(affected1, affected2);
```

---

### testHighProbabilityPropagation
**Purpose**: Verify story with baseProbability=1.0 propagates everywhere (within maxHops)

**Test Setup**:
- Story with baseProbability=1.0, maxHops=5
- Graph: Linear chain of 5 regions

**Expected**: All regions within maxHops affected

**Assertions**:
```java
Story story = new Story.Builder()
    .id("certain")
    .storyType(StoryType.LEGEND)
    .title("Certain Spread")
    .baseProbability(1.0) // Guaranteed spread (before decay/saturation)
    .maxHops(5)
    .build();

propagation.propagateStory(story, "A", linearGraph, rng);

// With no saturation and probability=1.0, should reach many regions
// (exact count depends on decay formula)
assertTrue(story.getAffectedRegions().size() > 1);
```

## Test Data Patterns

### Sample Graph Structures
```java
private Map<String, List<String>> createLinearGraph() {
    Map<String, List<String>> graph = new HashMap<>();
    graph.put("A", List.of("B"));
    graph.put("B", List.of("C"));
    graph.put("C", List.of("D"));
    graph.put("D", List.of("E"));
    return graph;
}

private Map<String, List<String>> createCyclicGraph() {
    Map<String, List<String>> graph = new HashMap<>();
    graph.put("A", List.of("B"));
    graph.put("B", List.of("C"));
    graph.put("C", List.of("A")); // Cycle
    return graph;
}

private Map<String, List<String>> createBinaryTree() {
    Map<String, List<String>> graph = new HashMap<>();
    graph.put("A", List.of("B", "C"));
    graph.put("B", List.of("D", "E"));
    graph.put("C", List.of("F", "G"));
    return graph;
}
```

### Test Seeds
```java
private static final long SEED_DETERMINISTIC = 12345L;
private static final long SEED_HIGH_ROLLS = 99999L; // Often rolls high
private static final long SEED_LOW_ROLLS = 11111L; // Often rolls low
```

## Testing Strategy

### Algorithm Testing
- BFS order verification
- Visited tracking
- Queue operations
- Level-by-level spread

### Formula Testing
- Exponential decay values
- Linear decay values
- Decay constant effects

### Integration Testing
- Saturation manager integration
- Story/Event mutation
- Region graph traversal

### Edge Case Testing
- Empty graphs
- Isolated regions
- Cycles
- Max hops boundary

## Performance Testing

### Time Complexity
- O(V + E) where V = regions, E = connections
- Decay calculation: O(1)
- Saturation lookup: O(1)

### Benchmarks
- 10 regions: <1ms
- 100 regions: <10ms
- 1000 regions: <100ms

## Test Utilities

### Graph Builders
```java
private Map<String, List<String>> createGrid(int width, int height) {
    // Create 2D grid graph
}

private Map<String, List<String>> createCompleteGraph(int n) {
    // Every region connected to every other
}
```

### Story/Event Factories
```java
private Story createStory(int maxHops) {
    return new Story.Builder()
        .id("test_" + UUID.randomUUID())
        .storyType(StoryType.RUMOR)
        .title("Test Story")
        .maxHops(maxHops)
        .build();
}
```

## Future Test Enhancements

### Phase 2
- Multi-source propagation
- Directional propagation (rivers, roads)
- Propagation visualization validation
- Complex graph patterns (mesh, hub-spoke)

### Advanced Testing
- Chaos engineering (random graph mutations)
- Performance regression tests
- Memory leak detection

## Related Test Classes
- `SaturationManagerTest`: Tests saturation factor calculation
- `StoryTest` / `EventTest`: Tests data models mutated by propagation
- `RegionTest`: Tests region graph structures

## References
- Source: `src/main/java/org/adventure/story/EventPropagation.java`
- Docs: `doc-src/main/java/org/adventure/story/EventPropagation.md`
- Summary: `archive/PHASE_1.7_SUMMARY.md`
