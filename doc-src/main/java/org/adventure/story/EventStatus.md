# EventStatus.java

## Overview
`EventStatus` is an enumeration that defines the lifecycle states of an event in the game. Events transition through statuses from creation through resolution or expiration.

## Package
`org.adventure.story`

## Purpose
- Track event lifecycle from creation to completion
- Control event propagation and activation
- Manage event cleanup and archival
- Enable conditional event triggering

## Enum Values

### PENDING
**Description**: Event created but not yet active, waiting for trigger conditions

**Characteristics**:
- Initial status for conditional events
- Event exists but does not propagate
- Does not count toward saturation caps
- Trigger conditions must be checked each tick
- Can transition to ACTIVE when conditions met

**Transitions**:
- → ACTIVE: Trigger conditions satisfied
- → CANCELLED: Conditions will never be met
- → EXPIRED: Time limit exceeded

**Usage**: Delayed events, conditional events, quest triggers

**Example**:
```java
Event quest = new Event.Builder()
    .eventCategory(EventCategory.DISCOVERY)
    .status(EventStatus.PENDING)
    .triggerCondition("playerLevel", ">=10")
    .triggerCondition("hasItem", "ANCIENT_KEY")
    .build();

// Check each tick
if (checkTriggerConditions(quest)) {
    quest.setStatus(EventStatus.ACTIVE);
}
```

---

### ACTIVE
**Description**: Event is occurring and can propagate to neighboring regions

**Characteristics**:
- Default status for unconditional events
- Event propagates via BFS algorithm
- Counts toward saturation caps (20 events per region)
- Effects are applied to affected regions
- Can spread to new regions each tick (based on probability)

**Transitions**:
- → RESOLVED: Event completed successfully
- → CANCELLED: Event interrupted or prevented
- → EXPIRED: Event duration elapsed

**Usage**: Most events spend their lifecycle in ACTIVE status

**Example**:
```java
Event combat = new Event.Builder()
    .eventCategory(EventCategory.COMBAT)
    .status(EventStatus.ACTIVE) // Starts immediately
    .build();

// Propagate each tick
if (combat.getStatus() == EventStatus.ACTIVE) {
    propagateEvent(combat);
}
```

---

### RESOLVED
**Description**: Event has completed or been resolved

**Characteristics**:
- Terminal status for successfully completed events
- No longer propagates
- Effects already applied
- May trigger follow-up events
- Archived after cooldown period
- Still counts toward caps until archived

**Transitions**:
- → (Archived/Removed): After cooldown period

**Usage**: Completed quests, resolved conflicts, finished discoveries

**Example**:
```java
// Combat ended
if (enemiesDefeated) {
    combat.setStatus(EventStatus.RESOLVED);
    combat.setLastProcessedTick(currentTick);
    combat.setMetadata("outcome", "VICTORY");
    combat.setMetadata("casualties", 3);
    
    // Trigger follow-up event
    Event celebration = new Event.Builder()
        .eventCategory(EventCategory.SOCIAL)
        .title("Victory Celebration")
        .linkedStoryId(combat.getLinkedStoryId())
        .build();
}
```

---

### CANCELLED
**Description**: Event was prevented, interrupted, or stopped before completion

**Characteristics**:
- Terminal status for interrupted events
- Effects not applied (or partially applied)
- No further propagation
- May trigger compensating events
- Cleaned up after short cooldown

**Transitions**:
- → (Removed): After cooldown period

**Usage**: Prevented disasters, stopped attacks, failed quests

**Example**:
```java
// Environmental disaster prevented
if (playerUsedProtectionSpell) {
    earthquake.setStatus(EventStatus.CANCELLED);
    earthquake.setMetadata("preventedBy", playerId);
    
    // Generate follow-up event
    Event relief = new Event.Builder()
        .eventCategory(EventCategory.SOCIAL)
        .title("Town Celebrates Narrow Escape")
        .build();
}
```

---

### EXPIRED
**Description**: Event's time window passed without triggering or completing

**Characteristics**:
- Terminal status for time-limited events
- PENDING events that never activated
- ACTIVE events that timed out
- No effects applied
- Cleaned up immediately or after brief cooldown

**Transitions**:
- → (Removed): Immediate cleanup

**Usage**: Timed quests, seasonal events, limited-time opportunities

**Example**:
```java
// Quest expired
long questDuration = 5000; // ticks
if (quest.getStatus() == EventStatus.PENDING &&
    currentTick - quest.getOriginTick() > questDuration) {
    quest.setStatus(EventStatus.EXPIRED);
    quest.setMetadata("expiredAt", currentTick);
}
```

## Status Transition Diagram

```
         ┌─────────┐
    ┌───►│ PENDING │
    │    └────┬────┘
    │         │
    │         │ Conditions
    │         │ met
    │         ▼
    │    ┌────────┐   Interrupted    ┌───────────┐
    │    │ ACTIVE │─────────────────►│ CANCELLED │
    │    └───┬────┘                  └─────┬─────┘
    │        │                             │
    │        │ Completed                   │
    │        │                             │
    │        ▼                             │
    │   ┌──────────┐                       │
    │   │ RESOLVED │                       │
    │   └────┬─────┘                       │
    │        │                             │
    │        │                             │
    │        ▼                             ▼
    │   ┌─────────────────────────────────────┐
    └───┤            EXPIRED                  │
        │   (time limit exceeded)             │
        └─────────────────────────────────────┘
                       │
                       ▼
                  (Removed/Archived)
```

