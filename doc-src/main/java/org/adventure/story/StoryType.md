# StoryType.java

## Overview
`StoryType` is an enumeration that defines the seven categories of narrative elements (stories) in the game world. Each type has distinct characteristics, propagation parameters, and biome affinities.

## Package
`org.adventure.story`

## Purpose
- Categorize stories into thematic types
- Define default propagation behavior per type
- Enable biome-specific story generation
- Support narrative diversity and world flavor

## Enum Values

### LEGEND
**Description**: Legendary tales of heroes, artifacts, or world-shaping events

**Characteristics**:
- **Priority**: 9 (very high)
- **Base Probability**: 0.95 (spreads easily)
- **Max Hops**: 8 (long propagation distance)
- **Longevity**: Never pruned due to high priority
- **Biome Affinity**: Mountains, Rainforests/Jungles, Oceans, Volcanic regions

**Examples**:
- "The Lost Sword of Kings"
- "The Battle of Three Dragons"
- "The Founding of the First City"

**Usage**: Major historical events, mythical artifacts, legendary heroes

---

### RUMOR
**Description**: Unverified information, gossip, potentially false information

**Characteristics**:
- **Priority**: 4 (low-medium)
- **Base Probability**: 0.75 (moderate spread)
- **Max Hops**: 6 (standard propagation distance)
- **Longevity**: May be discredited or archived quickly
- **Biome Affinity**: Coast, Savanna, Lakes (population centers)

**Examples**:
- "The merchant is cheating customers"
- "A strange light was seen in the forest"
- "The mayor is secretly a vampire"

**Usage**: Short-term information spread, player investigation hooks, social dynamics

---

### QUEST
**Description**: Player or NPC objectives, tasks, missions to be completed

**Characteristics**:
- **Priority**: 6 (medium-high)
- **Base Probability**: 0.85 (good spread)
- **Max Hops**: 5 (moderate propagation distance)
- **Longevity**: Becomes RESOLVED when completed
- **Biome Affinity**: Forests, Grasslands, Hills, Savanna

**Examples**:
- "Collect 10 rare herbs from the forest"
- "Escort the caravan to the next town"
- "Investigate the abandoned mine"

**Usage**: Player content, NPC goals, world objectives

---

### PROPHECY
**Description**: Predictions and foreshadowing of future events

**Characteristics**:
- **Priority**: 8 (high)
- **Base Probability**: 0.90 (spreads well)
- **Max Hops**: 7 (long propagation distance)
- **Longevity**: Persists until fulfilled or disproven
- **Biome Affinity**: Mountains, Rainforests/Jungles, Volcanic, Magical regions

**Examples**:
- "When the red comet appears, a hero will rise"
- "The kingdom shall fall within three generations"
- "A great darkness will consume the land"

**Usage**: Long-term narrative arcs, world events, player destiny hooks

---

### TRAGEDY
**Description**: Dark tales of loss, betrayal, downfall, and suffering

**Characteristics**:
- **Priority**: 5 (medium)
- **Base Probability**: 0.70 (slower spread)
- **Max Hops**: 4 (short propagation distance)
- **Longevity**: Moderate, may be archived after some time
- **Biome Affinity**: Tundra, Taiga, Deserts, Swamps (harsh environments)

**Examples**:
- "The village was destroyed by plague"
- "The king betrayed his most loyal knight"
- "The family was torn apart by war"

**Usage**: Dark world flavor, NPC backstories, emotional depth

---

### COMEDY
**Description**: Humorous anecdotes and lighthearted tales

**Characteristics**:
- **Priority**: 2 (low)
- **Base Probability**: 0.65 (weak spread)
- **Max Hops**: 3 (very short propagation distance)
- **Longevity**: Low, quickly archived
- **Biome Affinity**: Grasslands, Coast (pleasant environments)

**Examples**:
- "The mayor fell into the fountain during his speech"
- "A pig escaped and wreaked havoc in the market"
- "The bard forgot the lyrics to his own song"

**Usage**: Comic relief, tavern stories, light world flavor

---

### MYSTERY
**Description**: Unsolved puzzles, hidden treasures, enigmas to be discovered

**Characteristics**:
- **Priority**: 6 (medium-high)
- **Base Probability**: 0.80 (good spread)
- **Max Hops**: 5 (moderate propagation distance)
- **Longevity**: Persists until solved
- **Biome Affinity**: Oceans, Lakes, Deserts, Forests, Taiga, Swamps

**Examples**:
- "Strange symbols were found in the ancient ruins"
- "The treasure map leads to an unknown location"
- "Who stole the royal crown?"

**Usage**: Player investigation, puzzles, discovery mechanics

## Usage

### In Story Creation
```java
Story legend = new Story.Builder()
    .storyType(StoryType.LEGEND)
    .title("The Dragon's Hoard")
    // ...
    .build();
```

