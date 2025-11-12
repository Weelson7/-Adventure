# StructureType

**Package:** `org.adventure.structure`  
**Type:** Enum  
**Values:** 24 structure types

---

## Overview

`StructureType` defines all available structure categories in the game. Each type belongs to a primary category (Residential, Military, Commercial, Magical, Ruins, Special) and has a description of its primary function.

---

## Structure Types

### Residential (4 types)
Buildings for living quarters:
- **HOUSE**: Basic dwelling for individuals or small families
- **MANOR**: Large estate with multiple rooms
- **APARTMENT**: Multi-unit residential building
- **CASTLE**: Fortified noble residence with defensive capabilities

### Military (4 types)
Defensive and combat structures:
- **BARRACKS**: Housing for soldiers and guards
- **FORTRESS**: Heavily fortified military stronghold
- **WATCHTOWER**: Defensive tower for surveillance
- **ARMORY**: Storage and maintenance facility for weapons

### Commercial (4 types)
Trade and economic structures:
- **SHOP**: Small retail establishment
- **MARKET**: Large trading area with multiple vendors
- **WAREHOUSE**: Storage facility for goods
- **INN**: Lodging and dining establishment

### Magical (3 types)
Structures for magical practice:
- **WIZARD_TOWER**: Tall spire for magical study and practice
- **ENCHANTED_LIBRARY**: Repository of magical knowledge
- **RITUAL_CHAMBER**: Dedicated space for magical rituals

### Ruins (4 types)
Abandoned or ancient structures:
- **ANCIENT_RUINS**: Remnants of ancient civilization
- **CRYPT**: Underground burial chamber
- **LABYRINTH**: Complex maze-like dungeon
- **SUBMERGED_CITY**: Underwater ruins of lost civilization

### Special (5 types)
Unique or multi-purpose structures:
- **TEMPLE**: Religious or spiritual structure
- **GUILD_HALL**: Headquarters for guilds or organizations
- **TRAINING_CENTER**: Facility for skill development
- **LEGENDARY_STRUCTURE**: Unique structure with story significance

---

## Methods

### Category Queries
Each type provides boolean methods to check its category:

```java
boolean isResidential()  // True for HOUSE, MANOR, APARTMENT, CASTLE
boolean isMilitary()     // True for BARRACKS, FORTRESS, WATCHTOWER, ARMORY
boolean isCommercial()   // True for SHOP, MARKET, WAREHOUSE, INN
boolean isMagical()      // True for WIZARD_TOWER, ENCHANTED_LIBRARY, RITUAL_CHAMBER
boolean isRuins()        // True for ANCIENT_RUINS, CRYPT, LABYRINTH, SUBMERGED_CITY
boolean isSpecial()      // True for TEMPLE, GUILD_HALL, TRAINING_CENTER, LEGENDARY_STRUCTURE
```

### Accessors
```java
String getCategory()     // Returns category name (e.g., "Residential")
String getDescription()  // Returns structure description
```

---

## Usage Examples

### Check Structure Category
```java
StructureType type = StructureType.HOUSE;
if (type.isResidential()) {
    // Handle residential structure logic
}
```

### Get Category Information
```java
String category = StructureType.WIZARD_TOWER.getCategory();     // "Magical"
String desc = StructureType.WIZARD_TOWER.getDescription();      // "Tall spire for magical study..."
```

---

## Design Decisions

1. **Why categories?**: Enables rule application by category (e.g., "all residential structures get X bonus")

2. **Why fixed enum?**: Prevents invalid types; data-driven expansion can be added via ItemPrototype-style registry

3. **Why description in enum?**: Provides self-documenting code and UI tooltip text

---

## Upgrade Paths (Future)

Structures can be upgraded within or across categories:
- HOUSE → MANOR (expand residential)
- WATCHTOWER → FORTRESS (upgrade military)
- SHOP → MARKET (expand commercial)

---

## Extension (Post-MVP)

For modding support, consider:
1. Registry-based structure types (like ItemPrototype)
2. JSON-defined structure properties
3. Custom upgrade paths per world preset

---

## Testing

**Test Coverage**: Included in `StructureTest.java`  
**Test Method**: `testStructureTypeCategories()`

### Validation
- Each type has valid category
- Each type has non-null description
- Category query methods return correct values

---

## References

- Design: `docs/structures_ownership.md` → Structure Types & Hierarchies
- Summary: `archive/PHASE_1.5_SUMMARY.md`