## Usage

### Creating Events with Status
```java
// Immediate event
Event immediate = new Event.Builder()
    .status(EventStatus.ACTIVE) // Default
    .build();

// Conditional event
Event conditional = new Event.Builder()
    .status(EventStatus.PENDING)
    .triggerCondition("requirement", "value")
    .build();
```

### Checking Event State
```java
if (event.getStatus() == EventStatus.ACTIVE) {
    // Event can propagate
    propagateEvent(event);
} else if (event.getStatus() == EventStatus.PENDING) {
    // Check if conditions are met
    if (event.isTriggered()) {
        event.setStatus(EventStatus.ACTIVE);
    }
}
```

### Transitioning Status
```java
// Complete an event
if (eventCompleted) {
    event.setStatus(EventStatus.RESOLVED);
    event.setLastProcessedTick(currentTick);
    applyEventEffects(event);
}

// Cancel an event
if (eventPrevented) {
    event.setStatus(EventStatus.CANCELLED);
    notifyPlayers("Event prevented!");
}

// Expire an event
if (isExpired(event)) {
    event.setStatus(EventStatus.EXPIRED);
}
```

### Filtering by Status
```java
// Get all active events
List<Event> activeEvents = allEvents.stream()
    .filter(e -> e.getStatus() == EventStatus.ACTIVE)
    .collect(Collectors.toList());

// Get events to clean up
List<Event> toRemove = allEvents.stream()
    .filter(e -> e.getStatus() == EventStatus.RESOLVED ||
                 e.getStatus() == EventStatus.CANCELLED ||
                 e.getStatus() == EventStatus.EXPIRED)
    .filter(e -> currentTick - e.getLastProcessedTick() > CLEANUP_THRESHOLD)
    .collect(Collectors.toList());
```

## Saturation Management

### Events That Count Toward Caps
- ACTIVE: Always counts
- PENDING: May count (configurable)
- RESOLVED: Counts briefly until cleanup

### Events That Don't Count
- CANCELLED: Excluded after status change
- EXPIRED: Excluded after status change

### Cap Enforcement
```java
// SaturationManager tracks by status
public int getActiveEventCount(String regionId) {
    return eventsByRegion.get(regionId).stream()
        .filter(e -> e.getStatus() == EventStatus.ACTIVE ||
                     e.getStatus() == EventStatus.PENDING)
        .count();
}
```

## Implementation Details

### Enum Declaration
```java
public enum EventStatus {
    PENDING,
    ACTIVE,
    RESOLVED,
    CANCELLED,
    EXPIRED
}
```

### No Additional Methods
Simple enumeration with no instance methods. All logic in `Event` and event management systems.

### Default Value
Events default to `ACTIVE` unless explicitly set to `PENDING` via builder.

## Design Decisions

### Five States
Covers full lifecycle:
- **Creation**: PENDING (conditional) or ACTIVE (immediate)
- **Execution**: ACTIVE (propagating and affecting world)
- **Completion**: RESOLVED (success), CANCELLED (prevented), EXPIRED (timeout)

### Terminal States
RESOLVED, CANCELLED, and EXPIRED are terminal - events don't transition out. Prevents complex state machines.

### No Archival Status
Unlike stories, events are simply removed after cooldown. Events are more transient than stories.

### Cooldown Periods
- RESOLVED → removal: Standard cooldown (allow effects to settle)
- CANCELLED → removal: Short cooldown (quick cleanup)
- EXPIRED → removal: Immediate (no effects applied)

## Testing

Covered in `EventTest.java`:
- Status transitions
- Default status (ACTIVE)
- Status-based filtering
- Saturation counting by status
- Terminal state verification

## Integration Points

### Event Class
- Stores status as mutable field
- Validates status changes
- Tracks lastProcessedTick for cleanup timing

### EventPropagation
- Only propagates ACTIVE events
- Skips PENDING (checks triggers separately)
- Ignores RESOLVED, CANCELLED, EXPIRED

### SaturationManager
- Counts events based on status
- Excludes terminal states from active caps
- May include PENDING in counts

### Region Simulation
- Processes ACTIVE events each tick
- Checks PENDING → ACTIVE triggers
- Cleans up terminal states after cooldown

## Future Enhancements

### Phase 2
- PAUSED status for temporarily halted events
- RECURRING status for repeated events
- CHAINED status for event sequences
- CONDITIONAL_ACTIVE status for events that can pause

### Advanced Features
- Status history tracking
- Status-based event triggers
- Player-visible status notifications
- Status-specific UI indicators
- Auto-cleanup configuration per status

## Related Classes
- `Event`: Main data model that uses EventStatus
- `EventCategory`: Enum for event types
- `StoryStatus`: Similar enum for story lifecycle
- `EventPropagation`: Checks status before propagation
- `SaturationManager`: Counts events by status

## References
- Design: `docs/stories_events.md` → Event Lifecycle
- Specs: `docs/specs_summary.md` → Event States
- Implementation: `src/main/java/org/adventure/story/Event.java`
- Summary: `archive/PHASE_1.7_SUMMARY.md`
