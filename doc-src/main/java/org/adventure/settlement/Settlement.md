# Settlement

**Package:** `org.adventure.settlement`  
**Type:** Class (Entity)  
**Since:** Phase 1.10.1

---

## Overview

`Settlement` represents a clustered group of structures forming a village, town, or city. Settlements are detected automatically from structure proximity and grow over time.

---

## Fields

```java
private String id;                    // Unique ID
private String name;                  // Settlement name
private SettlementType type;          // VILLAGE, TOWN, CITY
private String clanId;                // Governing clan
private int centerX;                  // Center coordinates
private int centerY;
private List<String> structureIds;    // All structures in settlement
private int population;               // Sum of NPC residents
private long foundedTick;             // When settlement was founded
```

---

## Settlement Types

### VILLAGE
- 3-14 structures
- Any structure types
- 10-49 NPCs
- Example: Small farming hamlet

### TOWN
- 15-29 structures OR has MARKET
- Mixed residential/commercial
- 50-99 NPCs
- Example: Trading hub

### CITY
- 30+ structures OR (20+ structures + 50+ NPCs + special building)
- Diverse structure types
- 100+ NPCs
- Special: TEMPLE, GUILD_HALL, or multiple MARKET
- Example: Major population center

---

## Related Classes

- `SettlementGenerator` — Creates initial settlements
- `SettlementType` — Village/Town/City enum
- `VillageManager` — Detects and manages settlements (Phase 1.10.2)
