# Village

**Package:** `org.adventure.settlement`  
**Type:** Immutable Data Class (with mutable state for name/type/population)  
**Schema Version:** 1

---

## Overview

`Village` represents a settlement formed from structure clusters. Villages are automatically detected when 3+ structures are within 10-tile radius. Settlements can be promoted to towns (15+ structures) or cities (30+ structures + population/special buildings).

---

## Design Principles

1. **Automatic Detection**: Villages form naturally from structure clustering
2. **Dynamic Classification**: Settlements are promoted based on size and composition
3. **Clan Governance**: Most common structure owner becomes governing clan
4. **Builder Pattern**: Flexible construction with validation
5. **Structure Membership**: Tracks all structures belonging to the village

---

## Class Structure

```java
public final class Village {
    // Immutable identity
    private final String id;
    private final int centerX;
    private final int centerY;
    private final List<String> structureIds;
    private final long foundedTick;
    private final int schemaVersion;
    
    // Mutable state
    private String name;
    private VillageType type;
    private int population;
    private String governingClanId;
}
```

---

## Key Methods

### Creation
- **Builder Pattern**: `new Village.Builder()...build()`
  - Fluent API for flexible construction
  - `addStructure(id)` for single structure addition
  - Validation at build time

### Structure Management
- **`addStructure(structureId)`**: Add structure to village
- **`removeStructure(structureId)`**: Remove structure from village
- **`getStructureIds()`**: Get all structure IDs (unmodifiable copy)

### Mutable Properties
- **`setName(name)`**: Change village name
- **`setType(type)`**: Update village type (VILLAGE/TOWN/CITY)
- **`setPopulation(population)`**: Update population count
- **`setGoverningClanId(clanId)`**: Change governing clan

### Accessors
- **`getId()`**: Unique village identifier
- **`getName()`**: Village name
- **`getType()`**: Village classification (VILLAGE/TOWN/CITY)
- **`getCenterX()`**, **`getCenterY()`**: Village center coordinates
- **`getPopulation()`**: Current population
- **`getGoverningClanId()`**: ID of governing clan (nullable)
- **`getFoundedTick()`**: Tick when village was founded

---

## Validation Rules

### At Construction
- `id` cannot be null or empty
- `name` cannot be null or empty
- `type` cannot be null
- `structureIds` must contain at least one structure
- `population` cannot be negative
- `foundedTick` cannot be negative

### At Runtime
- Name cannot be set to null or empty
- Type cannot be set to null
- Population cannot be set to negative
- Structure IDs must be non-null and non-empty when added

---

## Village Classification

### VILLAGE (3-14 structures)
- Minimum: 3 structures within 10-tile radius
- Maximum: 14 structures
- No special requirements

### TOWN (15-29 structures OR has MARKET)
- Minimum: 15 structures OR any count with MARKET
- Maximum: 29 structures (without city criteria)
- Market presence auto-promotes to TOWN

### CITY (30+ structures OR special)
- Option 1: 30+ structures
- Option 2: 20+ structures + 50+ NPCs + TEMPLE/GUILD_HALL
- Highest settlement tier

---

## Equality and Hashing

- **Equality**: Based on `id` only
- **Hash Code**: Based on `id` only
- Villages with the same ID are considered equal

---

## Persistence

### JSON Schema (v1)
```json
{
  "id": "village_1",
  "name": "Meadowdale",
  "type": "VILLAGE",
  "centerX": 100,
  "centerY": 200,
  "structureIds": ["struct_1", "struct_2", "struct_3"],
  "population": 25,
  "governingClanId": "clan_1",
  "foundedTick": 1000,
  "schemaVersion": 1
}
```

### Migration Notes
- Schema version 1 (current)
- Use `@JsonCreator` + `@JsonProperty` for Jackson compatibility

---

## Usage Examples

### Create a Basic Village
```java
Village village = new Village.Builder()
    .id("village_1")
    .name("Meadowdale")
    .type(VillageType.VILLAGE)
    .centerX(100)
    .centerY(200)
    .addStructure("struct_1")
    .addStructure("struct_2")
    .addStructure("struct_3")
    .population(25)
    .governingClanId("clan_1")
    .foundedTick(1000)
    .build();
```

### Add Structures Dynamically
```java
village.addStructure("struct_4");
village.addStructure("struct_5");
```

### Promote Village to Town
```java
if (village.getStructureIds().size() >= 15) {
    village.setType(VillageType.TOWN);
    village.setName("Meadowdale Town");
}
```

### Update Population
```java
village.setPopulation(50);
```

---

## Related Classes

- **VillageType**: Enumeration of settlement tiers (VILLAGE/TOWN/CITY)
- **VillageManager**: Detects and manages villages from structure clusters
- **Structure**: Buildings that comprise the village
- **RoadGenerator**: Creates roads connecting village structures

---

## Testing

**Test Class**: `VillageTest.java`  
**Test Count**: 15+ tests  
**Coverage**: 85%+

### Test Categories
- Builder pattern and validation (5 tests)
- Structure management (3 tests)
- Mutable property updates (4 tests)
- Equality and hashing (2 tests)
- Edge cases (1+ tests)

---

## Design Decisions

1. **Why immutable center?**: Village center is calculated at formation; moving would create a different village.

2. **Why mutable name/type?**: Settlements evolve over time; names can change, classifications can be promoted.

3. **Why list of structure IDs not Structure objects?**: Prevents circular references and simplifies persistence.

4. **Why single governing clan?**: Simplifies governance model; contested villages can be added in Phase 2.

5. **Why minimum 3 structures?**: 1-2 structures are individual buildings; 3+ constitutes a settlement.

---

## Future Enhancements (Post-MVP)

1. **Village Resources**: Track goods, wealth, trade capacity
2. **Village Relations**: Alliances, trade agreements between villages
3. **Village Events**: Markets, festivals, disasters
4. **Multi-Clan Governance**: Contested or coalition-run villages
5. **Village Specialization**: Trading hubs, military outposts, etc.

---

## References

- Design: `BUILD_PHASE1.10.x.md` → Phase 1.10.2
- Related: `VillageType.md`, `VillageManager.md`, `Structure.md`
- Tests: `VillageTest.md`
