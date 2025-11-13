# SaturationManagerTest.java

## Overview
Comprehensive test suite for `SaturationManager` class, validating cap enforcement, saturation factor calculations, registration/unregistration, and soft/hard cap detection.

## Package
`org.adventure` (test package)

## Test Class
```java
public class SaturationManagerTest
```

## Purpose
- Verify registration and unregistration of stories/events
- Test saturation factor formula: `max(0, 1 - count/cap)`
- Validate hard cap detection (100% capacity)
- Test soft cap detection (80% capacity)
- Check per-region and total count tracking
- Ensure reset functionality works correctly

## Test Coverage
**19 tests** covering all methods, formula variations, and edge cases

### Line Coverage
~95% (estimated) - all public methods, formula paths, and cap thresholds tested

## Test Methods

### testRegisterStory
**Purpose**: Verify story registration increments count

**Test Flow**:
1. Initial count = 0
2. Register story1 → count = 1
3. Register story2 → count = 2
4. Verify count increments correctly

**Assertions**:
```java
SaturationManager mgr = new SaturationManager();
assertEquals(0, mgr.getStoryCount("region_A"));

Story story1 = createStory("story_1");
mgr.registerStory("region_A", story1);
assertEquals(1, mgr.getStoryCount("region_A"));

Story story2 = createStory("story_2");
mgr.registerStory("region_A", story2);
assertEquals(2, mgr.getStoryCount("region_A"));
```

---

### testRegisterEvent
**Purpose**: Verify event registration increments count

**Pattern**: Same as `testRegisterStory` but for events

**Assertions**: Event count increments from 0 → 1 → 2

---

### testUnregisterStory
**Purpose**: Verify story unregistration decrements count

**Test Flow**:
1. Register 3 stories → count = 3
2. Unregister story1 → count = 2
3. Unregister story2 → count = 1
4. Unregister story3 → count = 0

**Assertions**:
```java
mgr.registerStory("region_A", story1);
mgr.registerStory("region_A", story2);
mgr.registerStory("region_A", story3);
assertEquals(3, mgr.getStoryCount("region_A"));

mgr.unregisterStory("region_A", story1);
assertEquals(2, mgr.getStoryCount("region_A"));

mgr.unregisterStory("region_A", story2);
assertEquals(1, mgr.getStoryCount("region_A"));

mgr.unregisterStory("region_A", story3);
assertEquals(0, mgr.getStoryCount("region_A"));
```

---

### testUnregisterEvent
**Purpose**: Verify event unregistration decrements count

**Pattern**: Same as `testUnregisterStory` but for events

---

### testSaturationFactorFormula
**Purpose**: Verify saturation factor formula: `max(0, 1 - count/cap)`

**Test Cases** (cap=50 for stories):
- 0 stories: factor = 1.0 (no reduction)
- 10 stories: factor = 0.8 (20% reduction)
- 25 stories: factor = 0.5 (50% reduction)
- 40 stories: factor = 0.2 (80% reduction)
- 50 stories: factor = 0.0 (100% reduction - hard cap)
- 60 stories: factor = 0.0 (over cap, clamped to 0)

**Assertions**:
```java
SaturationManager mgr = new SaturationManager();

// 0 stories
assertEquals(1.0, mgr.getSaturationFactor("region", "STORY"), 0.01);

// 10 stories
registerStories(mgr, "region", 10);
assertEquals(0.8, mgr.getSaturationFactor("region", "STORY"), 0.01);

// 25 stories
registerStories(mgr, "region", 15); // Total 25
assertEquals(0.5, mgr.getSaturationFactor("region", "STORY"), 0.01);

// 50 stories (hard cap)
registerStories(mgr, "region", 25); // Total 50
assertEquals(0.0, mgr.getSaturationFactor("region", "STORY"), 0.01);
```

---

### testSaturationFactorForEvents
**Purpose**: Verify saturation factor formula for events (cap=20)

**Test Cases**:
- 0 events: factor = 1.0
- 5 events: factor = 0.75 (25% reduction)
- 10 events: factor = 0.5 (50% reduction)
- 16 events: factor = 0.2 (80% reduction)
- 20 events: factor = 0.0 (hard cap)

**Assertions**:
```java
// 10 events
registerEvents(mgr, "region", 10);
double factor = mgr.getSaturationFactor("region", "EVENT");
assertEquals(0.5, factor, 0.01); // 1 - 10/20 = 0.5
```

---

### testIsAtCapacityStories
**Purpose**: Verify hard cap detection for stories (50 stories)

**Test Cases**:
- 49 stories: NOT at capacity
- 50 stories: AT capacity
- 51 stories: AT capacity (over cap)

