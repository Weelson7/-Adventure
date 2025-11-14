# Gender

**Package:** `org.adventure.npc`  
**Type:** Enum  
**Since:** Phase 1.10.1

---

## Overview

`Gender` represents the biological gender of a Named NPC. It's used for:
- Marriage compatibility checking (typically opposite genders)
- Name generation (male vs female name lists)
- Fertility calculations (future: gender-specific fertility curves)

---

## Enum Values

### `MALE`
Represents a male NPC.

### `FEMALE`
Represents a female NPC.

---

## Usage

```java
// Creating an NPC with gender
Gender gender = Gender.MALE;

// Random gender selection
Random rng = new Random(seed);
Gender randomGender = rng.nextBoolean() ? Gender.MALE : Gender.FEMALE;

// Name generation based on gender
String name = (gender == Gender.MALE) ? selectMaleName() : selectFemaleName();

// Marriage compatibility
boolean canMarry = (npc1.getGender() != npc2.getGender()); // Opposite genders
```

---

## Design Notes

**Binary Gender Model:**
- Current implementation uses binary gender (MALE/FEMALE)
- This is a simplification for MVP
- Future phases may expand to support non-binary genders or gender fluidity

**Marriage Compatibility:**
- Default marriage system requires opposite genders
- Future: Configuration option for same-gender marriage
- Player marriage can be configured independently

---

## Related Classes

- `NamedNPC` — Uses Gender as a field
- `NPCGenerator` — Generates random genders for NPCs
- `NPCLifecycleManager` — Uses gender for marriage compatibility checks
- `PlayerNPCInteraction` — Uses gender for player-NPC marriage
