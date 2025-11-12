# AccessRole

**Package:** `org.adventure.structure`  
**Type:** Enum  
**Values:** 6 access roles

---

## Overview

`AccessRole` defines the categories of entities that can be granted access permissions to structures. Roles are mapped to `AccessLevel` values to determine what actions an entity can perform.

---

## Access Roles

### OWNER
**Description:** The structure owner (always has FULL access)  
**Default Level:** FULL (immutable)  
**Use Cases:**
- Character who built/owns the structure
- Clan that owns the structure
- Society that controls the structure

**Special Behavior:**
- Owner permission cannot be changed
- Always has FULL access regardless of explicit permission setting

### CLAN_MEMBER
**Description:** Members of the owner's clan  
**Default Level:** NONE  
**Use Cases:**
- Clan members accessing clan-owned structures
- Trusted clan members with elevated permissions
- Clan-specific features (clan bank, shared storage)

**Typical Permissions:**
- MODIFY for clan halls (can use facilities)
- MANAGE for clan officers (can change permissions)

### ALLY
**Description:** Allied factions or individuals  
**Default Level:** NONE  
**Use Cases:**
- Allied clan members visiting
- Friends granted access
- Diplomatic relationships

**Typical Permissions:**
- READ (can view contents)
- USE (can interact with features)

### PUBLIC
**Description:** General public, unaffiliated entities  
**Default Level:** NONE  
**Use Cases:**
- Public shops (READ or USE access)
- Public inns (READ and USE access)
- Private homes (typically NONE)

**Typical Permissions:**
- READ for most commercial structures
- USE for shops/inns
- NONE for private residences

### GUEST
**Description:** Explicitly invited guests  
**Default Level:** NONE  
**Use Cases:**
- Temporary access grants
- Event attendees
- Short-term permissions

**Typical Permissions:**
- READ and USE for duration of visit
- Time-limited (future enhancement)

### HOSTILE
**Description:** Banned or hostile entities  
**Default Level:** NONE (enforced)  
**Use Cases:**
- Banned players
- Enemy factions
- Explicit denials

**Special Behavior:**
- Always NONE (cannot be elevated)
- Overrides other role grants
- Enforced by game mechanics

---

## Role Hierarchy (Conceptual)

```
OWNER        → Always FULL (immutable)
CLAN_MEMBER  → Typically MODIFY or MANAGE
ALLY         → Typically READ or USE
GUEST        → Typically READ or USE (temporary)
PUBLIC       → Typically NONE or READ
HOSTILE      → Always NONE (enforced)
```

---

## Usage Examples

### Grant Public Read Access
```java
structure.setPermission(AccessRole.PUBLIC, AccessLevel.READ, currentTick);
// Anyone can view structure details
```

### Grant Clan Members Modify Access
```java
structure.setPermission(AccessRole.CLAN_MEMBER, AccessLevel.MODIFY, currentTick);
// Clan members can use facilities, repair structure
```

### Check Access
```java
boolean canModify = structure.hasAccess(AccessRole.PUBLIC, AccessLevel.MODIFY);
// Returns false if PUBLIC has only READ access
```

### Ban a Player (Hostile)
```java
structure.setPermission(AccessRole.HOSTILE, AccessLevel.NONE, currentTick);
// Explicitly deny access (future: per-entity bans)
```

---

## Role Resolution (Future Enhancement)

When an entity has multiple roles, use highest granted level:

**Example:**
- Entity is both CLAN_MEMBER (MODIFY) and GUEST (READ)
- Effective access: MODIFY (higher level)

**Priority Order:**
1. OWNER (always FULL)
2. HOSTILE (always NONE, overrides all)
3. CLAN_MEMBER
4. ALLY
5. GUEST
6. PUBLIC

---

## Design Decisions

1. **Why 6 roles?**: Covers common gameplay scenarios without excessive complexity.

2. **Why separate ALLY and GUEST?**: ALLY implies ongoing relationship; GUEST is temporary.

3. **Why HOSTILE?**: Explicit denial prevents accidental access grants; enables ban lists.

4. **Why immutable OWNER?**: Prevents owner lockout; owner always retains control.

---

## Future Enhancements

1. **Per-Entity Permissions**: Map specific entity IDs to roles (e.g., "char-001" → GUEST)
2. **Time-Limited Roles**: GUEST expires after X ticks
3. **Role Stacking**: Entity can have multiple roles, use highest level
4. **Dynamic Roles**: Calculate role based on diplomacy, reputation, etc.

---

## Related Classes

- **AccessLevel**: Hierarchical permission levels (NONE → FULL)
- **Permission**: Maps AccessRole to AccessLevel
- **Structure**: Uses roles to determine access

---

## Testing

**Test Coverage**: Included in `StructureTest.java`
- `testSetPermission()`
- `testAccessLevelHierarchy()`
- `testOwnerPermissionCannotBeChanged()`

---

## References

- Design: `docs/structures_ownership.md` → Access Control
- Summary: `archive/PHASE_1.5_SUMMARY.md`
