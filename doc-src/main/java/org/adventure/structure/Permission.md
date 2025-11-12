# Permission

**Package:** `org.adventure.structure`  
**Type:** Immutable Value Class  
**Purpose:** Maps AccessRole to AccessLevel

---

## Overview

`Permission` is an immutable data class that associates an `AccessRole` with an `AccessLevel`. It is used internally by `Structure` to store access permissions.

---

## Class Structure

```java
public final class Permission {
    private final AccessRole role;
    private final AccessLevel level;
}
```

---

## Usage

### Creation
```java
Permission permission = new Permission(AccessRole.PUBLIC, AccessLevel.READ);
```

### Accessors
```java
AccessRole role = permission.getRole();       // PUBLIC
AccessLevel level = permission.getLevel();    // READ
```

---

## Equality and Hashing

- **Equality**: Based on both `role` and `level`
- **Hash Code**: Based on both `role` and `level`

**Example:**
```java
Permission p1 = new Permission(AccessRole.PUBLIC, AccessLevel.READ);
Permission p2 = new Permission(AccessRole.PUBLIC, AccessLevel.READ);
Permission p3 = new Permission(AccessRole.PUBLIC, AccessLevel.USE);

assertEquals(p1, p2);   // Same role and level
assertNotEquals(p1, p3); // Different level
```

---

## Usage in Structure System

Permissions are stored internally in `Structure` as a `Map<AccessRole, AccessLevel>`. The `Permission` class is primarily used for:

1. **JSON Serialization**: Jackson serializes permissions as `Permission` objects
2. **API Clarity**: Explicit Permission type documents role-level pairing
3. **Future Extensions**: Easy to add permission metadata (expiry, conditions, etc.)

### Internal Storage
```java
// Inside Structure class
private final Map<AccessRole, AccessLevel> permissions = new HashMap<>();
```

### Setting Permission
```java
structure.setPermission(AccessRole.PUBLIC, AccessLevel.READ, tick);
// Internally stores: permissions.put(AccessRole.PUBLIC, AccessLevel.READ)
```

---

## Validation Rules

### At Construction
- `role` cannot be null
- `level` cannot be null

---

## Persistence

### JSON Representation
```json
{
  "role": "PUBLIC",
  "level": "READ"
}
```

Used in Structure's permissions map:
```json
{
  "permissions": {
    "OWNER": "FULL",
    "PUBLIC": "READ",
    "CLAN_MEMBER": "MODIFY"
  }
}
```

---

## Design Decisions

1. **Why immutable?**: Permissions should not change after creation; create new Permission to modify.

2. **Why separate class?**: Clarifies API; explicit type is more readable than Map.Entry<AccessRole, AccessLevel>.

3. **Why not used directly in Structure?**: Structure uses Map for efficient lookups; Permission is for external representation.

---

## Future Enhancements

1. **Expiry Timestamp**: Time-limited permissions
   ```java
   private final int expiresAtTick;
   ```

2. **Conditions**: Event-based or state-based conditions
   ```java
   private final List<PermissionCondition> conditions;
   ```

3. **Grantee Tracking**: Who granted this permission
   ```java
   private final String grantedBy;
   private final int grantedAtTick;
   ```

---

## Related Classes

- **AccessRole**: Role category for permission
- **AccessLevel**: Level granted to role
- **Structure**: Uses Permission for access control

---

## Testing

**Test Coverage**: Implicitly tested via `StructureTest.java`
- Permission equality via structure permission tests
- Permission storage/retrieval via access tests

---

## References

- Design: `docs/structures_ownership.md` → Access Control
- Summary: `archive/PHASE_1.5_SUMMARY.md`
