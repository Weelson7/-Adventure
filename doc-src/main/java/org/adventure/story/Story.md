# Story.java

## Overview
`Story` represents a narrative element in the game world. Stories are seeded at worldgen and can propagate across regions through events, affecting NPC behavior, player interactions, and world state.

## Package
`org.adventure.story`

## Purpose
- Represent narrative elements (legends, rumors, quests, prophecies, tragedies, comedies, mysteries)
- Track story lifecycle and propagation state
- Enable deterministic story generation and propagation
- Support persistence with schema versioning

## Key Features

### Data Model
- **Unique ID**: String identifier for each story
- **Story Type**: Category (LEGEND, RUMOR, QUEST, PROPHECY, TRAGEDY, COMEDY, MYSTERY)
- **Status**: Lifecycle state (ACTIVE, DORMANT, RESOLVED, ARCHIVED, DISCREDITED)
- **Origin Tracking**: originTileId, originTick for reproducibility
- **Propagation Data**: baseProbability, hopCount, maxHops, priority
- **Affected Regions**: Set of region IDs where story has spread
- **Metadata**: Flexible key-value storage (biome, coordinates, etc.)

### Builder Pattern
```java
Story story = new Story.Builder()
    .id("story_12345_1")
    .storyType(StoryType.LEGEND)
    .title("The Lost Sword of Kings")
    .description("An ancient sword was lost in the mountains centuries ago.")
    .originTileId(1024)
    .originTick(0)
    .baseProbability(0.95)
    .maxHops(8)
    .priority(9)
    .metadata("biome", "MOUNTAIN")
    .build();
```

### Validation
- ID cannot be null or empty
- StoryType must be specified
- Title cannot be null or empty
- baseProbability must be in [0.0, 1.0]
- priority must be in [0, 10]

## Fields

### Required Fields
- `id` (String): Unique identifier
- `type` (String): Always "story/Story" for persistence
- `schemaVersion` (int): Current version = 1
- `storyType` (StoryType): Category of story
- `status` (StoryStatus): Current lifecycle state
- `title` (String): Display name
- `description` (String): Narrative text

### Origin Tracking
- `originTileId` (int): Tile where story was created
- `originTick` (long): Game tick when story originated

### Propagation Parameters
- `baseProbability` (double): Base chance of propagation [0.0, 1.0], default 0.9
- `hopCount` (int): Current propagation distance from origin, default 0
- `maxHops` (int): Maximum propagation distance, default 6
- `priority` (int): Story importance [0, 10], default 5

### State Tracking
- `lastProcessedTick` (long): Last tick when story was updated, default 0
- `affectedRegions` (Set<String>): Region IDs where story is known
- `metadata` (Map<String, Object>): Additional data (biome, coordinates, etc.)

## Story Types and Characteristics

### LEGEND
- **Priority**: 9 (very high)
- **Base Probability**: 0.95
- **Max Hops**: 8
- **Description**: Legendary tales of heroes, artifacts, world-shaping events
- **Biome Affinity**: Mountains, Rainforests/Jungles, Oceans, Volcanic

### RUMOR
- **Priority**: 4 (low-medium)
- **Base Probability**: 0.75
- **Max Hops**: 6
- **Description**: Unverified information, gossip, may be false
- **Biome Affinity**: Coast, Savanna, Lakes

### QUEST
- **Priority**: 6 (medium-high)
- **Base Probability**: 0.85
- **Max Hops**: 5
- **Description**: Player or NPC objectives, tasks, missions
- **Biome Affinity**: Forests, Grasslands, Hills, Savanna

### PROPHECY
- **Priority**: 8 (high)
- **Base Probability**: 0.90
- **Max Hops**: 7
- **Description**: Predictions and foreshadowing of future events
- **Biome Affinity**: Mountains, Rainforests/Jungles, Volcanic, Magical

### TRAGEDY
- **Priority**: 5 (medium)
- **Base Probability**: 0.70
- **Max Hops**: 4
- **Description**: Dark tales of loss, betrayal, downfall
- **Biome Affinity**: Tundra, Taiga, Deserts, Swamps

### COMEDY
- **Priority**: 2 (low)
- **Base Probability**: 0.65
- **Max Hops**: 3
- **Description**: Humorous anecdotes and lighthearted tales
- **Biome Affinity**: Grasslands, Coast

### MYSTERY
- **Priority**: 6 (medium-high)
- **Base Probability**: 0.80
- **Max Hops**: 5
- **Description**: Unsolved puzzles, hidden treasures, enigmas
- **Biome Affinity**: Oceans, Lakes, Deserts, Forests, Taiga, Swamps

## Status Lifecycle

### ACTIVE
- Story is actively spreading and affecting the world
- Default status for newly generated stories
- Counts toward saturation caps

### DORMANT
- Story is waiting for trigger conditions
- Does not propagate until activated
- May not count toward saturation caps

### RESOLVED
- Story has been completed or concluded
- No longer propagates
- Can be archived after cooldown period

### ARCHIVED
- Story stored for historical reference
- Compressed form (title, summary, key outcomes)
- Does not count toward saturation caps
- Legendary stories never pruned

### DISCREDITED
- Story was proven false or abandoned
- Removed from active circulation
- May generate follow-up "truth revealed" events

## Methods

