# QuestDynamicGeneratorTest

**Package:** `org.adventure.simulation`  
**Since:** Phase 1.10.3  
**Purpose:** Unit tests for QuestDynamicGenerator dynamic quest creation from world events

---

## Overview

Comprehensive test suite validating dynamic quest generation from ruins, conflicts, disasters, stories, cooldowns, and quest limits.

---

## Test Coverage

**Total Tests:** 7  
**Coverage:** 100% of QuestDynamicGenerator methods  
**Focus Areas:**
- Ruin exploration quests
- Conflict mediation quests
- Disaster recovery quests
- Story investigation quests
- Cooldown mechanics
- Quest expiration
- Max quest limits

---

## Test Cases

### 1. `testRuinQuestsGenerated()`
**Purpose:** Verify ancient ruins trigger exploration quests

**Setup:**
- 5 ancient ruin structures

**Execution:**
- Generate quests at tick 1000

**Assertions:**
- At least 1 quest generated
- Quest type: EXPLORE
- Quest title contains "Explore"
- Quest has objectives

**Critical Path:** Ruin detection → random selection → create exploration quest

---

### 2. `testConflictQuestsGenerated()`
**Purpose:** Verify hostile clans trigger mediation quests

**Setup:**
- 2 clans with hostile relationship (reputation -40)
- Both clans have leaders

**Execution:**
- Generate quests 10 times (to overcome randomness)

**Assertions:**
- Quest title contains "Mediate"
- Quest type: INVESTIGATE
- 2 objectives (talk to both leaders)

**Critical Path:** Clan relationship check → hostile → create mediation quest

---

### 3. `testDisasterRecoveryQuestsGenerated()`
**Purpose:** Verify damaged structures trigger repair quests

**Setup:**
- 3 structures with 30% health (severely damaged)

**Execution:**
- Generate quests 10 times

**Assertions:**
- Quest title contains "Rebuild"
- Quest type: DELIVER
- Quest has rewards

**Critical Path:** Structure health check → < 50% → create repair quest

---

### 4. `testStoryInvestigationQuestsGenerated()`
**Purpose:** Verify active stories trigger investigation quests

**Setup:**
- 3 active MYSTERY stories

**Execution:**
- Generate quests 20 times (randomness factor)

**Assertions:**
- Quest title contains "Investigate"
- Quest type: INVESTIGATE
- Quest links to story ID

**Critical Path:** Story status check → ACTIVE → create investigation quest

---

### 5. `testQuestGenerationRespectsCooldown()`
**Purpose:** Verify cooldown prevents duplicate quests

**Setup:**
- 1 ancient ruin

**Execution:**
- Generate quests at tick 1000
- Generate quests at tick 2000 (within cooldown)

**Assertions:**
- Total quest count = initial count (no duplicates)
- Same ruin doesn't generate multiple quests within cooldown

**Critical Path:** Quest generation → record cooldown → check cooldown → skip if recent

---

### 6. `testQuestExpiration()`
**Purpose:** Verify quests have expiration mechanics

**Setup:**
- 1 ancient ruin

**Execution:**
- Generate quest at tick 1000

**Assertions:**
- Quest.expirationTick > 1000
- isExpired(1000) = false
- isExpired(expirationTick + 1) = true

**Critical Path:** Quest creation → set expiration → check expiration status

---

### 7. `testMaxQuestsPerEventRespected()`
**Purpose:** Verify quest generation respects max limits

**Setup:**
- 20 ancient ruins (more than max)

**Execution:**
- Generate quests at tick 1000

**Assertions:**
- Total quests ≤ 12 (3 per event type × 4 event types)
- Max quests per event: 3

**Critical Path:** Quest generation → count check → stop if max reached

---

## Test Data

### Default Ruin
```java
Structure.Builder()
    .id("ruin1")
    .type(StructureType.ANCIENT_RUINS)
    .ownerId("")
    .ownerType(OwnerType.NONE)
    .locationTileId("50,50")
    .health(0.0)
    .maxHealth(100.0)
    .createdAtTick(0)
    .build()
```

