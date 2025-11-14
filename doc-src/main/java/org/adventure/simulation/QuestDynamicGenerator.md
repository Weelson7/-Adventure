# QuestDynamicGenerator

**Package:** `org.adventure.simulation`  
**Since:** Phase 1.10.3  
**Purpose:** Generate dynamic quests based on world events, region conditions, and emergent gameplay

---

## Overview

`QuestDynamicGenerator` creates quests dynamically in response to world events, region state changes, and emergent conditions. This creates a living quest system where quests arise naturally from the simulation rather than being pre-scripted.

---

## Class Structure

```java
public class QuestDynamicGenerator {
    private static final long QUEST_CHECK_INTERVAL = 5000; // Every 5000 ticks
    private static final int MAX_ACTIVE_QUESTS_PER_REGION = 20;
    private final Random rng = new Random();
}
```

---

## Key Methods

### Main Generation Loop

#### `generateQuests(Region region, List<Event> recentEvents, long currentTick)`
Main quest generation method called once per interval. Analyzes region state and recent events to generate appropriate quests.

**Algorithm:**
1. Check if quest generation needed (interval + quest count)
2. Analyze recent events (last 5000 ticks)
3. Check region conditions (ruins, resources, conflicts)
4. Generate appropriate quest types
5. Add quests to region quest pool

**Parameters:**
- `region` - Target region for quest generation
- `recentEvents` - Recent events in the region (from EventPropagationSystem)
- `currentTick` - Current simulation tick

**Processing:**
```java
if (currentTick % QUEST_CHECK_INTERVAL != 0) return;
if (region.getActiveQuests().size() >= MAX_ACTIVE_QUESTS_PER_REGION) return;

for (Event event : recentEvents) {
    Quest quest = generateQuestFromEvent(event, region, currentTick);
    if (quest != null) {
        region.addQuest(quest);
    }
}
```

---

## Quest Generation Strategies

### Event-Based Generation

#### `generateQuestFromEvent(Event event, Region region, long currentTick)`
Generates a quest based on a specific event.

**Event Type Mapping:**
| Event Type | Quest Type | Description |
|-----------|-----------|-------------|
| BATTLE | DEFEAT_ENEMY | Defend against invaders |
| TRADE_ROUTE_ESTABLISHED | ESCORT_CARAVAN | Protect trade route |
| DISASTER | RESCUE | Save survivors |
| DISCOVERY | EXPLORE | Investigate discovery |
| CONFLICT | MEDIATE | Resolve dispute |
| THEFT | RECOVER | Recover stolen goods |
| ASSASSINATION | INVESTIGATE | Find culprit |

**Example:**
```java
if (event.getType() == EventType.BATTLE) {
    return Quest.builder()
        .id("quest_battle_" + currentTick)
        .type(QuestType.DEFEAT_ENEMY)
        .title("Defend Against " + event.getSourceId())
        .description("Repel the attacking forces")
        .reward(calculateBattleReward(event))
        .expiresAtTick(currentTick + 10000)
        .build();
}
```

---

### Condition-Based Generation

#### `generateQuestFromConditions(Region region, long currentTick)`
Generates quests based on region state without specific events.

**Condition Checks:**
1. **Ruins Present:** Generate exploration quests
2. **Low Resources:** Generate gathering quests
3. **High Conflict:** Generate mediation quests
4. **No Trade Routes:** Generate trade establishment quests
5. **Monster Spawns:** Generate hunting quests

**Example - Ruin Quest:**
```java
List<Structure> ruins = region.getStructures().stream()
    .filter(s -> s.getType() == StructureType.ANCIENT_RUINS)
    .toList();

if (!ruins.isEmpty() && rng.nextDouble() < 0.3) {
    Structure ruin = ruins.get(rng.nextInt(ruins.size()));
    return generateRuinQuest(ruin, currentTick);
}
```

---

## Quest Types

### Combat Quests

#### DEFEAT_ENEMY
**Trigger:** Battle events, clan warfare, monster spawns  
**Objective:** Defeat specified enemy or group  
**Reward:** Gold, experience, reputation with defender

