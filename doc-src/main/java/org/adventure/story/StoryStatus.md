# StoryStatus.java

## Overview
`StoryStatus` is an enumeration that defines the lifecycle states of a story in the game world. Stories transition through different statuses as they are created, propagate, complete, and are eventually archived or discredited.

## Package
`org.adventure.story`

## Purpose
- Track story lifecycle from creation to archival
- Control story propagation behavior
- Manage saturation and cleanup
- Enable story resolution mechanics

## Enum Values

### ACTIVE
**Description**: Story is actively spreading and affecting the world

**Characteristics**:
- Default status for newly generated stories
- Story propagates normally via BFS algorithm
- Counts toward saturation caps (50 stories per region)
- NPCs can reference and react to story
- Players can interact with story

**Transitions**:
- → DORMANT: Waiting for trigger conditions
- → RESOLVED: Story completed or concluded
- → DISCREDITED: Story proven false

**Usage**: Most stories start and remain in ACTIVE status until completion

---

### DORMANT
**Description**: Story is waiting for trigger conditions before becoming active

**Characteristics**:
- Story exists but does not propagate
- May or may not count toward saturation caps (configurable)
- Can be activated by events, player actions, or world state
- Useful for time-delayed stories or conditional narratives

**Transitions**:
- → ACTIVE: Trigger conditions met
- → DISCREDITED: Conditions never met, story abandoned

**Usage**: Seasonal stories, prophecies with specific activation, delayed quest chains

**Example**:
```java
// Prophecy becomes active when comet appears
if (celestialEvent.equals("red_comet")) {
    prophecy.setStatus(StoryStatus.ACTIVE);
}
```

---

### RESOLVED
**Description**: Story has been completed or concluded

**Characteristics**:
- No longer propagates to new regions
- Counts toward saturation caps until archived
- Can trigger follow-up stories or events
- Resolution data stored in metadata
- After cooldown period (default 1000 ticks), transitions to ARCHIVED

**Transitions**:
- → ARCHIVED: After cooldown period expires
- → DISCREDITED: Resolution invalidated (rare)

**Usage**: Completed quests, fulfilled prophecies, concluded mysteries

**Example**:
```java
// Quest completed
quest.setStatus(StoryStatus.RESOLVED);
quest.setLastProcessedTick(currentTick);
quest.setMetadata("completedBy", playerId);
quest.setMetadata("completionTime", currentTick);
```

---

### ARCHIVED
**Description**: Story stored for historical reference in compressed form

**Characteristics**:
- Does not count toward saturation caps
- Compressed to save memory (title, summary, key outcomes)
- Can be retrieved for historical queries
- LEGEND stories (priority 9) never pruned from archive
- Lower priority stories may be pruned over time

**Transitions**:
- No further transitions (terminal state)
- Exception: LEGEND stories never deleted

**Usage**: Long-term world history, completed content, memory management

**Compression Example**:
```java
// Archive story with minimal data
archivedStory.setMetadata("originalDescription", null); // Remove verbose text
archivedStory.setMetadata("summary", "Brief summary of story outcome");
archivedStory.setMetadata("keyOutcomes", List.of("Kingdom saved", "Hero honored"));
```

---

### DISCREDITED
**Description**: Story was proven false or abandoned

**Characteristics**:
- Removed from active circulation
- Does not propagate
- May generate follow-up "truth revealed" events
- Can create new stories (e.g., RUMOR → TRAGEDY when truth is dark)
- After cooldown, transitions to ARCHIVED with "discredited" flag

**Transitions**:
- → ARCHIVED: After cooldown period

**Usage**: False rumors, disproven prophecies, failed quests, narrative reversals

**Example**:
```java
// Rumor proven false
rumor.setStatus(StoryStatus.DISCREDITED);
rumor.setMetadata("discreditedBy", investigatorId);
rumor.setMetadata("truthRevealed", "The merchant was innocent");

// Generate new story about the truth
Story truth = new Story.Builder()
    .storyType(StoryType.RUMOR)
    .title("The Truth About the Merchant")
    .description("Investigation reveals the merchant was wrongly accused")
    .build();
```

## Status Transition Diagram

```
         ┌─────────┐
         │ DORMANT │◄─────┐
         └────┬────┘      │
              │           │
              ▼           │
         ┌────────┐   Conditions
    ┌───►│ ACTIVE │   not met
    │    └───┬────┘       │
    │        │            │
    │        ├────────────┘
    │        │
    │        ├─────────────┐
    │        │             │
Trigger     │             ▼
conditions  │      ┌──────────────┐
    │        │      │ DISCREDITED  │
    │        │      └──────┬───────┘
    │        │             │
    └────────┘             │
              │            │
              ▼            │
         ┌──────────┐      │
         │ RESOLVED │      │
         └────┬─────┘      │
              │            │
              ├────────────┘
              │
              ▼
         ┌──────────┐
         │ ARCHIVED │ (terminal)
         └──────────┘
```

