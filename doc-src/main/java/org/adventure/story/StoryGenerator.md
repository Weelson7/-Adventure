# StoryGenerator.java

## Overview
`StoryGenerator` is responsible for deterministic story seeding during worldgen (tick 0). It creates initial stories based on world characteristics, biome types, and a seeded random number generator to ensure reproducibility.

## Package
`org.adventure.story`

## Purpose
- Generate initial stories at worldgen (tick 0)
- Ensure deterministic story placement from world seed
- Scale story count with world size
- Map story types to appropriate biomes via affinity system
- Create diverse narrative foundation for gameplay

## Key Features

### Deterministic Generation
- Uses seeded RNG for reproducibility
- Same world seed → same stories every time
- Seed derivation: `worldSeed XOR 0xDEADBEEF`
- Critical for regression testing and speedruns

### Biome-Based Story Selection
- Each story type has biome affinity weights
- Mountain biomes favor LEGEND and PROPHECY
- Coast biomes favor SOCIAL and RUMOR
- Volcanic biomes favor MAGICAL and TRAGEDY
- Ensures thematic coherence

### Scaling with World Size
- **Default**: 5 stories per 10,000 tiles
- Adjustable via `storiesPerTenKTiles` parameter
- Prevents overwhelming small worlds or starving large worlds
- Example: 100,000 tile world → ~50 initial stories

## Constructor

```java
public StoryGenerator()
```

No-argument constructor. Simple utility class with static-like behavior.

## Methods

### generateStories
```java
public List<Story> generateStories(
    long worldSeed,
    int totalTiles,
    Map<Integer, Biome> tileIdToBiomeMap
)
```

**Description**: Generates initial stories for worldgen based on world parameters.

**Parameters**:
- `worldSeed` (long): World generation seed for reproducibility
- `totalTiles` (int): Total number of tiles in the world
- `tileIdToBiomeMap` (Map<Integer, Biome>): Mapping of tile IDs to their biomes

**Returns**: `List<Story>` - Generated stories, each with unique ID and placement

**Algorithm**:
1. Calculate story count: `(totalTiles / 10000) * storiesPerTenKTiles`
2. Create seeded RNG: `new Random(worldSeed ^ 0xDEADBEEF)`
3. For each story:
   - Select random tile from map
   - Get biome for that tile
   - Select story type based on biome affinity
   - Generate story with appropriate parameters
   - Add to results list
4. Return list of stories

**Example**:
```java
// Worldgen context
long worldSeed = 12345L;
int totalTiles = 50000;
Map<Integer, Biome> biomes = worldGen.getTileBiomeMap();

// Generate stories
StoryGenerator generator = new StoryGenerator();
List<Story> initialStories = generator.generateStories(
    worldSeed,
    totalTiles,
    biomes
);

// Result: ~25 stories (50000 / 10000 * 5)
```

**Thread Safety**: Not thread-safe. Should be called once during worldgen from single thread.

---

### selectStoryType
```java
private StoryType selectStoryType(Biome biome, Random random)
```

**Description**: Selects a story type based on biome affinity weights.

**Parameters**:
- `biome` (Biome): The biome enum value
- `random` (Random): Seeded RNG for deterministic selection

**Returns**: `StoryType` - Selected story type based on weighted probabilities

**Biome Affinity Weights**:

| Story Type | High Affinity (3.0) | Medium Affinity (2.0) | Low Affinity (1.5) | Base (1.0) |
|------------|---------------------|----------------------|-------------------|-----------|
| LEGEND | Mountains, Rainforest/Jungle, Oceans, Volcanic | - | - | All others |
| RUMOR | Coast, Savanna, Lakes | - | - | All others |
| QUEST | - | Forests, Grasslands, Hills, Savanna | - | All others |
| PROPHECY | Mountains, Rainforest/Jungle, Volcanic | - | - | All others |
| TRAGEDY | Tundra, Taiga, Deserts, Swamps | - | - | All others |
| COMEDY | - | - | Grasslands, Coast | All others |
| MYSTERY | Oceans, Lakes, Deserts, Forests, Taiga, Swamps | - | - | All others |

**Algorithm**:
1. Initialize weights map for all story types (default 1.0)
2. Apply biome-specific multipliers based on affinity table
3. Calculate total weight sum
4. Generate random value in [0, totalWeight]
5. Iterate through weights, subtract each until random value <= 0
6. Return corresponding story type

**Example**:
```java
// Mountain biome - favors LEGEND and PROPHECY
Random rng = new Random(12345);
StoryType type = selectStoryType(Biome.MOUNTAIN, rng);
// More likely to return LEGEND or PROPHECY (3x weight)

// Coast biome - favors RUMOR and COMEDY
type = selectStoryType(Biome.COAST, rng);
// More likely to return RUMOR (3x) or COMEDY (1.5x)
```

## Story Parameters by Type

When generating stories, type-specific defaults are applied:

### LEGEND
```java
.storyType(StoryType.LEGEND)
.baseProbability(0.95)
.maxHops(8)
.priority(9)
```

### RUMOR
```java
.storyType(StoryType.RUMOR)
.baseProbability(0.75)
.maxHops(6)
.priority(4)
```

### QUEST
```java
.storyType(StoryType.QUEST)
.baseProbability(0.85)
.maxHops(5)
.priority(6)
```

### PROPHECY
```java
.storyType(StoryType.PROPHECY)
.baseProbability(0.90)
.maxHops(7)
.priority(8)
```

