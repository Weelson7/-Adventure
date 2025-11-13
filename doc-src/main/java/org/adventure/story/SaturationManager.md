# SaturationManager.java

## Overview
`SaturationManager` enforces per-region caps on stories and events to prevent narrative spam and maintain performance. It tracks active stories/events per region and calculates saturation factors that reduce propagation probability as caps are approached.

## Package
`org.adventure.story`

## Purpose
- Enforce per-region caps (50 stories, 20 events)
- Calculate saturation factors for propagation probability reduction
- Track story/event counts by region and type
- Provide soft cap warnings (80% threshold)
- Support cleanup and reset for testing

## Key Features

### Per-Region Caps
- **Stories**: 50 per region (default)
- **Events**: 20 per region (default)
- Configurable via constructor parameters
- Prevents unbounded growth and memory issues

### Saturation Formula
```
saturationFactor = max(0, 1 - (currentCount / maxCap))
```

**Examples**:
- 0 stories (0%): factor = 1.0 (no reduction)
- 25 stories (50%): factor = 0.5 (50% reduction)
- 40 stories (80%): factor = 0.2 (80% reduction) ← **soft cap warning**
- 50 stories (100%): factor = 0.0 (no propagation) ← **hard cap**

### Soft Cap Warnings
- At 80% capacity: `isSoftCapReached()` returns true
- Allows operators to monitor saturation levels
- Enables proactive cleanup before hard cap

### Thread Safety
- Not thread-safe by default
- Single-threaded tick processing (no concurrent access expected)
- External synchronization required for multi-threaded use

## Constructor

```java
public SaturationManager()
```

**Default Configuration**:
- `maxStoriesPerRegion` = 50
- `maxEventsPerRegion` = 20

---

```java
public SaturationManager(int maxStoriesPerRegion, int maxEventsPerRegion)
```

**Parameters**:
- `maxStoriesPerRegion`: Maximum active stories per region
- `maxEventsPerRegion`: Maximum active events per region

**Example**:
```java
// Default caps
SaturationManager defaultMgr = new SaturationManager();

// Custom caps for testing
SaturationManager testMgr = new SaturationManager(10, 5);
```

## Methods

### registerStory
```java
public void registerStory(String regionId, Story story)
```

**Description**: Registers a story as active in a region, incrementing the count.

**Parameters**:
- `regionId` (String): Region identifier
- `story` (Story): Story to register

**Behavior**:
- Increments story count for region
- Adds story to tracking map
- Does NOT enforce cap (use `isAtCapacity()` to check first)

**Example**:
```java
Story legend = createLegend();
String regionId = "region_1024";

if (!satMgr.isAtCapacity(regionId, "STORY")) {
    satMgr.registerStory(regionId, legend);
} else {
    System.out.println("Region at story cap!");
}
```

**Note**: Duplicate registration is allowed but should be avoided (can exceed caps).

---

### registerEvent
```java
public void registerEvent(String regionId, Event event)
```

**Description**: Registers an event as active in a region.

**Parameters**: Same pattern as `registerStory`

**Example**:
```java
Event combat = createCombatEvent();
satMgr.registerEvent("region_2048", combat);
```

---

### unregisterStory
```java
public void unregisterStory(String regionId, Story story)
```

**Description**: Removes a story from region tracking, decrementing the count.

**Parameters**:
- `regionId` (String): Region identifier
- `story` (Story): Story to unregister

**Behavior**:
- Decrements story count for region
- Removes story from tracking map
- Count cannot go below 0 (safeguard)

**Example**:
```java
// Story resolved, remove from active count
if (story.getStatus() == StoryStatus.RESOLVED) {
    satMgr.unregisterStory(regionId, story);
}
```

---

### unregisterEvent
```java
public void unregisterEvent(String regionId, Event event)
```

**Description**: Removes an event from region tracking.

**Parameters**: Same pattern as `unregisterStory`

**Example**:
```java
// Event expired, cleanup
if (event.getStatus() == EventStatus.EXPIRED) {
    satMgr.unregisterEvent(regionId, event);
}
```

---

### getStoryCount
```java
public int getStoryCount(String regionId)
```

**Description**: Gets current number of active stories in region.

**Parameters**:
- `regionId` (String): Region identifier

**Returns**: `int` - Story count (0 if region not found)

**Example**:
```java
int count = satMgr.getStoryCount("region_1024");
System.out.println("Active stories: " + count);
```

---

### getEventCount
```java
public int getEventCount(String regionId)
```

**Description**: Gets current number of active events in region.

**Parameters**: Same pattern as `getStoryCount`

**Example**:
```java
int count = satMgr.getEventCount("region_2048");
System.out.println("Active events: " + count);
```

---

### getSaturationFactor
```java
public double getSaturationFactor(String regionId, String type)
```

**Description**: Calculates saturation factor for propagation probability reduction.

**Parameters**:
- `regionId` (String): Region identifier
- `type` (String): "STORY" or "EVENT"

**Returns**: `double` - Saturation factor in [0.0, 1.0]

**Formula**: `max(0, 1 - (currentCount / maxCap))`

