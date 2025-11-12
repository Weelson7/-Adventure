# Structure

**Package:** `org.adventure.structure`  
**Type:** Immutable Data Class (with mutable state for health/permissions)  
**Schema Version:** 1

---

## Overview

`Structure` represents a physical building or construction in the game world. Structures can be owned, damaged, upgraded, and contain rooms. They follow a single-owner model with separate access permissions for different roles.

---

## Design Principles

1. **Single-Owner Model**: Each structure has exactly one owner (Character, Clan, Society, or None)
2. **Separate Access Control**: Ownership and access permissions are independent
3. **Health Management**: Structures can take damage and be repaired (but not if destroyed)
4. **Extensible Design**: Builder pattern + properties for future additions
5. **Immutable Collections**: Rooms, upgrades, and permissions are exposed as unmodifiable views

---

## Class Structure

```java
public final class Structure {
    // Immutable identity
    private final String id;
    private final StructureType type;
    private final String locationTileId;
    private final double maxHealth;
    private final int createdAtTick;
    private final int schemaVersion;
    
    // Mutable ownership
    private String ownerId;
    private OwnerType ownerType;
    
    // Mutable state
    private double health;
    private int lastUpdatedTick;
    
    // Collections (internally mutable, exposed as unmodifiable)
    private final List<Room> rooms;
    private final List<Upgrade> upgrades;
    private final Map<AccessRole, AccessLevel> permissions;
}
```

---

## Key Methods

### Creation
- **Builder Pattern**: `new Structure.Builder()...build()`
  - Fluent API for flexible construction
  - Validation at build time
  - Default values for optional fields

### Health Management
- **`takeDamage(amount, tick)`**: Apply damage (clamped to 0)
- **`repair(amount, tick)`**: Restore health (clamped to maxHealth, throws if destroyed)
- **`isDestroyed()`**: Health == 0
- **`isDamaged()`**: Health < maxHealth
- **`getHealthPercentage()`**: Returns 0.0 to 1.0

### Ownership
- **`transferOwnership(newOwnerId, newOwnerType, tick)`**: Change owner
  - Clears old permissions
  - Grants new owner FULL access
  - Updates `lastUpdatedTick`

### Access Control
- **`setPermission(role, level, tick)`**: Grant/modify permission
  - Owner permission always FULL (cannot be changed)
  - Updates `lastUpdatedTick`
- **`hasAccess(role, requiredLevel)`**: Check if role meets requirement
- **`getAccessLevel(role)`**: Get granted level (defaults to NONE)

### Collections
- **`addRoom(room, tick)`**: Add room to structure
- **`applyUpgrade(upgrade, tick)`**: Apply upgrade to structure
- **`getRooms()`**: Unmodifiable list of rooms
- **`getUpgrades()`**: Unmodifiable list of upgrades
- **`getPermissions()`**: Unmodifiable map of permissions

---

## Validation Rules

### At Construction
- `id` cannot be null or empty
- `type` cannot be null
- `locationTileId` cannot be null or empty
- `maxHealth` must be positive
- `health` cannot be negative
- `health` cannot exceed `maxHealth`

### At Runtime
- Damage amount cannot be negative
- Repair amount cannot be negative
- Cannot repair destroyed structures (health == 0)
- Owner always has FULL access (enforced at construction and transfer)
- Room and Upgrade cannot be null

---

## Access Control System

### Roles (AccessRole)
- **OWNER**: Structure owner (always FULL access)
- **CLAN_MEMBER**: Member of owner's clan
- **ALLY**: Allied faction/individual
- **PUBLIC**: General public
- **GUEST**: Explicitly granted guest
- **HOSTILE**: Banned entity

### Levels (AccessLevel)
Hierarchical (higher includes lower):
- **NONE** (0): No access
- **READ** (1): View details
- **USE** (2): Interact with features
- **MODIFY** (3): Change contents, repair
- **MANAGE** (4): Change permissions, upgrade
- **FULL** (5): Transfer ownership, destroy

### Permission Logic
- Owner permission is **immutable** (always FULL)
- Default access is NONE for undefined roles
- `hasAccess(role, level)` returns true if granted level >= required level

---

## Equality and Hashing

- **Equality**: Based on `id` only (structures with same ID are equal)
- **Hash Code**: Based on `id` only

---

## Persistence

### JSON Schema (v1)
```json
{
  "id": "struct-001",
  "type": "HOUSE",
  "ownerId": "char-001",
  "ownerType": "CHARACTER",
  "locationTileId": "100:200:0",
  "health": 85.0,
  "maxHealth": 100.0,
  "rooms": [...],
  "upgrades": [...],
  "permissions": {
    "OWNER": "FULL",
    "PUBLIC": "READ"
  },
  "createdAtTick": 0,
  "lastUpdatedTick": 5000,
  "schemaVersion": 1
}
```

### Migration Notes
- Schema version 1 (current)
- Add `schemaVersion` field for future migrations
- Use `@JsonCreator` + `@JsonProperty` for Jackson compatibility

---

## Usage Examples

### Create a Basic House
```java
Structure house = new Structure.Builder()
    .id("struct-001")
    .type(StructureType.HOUSE)
    .ownerId("char-001")
    .ownerType(OwnerType.CHARACTER)
    .locationTileId("100:200:0")
    .health(100.0)
    .maxHealth(100.0)
    .permission(AccessRole.PUBLIC, AccessLevel.READ)
    .build();
```

### Apply Damage and Repair
```java
house.takeDamage(30.0, 100);  // Health: 70.0
house.repair(20.0, 200);       // Health: 90.0
```

### Check Access
```java
boolean canModify = house.hasAccess(AccessRole.CLAN_MEMBER, AccessLevel.MODIFY);
```

### Transfer Ownership
```java
house.transferOwnership("char-002", OwnerType.CHARACTER, 500);
// Old permissions cleared, new owner has FULL access
```

---

## Related Classes

- **StructureType**: Enumeration of structure categories
- **Room**: Interior spaces within structures
- **Upgrade**: Applied improvements to structures
- **OwnerType**: Type of owner entity
- **AccessRole**: Permission role categories
- **AccessLevel**: Hierarchical permission levels
- **Permission**: Role-to-level mapping

---

## Testing

**Test Class**: `StructureTest.java`  
**Test Count**: 27 tests  
**Coverage**: 85%+

### Test Categories
- Creation and validation (5 tests)
- Health management (8 tests)
- Ownership and permissions (7 tests)
- Collections (4 tests)
- Equality (1 test)
- Structure types (1 test)

---

## Design Decisions

1. **Why single-owner?**: Simplifies ownership semantics; access is granted via permissions, not ownership shares.

2. **Why immutable collections?**: Prevents external modification; all changes go through tracked methods that update `lastUpdatedTick`.

3. **Why owner permission cannot be changed?**: Prevents accidental lockout; owner always retains control.

4. **Why can't repair destroyed structures?**: Destroyed structures should be rebuilt, not repaired (represents total structural failure).

---

## Future Enhancements (Post-MVP)

1. **Legacy Effects**: Haunting, legendary status, historical significance
2. **Structure Events**: Triggers based on usage or story integration
3. **Multi-tile Structures**: Expand beyond single-tile placement
4. **Dynamic Max Health**: Upgrades increase max health capacity
5. **Durability Decay**: Gradual health loss over time without maintenance

---

## References

- Design: `docs/structures_ownership.md`
- Data Model: `docs/data_models.md` → Structure Schema
- Specs: `docs/specs_summary.md` → Ownership Defaults
- Summary: `archive/PHASE_1.5_SUMMARY.md`
