# AccessLevel

**Package:** `org.adventure.structure`  
**Type:** Enum (Hierarchical)  
**Values:** 6 access levels (0-5)

---

## Overview

`AccessLevel` defines hierarchical permission levels for structure access. Higher levels include all permissions of lower levels. Each level has a numeric value for easy comparison.

---

## Access Levels

### NONE (0)
**Description:** No access whatsoever  
**Permissions:**
- Cannot view structure details
- Cannot enter structure
- Cannot interact with structure

**Use Cases:**
- Default for undefined roles
- Hostile/banned entities
- Private structures

### READ (1)
**Description:** View-only access  
**Permissions:**
- View structure details (type, owner, rooms)
- View exterior
- Read descriptions

**Use Cases:**
- Public shops (customers can see goods)
- Public buildings (can see details)
- Allied structures (reconnaissance)

### USE (2)
**Description:** Basic interaction access  
**Permissions:**
- All READ permissions
- Enter structure
- Use facilities (beds, crafting stations)
- Purchase goods (shops)

**Use Cases:**
- Shop customers
- Inn guests
- Public training centers

### MODIFY (3)
**Description:** Content and state modification  
**Permissions:**
- All USE permissions
- Add/remove items
- Repair structure
- Use advanced facilities

**Use Cases:**
- Clan members in clan hall
- Trusted allies
- Tenants in rental properties

### MANAGE (4)
**Description:** Administrative access  
**Permissions:**
- All MODIFY permissions
- Change access permissions (for lower roles)
- Apply upgrades
- Manage rooms
- Configure structure settings

**Use Cases:**
- Clan officers
- Property managers
- Delegated administrators

### FULL (5)
**Description:** Complete control  
**Permissions:**
- All MANAGE permissions
- Transfer ownership
- Destroy structure
- Grant MANAGE access to others

**Use Cases:**
- Structure owner (always)
- God-mode admins (future)

---

## Hierarchical Logic

### `allows(AccessLevel required)` Method
Returns true if this level >= required level.

**Examples:**
```java
AccessLevel.MODIFY.allows(AccessLevel.READ);   // true (3 >= 1)
AccessLevel.READ.allows(AccessLevel.MODIFY);   // false (1 < 3)
AccessLevel.FULL.allows(AccessLevel.MANAGE);   // true (5 >= 4)
```

---

## Usage Examples

### Check Sufficient Access
```java
AccessLevel granted = structure.getAccessLevel(AccessRole.PUBLIC);

if (granted.allows(AccessLevel.USE)) {
    // Can enter and use structure
    character.enterStructure(structure);
}
```

### Compare Levels
```java
AccessLevel level1 = AccessLevel.MODIFY;
AccessLevel level2 = AccessLevel.READ;

if (level1.getValue() > level2.getValue()) {
    // MODIFY is higher than READ
}
```

### Grant Hierarchical Permissions
```java
// Grant MODIFY access (includes READ and USE)
structure.setPermission(AccessRole.CLAN_MEMBER, AccessLevel.MODIFY, tick);

// Clan members can now:
assertTrue(structure.hasAccess(AccessRole.CLAN_MEMBER, AccessLevel.READ));
assertTrue(structure.hasAccess(AccessRole.CLAN_MEMBER, AccessLevel.USE));
assertTrue(structure.hasAccess(AccessRole.CLAN_MEMBER, AccessLevel.MODIFY));

// But cannot:
assertFalse(structure.hasAccess(AccessRole.CLAN_MEMBER, AccessLevel.MANAGE));
```

---

## Permission Matrix (Examples)

| Structure Type | OWNER | CLAN_MEMBER | ALLY | PUBLIC | GUEST | HOSTILE |
|---------------|-------|-------------|------|--------|-------|---------|
| Private House | FULL  | NONE        | NONE | NONE   | USE   | NONE    |
| Clan Hall     | FULL  | MODIFY      | READ | READ   | READ  | NONE    |
| Public Shop   | FULL  | MODIFY      | USE  | USE    | USE   | NONE    |
| Fortress      | FULL  | MANAGE      | NONE | NONE   | NONE  | NONE    |
| Public Inn    | FULL  | MODIFY      | USE  | USE    | USE   | NONE    |

---

## Design Decisions

1. **Why hierarchical?**: Simplifies permission checks; higher levels automatically include lower permissions.

2. **Why numeric values?**: Enables easy comparison (`>=`, `>`) without complex logic.

3. **Why 6 levels?**: Covers common use cases without excessive granularity.

4. **Why READ before USE?**: Viewing is less invasive than interaction; common pattern in access control systems.

---

## Future Enhancements

1. **Granular Permissions**: Per-action permissions (can_repair, can_trade, etc.) instead of hierarchical levels
2. **Conditional Access**: Time-based or event-based permission changes
3. **Temporary Elevation**: Grant higher access for limited time
4. **Permission Inheritance**: Child rooms inherit parent structure permissions

---

## Related Classes

- **AccessRole**: Categories of entities receiving permissions
- **Permission**: Maps AccessRole to AccessLevel
- **Structure**: Uses AccessLevel to enforce access control

---

## Testing

**Test Coverage**: Included in `StructureTest.java`
- `testAccessLevelHierarchy()`: Validates hierarchical logic
- `testDefaultAccessIsNone()`: Validates default level

---

## References

- Design: `docs/structures_ownership.md` → Access Control System
- Summary: `archive/PHASE_1.5_SUMMARY.md`