### TRAGEDY
```java
.storyType(StoryType.TRAGEDY)
.baseProbability(0.70)
.maxHops(4)
.priority(5)
```

### COMEDY
```java
.storyType(StoryType.COMEDY)
.baseProbability(0.65)
.maxHops(3)
.priority(2)
```

### MYSTERY
```java
.storyType(StoryType.MYSTERY)
.baseProbability(0.80)
.maxHops(5)
.priority(6)
```

## Story ID Format

Generated IDs follow pattern: `story_{tileId}_{index}`

**Example**: `story_2048_3` (3rd story at tile 2048)

## Metadata Attached

Each generated story includes:
- `biome`: Biome name string (e.g., "MOUNTAIN")
- `x`: Tile X coordinate (calculated from tileId)
- `y`: Tile Y coordinate (calculated from tileId)

**Example**:
```java
story.setMetadata("biome", biome.name());
story.setMetadata("x", tileId % worldWidth);
story.setMetadata("y", tileId / worldWidth);
```

## Configuration Constants

### DEFAULT_STORIES_PER_10K_TILES
```java
private static final int DEFAULT_STORIES_PER_10K_TILES = 5;
```

Baseline story density. Adjust this value to change global story frequency.

## Usage Example

### Complete Worldgen Integration
```java
public class WorldGen {
    public World generate(long seed, int width, int height) {
        // Generate terrain
        Map<Integer, Biome> biomes = generateTerrain(seed, width, height);
        int totalTiles = width * height;
        
        // Generate initial stories
        StoryGenerator storyGen = new StoryGenerator();
        List<Story> stories = storyGen.generateStories(
            seed,
            totalTiles,
            biomes
        );
        
        // Add stories to world
        World world = new World();
        stories.forEach(world::addStory);
        
        return world;
    }
}
```

### Custom Story Density
```java
// For a smaller world, reduce density
int customDensity = 3; // 3 stories per 10k tiles
// Modify DEFAULT_STORIES_PER_10K_TILES or pass as parameter (future enhancement)
```

## Design Decisions

### Seed Derivation
- Uses `XOR 0xDEADBEEF` to create separate RNG stream from worldgen
- Prevents correlation with terrain generation
- Consistent across all worldgen invocations

### Tile-Based Placement
- Stories anchored to specific tiles
- Enables spatial distribution
- Supports regional propagation

### Biome Affinity
- Creates thematic coherence (mountain legends, coastal rumors)
- Prevents mismatched narratives (comedy in volcanic wasteland)
- Still allows all types in all biomes (just weighted)

### Scaling Formula
- Linear scaling with world size
- Prevents story starvation in large worlds
- Prevents story spam in small worlds

### Stateless Design
- No instance state, operates on parameters
- Easy to test and reason about
- Could be static methods (future refactor)

## Testing

### Test Coverage
15 tests in `StoryGeneratorTest.java`:
- Determinism (same seed → same stories)
- Different seeds → different stories
- Story count scales with world size
- All generated stories are valid
- Biome affinity distribution
- Metadata presence
- No null stories
- Empty world handling

### Determinism Validation
```java
@Test
public void testGenerateStoriesDeterministic() {
    // Same seed → same stories
    List<Story> stories1 = generator.generateStories(seed, tiles, biomes);
    List<Story> stories2 = generator.generateStories(seed, tiles, biomes);
    
    assertEquals(stories1.size(), stories2.size());
    for (int i = 0; i < stories1.size(); i++) {
        assertEquals(stories1.get(i).getId(), stories2.get(i).getId());
        assertEquals(stories1.get(i).getStoryType(), stories2.get(i).getStoryType());
    }
}
```

## Performance Considerations

### Time Complexity
- O(n) where n = number of stories to generate
- Each story: O(1) tile selection + O(7) type selection = O(1) per story
- Total: O(tiles / 10000 * 5) = O(tiles)

### Space Complexity
- O(n) for result list
- O(1) additional working memory
- Biome map not copied, only referenced

### Optimization Opportunities
- Pre-calculate biome affinity weights (one-time setup)
- Batch story creation
- Parallel generation for large worlds (Phase 2)

## Integration Points

### WorldGen
- Called during worldgen phase (tick 0)
- After terrain generation, before region creation
- Stories added to world state

### Story Class
- Uses Story.Builder to construct instances
- Sets all required fields
- Validates via builder

### Biome Enum
- Reads from `org.adventure.world.Biome`
- Maps enum values to affinity weights
- No runtime biome creation

## Future Enhancements

### Phase 2
- Story templates (data-driven narrative content)
- Multi-biome stories (span biome boundaries)
- Story clusters (related stories near each other)
- Historical stories (world history generation)
- Custom story density per biome

### Advanced Features
- Story arc generation (linked story chains)
- Faction-based stories
- Player-influenced worldgen stories
- Procedural story content (AI-generated titles/descriptions)

## Related Classes
- `Story`: Data model for generated stories
- `StoryType`: Enum of story categories
- `Biome`: Enum of world biome types (from `org.adventure.world`)
- `EventPropagation`: Spreads generated stories after worldgen

## References
- Design: `docs/stories_events.md` → Story Generation
- Specs: `docs/specs_summary.md` → Story Defaults (5 per 10k tiles)
- Summary: `archive/PHASE_1.7_SUMMARY.md`
- Source: `src/main/java/org/adventure/story/StoryGenerator.java`