**Examples**:
```java
// Region with 10 stories (cap=50)
double factor = satMgr.getSaturationFactor("region_1", "STORY");
// factor = max(0, 1 - 10/50) = 0.8 (20% reduction)

// Region with 18 events (cap=20)
factor = satMgr.getSaturationFactor("region_2", "EVENT");
// factor = max(0, 1 - 18/20) = 0.1 (90% reduction)

// Region with 50 stories (cap=50)
factor = satMgr.getSaturationFactor("region_3", "STORY");
// factor = max(0, 1 - 50/50) = 0.0 (100% reduction - hard cap)
```

**Usage in Propagation**:
```java
double baseProb = story.getBaseProbability();
double decay = calculateDecay(hopCount);
double satFactor = satMgr.getSaturationFactor(regionId, "STORY");
double effectiveProb = baseProb * decay * satFactor;

if (random.nextDouble() < effectiveProb) {
    // Propagate
}
```

---

### isAtCapacity
```java
public boolean isAtCapacity(String regionId, String type)
```

**Description**: Checks if region has reached maximum capacity for given type.

**Parameters**:
- `regionId` (String): Region identifier
- `type` (String): "STORY" or "EVENT"

**Returns**: `boolean` - True if at hard cap (100%), false otherwise

**Example**:
```java
if (satMgr.isAtCapacity("region_1024", "STORY")) {
    System.out.println("Cannot add more stories!");
    return;
}

satMgr.registerStory("region_1024", newStory);
```

---

### isSoftCapReached
```java
public boolean isSoftCapReached(String regionId, String type)
```

**Description**: Checks if region has reached 80% capacity (soft cap warning threshold).

**Parameters**: Same as `isAtCapacity`

**Returns**: `boolean` - True if at or above 80% capacity

**Example**:
```java
if (satMgr.isSoftCapReached("region_1024", "STORY")) {
    logger.warn("Region approaching story cap: {}%", 
        (satMgr.getStoryCount("region_1024") * 100) / 50);
}
```

**Soft Cap Thresholds**:
- Stories: 40 / 50 (80%)
- Events: 16 / 20 (80%)

---

### reset
```java
public void reset()
```

**Description**: Clears all tracking data. Used primarily for testing.

**Example**:
```java
@BeforeEach
public void setup() {
    satMgr = new SaturationManager();
}

@AfterEach
public void cleanup() {
    satMgr.reset(); // Clean state for next test
}
```

**Warning**: Should NOT be called during normal gameplay (would lose all tracking data).

---

### getTotalStoryCount
```java
public int getTotalStoryCount()
```

**Description**: Gets total number of active stories across all regions.

**Returns**: `int` - Sum of all region story counts

**Example**:
```java
int total = satMgr.getTotalStoryCount();
System.out.println("Total active stories in world: " + total);
```

**Performance**: O(R) where R = number of tracked regions.

---

### getTotalEventCount
```java
public int getTotalEventCount()
```

**Description**: Gets total number of active events across all regions.

**Returns**: Same pattern as `getTotalStoryCount`

**Example**:
```java
int total = satMgr.getTotalEventCount();
System.out.println("Total active events in world: " + total);
```

## Internal Data Structures

### storiesByRegion
```java
private Map<String, Set<Story>> storiesByRegion;
```

Maps region IDs to sets of active stories. Used for precise tracking and cleanup.

### eventsByRegion
```java
private Map<String, Set<Event>> eventsByRegion;
```

Maps region IDs to sets of active events.

### storyCountByRegion
```java
private Map<String, Integer> storyCountByRegion;
```

Fast lookup for story counts without iterating sets.

### eventCountByRegion
```java
private Map<String, Integer> eventCountByRegion;
```

Fast lookup for event counts.

## Configuration Constants

### DEFAULT_MAX_STORIES_PER_REGION
```java
private static final int DEFAULT_MAX_STORIES_PER_REGION = 50;
```

Based on specs_summary.md → maxActiveStories=50.

### DEFAULT_MAX_EVENTS_PER_REGION
```java
private static final int DEFAULT_MAX_EVENTS_PER_REGION = 20;
```

Based on specs_summary.md → maxActiveEvents=20.

### SOFT_CAP_THRESHOLD
```java
private static final double SOFT_CAP_THRESHOLD = 0.8;
```

80% capacity triggers warnings.

## Usage Examples

### Basic Registration and Checking
```java
SaturationManager satMgr = new SaturationManager();

// Register stories
for (int i = 0; i < 30; i++) {
    Story story = createStory("story_" + i);
    satMgr.registerStory("region_A", story);
}

// Check saturation
int count = satMgr.getStoryCount("region_A"); // 30
double factor = satMgr.getSaturationFactor("region_A", "STORY");
// factor = 1 - 30/50 = 0.4 (60% reduction)

boolean softCap = satMgr.isSoftCapReached("region_A", "STORY");
// false (30 < 40)

// Add 10 more
for (int i = 30; i < 40; i++) {
    satMgr.registerStory("region_A", createStory("story_" + i));
}

softCap = satMgr.isSoftCapReached("region_A", "STORY");
// true (40 >= 40)
```