### In Story Generation
```java
// StoryGenerator selects type based on biome
StoryType type = selectStoryType(Biome.MOUNTAIN, random);
// More likely to return LEGEND or PROPHECY for mountains
```

### In Filtering
```java
// Get all active legends
List<Story> legends = stories.stream()
    .filter(s -> s.getStoryType() == StoryType.LEGEND)
    .filter(s -> s.getStatus() == StoryStatus.ACTIVE)
    .collect(Collectors.toList());
```

## Biome Affinity Mapping

### High Affinity (weight = 3.0)
Stories of these types are 3x more likely to generate in their preferred biomes:
- LEGEND: Mountains, Rainforests/Jungles, Oceans, Volcanic
- PROPHECY: Mountains, Rainforests/Jungles, Volcanic
- TRAGEDY: Tundra, Taiga, Deserts, Swamps
- MYSTERY: Oceans, Lakes, Deserts, Forests, Taiga, Swamps

### Medium Affinity (weight = 2.0)
- QUEST: Forests, Grasslands, Hills, Savanna
- RUMOR: Coast, Savanna, Lakes

### Low Affinity (weight = 1.5)
- COMEDY: Grasslands, Coast

### Default (weight = 1.0)
All biomes have at least base chance for any story type.

## Propagation Parameters

The `StoryGenerator` uses these defaults when creating stories:

| Type | Priority | Base Probability | Max Hops |
|------|----------|------------------|----------|
| LEGEND | 9 | 0.95 | 8 |
| RUMOR | 4 | 0.75 | 6 |
| QUEST | 6 | 0.85 | 5 |
| PROPHECY | 8 | 0.90 | 7 |
| TRAGEDY | 5 | 0.70 | 4 |
| COMEDY | 2 | 0.65 | 3 |
| MYSTERY | 6 | 0.80 | 5 |

## Design Decisions

### Seven Types
Chosen to provide diverse narrative flavors:
- Epic (LEGEND, PROPHECY)
- Interactive (QUEST, MYSTERY)
- Emotional (TRAGEDY, COMEDY)
- Social (RUMOR)

### Priority Values
Range from 2 (COMEDY) to 9 (LEGEND):
- High priority (8-9): Persist indefinitely
- Medium priority (5-7): Standard lifecycle
- Low priority (2-4): Archived quickly

### Base Probability
Higher values spread more easily:
- 0.90-0.95: Epic stories (LEGEND, PROPHECY)
- 0.75-0.85: Standard stories (QUEST, RUMOR, MYSTERY)
- 0.65-0.70: Limited stories (COMEDY, TRAGEDY)

### Max Hops
Controls how far stories can propagate:
- 7-8 hops: Epic stories (LEGEND, PROPHECY)
- 5-6 hops: Standard stories (QUEST, RUMOR, MYSTERY)
- 3-4 hops: Local stories (COMEDY, TRAGEDY)

## Implementation Details

### Enum Declaration
```java
public enum StoryType {
    LEGEND,
    RUMOR,
    QUEST,
    PROPHECY,
    TRAGEDY,
    COMEDY,
    MYSTERY
}
```

### No Additional Methods
The enum is a simple enumeration with no instance methods. All logic for propagation parameters is in `StoryGenerator`.

### Case Sensitivity
Enum names are all uppercase (Java convention). Use `StoryType.LEGEND`, not `StoryType.Legend`.

## Testing

Covered in `StoryTest.java` and `StoryGeneratorTest.java`:
- Story creation with each type
- Type-specific parameter defaults
- Biome affinity distribution
- Type equality and comparison

## Integration Points

### StoryGenerator
- Uses biome affinity to select appropriate types
- Sets default propagation parameters per type
- Ensures balanced distribution across world

### Story Class
- Stores type as immutable field
- Validates type is not null during construction

### Event System
- Events can filter by story type
- Different event types may link to specific story types

## Future Enhancements

### Phase 2
- Type-specific story templates
- Cross-type story interactions (e.g., RUMOR becomes LEGEND)
- Player-created custom types (via modding)
- Type-specific resolution mechanics

### Advanced Features
- Subtype system (LEGEND_HEROIC, LEGEND_TRAGIC, etc.)
- Dynamic type changes (RUMOR → TRAGEDY when proven true)
- Type combinations (a story that's both QUEST and MYSTERY)

## Related Classes
- `Story`: Main data model that uses StoryType
- `StoryGenerator`: Selects types based on biome affinity
- `StoryStatus`: Enum for story lifecycle states
- `EventCategory`: Similar enum for event categorization

## References
- Design: `docs/stories_events.md` → Story Categories
- Specs: `docs/specs_summary.md` → Story Defaults
- Implementation: `src/main/java/org/adventure/story/StoryGenerator.java`
- Summary: `archive/PHASE_1.7_SUMMARY.md`
