# QuestStatus

**Package:** `org.adventure.quest`  
**Type:** Enum  
**Since:** Phase 1.10.1

---

## Overview

`QuestStatus` tracks the current state of a quest.

---

## Values

### AVAILABLE
- Quest is generated and can be accepted
- Not yet started by player
- Displayed in quest board/NPC dialogues
- **Example:** Quest appears in town bulletin board

### ACTIVE
- Player has accepted and is working on quest
- Objectives are trackable
- Progress is saved
- **Example:** "Explore Ancient Ruins (1/3 objectives)"

### COMPLETED
- All objectives finished
- Rewards ready to be claimed
- Added to player's completed quest log
- **Example:** "Return to quest giver for reward"

### FAILED
- Quest expired or failed condition met
- No rewards granted
- May be repeatable (if configured)
- **Example:** "Quest failed: Time limit exceeded"

---

## State Transitions

```
AVAILABLE
  ↓ player accepts
ACTIVE
  ↓ all objectives complete
COMPLETED
  
ACTIVE
  ↓ time expired OR failure condition
FAILED
```

---

## Usage

```java
Quest quest = getQuestById("quest_123");

switch (quest.getStatus()) {
    case AVAILABLE:
        showQuestOffer(quest);
        break;
    case ACTIVE:
        showQuestProgress(quest);
        updateObjectiveMarkers(quest);
        break;
    case COMPLETED:
        grantRewards(quest);
        showCompletionMessage(quest);
        break;
    case FAILED:
        showFailureMessage(quest);
        break;
}
```

---

## Related Classes

- `Quest` — Entity using this status
- `QuestType` — Quest category
- `QuestObjective` — Tracks individual objective completion