### Propagation Integration
```java
// During event propagation
String targetRegion = "region_B";
Event event = currentEvent;

// Check if we can propagate
if (!satMgr.isAtCapacity(targetRegion, "EVENT")) {
    double satFactor = satMgr.getSaturationFactor(targetRegion, "EVENT");
    double effectiveProb = event.getBaseProbability() * decay * satFactor;
    
    if (random.nextDouble() < effectiveProb) {
        satMgr.registerEvent(targetRegion, event);
        event.addAffectedRegion(targetRegion);
    }
}
```

### Cleanup Loop
```java
// Periodic cleanup of resolved stories
for (String regionId : allRegions) {
    Set<Story> stories = getStoriesForRegion(regionId);
    for (Story story : stories) {
        if (story.getStatus() == StoryStatus.RESOLVED ||
            story.getStatus() == StoryStatus.ARCHIVED) {
            satMgr.unregisterStory(regionId, story);
        }
    }
}
```

### Monitoring and Alerting
```java
// Check all regions for saturation issues
for (String regionId : allRegions) {
    if (satMgr.isSoftCapReached(regionId, "STORY")) {
        int count = satMgr.getStoryCount(regionId);
        logger.warn("Region {} nearing story cap: {}/50", regionId, count);
        
        // Trigger cleanup or reduce story generation
        cleanupOldStories(regionId);
    }
    
    if (satMgr.isAtCapacity(regionId, "EVENT")) {
        logger.error("Region {} at EVENT hard cap!", regionId);
        // Stop event propagation to this region
    }
}
```

## Design Decisions

### Separate Story and Event Caps
- Stories are long-lived (50 cap)
- Events are transient (20 cap)
- Reflects different lifecycles and gameplay roles

### Soft Cap at 80%
- Provides early warning before hard cap
- Allows time for cleanup/mitigation
- Prevents sudden propagation failures

### Formula: Linear Reduction
- Simple `1 - (count/max)` formula
- Predictable behavior
- Graceful degradation (not cliff at cap)

### No Automatic Cleanup
- Manager only tracks and reports
- External systems handle cleanup
- Separation of concerns

### Set-Based Tracking
- Prevents duplicate registration
- Enables precise unregistration
- O(1) add/remove operations

## Testing

### Test Coverage
19 tests in `SaturationManagerTest.java`:
- Registration and unregistration
- Count tracking (story/event, per-region/total)
- Saturation factor calculation
- Hard cap detection
- Soft cap detection
- Formula validation at various capacities
- Reset functionality
- Edge cases (0%, 50%, 80%, 100%, >100%)

### Formula Validation Tests
```java
@Test
public void testSaturationFactorFormula() {
    // 0 stories: factor = 1.0
    assertEquals(1.0, satMgr.getSaturationFactor("region", "STORY"), 0.01);
    
    // 25 stories: factor = 0.5
    registerStories(25);
    assertEquals(0.5, satMgr.getSaturationFactor("region", "STORY"), 0.01);
    
    // 50 stories: factor = 0.0
    registerStories(25);
    assertEquals(0.0, satMgr.getSaturationFactor("region", "STORY"), 0.01);
}
```

## Performance Considerations

### Time Complexity
- `registerStory/Event`: O(1) average (HashMap + HashSet ops)
- `unregisterStory/Event`: O(1) average
- `getStoryCount/EventCount`: O(1) (cached counts)
- `getSaturationFactor`: O(1) (simple arithmetic)
- `getTotalStoryCount/EventCount`: O(R) where R = regions

### Space Complexity
- O(S + E) where S = total stories, E = total events
- Each story/event stored once per affected region
- Maps have overhead but bounded by caps

### Optimization Opportunities
- Lazy initialization of region maps (only create when needed)
- Periodic compaction of empty regions
- Batch registration/unregistration

## Integration Points

### EventPropagation
- Consulted during every propagation decision
- Provides saturation factors for probability calculation
- Notified of new story/event registrations

### Region Simulation
- Registers new stories/events during tick processing
- Unregisters expired/resolved stories/events
- Monitors saturation levels for warnings

### Story/Event Lifecycle
- Stories registered when active
- Unregistered when archived/discredited
- Events registered when active
- Unregistered when resolved/expired

## Future Enhancements

### Phase 2
- Category-specific caps (e.g., max 10 COMBAT events)
- Dynamic caps based on region size/population
- Configurable soft cap threshold
- Historical saturation tracking
- Auto-cleanup policies

### Advanced Features
- Priority-based eviction (remove low-priority stories first)
- Age-based cleanup (remove oldest stories when at cap)
- Player-influenced caps (famous regions get higher caps)
- Cross-region saturation balancing

## Related Classes
- `Story`: Registered and tracked by manager
- `Event`: Registered and tracked by manager
- `EventPropagation`: Uses saturation factors for propagation
- `Region`: Provides region IDs for tracking

## References
- Design: `docs/stories_events.md` → Saturation Management
- Specs: `docs/specs_summary.md` → maxActiveStories=50, maxActiveEvents=20
- Summary: `archive/PHASE_1.7_SUMMARY.md`
- Source: `src/main/java/org/adventure/story/SaturationManager.java`
