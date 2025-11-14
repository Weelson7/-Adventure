# Prophecy

**Package:** `org.adventure.prophecy`  
**Type:** Class (Entity)  
**Since:** Phase 1.10.1

---

## Overview

`Prophecy` represents a major world prediction or destiny in !Adventure. Prophecies are:
- **Generated at worldgen** (1-3 per world)
- **Linked to world features** (MAGIC_ZONE, ANCIENT_RUINS, VOLCANO)
- **Time-based or event-triggered** (activate at specific tick or on player action)
- **Story generators** (become stories when revealed/fulfilled)

Prophecies add narrative depth and long-term goals to worlds.

---

## Fields

### Core Identity
```java
private String id;                    // Unique ID (e.g., "prophecy_doom_12345")
private String title;                 // Display name (e.g., "The Volcanic Doom")
private String description;           // Full prophecy text
private ProphecyType type;            // DOOM, SALVATION, TRANSFORMATION, AWAKENING
private ProphecyStatus status;        // HIDDEN, REVEALED, IN_PROGRESS, FULFILLED, FAILED
```

### Trigger Mechanics
```java
private long revealTick;              // When prophecy becomes known (-1 = immediate)
private long triggerTick;             // When prophecy activates (-1 = event-based)
private List<String> triggerConditions; // Player/NPC actions that trigger early
private long timeLimit;               // Ticks after reveal before auto-trigger (-1 = none)
```

### World Integration
```java
private int linkedFeatureId;          // Associated RegionalFeature ID
private int linkedBiomeX;             // Biome X coordinate (for location)
private int linkedBiomeY;             // Biome Y coordinate (for location)
private List<String> affectedClanIds; // Clans involved in prophecy
```

### Outcomes
```java
private String fulfillmentEffect;     // What happens when fulfilled
private String failureEffect;         // What happens if failed
private boolean isPlayerInvolved;     // Whether player can influence outcome
```

### Metadata
```java
private String schemaVersion = "1.0";
private long createdAtTick;
private long lastUpdatedTick;
```

---

## Constructor & Builder

### Builder Pattern
```java
public static class Builder {
    public Builder id(String id);
    public Builder title(String title);
    public Builder description(String description);
    public Builder type(ProphecyType type);
    public Builder status(ProphecyStatus status);
    public Builder revealTick(long tick);
    public Builder triggerTick(long tick);
    public Builder addTriggerCondition(String condition);
    public Builder timeLimit(long limit);
    public Builder linkedFeature(int featureId, int x, int y);
    public Builder addAffectedClan(String clanId);
    public Builder fulfillmentEffect(String effect);
    public Builder failureEffect(String effect);
    public Builder playerInvolved(boolean involved);
    public Builder createdAtTick(long tick);
    
    public Prophecy build();
}
```

---

## Key Methods

### `isRevealed()`
```java
public boolean isRevealed(long currentTick) {
    return status != ProphecyStatus.HIDDEN 
        || (revealTick >= 0 && currentTick >= revealTick);
}
```

### `shouldTrigger()`
```java
public boolean shouldTrigger(long currentTick) {
    if (!isRevealed(currentTick)) return false;
    if (status != ProphecyStatus.REVEALED && 
        status != ProphecyStatus.IN_PROGRESS) {
        return false;
    }
    
    // Time-based trigger
    if (triggerTick >= 0 && currentTick >= triggerTick) {
        return true;
    }
    
    // Time limit exceeded
    if (timeLimit > 0 && 
        currentTick >= revealTick + timeLimit) {
        return true;
    }
    
    return false;
}
```

### `checkTriggerCondition()`
```java
public boolean checkTriggerCondition(String condition) {
    return triggerConditions.contains(condition);
}
```

### `reveal()`
```java
public void reveal(long currentTick) {
    if (status == ProphecyStatus.HIDDEN) {
        status = ProphecyStatus.REVEALED;
        lastUpdatedTick = currentTick;
    }
}
```

### `fulfill()`
```java
public void fulfill(long currentTick) {
    status = ProphecyStatus.FULFILLED;
    lastUpdatedTick = currentTick;
}
```

### `fail()`
```java
public void fail(long currentTick) {
    status = ProphecyStatus.FAILED;
    lastUpdatedTick = currentTick;
}
```

---

## Prophecy Lifecycle

```
CREATION (worldgen, tick 0)
  ↓
HIDDEN (unknown to world)
  ↓ revealTick reached OR event occurs
REVEALED (known, but not active)
  ↓ triggerTick reached OR condition met
IN_PROGRESS (actively unfolding)
  ↓ player action OR time elapsed
FULFILLED (prophecy came true)
  OR
FAILED (prophecy prevented/expired)
```

---

## Example Prophecies

