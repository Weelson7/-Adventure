# Event.java

## Overview
`Event` represents a discrete gameplay occurrence that can affect characters, regions, stories, and world state. Events are triggered by conditions, propagate across regions, and produce measurable effects on game systems.

## Package
`org.adventure.story`

## Purpose
- Represent discrete gameplay occurrences (combat, discovery, social, environmental, magical)
- Link events to stories for narrative progression
- Enable event propagation with decay
- Track event triggers, conditions, and effects
- Support deterministic event generation

## Key Features

### Data Model
- **Unique ID**: String identifier
- **Category**: Type of event (COMBAT, DISCOVERY, SOCIAL, ENVIRONMENTAL, MAGICAL)
- **Status**: Lifecycle state (PENDING, ACTIVE, RESOLVED, CANCELLED, EXPIRED)
- **Story Linkage**: Optional linkedStoryId for narrative integration
- **Trigger System**: Conditions map for activation requirements
- **Effects System**: Effects map for gameplay impact
- **Propagation**: baseProbability, hopCount, maxHops for spread control

### Builder Pattern
```java
Event event = new Event.Builder()
    .id("event_12345_1")
    .eventCategory(EventCategory.DISCOVERY)
    .title("Ancient Ruins Discovered")
    .description("Explorers found mysterious ruins in the mountains.")
    .originTileId(2048)
    .originTick(150)
    .linkedStoryId("story_12345_0")
    .baseProbability(0.85)
    .maxHops(5)
    .priority(7)
    .triggerCondition("regionBiome", "MOUNTAIN")
    .effect("addResourceNode", "ANCIENT_ARTIFACT")
    .build();
```

### Validation
- ID cannot be null or empty
- EventCategory must be specified
- Title cannot be null or empty
- baseProbability in [0.0, 1.0]
- priority in [0, 10]

## Fields

### Required Fields
- `id` (String): Unique identifier
- `type` (String): Always "story/Event" for persistence
- `schemaVersion` (int): Current version = 1
- `eventCategory` (EventCategory): Type of event
- `status` (EventStatus): Current lifecycle state
- `title` (String): Display name
- `description` (String): Event description

### Origin Tracking
- `originTileId` (int): Tile where event originated
- `originTick` (long): Game tick when event occurred

### Story Integration
- `linkedStoryId` (String): Optional story this event relates to

### Trigger System
- `triggerConditions` (Map<String, Object>): Conditions for event activation
  - Example: `{"regionPopulation": ">100", "hasStructureType": "TEMPLE"}`

### Effects System
- `effects` (Map<String, Object>): Gameplay impacts when event triggers
  - Example: `{"modifyResource": {"GOLD": -500}, "addBuff": "BLESSED"}`

### Propagation Parameters
- `baseProbability` (double): Base spread chance [0.0, 1.0], default 0.9
- `hopCount` (int): Current propagation distance, default 0
- `maxHops` (int): Maximum propagation distance, default 6
- `priority` (int): Event importance [0, 10], default 5

### State Tracking
- `lastProcessedTick` (long): Last tick when event was updated, default 0
- `affectedRegions` (Set<String>): Region IDs where event has occurred
- `metadata` (Map<String, Object>): Additional data

## Event Categories

### COMBAT
**Description**: Battle, conflict, warfare events

**Priority**: 7 (high)
**Base Probability**: 0.80
**Max Hops**: 5

**Examples**:
- "Bandit raid on village"
- "Dragon attack"
- "Clan warfare"

**Trigger Conditions**:
- `enemyPresent`: boolean
- `playerLevel`: ">5"
- `hasWeapon`: "SWORD"

**Effects**:
- `modifyHealth`: -20
- `addXP`: 100
- `dropLoot`: "BANDIT_LOOT_TABLE"

---

### DISCOVERY
**Description**: Finding new locations, items, secrets

**Priority**: 6 (medium-high)
**Base Probability**: 0.85
**Max Hops**: 5

**Examples**:
- "Ancient ruins discovered"
- "Hidden treasure found"
- "New region explored"

**Trigger Conditions**:
- `hasExplorationSkill`: ">50"
- `inRegionType`: "UNEXPLORED"
- `randomChance`: 0.3

**Effects**:
- `addResourceNode`: "ARTIFACT"
- `revealMapArea`: "RUINS_001"
- `addXP`: 50

---

