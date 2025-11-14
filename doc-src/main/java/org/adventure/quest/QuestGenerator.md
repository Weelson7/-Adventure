# QuestGenerator

**Package:** `org.adventure.quest`  
**Type:** Class (Factory)  
**Since:** Phase 1.10.1

---

## Overview

`QuestGenerator` creates deterministic quests at worldgen based on world features and stories. Generates 5-20 quests per world with varied types and objectives.

---

## Key Method

### `generateFeatureQuests()`

**Signature:**
```java
public static List<Quest> generateFeatureQuests(
    long worldSeed,
    List<RegionalFeature> features,
    List<Story> stories
)
```

**Algorithm:**
```
1. Initialize seeded RNG
2. Filter quest-eligible features (MAGIC_ZONE, ANCIENT_RUINS, etc.)
3. For each feature:
   - Determine quest type from feature type
   - Generate 1-3 quests per feature
   - Create objectives based on quest type
   - Add rewards scaled to difficulty
   - Link to related stories
4. Return quest list
```

---

## Feature → Quest Type Mapping

| Feature Type | Quest Type | Example |
|--------------|------------|---------|
| ANCIENT_RUINS | EXPLORE | "Explore the lost temple" |
| SUBMERGED_CITY | RETRIEVE | "Retrieve artifact from underwater city" |
| VOLCANO | DEFEAT | "Defeat fire elementals" |
| MAGIC_ZONE | INVESTIGATE | "Investigate magical anomaly" |
| CAVE_SYSTEM | EXPLORE | "Map the cave network" |
| SACRED_GROVE | RETRIEVE | "Collect sacred herbs" |

---

## Quest Generation Rules

### Quest Count
```java
int questsPerFeature = 1 + rng.nextInt(3); // 1-3 quests
int totalQuests = Math.min(20, features.size() * questsPerFeature);
```

### Difficulty Scaling
```java
int minLevel = 1 + (featureIndex * 2); // Increases with feature discovery order
```

### Reward Scaling
```java
int goldReward = 50 + (minLevel * 25); // 50-500 gold
int itemCount = 1 + (minLevel / 5);    // 1-4 items
```

---

## Related Classes

- `Quest` — Entity class
- `QuestType` — Quest category
- `QuestObjective` — Quest step
- `QuestReward` — Quest reward
- `RegionalFeature` — World features
- `Story` — Linked narratives