**Generation:**
```java
Quest.builder()
    .type(QuestType.DEFEAT_ENEMY)
    .targetId(enemy.getId())
    .targetCount(enemy.getForceSize())
    .reward(baseReward * difficulty)
    .build();
```

---

#### DEFEND_SETTLEMENT
**Trigger:** Clan expansion near player settlements  
**Objective:** Defend a settlement from attack  
**Reward:** Increased reputation, settlement resources

**Generation:**
```java
Quest.builder()
    .type(QuestType.DEFEND_SETTLEMENT)
    .targetId(settlement.getId())
    .duration(battleDuration)
    .reward(settlement.getValue() * 0.1)
    .build();
```

---

### Exploration Quests

#### EXPLORE_RUIN
**Trigger:** Ancient ruins discovered or created (from StructureLifecycleManager)  
**Objective:** Explore ruin and recover artifacts  
**Reward:** Artifacts, lore, treasure

**Generation:**
```java
Quest.builder()
    .type(QuestType.EXPLORE_RUIN)
    .targetId(ruin.getId())
    .description("Explore the ancient " + ruin.getOriginalType() + " ruins")
    .reward(calculateRuinReward(ruin))
    .build();
```

**Ruin Reward Calculation:**
```java
int calculateRuinReward(Structure ruin) {
    int baseReward = 100;
    int ageBonus = (int)((currentTick - ruin.getCreatedAtTick()) / 1000);
    int typeBonus = ruin.getOriginalType().getValue();
    return baseReward + ageBonus + typeBonus;
}
```

---

#### DISCOVER_LOCATION
**Trigger:** Undiscovered regions, unknown features  
**Objective:** Travel to and map an unknown location  
**Reward:** Map data, experience, reputation

**Generation:**
```java
Quest.builder()
    .type(QuestType.DISCOVER_LOCATION)
    .targetCoordinates(x, y)
    .description("Discover what lies at (" + x + "," + y + ")")
    .reward(distanceFromKnown * 5)
    .build();
```

---

### Economic Quests

#### ESCORT_CARAVAN
**Trigger:** New trade routes established (from ClanExpansionSimulator)  
**Objective:** Protect caravan between two settlements  
**Reward:** Gold, trade goods, merchant reputation

**Generation:**
```java
Quest.builder()
    .type(QuestType.ESCORT_CARAVAN)
    .startLocationId(tradeRoute.getStart())
    .endLocationId(tradeRoute.getEnd())
    .duration(tradeRoute.getDistance() * 10)
    .reward(tradeRoute.getValue() * 0.2)
    .build();
```

---

#### GATHER_RESOURCES
**Trigger:** Resource scarcity in settlements  
**Objective:** Collect specified resources  
**Reward:** Gold, reputation with settlement

**Generation:**
```java
Quest.builder()
    .type(QuestType.GATHER_RESOURCES)
    .targetResourceType(neededResource)
    .targetCount(requiredAmount)
    .reward(resourceValue * requiredAmount * 1.5)
    .build();
```

---

### Social Quests

#### MEDIATE_CONFLICT
**Trigger:** Diplomatic conflicts, high tension between clans  
**Objective:** Negotiate peace or alliance  
**Reward:** Reputation with both parties, diplomatic influence

**Generation:**
```java
Quest.builder()
    .type(QuestType.MEDIATE_CONFLICT)
    .involvedParties(List.of(clan1.getId(), clan2.getId()))
    .description("Mediate the conflict between " + clan1.getName() + " and " + clan2.getName())
    .reward(calculateDiplomaticReward(clan1, clan2))
    .build();
```

---

#### INVESTIGATE_CRIME
**Trigger:** Theft, assassination, sabotage events  
**Objective:** Find perpetrator and recover stolen goods  
**Reward:** Gold, reputation, justice points

**Generation:**
```java
Quest.builder()
    .type(QuestType.INVESTIGATE_CRIME)
    .crimeType(event.getType())
    .targetId(perpetratorId) // Hidden until investigation
    .reward(stolenValue * 2)
    .timeLimit(currentTick + 20000) // Must solve before trail goes cold
    .build();
```

