# ProphecyType

**Package:** `org.adventure.prophecy`  
**Type:** Enum  
**Since:** Phase 1.10.1

---

## Overview

`ProphecyType` categorizes prophecies by their narrative role and impact on the world.

---

## Values

### DOOM
- **Theme:** Disaster, destruction, calamity
- **Player Role:** Try to prevent or mitigate damage
- **Typical Duration:** 5-10 years warning
- **Examples:**
  - "The volcano will erupt and destroy the northern clans"
  - "A plague will sweep across the land"
  - "The dark lord will rise and conquer all"

### SALVATION
- **Theme:** Hope, rescue, unification
- **Player Role:** Fulfill prophecy to save world
- **Typical Duration:** 50+ years to fulfill
- **Examples:**
  - "A hero will unite the warring clans"
  - "The chosen one will defeat the darkness"
  - "A sacred artifact will heal the cursed land"

### TRANSFORMATION
- **Theme:** Change, evolution, metamorphosis
- **Player Role:** Adapt to new reality (often unstoppable)
- **Typical Duration:** 10-20 years gradual change
- **Examples:**
  - "The magic zone will expand and transform the forest"
  - "The desert will bloom into a jungle"
  - "Technology will replace magic"

### AWAKENING
- **Theme:** Discovery, emergence, revelation
- **Player Role:** Explore and uncover secrets
- **Typical Duration:** 20-30 years until awakening
- **Examples:**
  - "Ancient ruins will reveal a lost civilization"
  - "The sunken city will rise from the ocean"
  - "Sleeping dragons will awaken from their slumber"

---

## Feature Affinity

| Feature | Primary Type | Secondary Type |
|---------|--------------|----------------|
| VOLCANO | DOOM | TRANSFORMATION |
| MAGIC_ZONE | TRANSFORMATION | AWAKENING |
| ANCIENT_RUINS | AWAKENING | SALVATION |
| SUBMERGED_CITY | AWAKENING | DOOM |
| CAVE_SYSTEM | DOOM | AWAKENING |
| SACRED_GROVE | SALVATION | TRANSFORMATION |

---

## Gameplay Impact

### DOOM
- Creates urgency (time-limited)
- Drives evacuation/preparation
- High stakes (can't fully prevent)
- Generates conflict (who gets saved?)

### SALVATION
- Long-term quest goal
- Unifies player actions
- Positive outcome (if fulfilled)
- Alternative to DOOM (cancel/mitigate)

### TRANSFORMATION
- Environmental changes
- Affects biomes, resources, NPC behavior
- Often inevitable (low player influence)
- Creates new opportunities/challenges

### AWAKENING
- Exploration-focused
- Reveals hidden content
- Medium player influence
- Can be positive or negative outcome

---

## Usage

```java
Prophecy prophecy = getProphecyById("doom_volcano");

switch (prophecy.getType()) {
    case DOOM:
        // Show warning UI, start evacuation timer
        startEvacuationQuest(prophecy);
        break;
    case SALVATION:
        // Show quest chain, long-term goal
        startHeroQuest(prophecy);
        break;
    case TRANSFORMATION:
        // Show environmental change preview
        previewBiomeTransform(prophecy);
        break;
    case AWAKENING:
        // Show exploration marker, mysterious hint
        addExplorationMarker(prophecy);
        break;
}
```

---

## Related Classes

- `Prophecy` — Entity using this type
- `ProphecyStatus` — Current state of prophecy
- `ProphecyGenerator` — Selects type based on world features