### SOCIAL
**Description**: Interpersonal events, politics, relationships

**Priority**: 5 (medium)
**Base Probability**: 0.75
**Max Hops**: 6

**Examples**:
- "Wedding celebration"
- "Clan alliance formed"
- "Diplomatic incident"

**Trigger Conditions**:
- `relationshipLevel`: ">75"
- `inSettlement`: true
- `timeOfDay`: "EVENING"

**Effects**:
- `modifyRelationship`: {target: "CLAN_A", amount: 10}
- `grantTitle`: "HONORED_GUEST"
- `triggerQuest`: "WEDDING_QUEST"

---

### ENVIRONMENTAL
**Description**: Natural phenomena, weather, terrain changes

**Priority**: 4 (medium-low)
**Base Probability**: 0.70
**Max Hops**: 7

**Examples**:
- "Volcanic eruption"
- "Earthquake"
- "Season change"

**Trigger Conditions**:
- `biome`: "VOLCANIC"
- `tick`: ">1000"
- `randomChance`: 0.1

**Effects**:
- `modifyTerrain`: "ADD_LAVA"
- `damageStructures`: 0.5
- `spawnEvent`: "REFUGEE_MIGRATION"

---

### MAGICAL
**Description**: Supernatural occurrences, magical phenomena

**Priority**: 8 (high)
**Base Probability**: 0.90
**Max Hops**: 6

**Examples**:
- "Arcane anomaly detected"
- "Portal opening"
- "Magical curse activated"

**Trigger Conditions**:
- `manaLevel`: ">50"
- `hasMagicSkill`: ">75"
- `linkedStoryStatus`: "ACTIVE"

**Effects**:
- `modifyMana`: -30
- `addBuff": "ARCANE_POWER"
- `spawnCreature`: "ELEMENTAL"

## Event Status Lifecycle

### PENDING
- Event created but not yet active
- Waiting for trigger conditions
- Does not propagate

### ACTIVE
- Event is occurring and can propagate
- Trigger conditions met
- Effects applied to affected regions
- Counts toward saturation caps (20 events per region)

### RESOLVED
- Event has completed
- Effects already applied
- No longer propagates
- Archived after cooldown

### CANCELLED
- Event was prevented or stopped
- Effects not applied
- May trigger compensating events

### EXPIRED
- Event's time window passed without triggering
- Cleanup state before archival

## Methods

### Getters
All fields have getters with defensive copies for collections:
- `getId()`, `getType()`, `getSchemaVersion()`
- `getEventCategory()`, `getStatus()`, `getTitle()`, `getDescription()`
- `getOriginTileId()`, `getOriginTick()`, `getLinkedStoryId()`
- `getTriggerConditions()` - defensive copy
- `getEffects()` - defensive copy
- `getBaseProbability()`, `getHopCount()`, `getMaxHops()`, `getPriority()`
- `getLastProcessedTick()`
- `getAffectedRegions()` - defensive copy
- `getMetadata()` - defensive copy

### Setters (Mutable Fields)
- `setStatus(EventStatus)`: Change lifecycle state
- `setHopCount(int)`: Update propagation distance
- `setLastProcessedTick(long)`: Track processing time
- `addAffectedRegion(String)`: Register region impact
- `setMetadata(String, Object)`: Add custom data

### Special Methods
- `isTriggered()`: Check if all trigger conditions are satisfied
  - Returns `true` if triggerConditions is empty (unconditional event)
  - Otherwise, requires external condition checking logic

### Object Methods
- `equals(Object)`: Equality based on ID only
- `hashCode()`: Hash based on ID
- `toString()`: Includes id, eventCategory, status, title, linkedStoryId, priority

## Usage Examples

### Creating a Combat Event
```java
Event combat = new Event.Builder()
    .id("event_combat_001")
    .eventCategory(EventCategory.COMBAT)
    .status(EventStatus.PENDING)
    .title("Bandit Raid")
    .description("A group of bandits is attacking the village.")
    .originTileId(1024)
    .originTick(500)
    .baseProbability(0.80)
    .maxHops(5)
    .priority(7)
    .triggerCondition("hasStructure", "VILLAGE")
    .triggerCondition("defensiveStrength", "<50")
    .effect("damageStructure", 20)
    .effect("spawnEnemies", "BANDIT_GROUP")
    .build();
