# VillageType

**Package:** `org.adventure.settlement`  
**Type:** Enum  
**Source File:** `VillageType.java`  
**Since:** Phase 1.10.1

---

## Overview

`VillageType` is an enum that categorizes settlements by size and complexity. The source enum is named `VillageType.java`, though it may be referenced as `SettlementType` in some documentation for clarity.

---

## Values

### VILLAGE
- **Structures:** 3-14
- **Population:** 10-49 NPCs
- **Characteristics:**
  - Mostly residential (HOUSE)
  - 1-2 commercial (SHOP)
  - No special buildings required
- **Examples:** Farming hamlet, mining outpost

### TOWN
- **Structures:** 15-29 OR has MARKET
- **Population:** 50-99 NPCs
- **Characteristics:**
  - Mixed residential/commercial
  - At least 1 MARKET
  - May have TEMPLE or GUILD_HALL
- **Examples:** Trading hub, regional center

### CITY
- **Structures:** 30+ OR (20+ structures + 50+ NPCs + special)
- **Population:** 100+ NPCs
- **Characteristics:**
  - Diverse structure types
  - Multiple MARKET
  - TEMPLE and/or GUILD_HALL
  - May have BARRACKS, WIZARD_TOWER
- **Examples:** Capital city, major port

---

## Promotion Rules

```
VILLAGE → TOWN:
  - Reach 15 structures OR
  - Build MARKET

TOWN → CITY:
  - Reach 30 structures OR
  - (20 structures + 50 NPCs + TEMPLE/GUILD_HALL)
```

These rules are used by the village detection and promotion logic in `VillageManager` (Phase 1.10.2).

---

## Usage

```java
// Using the actual enum name from source
VillageType type = settlement.getType();
if (type == VillageType.CITY) {
    // enable city-level features
}

// Tax rate example
Settlement settlement = getSettlementById("settlement_123");
switch (settlement.getType()) {
    case VILLAGE:
        taxRate = 0.05;  // Low taxes
        break;
    case TOWN:
        taxRate = 0.08;  // Medium taxes
        break;
    case CITY:
        taxRate = 0.10;  // High taxes
        break;
}
```

---

## Notes

**Naming Convention:**  
The source enum is named `VillageType` for historical reasons. Some documentation and APIs may refer to this concept as `SettlementType` interchangeably. Both terms represent the same classification hierarchy:
- Source code uses: `VillageType`
- Conceptual documentation may use: `SettlementType`
- Both refer to the same enum in `org.adventure.settlement.VillageType`

---

## Related Classes

- `Settlement` — Entity class using this type
- `SettlementGenerator` — Creates initial settlements at worldgen
- `VillageManager` — Detects and promotes settlements (Phase 1.10.2)