### Doom Prophecy
```java
Prophecy doom = new Prophecy.Builder()
    .id("prophecy_doom_volcano_123")
    .title("The Volcanic Doom")
    .description("When the volcano awakens, it shall consume the northern clans in fire and ash. Only those who flee south shall survive.")
    .type(ProphecyType.DOOM)
    .status(ProphecyStatus.HIDDEN)
    .revealTick(50000) // Revealed after 5 in-game years
    .triggerTick(100000) // Erupts after 10 years
    .linkedFeature(volcanoFeatureId, 50, 10)
    .addAffectedClan("clan_north_001")
    .addAffectedClan("clan_north_002")
    .fulfillmentEffect("VOLCANO_ERUPTION:50,10:radius=20")
    .failureEffect("NONE") // Can't be prevented
    .playerInvolved(true) // Player can evacuate clans
    .createdAtTick(0)
    .build();
```

### Salvation Prophecy
```java
Prophecy salvation = new Prophecy.Builder()
    .id("prophecy_salvation_hero_456")
    .title("The Chosen Hero")
    .description("From the swamp shall rise a hero bearing the Mark of Unity, destined to unite the warring clans under one banner.")
    .type(ProphecyType.SALVATION)
    .status(ProphecyStatus.HIDDEN)
    .revealTick(10000) // Revealed after 1 year
    .addTriggerCondition("PLAYER_ENTERS:swamp_feature_789")
    .addTriggerCondition("PLAYER_HAS_ITEM:mark_of_unity")
    .timeLimit(500000) // 50 years to fulfill
    .linkedFeature(swampFeatureId, 120, 80)
    .fulfillmentEffect("UNITE_CLANS:all")
    .failureEffect("ETERNAL_WAR:all_clans")
    .playerInvolved(true)
    .createdAtTick(0)
    .build();
```

### Transformation Prophecy
```java
Prophecy transformation = new Prophecy.Builder()
    .id("prophecy_transform_magic_789")
    .title("The Magical Expansion")
    .description("The arcane energies shall spread from the magic zone, transforming the forest into an enchanted realm of wonder and danger.")
    .type(ProphecyType.TRANSFORMATION)
    .status(ProphecyStatus.REVEALED) // Known from start
    .triggerTick(200000) // Happens after 20 years
    .linkedFeature(magicZoneId, 80, 60)
    .fulfillmentEffect("BIOME_TRANSFORM:forest→enchanted:radius=30")
    .failureEffect("NONE") // Inevitable
    .playerInvolved(false) // Player can't prevent
    .createdAtTick(0)
    .build();
```

---

## Trigger Conditions

### Format
```
"ACTION_TYPE:parameters"
```

### Examples
```java
"PLAYER_ENTERS:feature_123"           // Player visits location
"PLAYER_HAS_ITEM:ancient_artifact"    // Player possesses item
"CLAN_SIZE:clan_001:>100"             // Clan grows beyond threshold
"CLAN_DESTROYED:clan_002"             // Clan eliminated
"STRUCTURE_BUILT:TEMPLE:region_50_60" // Structure constructed
"TICK_REACHED:100000"                 // Specific time
"STORY_COMPLETED:story_456"           // Story arc finished
```

---

## Effects Format

### Fulfillment Effects
```java
"VOLCANO_ERUPTION:x,y:radius=20"           // Disaster at location
"UNITE_CLANS:clan1,clan2,clan3"            // Force clan merger
"BIOME_TRANSFORM:forest→enchanted:r=30"    // Change biome type
"SPAWN_BOSS:dragon:x,y"                    // Spawn enemy
"GRANT_ITEM:ancient_weapon:player"         // Give reward
"UNLOCK_REGION:hidden_valley"              // Open new area
```

### Failure Effects
```java
"ETERNAL_WAR:all_clans"                    // Permanent conflict
"CURSE:clan_001:morale=-50"                // Negative effect
"NONE"                                     // No consequence
```

---

## Integration

### With ProphecyGenerator
```java
List<Prophecy> prophecies = ProphecyGenerator.generateProphecies(
    worldSeed, features, biomes);

// Result: 1-3 prophecies linked to world features
```

### With StoryGenerator
```java
// When prophecy revealed → create story
if (prophecy.isRevealed(currentTick) && !prophecy.storyCreated) {
    Story story = StoryGenerator.createProphecyStory(prophecy);
    world.addStory(story);
    prophecy.storyCreated = true;
}
```

### With RegionSimulator
```java
// Check prophecy triggers each tick
for (Prophecy prophecy : world.getProphecies()) {
    if (prophecy.shouldTrigger(currentTick)) {
        executeProphecyEffect(prophecy);
        prophecy.fulfill(currentTick);
    }
}
```

---

## Testing

```java
@Test
public void testProphecyLifecycle() {
    Prophecy p = new Prophecy.Builder()
        .id("test_prophecy")
        .status(ProphecyStatus.HIDDEN)
        .revealTick(1000)
        .triggerTick(2000)
        .build();
    
    assertFalse(p.isRevealed(500));   // Before reveal
    assertTrue(p.isRevealed(1000));   // At reveal
    assertFalse(p.shouldTrigger(1000)); // Revealed but not triggered
    assertTrue(p.shouldTrigger(2000));  // At trigger
}
```

---

## Related Classes

- `ProphecyType` — Enum for prophecy categories
- `ProphecyStatus` — Enum for prophecy state
- `ProphecyGenerator` — Factory for creating prophecies
- `RegionalFeature` — Linked world features
- `Story` — Prophecies become stories when revealed
