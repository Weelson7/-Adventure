# StoryGeneratorTest.java

## Overview
Test suite for `StoryGenerator` class, focusing on deterministic story generation, biome affinity distribution, and worldgen integration.

## Package
`org.adventure` (test package)

## Test Class
```java
public class StoryGeneratorTest
```

## Purpose
- Verify deterministic story generation from world seed
- Test biome-based story type selection (affinity mapping)
- Validate story count scaling with world size
- Ensure all generated stories are valid and complete
- Check metadata attachment (biome, coordinates)

## Test Coverage
**15 tests** covering determinism, scaling, biome affinity, and edge cases

### Line Coverage
~95% (estimated) - all public methods and biome affinity paths tested

## Test Methods

### testGenerateStoriesDeterministic
**Purpose**: Verify same seed produces identical stories (critical for determinism)

**Test Flow**:
1. Generate stories with seed=12345, totalTiles=50000
2. Generate stories again with same seed
3. Assert counts equal
4. Assert story IDs match in order
5. Assert story types match in order

**Assertions**:
```java
List<Story> stories1 = generator.generateStories(12345L, 50000, biomeMap);
List<Story> stories2 = generator.generateStories(12345L, 50000, biomeMap);

assertEquals(stories1.size(), stories2.size());
for (int i = 0; i < stories1.size(); i++) {
    assertEquals(stories1.get(i).getId(), stories2.get(i).getId());
    assertEquals(stories1.get(i).getStoryType(), stories2.get(i).getStoryType());
    assertEquals(stories1.get(i).getOriginTileId(), stories2.get(i).getOriginTileId());
}
```

**Rationale**: Determinism is critical for regression testing, speedruns, and reproducible bugs.

---

### testGenerateStoriesDifferentSeeds
**Purpose**: Verify different seeds produce different stories

**Test Flow**:
1. Generate stories with seed=12345
2. Generate stories with seed=67890
3. Assert at least one story differs (ID, type, or origin)

**Assertions**:
```java
List<Story> stories1 = generator.generateStories(12345L, 50000, biomeMap);
List<Story> stories2 = generator.generateStories(67890L, 50000, biomeMap);

// At least one difference
boolean foundDifference = false;
for (int i = 0; i < Math.min(stories1.size(), stories2.size()); i++) {
    if (!stories1.get(i).getId().equals(stories2.get(i).getId()) ||
        !stories1.get(i).getStoryType().equals(stories2.get(i).getStoryType())) {
        foundDifference = true;
        break;
    }
}
assertTrue(foundDifference);
```

---

### testStoryCountScaling
**Purpose**: Verify story count scales correctly with world size

**Test Cases**:
- 10,000 tiles → ~5 stories (10k * 5/10k = 5)
- 50,000 tiles → ~25 stories (50k * 5/10k = 25)
- 100,000 tiles → ~50 stories (100k * 5/10k = 50)
- 200,000 tiles → ~100 stories (200k * 5/10k = 100)

**Formula**: `storyCount = (totalTiles / 10000) * 5`

**Assertions**:
```java
List<Story> stories = generator.generateStories(seed, 50000, biomeMap);
int expectedCount = (50000 / 10000) * 5; // 25
assertEquals(expectedCount, stories.size());
```

---

### testAllStoriesValid
**Purpose**: Verify all generated stories pass validation

**Validations**:
- ID not null or empty
- StoryType not null
- Title not null or empty
- baseProbability in [0.0, 1.0]
- priority in [0, 10]
- hopCount == 0 (initial)
- maxHops > 0
- status == ACTIVE

**Pattern**:
```java
List<Story> stories = generator.generateStories(seed, totalTiles, biomeMap);

for (Story story : stories) {
    assertNotNull(story.getId());
    assertFalse(story.getId().isEmpty());
    assertNotNull(story.getStoryType());
    assertNotNull(story.getTitle());
    assertTrue(story.getBaseProbability() >= 0.0 && 
               story.getBaseProbability() <= 1.0);
    assertTrue(story.getPriority() >= 0 && story.getPriority() <= 10);
    assertEquals(0, story.getHopCount());
    assertTrue(story.getMaxHops() > 0);
    assertEquals(StoryStatus.ACTIVE, story.getStatus());
}
```

---

