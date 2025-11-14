# Quest

**Package:** `org.adventure.quest`  
**Type:** Class (Entity)  
**Since:** Phase 1.10.1

---

## Overview

`Quest` represents a mission or objective for players in !Adventure. Quests are:
- **Generated from world features** (MAGIC_ZONE, ANCIENT_RUINS, etc.)
- **Multi-step objectives** (explore, retrieve, defeat, investigate)
- **Reward-based** (items, gold, reputation)
- **Story-integrated** (linked to stories and prophecies)

---

## Fields

### Core Identity
```java
private String id;                    // Unique ID
private String title;                 // Display name
private String description;           // Quest briefing
private QuestType type;               // EXPLORE, RETRIEVE, INVESTIGATE, DEFEAT
private QuestStatus status;           // AVAILABLE, ACTIVE, COMPLETED, FAILED
```

### Objectives & Progress
```java
private List<QuestObjective> objectives;  // Sequential objectives
private int currentObjectiveIndex;        // Progress tracker (0-based)
private Map<String, Integer> progressData; // Key-value progress tracking
```

### Rewards
```java
private List<QuestReward> rewards;    // Gold, items, reputation
private boolean rewardsGranted;       // Prevent duplicate rewards
```

### World Integration
```java
private String linkedFeatureId;       // Associated RegionalFeature
private String linkedStoryId;         // Associated Story
private String linkedProphecyId;      // Associated Prophecy (optional)
private String giverNPCId;            // NPC who gave quest (optional)
```

### Time Constraints
```java
private long expiresAtTick;           // Quest deadline (-1 = no limit)
private long acceptedAtTick;          // When player started quest
private long completedAtTick;         // When player finished
```

### Requirements
```java
private int minPlayerLevel;           // Level requirement (0 = none)
private List<String> prerequisiteQuestIds; // Must complete these first
private boolean isRepeatable;         // Can be done multiple times
private long repeatCooldown;          // Ticks between repeats
```

### Metadata
```java
private String schemaVersion = "1.0";
private long createdAtTick;
```

---

## Constructor & Builder

```java
public static class Builder {
    public Builder id(String id);
    public Builder title(String title);
    public Builder description(String description);
    public Builder type(QuestType type);
    public Builder status(QuestStatus status);
    public Builder addObjective(QuestObjective obj);
    public Builder addReward(QuestReward reward);
    public Builder linkedFeature(String featureId);
    public Builder linkedStory(String storyId);
    public Builder linkedProphecy(String prophecyId);
    public Builder giverNPC(String npcId);
    public Builder expiresAt(long tick);
    public Builder minLevel(int level);
    public Builder addPrerequisite(String questId);
    public Builder repeatable(boolean repeatable, long cooldown);
    public Builder createdAt(long tick);
    
    public Quest build();
}
```

---

## Key Methods

### `isAvailable()`
```java
public boolean isAvailable(long currentTick, int playerLevel, List<String> completedQuestIds) {
    if (status != QuestStatus.AVAILABLE) return false;
    if (playerLevel < minPlayerLevel) return false;
    if (!prerequisiteQuestIds.stream().allMatch(completedQuestIds::contains)) {
        return false;
    }
    if (expiresAtTick >= 0 && currentTick >= expiresAtTick) {
        return false;
    }
    return true;
}
```

### `accept()`
```java
public void accept(long currentTick) {
    if (status == QuestStatus.AVAILABLE) {
        status = QuestStatus.ACTIVE;
        acceptedAtTick = currentTick;
        currentObjectiveIndex = 0;
    }
}
```

### `getCurrentObjective()`
```java
public QuestObjective getCurrentObjective() {
    if (currentObjectiveIndex < objectives.size()) {
        return objectives.get(currentObjectiveIndex);
    }
    return null;
}
```

### `completeObjective()`
```java
public boolean completeObjective(int objectiveIndex) {
    if (objectiveIndex == currentObjectiveIndex) {
        objectives.get(objectiveIndex).setCompleted(true);
        currentObjectiveIndex++;
        
        // Check if all objectives complete
        if (currentObjectiveIndex >= objectives.size()) {
            status = QuestStatus.COMPLETED;
            return true;
        }
    }
    return false;
}
```

### `updateProgress()`
```java
public void updateProgress(String key, int value) {
    progressData.put(key, value);
}

public int getProgress(String key) {
    return progressData.getOrDefault(key, 0);
}
```

### `grantRewards()`
```java
public List<QuestReward> grantRewards() {
    if (!rewardsGranted && status == QuestStatus.COMPLETED) {
        rewardsGranted = true;
        return new ArrayList<>(rewards);
    }
    return Collections.emptyList();
}
```

### `fail()`
```java
public void fail(long currentTick) {
    status = QuestStatus.FAILED;
    completedAtTick = currentTick;
}
```

---

## Quest Lifecycle

```
AVAILABLE (generated, waiting for player)
  ↓ player accepts
ACTIVE (in progress)
  ↓ objectives completed
COMPLETED (success, rewards granted)
  
ACTIVE
  ↓ time expired OR failed condition
FAILED (no rewards)
```

---

## Example Quests

