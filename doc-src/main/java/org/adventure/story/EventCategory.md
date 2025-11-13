# EventCategory.java

## Overview
`EventCategory` is an enumeration that defines the five types of gameplay events. Each category has distinct characteristics, typical triggers, effects, and propagation behavior.

## Package
`org.adventure.story`

## Purpose
- Categorize events by gameplay type
- Define default propagation parameters per category
- Enable event filtering and management
- Support diverse gameplay experiences

## Enum Values

### COMBAT
**Description**: Battle, conflict, and warfare events

**Characteristics**:
- **Priority**: 7 (high)
- **Base Probability**: 0.80
- **Max Hops**: 5
- **Typical Duration**: Short (immediate resolution)
- **Impact**: Direct damage, loot, XP

**Examples**:
- "Bandit raid on village"
- "Dragon attack on castle"
- "Clan warfare erupts"
- "Monster ambush on travelers"
- "Naval battle at sea"

**Common Trigger Conditions**:
- `enemyPresent`: true/false
- `playerLevel`: comparison (">5", ">=10")
- `hasWeapon`: item check
- `defensiveStrength`: numeric threshold
- `timeOfDay`: "NIGHT" (for ambushes)

**Common Effects**:
- `modifyHealth`: damage amounts
- `addXP`: experience gains
- `dropLoot`: loot table references
- `damageStructure`: structure damage
- `spawnEnemies`: enemy group IDs
- `modifyRelationship`: faction standing changes

**Usage**: Combat encounters, raids, sieges, wars

---

### DISCOVERY
**Description**: Finding new locations, items, secrets, and knowledge

**Characteristics**:
- **Priority**: 6 (medium-high)
- **Base Probability**: 0.85
- **Max Hops**: 5
- **Typical Duration**: Instant (permanent unlocks)
- **Impact**: Reveal areas, grant items, unlock content

**Examples**:
- "Ancient ruins discovered in mountains"
- "Hidden treasure found in cave"
- "New trade route explored"
- "Lost city uncovered"
- "Mysterious artifact unearthed"

**Common Trigger Conditions**:
- `hasExplorationSkill`: skill level check
- `inRegionType`: region category ("UNEXPLORED", "WILDERNESS")
- `randomChance`: probability value (0.0-1.0)
- `hasItem`: tool requirements ("SHOVEL", "MAP")
- `completedQuest`: prerequisite quest IDs

**Common Effects**:
- `addResourceNode`: new resource spawns
- `revealMapArea`: fog of war removal
- `addXP`: discovery rewards
- `grantItem`: item additions
- `unlockRegion`: access grants
- `progressStory`: linked story advancement

**Usage**: Exploration mechanics, treasure hunting, world expansion

---

### SOCIAL
**Description**: Interpersonal events, politics, relationships, and culture

**Characteristics**:
- **Priority**: 5 (medium)
- **Base Probability**: 0.75
- **Max Hops**: 6
- **Typical Duration**: Variable (instant to long-running)
- **Impact**: Relationship changes, faction shifts, quests

**Examples**:
- "Wedding celebration in village"
- "Clan alliance formed"
- "Diplomatic incident between kingdoms"
- "Festival begins"
- "Trade agreement signed"

**Common Trigger Conditions**:
- `relationshipLevel`: numeric threshold (">75")
- `inSettlement`: true/false
- `timeOfDay`: time requirements ("EVENING")
- `hasReputation`: faction standing
- `populationSize`: settlement size check

**Common Effects**:
- `modifyRelationship`: faction/NPC standing changes
- `grantTitle`: honor/rank awards
- `triggerQuest`: quest activation
- `addBuff`: social buffs ("HONORED_GUEST")
- `modifyEconomy`: trade impacts
- `spawnNPC`: new character introductions

**Usage**: NPC interactions, politics, festivals, diplomacy

---

### ENVIRONMENTAL
**Description**: Natural phenomena, weather, terrain changes, and disasters

**Characteristics**:
- **Priority**: 4 (medium-low)
- **Base Probability**: 0.70
- **Max Hops**: 7 (spreads widely)
- **Typical Duration**: Variable (instant to persistent)
- **Impact**: Terrain modification, structure damage, resource changes

**Examples**:
- "Volcanic eruption devastates region"
- "Earthquake strikes"
- "Severe storm approaching"
- "Season changes to winter"
- "Drought affects crops"

**Common Trigger Conditions**:
- `biome`: biome requirements ("VOLCANIC", "COASTAL")
- `tick`: time-based triggers (">1000")
- `randomChance`: random occurrence probability
- `season`: seasonal requirements
- `weatherCondition`: weather state

**Common Effects**:
- `modifyTerrain`: terrain type changes ("ADD_LAVA", "FLOOD_PLAINS")
- `damageStructures`: percentage damage (0.0-1.0)
- `modifyResource`: resource availability changes
- `spawnEvent`: chain event triggers ("REFUGEE_MIGRATION")
- `addDebuff`: environmental debuffs ("FLOODED", "FROZEN")
- `changeBiome`: biome transitions (rare)

**Usage**: Natural disasters, weather systems, seasonal cycles

---