### Getters
All fields have corresponding getters that return defensive copies for collections:
- `getId()`, `getType()`, `getSchemaVersion()`
- `getStoryType()`, `getStatus()`, `getTitle()`, `getDescription()`
- `getOriginTileId()`, `getOriginTick()`
- `getBaseProbability()`, `getHopCount()`, `getMaxHops()`, `getPriority()`
- `getLastProcessedTick()`
- `getAffectedRegions()` - returns defensive copy
- `getMetadata()` - returns defensive copy

### Setters (Mutable Fields)
- `setStatus(StoryStatus)`: Change lifecycle state
- `setHopCount(int)`: Update propagation distance
- `setLastProcessedTick(long)`: Track processing time
- `addAffectedRegion(String)`: Register region awareness
- `setMetadata(String, Object)`: Add custom data

### Object Methods
- `equals(Object)`: Equality based on ID only
- `hashCode()`: Hash based on ID
- `toString()`: Includes id, storyType, status, title, priority, hopCount, maxHops

## Usage Examples

### Creating a Legend at Worldgen
```java
Story legend = new Story.Builder()
    .id("story_12345_0")
    .storyType(StoryType.LEGEND)
    .title("The Legend of the Mountain Guardian")
    .description("An ancient legend speaks of a powerful guardian that once protected the mountain.")
    .originTileId(2048)
    .originTick(0)
    .baseProbability(0.95)
    .maxHops(8)
    .priority(9)
    .metadata("biome", "MOUNTAIN")
    .metadata("x", 64)
    .metadata("y", 32)
    .build();
```

### Tracking Story Propagation
```java
// Story spreads to new region
story.addAffectedRegion("region_1024");
story.setHopCount(story.getHopCount() + 1);
story.setLastProcessedTick(currentTick);

// Check if story has reached max distance
if (story.getHopCount() >= story.getMaxHops()) {
    // Stop propagation
}
```

### Resolving a Quest Story
```java
if (questCompleted) {
    story.setStatus(StoryStatus.RESOLVED);
    story.setLastProcessedTick(currentTick);
    story.setMetadata("completedBy", playerId);
    story.setMetadata("completionTime", currentTick);
}
```

### Archiving Old Stories
```java
long archiveThreshold = 1000; // ticks
if (story.getStatus() == StoryStatus.RESOLVED &&
    currentTick - story.getLastProcessedTick() > archiveThreshold) {
    story.setStatus(StoryStatus.ARCHIVED);
    // Compress story data if needed
}
```

## Persistence

### Schema Version
Current version: 1

### Required Fields for Persistence
- `type`: "story/Story"
- `schemaVersion`: 1
- `lastProcessedTick`: For resynchronization

### JSON Serialization
Uses Jackson annotations (`@JsonProperty`) for all fields. Collections are serialized as JSON arrays/objects.

## Integration Points

### Worldgen
Stories are generated by `StoryGenerator` during worldgen:
- Deterministic placement based on world seed
- Biome-specific story type selection
- Scales with world size (5 stories per 10k tiles)

### Event System
Stories can link to events via `Event.linkedStoryId`:
- Events trigger story progression
- Story resolution creates new events

### Region Simulation
Stories propagate during tick processing:
- `EventPropagation.propagateStory()` handles BFS spread
- `SaturationManager` enforces per-region caps

### NPC Behavior
Stories can affect NPC actions:
- NPCs aware of local stories (via affectedRegions)
- Stories modify NPC dialogue, quests, behavior

## Design Decisions

### Determinism
- All story generation uses seeded RNG
- Same seed + parameters = identical stories
- Enables regression testing and reproducibility

### Immutability
- Most fields are final and set via builder
- Only lifecycle fields are mutable (status, hopCount, lastProcessedTick)
- Defensive copies for collections

### Equality
- Based solely on `id` field
- Two stories with same ID are considered equal
- Enables efficient Set/Map operations

### Metadata Flexibility
- Open-ended Map<String, Object> for extensibility
- No schema validation on metadata
- Used for biome, coordinates, custom data

## Testing

### Test Coverage
15 tests in `StoryTest.java`:
- Builder validation (required fields, constraints)
- Status transitions
- Hop count tracking
- Affected regions management
- Metadata operations
- Default values
- Schema version
- Equality and hashCode
- toString format

### Determinism Tests
Verified in `StoryGeneratorTest.java`:
- Same seed → same story placements
- Consistent IDs, types, properties

## Performance Considerations

- Defensive copies for collections add overhead
- Metadata Map has no size limits (consider caps in Phase 2)
- Equality checks are O(1) via ID comparison

## Future Enhancements

### Phase 2 Improvements
- Story chaining: stories trigger follow-up stories
- Story merging: similar stories combine
- Player-driven story progression
- AI-generated story content
- Story discovery mechanics

### Advanced Features
- Story branches and multiple resolutions
- Hidden outcomes and secret endings
- Cross-region story arcs
- Story impact on world state (biome changes, structure bonuses)

## Related Classes
- `StoryType`: Enum of story categories
- `StoryStatus`: Enum of lifecycle states
- `StoryGenerator`: Creates stories at worldgen
- `EventPropagation`: Handles story spread via BFS
- `SaturationManager`: Enforces per-region caps
- `Event`: Gameplay occurrences that link to stories

## References
- Design: `docs/stories_events.md`
- Specs: `docs/specs_summary.md` → Event Propagation & Saturation
- Summary: `archive/PHASE_1.7_SUMMARY.md`