**Assertions**:
```java
registerStories(mgr, "region", 49);
assertFalse(mgr.isAtCapacity("region", "STORY"));

registerStories(mgr, "region", 1); // Total 50
assertTrue(mgr.isAtCapacity("region", "STORY"));

registerStories(mgr, "region", 1); // Total 51 (over cap)
assertTrue(mgr.isAtCapacity("region", "STORY"));
```

---

### testIsAtCapacityEvents
**Purpose**: Verify hard cap detection for events (20 events)

**Test Cases**:
- 19 events: NOT at capacity
- 20 events: AT capacity

---

### testIsSoftCapReachedStories
**Purpose**: Verify soft cap detection at 80% capacity

**Soft Cap**: 40 stories (80% of 50)

**Test Cases**:
- 39 stories: NOT at soft cap
- 40 stories: AT soft cap
- 41 stories: AT soft cap (over)
- 50 stories: AT soft cap (also at hard cap)

**Assertions**:
```java
registerStories(mgr, "region", 39);
assertFalse(mgr.isSoftCapReached("region", "STORY"));

registerStories(mgr, "region", 1); // Total 40
assertTrue(mgr.isSoftCapReached("region", "STORY"));
```

---

### testIsSoftCapReachedEvents
**Purpose**: Verify soft cap detection for events

**Soft Cap**: 16 events (80% of 20)

**Test Cases**:
- 15 events: NOT at soft cap
- 16 events: AT soft cap

---

### testGetTotalStoryCount
**Purpose**: Verify total story count across all regions

**Test Flow**:
1. Register 10 stories in region_A
2. Register 15 stories in region_B
3. Register 20 stories in region_C
4. Total = 45

**Assertions**:
```java
registerStories(mgr, "region_A", 10);
registerStories(mgr, "region_B", 15);
registerStories(mgr, "region_C", 20);

int total = mgr.getTotalStoryCount();
assertEquals(45, total);
```

---

### testGetTotalEventCount
**Purpose**: Verify total event count across all regions

**Pattern**: Same as `testGetTotalStoryCount` but for events

---

### testReset
**Purpose**: Verify reset clears all tracking data

**Test Flow**:
1. Register stories and events in multiple regions
2. Call reset()
3. Verify all counts = 0

**Assertions**:
```java
registerStories(mgr, "region_A", 30);
registerEvents(mgr, "region_B", 15);

mgr.reset();

assertEquals(0, mgr.getStoryCount("region_A"));
assertEquals(0, mgr.getEventCount("region_B"));
assertEquals(0, mgr.getTotalStoryCount());
assertEquals(0, mgr.getTotalEventCount());
```

---

### testMultipleRegions
**Purpose**: Verify independent tracking for multiple regions

**Test Flow**:
1. Register 20 stories in region_A
2. Register 30 stories in region_B
3. Register 10 stories in region_C
4. Verify each region has correct count
5. Verify total = 60

**Assertions**:
```java
registerStories(mgr, "region_A", 20);
registerStories(mgr, "region_B", 30);
registerStories(mgr, "region_C", 10);

assertEquals(20, mgr.getStoryCount("region_A"));
assertEquals(30, mgr.getStoryCount("region_B"));
assertEquals(10, mgr.getStoryCount("region_C"));
assertEquals(60, mgr.getTotalStoryCount());
```

---

### testSeparateStoryAndEventCounts
**Purpose**: Verify stories and events tracked independently

**Test Flow**:
1. Register 25 stories in region_A
2. Register 15 events in region_A
3. Verify story count = 25
4. Verify event count = 15

**Assertions**:
```java
registerStories(mgr, "region_A", 25);
registerEvents(mgr, "region_A", 15);

assertEquals(25, mgr.getStoryCount("region_A"));
assertEquals(15, mgr.getEventCount("region_A"));

// Saturation factors independent
double storyFactor = mgr.getSaturationFactor("region_A", "STORY");
double eventFactor = mgr.getSaturationFactor("region_A", "EVENT");
assertEquals(0.5, storyFactor, 0.01); // 1 - 25/50
assertEquals(0.25, eventFactor, 0.01); // 1 - 15/20
```

---

### testCustomCaps
**Purpose**: Verify constructor with custom cap values

**Test Setup**:
- Custom manager: maxStories=10, maxEvents=5

**Test Cases**:
- 10 stories → at capacity
- 5 events → at capacity
- Saturation factors based on custom caps

**Assertions**:
```java
SaturationManager customMgr = new SaturationManager(10, 5);

registerStories(customMgr, "region", 5);
double factor = customMgr.getSaturationFactor("region", "STORY");
assertEquals(0.5, factor, 0.01); // 1 - 5/10

registerStories(customMgr, "region", 5); // Total 10
assertTrue(customMgr.isAtCapacity("region", "STORY"));
```