## Usage

### Creating a New Story
```java
Story story = new Story.Builder()
    .storyType(StoryType.LEGEND)
    .status(StoryStatus.ACTIVE) // Default
    .build();
```

### Checking Story State
```java
if (story.getStatus() == StoryStatus.ACTIVE) {
    // Story can propagate
    propagateStory(story);
}
```

### Transitioning Status
```java
// Complete a quest
if (questCompleted) {
    story.setStatus(StoryStatus.RESOLVED);
    story.setLastProcessedTick(currentTick);
}

// Archive after cooldown
long archiveThreshold = 1000;
if (story.getStatus() == StoryStatus.RESOLVED &&
    currentTick - story.getLastProcessedTick() > archiveThreshold) {
    story.setStatus(StoryStatus.ARCHIVED);
}
```

### Filtering by Status
```java
// Get all active stories
List<Story> activeStories = allStories.stream()
    .filter(s -> s.getStatus() == StoryStatus.ACTIVE)
    .collect(Collectors.toList());

// Get stories to archive
List<Story> toArchive = allStories.stream()
    .filter(s -> s.getStatus() == StoryStatus.RESOLVED)
    .filter(s -> currentTick - s.getLastProcessedTick() > ARCHIVE_THRESHOLD)
    .collect(Collectors.toList());
```

## Saturation Management

### Stories That Count Toward Caps
- ACTIVE: Always counts
- RESOLVED: Counts until archived
- DORMANT: Configurable (default: counts)

### Stories That Don't Count
- ARCHIVED: Excluded from caps
- DISCREDITED: May count briefly, then excluded

### Cap Enforcement
```java
// SaturationManager checks status
public int getActiveStoryCount(String regionId) {
    return storiesByRegion.get(regionId).stream()
        .filter(s -> s.getStatus() == StoryStatus.ACTIVE || 
                     s.getStatus() == StoryStatus.DORMANT ||
                     s.getStatus() == StoryStatus.RESOLVED)
        .count();
}
```

## Implementation Details

### Enum Declaration
```java
public enum StoryStatus {
    ACTIVE,
    DORMANT,
    RESOLVED,
    ARCHIVED,
    DISCREDITED
}
```

### No Additional Methods
Simple enumeration with no instance methods. All lifecycle logic is in `Story` and related systems.

### Default Value
New stories default to `ACTIVE` status unless explicitly set via builder.

## Design Decisions

### Five States
Chosen to cover full lifecycle:
- Creation (ACTIVE or DORMANT)
- Lifecycle (ACTIVE ↔ DORMANT)
- Completion (RESOLVED or DISCREDITED)
- Archival (ARCHIVED)

### Immutability
Status is mutable (via `setStatus()`) to allow lifecycle transitions. Most other Story fields are immutable.

### Terminal State
ARCHIVED is terminal - stories never transition out of it. Prevents complex state machines.

### Cooldown Periods
RESOLVED → ARCHIVED and DISCREDITED → ARCHIVED transitions include cooldown to avoid immediate pruning.

## Testing

Covered in `StoryTest.java`:
- Status transitions
- Default status (ACTIVE)
- Status-based filtering
- Saturation counting by status

## Integration Points

### Story Class
- Stores status as mutable field
- Validates status changes
- Tracks lastProcessedTick for archival timing

### EventPropagation
- Only propagates ACTIVE stories
- Skips DORMANT, RESOLVED, ARCHIVED, DISCREDITED

### SaturationManager
- Counts stories based on status
- Excludes ARCHIVED from caps
- May exclude DISCREDITED from caps

### Region Simulation
- Processes ACTIVE stories each tick
- Checks for DORMANT → ACTIVE triggers
- Archives RESOLVED stories after cooldown

## Future Enhancements

### Phase 2
- AUTO_ARCHIVE status for automatic cleanup
- SUSPENDED status for temporary pausing
- CONTESTED status for conflicting narratives
- EVOLVING status for stories that change over time

### Advanced Features
- Status-based event triggers
- Player-visible status changes
- Status history tracking (when did story become RESOLVED?)
- Status-specific UI indicators

## Related Classes
- `Story`: Main data model that uses StoryStatus
- `StoryType`: Enum for story categories
- `EventPropagation`: Checks status before propagation
- `SaturationManager`: Counts stories by status

## References
- Design: `docs/stories_events.md` → Story Lifecycle
- Specs: `docs/specs_summary.md` → Story States
- Implementation: `src/main/java/org/adventure/story/Story.java`
- Summary: `archive/PHASE_1.7_SUMMARY.md`
