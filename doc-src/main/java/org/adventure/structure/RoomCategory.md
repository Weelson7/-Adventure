# RoomCategory

**Package:** `org.adventure.structure`  
**Type:** Enum  
**Values:** 10 room categories

---

## Overview

`RoomCategory` defines the types of interior spaces that can exist within structures. Each category provides different gameplay effects and is used to determine room functionality.

---

## Room Categories

### LIVING_QUARTERS
**Description:** Residential space for rest and recovery  
**Typical Effects:**
- Health regeneration: +5% per hour
- Stat recovery: Faster recovery from debuffs
- Rested bonus: +10% XP gain for 1 hour after rest

**Use Cases:**
- Bedrooms in houses
- Barracks sleeping areas
- Inn guest rooms

**Properties Example:**
```java
new Room.Builder()
    .category(RoomCategory.LIVING_QUARTERS)
    .property("comfort", 7)      // Comfort rating (1-10)
    .property("capacity", 4)     // Number of beds
    .property("privacy", true)   // Private room
    .build();
```

---

### STORAGE
**Description:** Space for storing items and resources  
**Typical Effects:**
- Increased inventory capacity
- Item preservation (reduced decay)
- Organized storage (faster item retrieval)

**Use Cases:**
- Warehouse rooms
- Personal storage chests
- Clan bank vaults

**Properties Example:**
```java
new Room.Builder()
    .category(RoomCategory.STORAGE)
    .property("capacity", 1000)  // Max items
    .property("secure", true)    // Locked storage
    .build();
```

---

### TRAINING
**Description:** Facility for skill development  
**Typical Effects:**
- Skill XP bonus: +10-20% based on room quality
- Training efficiency: Reduced training time
- Stat gain bonuses

**Use Cases:**
- Training yards
- Combat arenas
- Skill-specific training rooms

**Properties Example:**
```java
new Room.Builder()
    .category(RoomCategory.TRAINING)
    .property("bonus", 15)           // +15% XP
    .property("skillFocus", "Combat") // Focused skill
    .build();
```

---

### CRAFTING
**Description:** Workshop for crafting items  
**Typical Effects:**
- Crafting proficiency bonus: +5-20%
- Success rate bonus: +5%
- Quality bonus: Higher chance of superior items

**Use Cases:**
- Blacksmith forge
- Alchemy lab
- Enchanting workshop

**Properties Example:**
```java
new Room.Builder()
    .category(RoomCategory.CRAFTING)
    .property("bonus", 10)              // +10% proficiency
    .property("stations", 2)            // Number of workstations
    .property("specialty", "Blacksmithing")
    .build();
```

---

### MAGICAL
**Description:** Space for magical practice and research  
**Typical Effects:**
- Mana regeneration: +10 mana per hour
- Spell research: Unlock new spells faster
- Casting power: +5% spell effectiveness

**Use Cases:**
- Wizard tower study
- Ritual chambers
- Enchanted libraries (if used for spellcasting)

**Properties Example:**
```java
new Room.Builder()
    .category(RoomCategory.MAGICAL)
    .property("manaRegen", 15)      // +15 mana/hour
    .property("researchBonus", 10)  // +10% research speed
    .property("spellPower", 5)      // +5% spell power
    .build();
```

---

### TREASURY
**Description:** Secure storage for valuables and currency  
**Typical Effects:**
- High security (harder to steal from)
- Currency storage (gold, gems)
- Valuable item protection

**Use Cases:**
- Clan treasury
- Personal vault
- Bank safe deposit

**Properties Example:**
```java
new Room.Builder()
    .category(RoomCategory.TREASURY)
    .property("security", 9)       // Security level (1-10)
    .property("capacity", 10000)   // Max gold value
    .property("trapped", true)     // Has traps
    .build();
```

---

### DEFENSIVE
**Description:** Military/defensive installations  
**Typical Effects:**
- Structure defense bonus: +10% health
- Combat bonuses for defenders
- Increased protection from siege

**Use Cases:**
- Guard towers
- Armory rooms
- Fortification chambers

**Properties Example:**
```java
new Room.Builder()
    .category(RoomCategory.DEFENSIVE)
    .property("defenseBonus", 15)  // +15% defense
    .property("garrison", 10)      // Max defenders
    .build();
```

---

### DINING
**Description:** Space for meals and social gatherings  
**Typical Effects:**
- Food consumption bonuses
- Social interaction bonuses
- Morale/happiness increase

**Use Cases:**
- Dining halls
- Inn common rooms
- Feast halls

**Properties Example:**
```java
new Room.Builder()
    .category(RoomCategory.DINING)
    .property("seating", 20)       // Capacity
    .property("foodQuality", 7)    // Quality (1-10)
    .property("morale", 5)         // +5% morale
    .build();
```

---

### LIBRARY
**Description:** Repository of knowledge and books  
**Typical Effects:**
- Intelligence bonus: +2 INT while studying
- Wisdom bonus: +1 WIS while studying
- Research speed: +15%

**Use Cases:**
- Personal libraries
- Enchanted libraries (if used for research)
- University study halls

**Properties Example:**
```java
new Room.Builder()
    .category(RoomCategory.LIBRARY)
    .property("intBonus", 2)       // +2 INT
    .property("wisBonus", 1)       // +1 WIS
    .property("books", 500)        // Number of books
    .build();
```

---

### HALL
**Description:** Large gathering space for meetings and events  
**Typical Effects:**
- Enable clan meetings
- Diplomacy bonuses
- Event capacity

**Use Cases:**
- Clan meeting halls
- Throne rooms
- Guild headquarters

**Properties Example:**
```java
new Room.Builder()
    .category(RoomCategory.HALL)
    .property("capacity", 50)      // Max attendees
    .property("prestige", 8)       // Prestige level
    .build();
```

---

## Design Decisions

1. **Why 10 categories?**: Covers primary gameplay needs without excessive complexity.

2. **Why property-based effects?**: Flexibility for different room qualities; same category can have varied effects.

3. **Why separate MAGICAL and LIBRARY?**: Different primary functions (casting vs. research).

4. **Why HALL category?**: Enables social/organizational gameplay (clan meetings, events).

---

## Future Enhancements

1. **GARDEN**: Outdoor/semi-outdoor space for farming
2. **LABORATORY**: Scientific research (distinct from magical)
3. **PRISON**: For captured enemies
4. **STABLE**: For mounts and animals
5. **CHAPEL**: Religious/spiritual functions

---

## Related Classes

- **Room**: Uses RoomCategory to define room type
- **Structure**: Contains rooms of various categories

---

## Testing

**Test Coverage**: Implicitly tested via `StructureTest.java`

---

## References

- Design: `docs/structures_ownership.md` → Room System
- Summary: `archive/PHASE_1.5_SUMMARY.md`
