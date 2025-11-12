# Room

**Package:** `org.adventure.structure`  
**Type:** Immutable Value Class  
**Schema Version:** 1

---

## Overview

`Room` represents an interior space within a structure. Rooms provide various gameplay effects based on their category and can be customized with properties. Rooms are added to structures via `Structure.addRoom()`.

---

## Class Structure

```java
public final class Room {
    private final String id;
    private final RoomCategory category;
    private final int size;
    private final Map<String, Object> properties;
    private final int createdAtTick;
    private final int schemaVersion;
}
```

---

## Room Categories

**RoomCategory Enum** defines 10 categories:

1. **LIVING_QUARTERS**: Provides rest and stat recovery
2. **STORAGE**: Increases inventory capacity
3. **TRAINING**: Provides skill XP bonuses
4. **CRAFTING**: Enables crafting and proficiency bonuses
5. **MAGICAL**: Mana regeneration and spell research
6. **TREASURY**: Secure storage for valuables
7. **DEFENSIVE**: Protection and military bonuses
8. **DINING**: Food buffs and social bonuses
9. **LIBRARY**: Intelligence and wisdom bonuses
10. **HALL**: Enables clan meetings and diplomacy

---

## Builder Pattern

### Construction
```java
Room room = new Room.Builder()
    .id("room-001")
    .category(RoomCategory.LIVING_QUARTERS)
    .size(10)
    .property("comfort", 5)
    .property("capacity", 4)
    .createdAtTick(1000)
    .build();
```

### Builder Methods
- **`id(String)`**: Unique room identifier (required)
- **`category(RoomCategory)`**: Room category (required)
- **`size(int)`**: Room size in arbitrary units (default: 1)
- **`properties(Map<String, Object>)`**: Bulk property assignment
- **`property(String, Object)`**: Add individual property
- **`createdAtTick(int)`**: Creation timestamp (default: 0)
- **`schemaVersion(int)`**: Schema version (default: 1)

---

## Validation Rules

### At Construction
- `id` cannot be null or empty
- `category` cannot be null
- `size` must be positive

---

## Properties System

Rooms use a flexible `Map<String, Object>` for custom properties:

### Common Properties (examples)
- **comfort**: Integer rating (1-10) for living quarters
- **capacity**: Number of occupants/items
- **bonus**: Percentage bonus for skills/stats
- **security**: Security level for treasuries
- **research**: Research speed multiplier for libraries

### Property Access
```java
Map<String, Object> props = room.getProperties();
Integer comfort = (Integer) props.get("comfort");
```

---

## Equality and Hashing

- **Equality**: Based on `id` and `category`
- **Hash Code**: Based on `id` and `category`

---

## Persistence

### JSON Schema (v1)
```json
{
  "id": "room-001",
  "category": "LIVING_QUARTERS",
  "size": 10,
  "properties": {
    "comfort": 5,
    "capacity": 4
  },
  "createdAtTick": 1000,
  "schemaVersion": 1
}
```

---

## Usage Examples

### Create a Crafting Workshop
```java
Room workshop = new Room.Builder()
    .id("room-craft-01")
    .category(RoomCategory.CRAFTING)
    .size(15)
    .property("bonus", 10)  // 10% crafting proficiency bonus
    .property("stations", 3) // 3 crafting stations
    .build();

structure.addRoom(workshop, currentTick);
```

### Create a Treasury
```java
Room treasury = new Room.Builder()
    .id("room-treasury-01")
    .category(RoomCategory.TREASURY)
    .size(8)
    .property("security", 9)
    .property("capacity", 1000)
    .build();
```

---

## Room Effects (Future Implementation)

Rooms will provide effects based on category:

### LIVING_QUARTERS
- Health regeneration: +5% per hour
- Stat recovery: Faster recovery from debuffs

### TRAINING
- Skill XP bonus: +10% for related skills
- Training efficiency: Reduced training time

### CRAFTING
- Proficiency bonus: +5-20% based on room quality
- Success rate bonus: +5% for recipes

### MAGICAL
- Mana regeneration: +10 mana per hour
- Spell research: Unlock new spells faster

---

## Related Classes

- **RoomCategory**: Enum of room types
- **Structure**: Parent structure containing rooms
- **Upgrade**: Can affect room properties

---

## Testing

**Test Coverage**: Included in `StructureTest.java`  
**Test Methods**: `testAddRoom()`, `testAddRoomRejectsNull()`

---

## Design Decisions

1. **Why immutable?**: Rooms don't change after creation; replace entire room to modify.

2. **Why properties map?**: Allows extensibility without schema changes; different categories need different properties.

3. **Why size is integer?**: Abstract unit allows flexibility (could be square meters, tiles, or arbitrary units).

---

## Future Enhancements

1. **Room Connections**: Graph of connected rooms for pathfinding
2. **Room States**: Occupied, locked, damaged states
3. **Room Events**: Trigger events when characters enter/exit
4. **Dynamic Effects**: Calculate effects based on properties + upgrades

---

## References

- Design: `docs/structures_ownership.md` → Room System
- Summary: `archive/PHASE_1.5_SUMMARY.md`