### testBiomeAffinityDistribution
**Purpose**: Verify story types match biome affinities

**Test Setup**:
- Create biome map with 1000 MOUNTAIN tiles
- Generate stories
- Count story types

**Expected**:
- LEGEND and PROPHECY should be more common (3x weight)
- RUMOR, QUEST, etc. should be less common (1x weight)

**Pattern**:
```java
// Create mountain-heavy biome map
Map<Integer, Biome> mountainMap = new HashMap<>();
for (int i = 0; i < 10000; i++) {
    mountainMap.put(i, Biome.MOUNTAIN);
}

List<Story> stories = generator.generateStories(seed, 10000, mountainMap);

// Count types
int legendCount = 0;
int prophecyCount = 0;
int comedyCount = 0;

for (Story story : stories) {
    if (story.getStoryType() == StoryType.LEGEND) legendCount++;
    if (story.getStoryType() == StoryType.PROPHECY) prophecyCount++;
    if (story.getStoryType() == StoryType.COMEDY) comedyCount++;
}

// LEGEND and PROPHECY should dominate
assertTrue(legendCount + prophecyCount > comedyCount * 2);
```

---

### testMountainBiomeAffinity
**Purpose**: Verify MOUNTAIN biome favors LEGEND and PROPHECY

**Affinity Weights**:
- LEGEND: 3.0 (high)
- PROPHECY: 3.0 (high)
- Others: 1.0 (base)

**Test**: Generate 100 stories from mountain biomes, verify LEGEND+PROPHECY > 40%

---

### testCoastBiomeAffinity
**Purpose**: Verify COAST biome favors RUMOR and COMEDY

**Affinity Weights**:
- RUMOR: 3.0 (high)
- COMEDY: 1.5 (medium)
- Others: 1.0 (base)

**Test**: Generate 100 stories from coast biomes, verify RUMOR+COMEDY > 30%

---

### testDesertBiomeAffinity
**Purpose**: Verify DESERT biome favors TRAGEDY and MYSTERY

**Affinity Weights**:
- TRAGEDY: 3.0 (high)
- MYSTERY: 3.0 (high)
- Others: 1.0 (base)

---

### testForestBiomeAffinity
**Purpose**: Verify FOREST biome favors QUEST and MYSTERY

**Affinity Weights**:
- QUEST: 2.0 (medium)
- MYSTERY: 3.0 (high)

---

### testStoryMetadataAttachment
**Purpose**: Verify metadata includes biome, x, y coordinates

**Expected Metadata**:
- "biome": biome name string (e.g., "MOUNTAIN")
- "x": tile X coordinate
- "y": tile Y coordinate

**Assertions**:
```java
List<Story> stories = generator.generateStories(seed, totalTiles, biomeMap);

for (Story story : stories) {
    Map<String, Object> metadata = story.getMetadata();
    assertTrue(metadata.containsKey("biome"));
    assertTrue(metadata.containsKey("x"));
    assertTrue(metadata.containsKey("y"));
    
    // Verify biome matches origin tile
    int tileId = story.getOriginTileId();
    Biome expectedBiome = biomeMap.get(tileId);
    assertEquals(expectedBiome.name(), metadata.get("biome"));
}
```

---

### testEmptyBiomeMap
**Purpose**: Verify generator handles empty biome map gracefully

**Expected**: Return empty list (no tiles → no stories)

**Assertion**:
```java
Map<Integer, Biome> emptyMap = new HashMap<>();
List<Story> stories = generator.generateStories(seed, 0, emptyMap);
assertTrue(stories.isEmpty());
```

---

### testSmallWorld
**Purpose**: Verify generator handles small worlds (<10k tiles)

**Test Case**: 5,000 tiles → ~2-3 stories

**Assertions**:
```java
List<Story> stories = generator.generateStories(seed, 5000, biomeMap);
int expectedCount = (5000 / 10000) * 5; // 2.5 → 2
assertTrue(stories.size() >= 0 && stories.size() <= 5);
```

---

### testLargeWorld
**Purpose**: Verify generator handles large worlds (>100k tiles)

**Test Case**: 500,000 tiles → ~250 stories

**Assertions**:
```java
List<Story> stories = generator.generateStories(seed, 500000, largeBiomeMap);
int expectedCount = (500000 / 10000) * 5; // 250
assertEquals(expectedCount, stories.size());
```

