# OwnerType

**Package:** `org.adventure.structure`  
**Type:** Enum  
**Values:** 5 owner types

---

## Overview

`OwnerType` defines the categories of entities that can own structures in the game. It is used in conjunction with `ownerId` to identify structure ownership.

---

## Owner Types

### CHARACTER
**Description:** Individual character ownership  
**Use Cases:**
- Player-owned houses, shops, towers
- NPC-owned structures
- Personal property

**Example:**
```java
Structure house = new Structure.Builder()
    .ownerId("char-001")
    .ownerType(OwnerType.CHARACTER)
    .build();
```

### CLAN
**Description:** Clan/guild collective ownership  
**Use Cases:**
- Clan halls, clan-owned fortresses
- Shared training facilities
- Clan treasuries

**Example:**
```java
Structure clanHall = new Structure.Builder()
    .ownerId("clan-001")
    .ownerType(OwnerType.CLAN)
    .build();
```

### SOCIETY
**Description:** Society/kingdom collective ownership  
**Use Cases:**
- Kingdom castles, city walls
- Public infrastructure (roads, bridges)
- Society-owned temples

**Example:**
```java
Structure temple = new Structure.Builder()
    .ownerId("society-001")
    .ownerType(OwnerType.SOCIETY)
    .build();
```

### NONE
**Description:** Unowned or abandoned structures  
**Use Cases:**
- Newly generated structures
- Abandoned buildings
- Ruins without claimants

**Example:**
```java
Structure ruins = new Structure.Builder()
    .ownerId("")
    .ownerType(OwnerType.NONE)
    .build();
```

**Behavior:**
- No owner, no default permissions
- Can be claimed through game mechanics
- Typically free to enter/use

### GOVERNMENT
**Description:** Government/system ownership (seized structures)  
**Use Cases:**
- Tax-seized structures
- Government buildings (post offices, courts)
- Public infrastructure under government control

**Example:**
```java
// After tax seizure
structure.transferOwnership("gov-001", OwnerType.GOVERNMENT, tick);
```

**Behavior:**
- Managed by game systems
- May be auctioned or redistributed
- Special rules for access/usage

---

## Usage in Structure System

### Ownership Transfer
```java
// Transfer from character to clan
structure.transferOwnership("clan-001", OwnerType.CLAN, currentTick);
```

### Query Ownership
```java
OwnerType type = structure.getOwnerType();
String ownerId = structure.getOwnerId();

if (type == OwnerType.CHARACTER) {
    // Handle character ownership
} else if (type == OwnerType.CLAN) {
    // Handle clan ownership
}
```

---

## Design Decisions

1. **Why NONE instead of null?**: Explicit representation of unowned state; prevents null pointer errors.

2. **Why separate CLAN and SOCIETY?**: Different ownership semantics; clans are smaller, voluntary groups; societies are larger, hierarchical organizations.

3. **Why GOVERNMENT?**: Represents system-owned structures (seized, public buildings); different rules from player-owned.

---

## Future Enhancements

1. **FACTION**: Enemy factions with different rules
2. **MERCHANT_GUILD**: Special merchant collective ownership
3. **RELIGIOUS_ORDER**: Temple/church hierarchies
4. **COMPANY**: Trading company ownership

---

## Related Classes

- **Structure**: Uses OwnerType to identify owner category
- **AccessRole**: Different roles have different access based on owner type
- **TaxationSystem**: Seizure transfers to GOVERNMENT owner type

---

## Testing

**Test Coverage**: Included in `StructureTest.java`

---

## References

- Design: `docs/structures_ownership.md` → Ownership Model
- Summary: `archive/PHASE_1.5_SUMMARY.md`