### Explore Quest
```java
Quest exploreQuest = new Quest.Builder()
    .id("quest_explore_ruins_123")
    .title("Explore the Ancient Ruins")
    .description("Investigate the mysterious ruins in the eastern mountains.")
    .type(QuestType.EXPLORE)
    .status(QuestStatus.AVAILABLE)
    .addObjective(new QuestObjective(
        "obj_1", "Reach the ruins entrance", "ENTER_LOCATION:ruins_123"))
    .addObjective(new QuestObjective(
        "obj_2", "Explore the main chamber", "ENTER_LOCATION:ruins_123_chamber"))
    .addObjective(new QuestObjective(
        "obj_3", "Document your findings", "INTERACT:ruins_123_inscription"))
    .addReward(new QuestReward(RewardType.GOLD, 100))
    .addReward(new QuestReward(RewardType.ITEM, "ancient_artifact"))
    .linkedFeature("feature_ancient_ruins_123")
    .expiresAt(-1) // No time limit
    .minLevel(5)
    .createdAt(0)
    .build();
```

### Retrieve Quest
```java
Quest retrieveQuest = new Quest.Builder()
    .id("quest_retrieve_artifact_456")
    .title("Retrieve the Lost Artifact")
    .description("The temple elders need the sacred relic from the sunken city.")
    .type(QuestType.RETRIEVE)
    .status(QuestStatus.AVAILABLE)
    .addObjective(new QuestObjective(
        "obj_1", "Travel to the sunken city", "ENTER_LOCATION:sunken_city_789"))
    .addObjective(new QuestObjective(
        "obj_2", "Find the sacred relic", "ACQUIRE_ITEM:sacred_relic"))
    .addObjective(new QuestObjective(
        "obj_3", "Return to the temple", "DELIVER_ITEM:sacred_relic:temple_001"))
    .addReward(new QuestReward(RewardType.GOLD, 500))
    .addReward(new QuestReward(RewardType.REPUTATION, "temple_clan", 50))
    .linkedFeature("feature_submerged_city_789")
    .linkedStory("story_sacred_relic")
    .giverNPC("npc_temple_elder_001")
    .expiresAt(100000) // 10 year deadline
    .minLevel(10)
    .createdAt(0)
    .build();
```

### Defeat Quest
```java
Quest defeatQuest = new Quest.Builder()
    .id("quest_defeat_elementals_789")
    .title("Defeat the Fire Elementals")
    .description("Dangerous fire elementals have appeared near the volcano. Eliminate them.")
    .type(QuestType.DEFEAT)
    .status(QuestStatus.AVAILABLE)
    .addObjective(new QuestObjective(
        "obj_1", "Defeat 5 fire elementals", "KILL_COUNT:fire_elemental:5"))
    .addObjective(new QuestObjective(
        "obj_2", "Report to the guard captain", "TALK_TO:npc_guard_captain_002"))
    .addReward(new QuestReward(RewardType.GOLD, 300))
    .addReward(new QuestReward(RewardType.ITEM, "fire_resistance_potion", 3))
    .linkedFeature("feature_volcano_456")
    .giverNPC("npc_guard_captain_002")
    .expiresAt(-1)
    .minLevel(8)
    .repeatable(true, 50000) // Repeatable every 5 years
    .createdAt(0)
    .build();
```

---

## Integration

### With QuestGenerator
```java
List<Quest> quests = QuestGenerator.generateFeatureQuests(
    worldSeed, features, stories);

// Result: Quests linked to world features and stories
```

### With Player
```java
// Player accepts quest
Quest quest = getQuestById("quest_explore_ruins_123");
if (quest.isAvailable(currentTick, player.getLevel(), player.getCompletedQuestIds())) {
    quest.accept(currentTick);
    player.addActiveQuest(quest.getId());
}

// Player makes progress
QuestObjective current = quest.getCurrentObjective();
if (player.meetsCondition(current.getCondition())) {
    quest.completeObjective(quest.getCurrentObjectiveIndex());
}

// Quest completed
if (quest.getStatus() == QuestStatus.COMPLETED) {
    List<QuestReward> rewards = quest.grantRewards();
    player.receiveRewards(rewards);
}
```

---

## Testing

```java
@Test
public void testQuestLifecycle() {
    Quest quest = new Quest.Builder()
        .id("test_quest")
        .status(QuestStatus.AVAILABLE)
        .addObjective(new QuestObjective("obj_1", "Test", "TEST"))
        .addReward(new QuestReward(RewardType.GOLD, 100))
        .build();
    
    // Accept
    quest.accept(0L);
    assertEquals(QuestStatus.ACTIVE, quest.getStatus());
    
    // Complete objective
    quest.completeObjective(0);
    assertEquals(QuestStatus.COMPLETED, quest.getStatus());
    
    // Grant rewards
    List<QuestReward> rewards = quest.grantRewards();
    assertEquals(1, rewards.size());
    assertEquals(100, rewards.get(0).getAmount());
}
```

---

## Related Classes

- `QuestType` — Quest category
- `QuestStatus` — Quest state
- `QuestObjective` — Individual quest step
- `QuestReward` — Quest reward
- `QuestGenerator` — Factory for creating quests