### MAGICAL
**Description**: Supernatural occurrences, magical phenomena, and arcane events

**Characteristics**:
- **Priority**: 8 (high)
- **Base Probability**: 0.90
- **Max Hops**: 6
- **Typical Duration**: Variable (instant to permanent)
- **Impact**: Magic effects, anomalies, supernatural changes

**Examples**:
- "Arcane anomaly detected"
- "Portal to another realm opens"
- "Magical curse activated"
- "Celestial alignment occurs"
- "Ancient spell unleashed"

**Common Trigger Conditions**:
- `manaLevel`: mana threshold (">50")
- `hasMagicSkill`: magic skill level (">75")
- `linkedStoryStatus`: story state check ("ACTIVE")
- `hasMagicItem`: artifact requirements
- `celestialEvent`: astronomical triggers

**Common Effects**:
- `modifyMana`: mana changes
- `addBuff`: magical buffs ("ARCANE_POWER", "BLESSED")
- `addDebuff`: magical debuffs ("CURSED", "SILENCED")
- `spawnCreature`: magical creature spawns ("ELEMENTAL", "DEMON")
- `teleportPlayer`: location changes
- `modifyMagicZone`: magic intensity areas

**Usage**: Magic systems, supernatural events, mystical storylines

## Usage

### In Event Creation
```java
Event event = new Event.Builder()
    .eventCategory(EventCategory.COMBAT)
    .title("Bandit Raid")
    // ...
    .build();
```

### In Filtering
```java
// Get all active combat events
List<Event> combatEvents = events.stream()
    .filter(e -> e.getEventCategory() == EventCategory.COMBAT)
    .filter(e -> e.getStatus() == EventStatus.ACTIVE)
    .collect(Collectors.toList());
```

### In Event Generation
```java
// Select category based on context
EventCategory category = selectEventCategory(region, random);
Event event = generateEvent(category, region);
```

## Propagation Parameters

Default values used when creating events:

| Category | Priority | Base Probability | Max Hops |
|----------|----------|------------------|----------|
| COMBAT | 7 | 0.80 | 5 |
| DISCOVERY | 6 | 0.85 | 5 |
| SOCIAL | 5 | 0.75 | 6 |
| ENVIRONMENTAL | 4 | 0.70 | 7 |
| MAGICAL | 8 | 0.90 | 6 |

**Notes**:
- MAGICAL has highest priority (8) → rarely pruned
- ENVIRONMENTAL has longest range (7 hops) → natural phenomena spread far
- SOCIAL has medium range (6 hops) → news travels
- COMBAT and DISCOVERY have shorter range (5 hops) → localized events

## Design Decisions

### Five Categories
Chosen to cover core gameplay pillars:
- **Action**: COMBAT
- **Exploration**: DISCOVERY
- **Roleplay**: SOCIAL
- **Survival**: ENVIRONMENTAL
- **Fantasy**: MAGICAL

### Priority Distribution
- High (7-8): COMBAT, MAGICAL (important gameplay)
- Medium (5-6): SOCIAL, DISCOVERY (standard content)
- Low (4): ENVIRONMENTAL (background events)

### Propagation Patterns
- **Fast spread, short range**: COMBAT (urgent but localized)
- **Fast spread, medium range**: DISCOVERY, MAGICAL (exciting news)
- **Slow spread, long range**: ENVIRONMENTAL (gradual natural effects)
- **Medium spread, medium range**: SOCIAL (gossip and news)

## Implementation Details

### Enum Declaration
```java
public enum EventCategory {
    COMBAT,
    DISCOVERY,
    SOCIAL,
    ENVIRONMENTAL,
    MAGICAL
}
```

### No Additional Methods
Simple enumeration. All logic for parameters is in event generation systems.

### Case Sensitivity
All uppercase (Java convention): `EventCategory.COMBAT`, not `EventCategory.Combat`

## Testing

Covered in `EventTest.java`:
- Event creation with each category
- Category-specific default parameters
- Category filtering
- Category equality

## Integration Points

### Event Class
- Stores category as immutable field
- Validates category is not null

### Event Generation Systems
- Select category based on context
- Apply category-specific defaults
- Weight categories by region type

### SaturationManager
- Tracks events by category
- Enforces category-specific caps (future)
- Reports category distribution

## Future Enhancements

### Phase 2
- Category-specific saturation caps
- Category combinations (COMBAT + MAGICAL)
- Subcategories (COMBAT_MELEE, COMBAT_RANGED, COMBAT_SIEGE)
- Player-defined custom categories (via modding)
- Category-based UI filtering

### Advanced Features
- Category affinity by region type
- Category exclusion rules (no COMBAT in safe zones)
- Category synergies (MAGICAL + ENVIRONMENTAL = weather magic)
- Dynamic category balancing

## Related Classes
- `Event`: Main data model that uses EventCategory
- `EventStatus`: Enum for event lifecycle states
- `StoryType`: Similar enum for story categorization
- `EventPropagation`: Uses category for propagation logic

## References
- Design: `docs/stories_events.md` → Event Types
- Specs: `docs/specs_summary.md` → Event Defaults
- Summary: `archive/PHASE_1.7_SUMMARY.md`