```

### Linking Event to Story
```java
Event discovery = new Event.Builder()
    .id("event_discovery_001")
    .eventCategory(EventCategory.DISCOVERY)
    .linkedStoryId("story_legend_001") // Link to "Lost Sword" legend
    .title("Sword Fragment Found")
    .description("A fragment of the legendary sword was discovered.")
    .triggerCondition("linkedStoryStatus", "ACTIVE")
    .effect("progressStory", "story_legend_001")
    .build();
```

### Checking Trigger Conditions
```java
// Simple unconditional check
if (event.isTriggered()) {
    // Event has no conditions, trigger immediately
}

// External condition validation
Map<String, Object> conditions = event.getTriggerConditions();
boolean canTrigger = validateConditions(conditions, currentGameState);
if (canTrigger) {
    event.setStatus(EventStatus.ACTIVE);
    applyEffects(event.getEffects());
}
```

### Propagating an Event
```java
// Event spreads to neighboring region
event.addAffectedRegion("region_1025");
event.setHopCount(event.getHopCount() + 1);
event.setLastProcessedTick(currentTick);

// Check if reached max distance
if (event.getHopCount() >= event.getMaxHops()) {
    event.setStatus(EventStatus.EXPIRED);
}
```

### Resolving an Event
```java
if (eventCompleted) {
    event.setStatus(EventStatus.RESOLVED);
    event.setLastProcessedTick(currentTick);
    event.setMetadata("resolvedBy", playerId);
    event.setMetadata("outcome", "SUCCESS");
    
    // Update linked story
    if (event.getLinkedStoryId() != null) {
        Story story = getStory(event.getLinkedStoryId());
        story.setMetadata("eventProgress", story.getMetadata().get("eventProgress") + 1);
    }
}
```

## Persistence

### Schema Version
Current version: 1

### Required Fields for Persistence
- `type`: "story/Event"
- `schemaVersion`: 1
- `lastProcessedTick`: For resynchronization

### JSON Serialization
Uses Jackson annotations for all fields. Maps serialized as JSON objects.

## Integration Points

### Story System
- `linkedStoryId` links events to stories
- Events drive story progression
- Story status can be event trigger condition

### Region Simulation
- Events processed each tick
- Region state affects trigger conditions
- Effects modify region state

### EventPropagation
- BFS-based spread with decay
- Uses `EventPropagation.propagateEvent()`
- Respects saturation caps (20 events per region)

### Character/NPC System
- Events affect NPC behavior
- NPCs can trigger events
- Events modify character stats

## Design Decisions

### Flexible Trigger System
- Map<String, Object> allows any condition type
- No schema validation for extensibility
- Game logic validates conditions externally

### Flexible Effects System
- Map<String, Object> allows any effect type
- Effects interpreted by game systems
- Enables data-driven event design

### Story Linkage
- Optional linkedStoryId (can be null)
- One event → one story (Phase 1)
- Future: Events can affect multiple stories

### Propagation Model
- Same BFS algorithm as stories
- Separate saturation caps (20 vs 50)
- Lower caps reflect event transience

## Testing

### Test Coverage
19 tests in `EventTest.java`:
- Builder validation
- Trigger system (isTriggered, conditions)
- Effects system
- Story linkage
- Status transitions
- Affected regions
- Default values
- Equality and hashCode

### Determinism
Validated in integration tests with EventPropagation.

## Performance Considerations

- Trigger/effect maps have no size limits (consider caps in Phase 2)
- `isTriggered()` is O(1) for empty conditions, O(n) for validation
- Defensive copies add overhead

## Future Enhancements

### Phase 2
- Event chaining (one event triggers another)
- Event templates (reusable event definitions)
- Conditional effects (if-then-else in effects map)
- Event history tracking
- Player-created events

### Advanced Features
- Multi-story linkage
- Complex trigger logic (AND, OR, NOT)
- Timed events (duration, cooldowns)
- Event cancellation mechanics
- Event impact visualization

## Related Classes
- `EventCategory`: Enum of event types
- `EventStatus`: Enum of lifecycle states
- `EventPropagation`: Handles event spread via BFS
- `SaturationManager`: Enforces per-region event caps
- `Story`: Events can link to stories

## References
- Design: `docs/stories_events.md`
- Specs: `docs/specs_summary.md` → Event Propagation
- Summary: `archive/PHASE_1.7_SUMMARY.md`