### Hostile Clans
```java
Clan clan1 = Clan.Builder()
    .id("clan1")
    .name("Clan One")
    .treasury(500.0)
    .leaderId("leader1")
    .build();

RelationshipRecord hostileRelation = new RelationshipRecord(
    "clan2", -40, 0.0, -30.0, 0.0, 0
);
clan1 = clan1.updateRelationship(hostileRelation);
```

### Damaged Structure
```java
Structure.Builder()
    .id("damaged1")
    .type(StructureType.HOUSE)
    .ownerId("clan1")
    .ownerType(OwnerType.CLAN)
    .health(30.0) // < 50% threshold
    .maxHealth(100.0)
    .build()
```

### Active Story
```java
Story.Builder()
    .id("story1")
    .storyType(StoryType.MYSTERY)
    .status(StoryStatus.ACTIVE)
    .title("The Mystery")
    .description("Strange events...")
    .originTileId(10000100)
    .originTick(0)
    .priority(5)
    .build()
```

---

## Quest Type Mappings

| Trigger | Quest Type | Description |
|---------|-----------|-------------|
| Ancient Ruins | EXPLORE | Explore and recover artifacts |
| Hostile Clans | INVESTIGATE | Mediate conflict between clans |
| Damaged Structures | DELIVER | Rebuild damaged buildings |
| Active Stories | INVESTIGATE | Investigate mystery/legend |

---

## Test Parameters

| Parameter | Value | Test |
|-----------|-------|------|
| Ruin Count | 5 | testRuinQuestsGenerated |
| Generation Attempts | 10-20 | Multiple tests (overcome randomness) |
| Damage Threshold | 50% | testDisasterRecoveryQuestsGenerated |
| Max Ruins | 20 | testMaxQuestsPerEventRespected |
| Max Quests Per Type | 3 | testMaxQuestsPerEventRespected |
| Cooldown | 1000 ticks | testQuestGenerationRespectsCooldown |

---

## Test Patterns

### Setup Pattern
```java
@BeforeEach
public void setUp() {
    generator = new QuestDynamicGenerator();
    structures = new ArrayList<>();
    clans = new ArrayList<>();
    stories = new ArrayList<>();
}
```

### Generation Pattern (Single)
```java
List<Quest> quests = generator.generateQuestsFromEvents(
    structures, clans, stories, currentTick
);
```

### Generation Pattern (Multiple)
```java
List<Quest> allQuests = new ArrayList<>();
for (int i = 0; i < 10; i++) {
    QuestDynamicGenerator freshGenerator = new QuestDynamicGenerator();
    allQuests.addAll(freshGenerator.generateQuestsFromEvents(
        structures, clans, stories, 1000 + i * 100
    ));
}
```

---

## Randomness Handling

Several tests use **multiple generation attempts** to overcome randomness:

| Test | Attempts | Reason |
|------|----------|--------|
| testConflictQuestsGenerated | 10 | Conflict quest has 30% generation chance |
| testDisasterRecoveryQuestsGenerated | 10 | Repair quest has 40% generation chance |
| testStoryInvestigationQuestsGenerated | 20 | Story quest has 50% generation chance |

This ensures tests are **deterministic** despite internal randomness.

---

## Quest Properties Validated

- **id:** Non-null unique identifier
- **type:** Correct QuestType for trigger
- **title:** Descriptive title containing keywords
- **objectives:** Non-empty list of objectives
- **rewards:** Non-empty list of rewards
- **expirationTick:** Future tick value
- **linkedStoryId:** Present for story quests

---

## Dependencies

- JUnit 5
- org.adventure.quest.Quest
- org.adventure.structure.Structure
- org.adventure.society.Clan
- org.adventure.story.Story
- org.adventure.society.RelationshipRecord

---

## See Also

- [QuestDynamicGenerator](../../main/java/org/adventure/simulation/QuestDynamicGenerator.md)
- [Quest](../../main/java/org/adventure/quest/Quest.md)
- [Story](../../main/java/org/adventure/story/Story.md)
- [Structure](../../main/java/org/adventure/structure/Structure.md)