---

### testStoryIDUniqueness
**Purpose**: Verify all generated story IDs are unique

**Pattern**:
```java
List<Story> stories = generator.generateStories(seed, totalTiles, biomeMap);

Set<String> ids = new HashSet<>();
for (Story story : stories) {
    assertTrue(ids.add(story.getId())); // add() returns false if duplicate
}
```

---

### testStoryOriginTileValidity
**Purpose**: Verify all origin tile IDs exist in biome map

**Assertions**:
```java
List<Story> stories = generator.generateStories(seed, totalTiles, biomeMap);

for (Story story : stories) {
    int tileId = story.getOriginTileId();
    assertTrue(biomeMap.containsKey(tileId));
}
```

## Test Data Patterns

### Sample Biome Map
```java
private Map<Integer, Biome> createTestBiomeMap(int tileCount, Biome biome) {
    Map<Integer, Biome> map = new HashMap<>();
    for (int i = 0; i < tileCount; i++) {
        map.put(i, biome);
    }
    return map;
}

private Map<Integer, Biome> createMixedBiomeMap(int tileCount) {
    Map<Integer, Biome> map = new HashMap<>();
    Biome[] biomes = Biome.values();
    for (int i = 0; i < tileCount; i++) {
        map.put(i, biomes[i % biomes.length]);
    }
    return map;
}
```

### Test Seeds
```java
private static final long SEED_A = 12345L;
private static final long SEED_B = 67890L;
private static final long SEED_C = 11111L;
```

## Testing Strategy

### Determinism Testing
- Same seed → identical output (IDs, types, origins)
- Different seeds → different output
- Reproducible across test runs

### Scaling Testing
- Small worlds (1k-10k tiles)
- Medium worlds (50k-100k tiles)
- Large worlds (500k+ tiles)

### Distribution Testing
- Biome affinity weights respected
- All story types can appear
- No type completely absent (unless 100% affinity elsewhere)

### Edge Case Testing
- Empty biome map
- Single tile
- All tiles same biome
- Mixed biomes

## Integration Testing

### With WorldGen
```java
@Test
public void testWorldGenIntegration() {
    WorldGen worldGen = new WorldGen();
    World world = worldGen.generate(12345L, 100, 100); // 10k tiles
    
    Map<Integer, Biome> biomes = world.getTileBiomeMap();
    StoryGenerator gen = new StoryGenerator();
    
    List<Story> stories = gen.generateStories(12345L, 10000, biomes);
    
    // Should generate ~5 stories
    assertEquals(5, stories.size());
}
```

## Performance Testing

### Time Complexity
- O(n) where n = number of stories
- Each story: O(1) creation
- Biome map lookup: O(1)

### Benchmarks
- 10k tiles: <5ms
- 100k tiles: <50ms
- 1M tiles: <500ms

### Memory Usage
- Biome map: not copied (read-only)
- Result list: O(n) where n = story count
- Temporary objects: minimal (RNG, builder)

## Test Utilities

### Story Counter
```java
private int countStoryType(List<Story> stories, StoryType type) {
    return (int) stories.stream()
        .filter(s -> s.getStoryType() == type)
        .count();
}
```

### Distribution Analyzer
```java
private Map<StoryType, Integer> analyzeDistribution(List<Story> stories) {
    Map<StoryType, Integer> counts = new HashMap<>();
    for (Story story : stories) {
        counts.merge(story.getStoryType(), 1, Integer::sum);
    }
    return counts;
}
```

## Future Test Enhancements

### Phase 2
- Story template testing
- Multi-biome story testing
- Story cluster testing
- Historical story generation
- Custom density testing

### Advanced Testing
- Property-based testing (QuickCheck)
- Mutation testing
- Coverage analysis
- Performance regression testing

## Related Test Classes
- `StoryTest`: Tests Story data model
- `BiomeTest`: Tests Biome enum
- `WorldGenTest`: Tests worldgen integration

## References
- Source: `src/main/java/org/adventure/story/StoryGenerator.java`
- Docs: `doc-src/main/java/org/adventure/story/StoryGenerator.md`
- Summary: `archive/PHASE_1.7_SUMMARY.md`
