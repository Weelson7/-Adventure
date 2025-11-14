# QuestObjective

**Package:** `org.adventure.quest`  
**Type:** Class  
**Since:** Phase 1.10.1

---

## Overview

`QuestObjective` represents a single step in a multi-step quest. Objectives are sequential (must complete in order).

---

## Fields

```java
private String id;                    // Unique objective ID
private String description;           // Display text
private String condition;             // Completion condition (format: "TYPE:params")
private boolean completed;            // Completion status
private Map<String, Object> metadata; // Additional data
```

---

## Condition Format

### Syntax
```
"ACTION_TYPE:parameters"
```

### Common Conditions

**ENTER_LOCATION:**
```java
"ENTER_LOCATION:ruins_123"           // Enter specific feature/structure
"ENTER_LOCATION:region_50_60"        // Enter specific region
```

**ACQUIRE_ITEM:**
```java
"ACQUIRE_ITEM:ancient_artifact"      // Get specific item
"ACQUIRE_ITEM:gold:500"              // Collect 500 gold
"ACQUIRE_ITEM:any_weapon"            // Get any weapon
```

**DELIVER_ITEM:**
```java
"DELIVER_ITEM:sacred_relic:temple_001"  // Deliver item to location
"DELIVER_ITEM:supplies:npc_123"         // Deliver to NPC
```

**KILL_COUNT:**
```java
"KILL_COUNT:bandit:10"               // Kill 10 bandits
"KILL_COUNT:fire_elemental:5"        // Kill 5 fire elementals
"KILL_COUNT:any_enemy:20"            // Kill any 20 enemies
```

**TALK_TO:**
```java
"TALK_TO:npc_elder_001"              // Talk to specific NPC
"TALK_TO:any_merchant"               // Talk to any merchant
```

**INTERACT:**
```java
"INTERACT:inscription_123"           // Examine object
"INTERACT:lever_456"                 // Pull lever
```

**REACH_COORDINATES:**
```java
"REACH_COORDINATES:120,80"           // Travel to (120, 80)
```

---

## Usage

```java
QuestObjective objective = new QuestObjective(
    "obj_explore_1",
    "Enter the ancient ruins",
    "ENTER_LOCATION:ruins_123"
);

// Check completion
if (player.getCurrentLocation().equals("ruins_123")) {
    objective.setCompleted(true);
    quest.completeObjective(0); // Advance to next objective
}
```

---

## Related Classes

- `Quest` — Contains list of objectives
- `QuestType` — Determines typical objectives
- `QuestStatus` — Overall quest status
