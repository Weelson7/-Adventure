# QuestReward

**Package:** `org.adventure.quest`  
**Type:** Class  
**Since:** Phase 1.10.1

---

## Overview

`QuestReward` represents a reward granted upon quest completion. Quests can have multiple rewards.

---

## Fields

```java
private RewardType type;              // GOLD, ITEM, REPUTATION, EXPERIENCE
private String target;                // Item ID, clan ID, etc. (type-dependent)
private int amount;                   // Quantity or value
private Map<String, Object> metadata; // Additional data
```

---

## Reward Types

### GOLD
```java
new QuestReward(RewardType.GOLD, 500)
// Grants 500 gold to player
```

### ITEM
```java
new QuestReward(RewardType.ITEM, "ancient_sword")
// Grants 1x ancient_sword

new QuestReward(RewardType.ITEM, "health_potion", 5)
// Grants 5x health_potion
```

### REPUTATION
```java
new QuestReward(RewardType.REPUTATION, "temple_clan", 50)
// Grants +50 reputation with temple_clan
```

### EXPERIENCE
```java
new QuestReward(RewardType.EXPERIENCE, 1000)
// Grants 1000 XP to player
```

---

## Constructor

```java
public QuestReward(RewardType type, int amount) {
    this.type = type;
    this.amount = amount;
}

public QuestReward(RewardType type, String target) {
    this.type = type;
    this.target = target;
    this.amount = 1;
}

public QuestReward(RewardType type, String target, int amount) {
    this.type = type;
    this.target = target;
    this.amount = amount;
}
```

---

## Usage

```java
// Define rewards
Quest quest = new Quest.Builder()
    .addReward(new QuestReward(RewardType.GOLD, 300))
    .addReward(new QuestReward(RewardType.ITEM, "fire_sword"))
    .addReward(new QuestReward(RewardType.ITEM, "health_potion", 3))
    .addReward(new QuestReward(RewardType.REPUTATION, "guard_clan", 25))
    .build();

// Grant rewards on completion
if (quest.getStatus() == QuestStatus.COMPLETED) {
    List<QuestReward> rewards = quest.grantRewards();
    
    for (QuestReward reward : rewards) {
        switch (reward.getType()) {
            case GOLD:
                player.addGold(reward.getAmount());
                break;
            case ITEM:
                player.addItem(reward.getTarget(), reward.getAmount());
                break;
            case REPUTATION:
                player.modifyReputation(reward.getTarget(), reward.getAmount());
                break;
            case EXPERIENCE:
                player.addXP(reward.getAmount());
                break;
        }
    }
}
```

---

## Related Classes

- `Quest` — Contains list of rewards
- `QuestStatus` — Rewards granted only when COMPLETED
- `RewardType` — Enum for reward categories