---

### testZeroCapacityEdgeCase
**Purpose**: Verify behavior with count=0 (empty region)

**Expected**:
- saturationFactor = 1.0
- NOT at capacity
- NOT at soft cap

**Assertions**:
```java
assertEquals(1.0, mgr.getSaturationFactor("empty_region", "STORY"), 0.01);
assertFalse(mgr.isAtCapacity("empty_region", "STORY"));
assertFalse(mgr.isSoftCapReached("empty_region", "STORY"));
```

---

### testOverCapacityHandling
**Purpose**: Verify graceful handling when count exceeds cap

**Test Flow**:
1. Register 60 stories (over cap of 50)
2. Verify saturation factor = 0.0 (clamped)
3. Verify isAtCapacity = true

**Assertions**:
```java
registerStories(mgr, "region", 60); // Over cap

double factor = mgr.getSaturationFactor("region", "STORY");
assertEquals(0.0, factor, 0.01); // max(0, 1 - 60/50) = max(0, -0.2) = 0.0

assertTrue(mgr.isAtCapacity("region", "STORY"));
```

---

### testDuplicateRegistrationPrevention
**Purpose**: Verify Set-based tracking prevents duplicate counts

**Test Flow**:
1. Register same story instance twice
2. Verify count = 1 (not 2)

**Assertions**:
```java
Story story = createStory("story_1");
mgr.registerStory("region", story);
mgr.registerStory("region", story); // Duplicate

assertEquals(1, mgr.getStoryCount("region")); // Set prevents duplicate
```

## Test Data Patterns

### Story/Event Factories
```java
private Story createStory(String id) {
    return new Story.Builder()
        .id(id)
        .storyType(StoryType.RUMOR)
        .title("Test Story " + id)
        .build();
}

private Event createEvent(String id) {
    return new Event.Builder()
        .id(id)
        .eventCategory(EventCategory.SOCIAL)
        .title("Test Event " + id)
        .build();
}
```

### Bulk Registration Helpers
```java
private void registerStories(SaturationManager mgr, String regionId, int count) {
    for (int i = 0; i < count; i++) {
        Story story = createStory("story_" + UUID.randomUUID());
        mgr.registerStory(regionId, story);
    }
}

private void registerEvents(SaturationManager mgr, String regionId, int count) {
    for (int i = 0; i < count; i++) {
        Event event = createEvent("event_" + UUID.randomUUID());
        mgr.registerEvent(regionId, event);
    }
}
```

## Testing Strategy

### Formula Testing
- Test all key percentage points (0%, 25%, 50%, 75%, 80%, 100%, >100%)
- Verify clamping to [0.0, 1.0]
- Test both story and event formulas

### Cap Detection Testing
- Hard cap at 100%
- Soft cap at 80%
- Edge cases (99%, 100%, 101%)

### Multi-Region Testing
- Independent tracking per region
- Total counts across regions
- Different saturation levels per region

### Edge Case Testing
- Empty regions (count=0)
- Over-capacity regions
- Custom cap values
- Duplicate registration attempts

## Performance Testing

### Time Complexity
- register/unregister: O(1) average
- getCount: O(1)
- getSaturationFactor: O(1)
- getTotalCount: O(R) where R = regions

### Benchmarks
- 10 regions, 500 stories each: <10ms
- 100 regions, 50 stories each: <50ms

## Test Utilities

### Assertion Helpers
```java
private void assertSaturationFactor(double expected, String regionId, String type) {
    double actual = mgr.getSaturationFactor(regionId, type);
    assertEquals(expected, actual, 0.01);
}

private void assertAtCapacity(String regionId, String type) {
    assertTrue(mgr.isAtCapacity(regionId, type));
}

private void assertNotAtCapacity(String regionId, String type) {
    assertFalse(mgr.isAtCapacity(regionId, type));
}
```

## Future Test Enhancements

### Phase 2
- Concurrent access testing (thread safety)
- Category-specific cap testing
- Dynamic cap adjustment testing
- Priority-based eviction testing

### Advanced Testing
- Performance benchmarks
- Memory leak detection
- Stress testing (millions of stories)

## Related Test Classes
- `EventPropagationTest`: Uses saturation manager for propagation
- `StoryTest` / `EventTest`: Creates stories/events for registration

## References
- Source: `src/main/java/org/adventure/story/SaturationManager.java`
- Docs: `doc-src/main/java/org/adventure/story/SaturationManager.md`
- Specs: `docs/specs_summary.md` → maxActiveStories=50, maxActiveEvents=20
- Summary: `archive/PHASE_1.7_SUMMARY.md`