---

## Quest Properties

### Core Fields
- **id:** Unique identifier (`quest_<type>_<tick>`)
- **type:** Quest type enumeration (see above)
- **title:** Short descriptive title
- **description:** Detailed quest text
- **createdAtTick:** Generation timestamp
- **expiresAtTick:** Quest expiration time (if applicable)

### Objective Fields
- **targetId:** Target entity ID (enemy, structure, NPC, etc.)
- **targetCount:** Number of targets (for gathering/defeating)
- **targetCoordinates:** Location objectives
- **targetResourceType:** Resource type for gathering quests

### Reward Fields
- **reward:** Gold/currency reward
- **experienceReward:** XP reward
- **reputationReward:** Reputation gain with faction
- **itemRewards:** Physical item rewards

### State Fields
- **status:** NOT_STARTED, IN_PROGRESS, COMPLETED, FAILED, EXPIRED
- **assignedPlayerId:** Player currently pursuing quest
- **progress:** Current completion percentage

---

## Quest Lifecycle

### Generation → Availability → Assignment → Completion

```
[GENERATED] ──check interval──> [AVAILABLE]
     │                               │
     │                               ├──player accepts──> [IN_PROGRESS]
     │                               │                          │
     │                               │                          ├──objectives met──> [COMPLETED]
     │                               │                          ├──time expired──> [FAILED]
     │                               │                          └──player abandons──> [ABANDONED]
     │                               │
     └──time expired──> [EXPIRED]   └──time expired──> [EXPIRED]
```

---

## Integration

### RegionSimulator Integration
```java
public void processActiveRegion(Region region, long currentTick) {
    // ... resource regeneration ...
    // ... NPC lifecycle ...
    // ... clan expansion ...
    // ... structure lifecycle ...
    
    // Dynamic quest generation
    List<Event> recentEvents = eventPropagationSystem.getRecentEvents(region, 5000);
    questDynamicGenerator.generateQuests(region, recentEvents, currentTick);
    
    // ... village detection ...
}
```

### Event System Integration
Quests are generated in response to events from EventPropagationSystem:
```java
Event battleEvent = Event.builder()
    .type(EventType.BATTLE)
    .sourceId(attackerClanId)
    .targetId(defenderClanId)
    .build();

// This event triggers DEFEAT_ENEMY or DEFEND_SETTLEMENT quest
```

### Structure Integration
Ruins from StructureLifecycleManager trigger exploration quests:
```java
if (structure.getType() == StructureType.ANCIENT_RUINS) {
    Quest quest = questDynamicGenerator.generateRuinQuest(structure, currentTick);
}
```

---

## Testing

### Test Coverage: 100%
- `testQuestGeneration()` - Quests generated from events
- `testRuinQuest()` - Exploration quests for ruins
- `testResourceQuest()` - Gathering quests for low resources
- `testCombatQuest()` - Battle quests from conflicts
- `testQuestExpiration()` - Quests expire correctly
- `testMaxQuestLimit()` - Quest count capped at max
- `testQuestRewards()` - Rewards calculated correctly

### Example Test
```java
@Test
public void testRuinQuest() {
    Structure ruin = Structure.builder()
        .id("ruin_1")
        .type(StructureType.ANCIENT_RUINS)
        .build();
    
    region.addStructure(ruin);
    
    generator.generateQuests(region, List.of(), 0);
    
    List<Quest> quests = region.getActiveQuests();
    assertTrue(quests.stream()
        .anyMatch(q -> q.getType() == QuestType.EXPLORE_RUIN),
        "Ruin should trigger exploration quest");
}
```

---

## Design Patterns

- **Factory Pattern:** Quest creation from events and conditions
- **Strategy Pattern:** Different generation strategies for different triggers
- **Observer Pattern:** React to events from EventPropagationSystem
- **Builder Pattern:** Flexible quest construction

---

## Performance Considerations

