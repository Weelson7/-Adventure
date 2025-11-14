# QuestType

**Package:** `org.adventure.quest`  
**Type:** Enum  
**Since:** Phase 1.10.1

---

## Overview

`QuestType` categorizes quests by their primary gameplay mechanic.

---

## Values

### EXPLORE
- **Objective:** Visit locations, discover areas
- **Typical Objectives:**
  - Reach specific coordinates
  - Enter structures/features
  - Uncover map regions
- **Examples:**
  - "Explore the ancient ruins"
  - "Map the cave system"
  - "Discover the hidden valley"

### RETRIEVE
- **Objective:** Obtain and deliver items
- **Typical Objectives:**
  - Acquire specific item
  - Deliver item to NPC/location
  - Collect multiple items
- **Examples:**
  - "Retrieve the sacred relic"
  - "Collect 10 magic crystals"
  - "Deliver supplies to the outpost"

### INVESTIGATE
- **Objective:** Gather information, solve mysteries
- **Typical Objectives:**
  - Talk to NPCs
  - Examine objects
  - Piece together clues
- **Examples:**
  - "Investigate the magical anomaly"
  - "Question the witnesses"
  - "Decipher the ancient inscriptions"

### DEFEAT
- **Objective:** Combat enemies
- **Typical Objectives:**
  - Kill X enemies
  - Defeat boss enemy
  - Clear area of hostiles
- **Examples:**
  - "Defeat 10 bandits"
  - "Slay the dragon"
  - "Clear the dungeon"

---

## Gameplay Characteristics

| Type | Combat | Exploration | Social | Duration |
|------|--------|-------------|--------|----------|
| EXPLORE | Low | High | Low | Medium |
| RETRIEVE | Medium | Medium | Low | Long |
| INVESTIGATE | Low | Medium | High | Short |
| DEFEAT | High | Low | Low | Short |

---

## Usage

```java
Quest quest = getQuestById("quest_123");

switch (quest.getType()) {
    case EXPLORE:
        markLocationOnMap(quest.getLinkedFeatureId());
        break;
    case RETRIEVE:
        highlightRequiredItem(quest.getObjectives().get(0));
        break;
    case INVESTIGATE:
        listRelevantNPCs(quest.getLinkedStoryId());
        break;
    case DEFEAT:
        spawnEnemies(quest.getLinkedFeatureId());
        break;
}
```

---

## Related Classes

- `Quest` — Entity using this type
- `QuestStatus` — Quest state
- `QuestObjective` — Objectives vary by type
- `QuestGenerator` — Selects type based on feature