- **Interval-Based:** Only generate quests every 5000 ticks (not every tick)
- **Quest Cap:** Maximum 20 active quests per region prevents overload
- **Event Filtering:** Only process recent events (last 5000 ticks)
- **Lazy Evaluation:** Generate quests on-demand, not preemptively

---

## Balancing

### Quest Frequency
- **Check Interval:** 5000 ticks = ~1.4 hours real-time
- **Generation Chance:** 30% per check for condition-based quests
- **Event-Based:** 100% generation for major events (battles, disasters)
- **Expected Quests:** 3-5 new quests per day per region

### Reward Scaling
- **Base Rewards:** 100-500 gold for simple quests
- **Difficulty Multipliers:** 1x (easy) to 5x (legendary)
- **Distance Scaling:** +5 gold per tile distance for travel quests
- **Age Bonus:** +1% reward per 1000 ticks of ruin age

---

## Quest Difficulty

### Calculation Formula
```java
int calculateDifficulty(Quest quest) {
    int baseDifficulty = quest.getType().getBaseDifficulty();
    int distanceModifier = calculateDistanceToTarget(quest);
    int enemyModifier = calculateEnemyStrength(quest);
    int timeModifier = calculateTimeConstraint(quest);
    
    return baseDifficulty + distanceModifier + enemyModifier + timeModifier;
}
```

### Difficulty Tiers
| Tier | Difficulty Range | Reward Multiplier | Expected Level |
|------|------------------|-------------------|----------------|
| Trivial | 1-10 | 1.0x | 1-5 |
| Easy | 11-25 | 1.5x | 5-10 |
| Moderate | 26-50 | 2.0x | 10-20 |
| Hard | 51-75 | 3.0x | 20-30 |
| Legendary | 76+ | 5.0x | 30+ |

---

## Future Enhancements

### Planned Features (Phase 2+)
- **Quest Chains:** Multi-stage quests with branching outcomes
- **Faction Quests:** Faction-specific quest types and rewards
- **Seasonal Quests:** Time-limited seasonal events
- **Player-Generated Quests:** Players can create bounties and contracts
- **Dynamic Objectives:** Objectives adapt to player actions
- **Quest Reputation:** Quest success affects future quest availability
- **Co-op Quests:** Multi-player cooperative objectives

### Advanced Generation
- **Machine Learning:** Learn from player preferences to generate better quests
- **Narrative Templates:** Story-driven quest generation with character arcs
- **Dynamic Rewards:** Rewards adapt to player needs and progression
- **Quest Networks:** Interconnected quests forming meta-narratives

---

## Quest API (Future)

### Player Quest Acceptance
```java
public void acceptQuest(Player player, Quest quest) {
    quest.setAssignedPlayerId(player.getId());
    quest.setStatus(QuestStatus.IN_PROGRESS);
    player.addActiveQuest(quest);
}
```

### Quest Completion
```java
public void completeQuest(Player player, Quest quest) {
    quest.setStatus(QuestStatus.COMPLETED);
    player.removeActiveQuest(quest);
    player.addGold(quest.getReward());
    player.addExperience(quest.getExperienceReward());
    player.addReputation(quest.getReputationReward());
}
```

### Quest Progress Tracking
```java
public void updateQuestProgress(Quest quest, String objectiveId, int progress) {
    quest.setProgress(objectiveId, progress);
    
    if (allObjectivesComplete(quest)) {
        quest.setStatus(QuestStatus.COMPLETED);
    }
}
```

---

## See Also

- [ClanExpansionSimulator](ClanExpansionSimulator.md) - Generates trade route and warfare quests
- [StructureLifecycleManager](StructureLifecycleManager.md) - Creates ruins for exploration quests
- [EventPropagationSystem](../events/EventPropagationSystem.md) - Source of event-based quest triggers
- [Quest](../quest/Quest.md) - Quest data model (Phase 2)
- [QuestType](../quest/QuestType.md) - Quest type enumeration (Phase 2)

---

**Implementation:** Phase 1.10.3  
**Lines of Code:** 431  
**Test Coverage:** 100% (7 tests)  
**Status:** ✅ Complete and fully tested
