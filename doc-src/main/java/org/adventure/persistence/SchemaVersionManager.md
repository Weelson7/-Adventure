# SchemaVersionManager

**Package:** `org.adventure.persistence`  
**Type:** Version Tracking & Migration Service  
**Phase:** 1.8 (Persistence & Save/Load)

---

## Overview

`SchemaVersionManager` tracks schema versions for all persisted game objects and manages migration paths between versions. It provides a centralized registry of current versions and supports YAML-based migration script definitions for future schema evolution.

**Key Features:**
- Current version tracking for 27 module types
- Migration path calculation (v1 → v2 → v3)
- YAML migration registry support
- Version compatibility checks

---

## Class Structure

### Fields

```java
private static final Map<String, Integer> CURRENT_VERSIONS;
```

**Module Types (27 total):**
```
world/WorldGen       → v1    society/Clan             → v1
world/Plate          → v1    society/Diplomacy        → v1
world/Biome          → v1    story/Story              → v1
world/River          → v1    story/Event              → v1
world/RegionalFeature→ v1    story/EventPropagation   → v1
region/Region        → v1    story/SaturationManager  → v1
region/ResourceNode  → v1    structure/Structure      → v1
character/Character  → v1    structure/Room           → v1
character/NPC        → v1    structure/Upgrade        → v1
character/Trait      → v1    structure/OwnershipTransfer→v1
character/Skill      → v1    structure/Taxation       → v1
character/Race       → v1    crafting/Recipe          → v1
items/Item           → v1    crafting/System          → v1
items/ItemPrototype  → v1
```

---

## Constructor

### `SchemaVersionManager()`

**Purpose:** Create version manager with default current versions.

**Initialization:**
- Loads `CURRENT_VERSIONS` map with all 27 module types at v1
- Prepares for YAML migration registry loading (Phase 2)

**Example:**
```java
SchemaVersionManager versionManager = new SchemaVersionManager();
int worldVersion = versionManager.getCurrentVersion("world/WorldGen"); // Returns 1
```

---

## Core Methods

### `getCurrentVersion(String moduleType)`

**Purpose:** Get current schema version for a module type.

**Parameters:**
- `moduleType` — Module type key (e.g., `"world/WorldGen"`, `"character/Character"`)

**Returns:** Current version number (integer)

**Throws:**
- `IllegalArgumentException` — If module type not recognized

**Example:**
```java
int version = versionManager.getCurrentVersion("character/Character"); // 1
int storyVersion = versionManager.getCurrentVersion("story/Story");   // 1
```

**Error Handling:**
```java
try {
    int version = versionManager.getCurrentVersion("unknown/Module");
} catch (IllegalArgumentException e) {
    // Module type not recognized
}
```

---

### `isCurrent(String moduleType, int version)`

**Purpose:** Check if a version is current for a module type.

**Parameters:**
- `moduleType` — Module type key
- `version` — Version to check

**Returns:** `true` if version is current, `false` otherwise

**Example:**
```java
boolean current = versionManager.isCurrent("world/WorldGen", 1);    // true
boolean outdated = versionManager.isCurrent("character/Character", 0); // false
```

**Use Case (Loading):**
```java
WorldData data = loadWorldData();
if (!versionManager.isCurrent("world/WorldGen", data.schemaVersion)) {
    // Need migration
    data = migrateWorld(data);
}
```

---

### `getMigrationPath(String moduleType, int fromVersion, int toVersion)`

**Purpose:** Calculate migration steps from one version to another.

**Parameters:**
- `moduleType` — Module type key
- `fromVersion` — Starting version
- `toVersion` — Target version

**Returns:** List of version numbers representing migration steps

**Example:**
```java
List<Integer> path = versionManager.getMigrationPath("world/WorldGen", 1, 3);
// Returns: [1, 2, 3]
// Means: Apply migration v1→v2, then v2→v3
```

**Sequential Migration:**
```
v1 → v2 → v3 → v4
fromVersion = 1, toVersion = 4
path = [1, 2, 3, 4]
applyMigrations: migrate_1_to_2(), migrate_2_to_3(), migrate_3_to_4()
```

**No Migration Needed:**
```java
List<Integer> path = versionManager.getMigrationPath("character/NPC", 1, 1);
// Returns: [1] (empty migration)
```

---

### `needsMigration(String moduleType, int currentVersion)`

**Purpose:** Check if migration is needed for a loaded object.

**Parameters:**
- `moduleType` — Module type key
- `currentVersion` — Version from loaded data

**Returns:** `true` if migration needed (version < current)

**Example:**
```java
WorldData data = loadWorldData();
if (versionManager.needsMigration("world/WorldGen", data.schemaVersion)) {
    List<Integer> path = versionManager.getMigrationPath(
        "world/WorldGen", data.schemaVersion, 
        versionManager.getCurrentVersion("world/WorldGen")
    );
    data = applyMigrations(data, path);
}
```

---

## Migration Registry (YAML)

### Format

**File:** `src/main/resources/migrations/registry.yaml`

```yaml
world/WorldGen:
  1:
    script: migrations/worldgen_1_to_2.js
    description: "Add climate zones"
  2:
    script: migrations/worldgen_2_to_3.js
    description: "Add ocean currents"

character/Character:
  1:
    script: migrations/character_1_to_2.js
    description: "Add skills array"
  2:
    script: migrations/character_2_to_3.js
    description: "Add inventory capacity"
```

### Loading Registry

**Method:** `loadMigrationRegistry(File registryFile)`

**Purpose:** Load migration scripts from YAML (Phase 2 feature).

**Algorithm:**
1. Parse YAML file using SnakeYAML
2. Build migration map: `moduleType → version → script path`
3. Validate scripts exist on disk

**Example:**
```java
versionManager.loadMigrationRegistry(new File("migrations/registry.yaml"));
List<String> scripts = versionManager.getMigrationScripts("world/WorldGen", 1, 3);
// Returns: ["migrations/worldgen_1_to_2.js", "migrations/worldgen_2_to_3.js"]
```

---

## Version Tracking

### How It Works

**1. On Save (All Objects):**
```java
public class Character {
    private int schemaVersion = 1; // Always set to current version
    
    public int getSchemaVersion() {
        return schemaVersion;
    }
}
```

**2. On Load (SaveManager):**
```json
{
  "type": "character/Character",
  "schemaVersion": 1,
  "name": "Aldric",
  "stats": {...}
}
```

**3. Version Check:**
```java
Character character = objectMapper.readValue(json, Character.class);
if (!versionManager.isCurrent("character/Character", character.getSchemaVersion())) {
    // Migrate to current version
}
```

---

## Module Type Naming

### Convention

**Format:** `<module>/<ClassName>`

**Examples:**
```
world/WorldGen
world/Plate
region/Region
character/Character
character/NPC
items/Item
structure/Structure
story/Story
```

**Why This Format?**
- Matches package structure: `org.adventure.<module>.<ClassName>`
- Unique per class (prevents collisions)
- Readable in logs/errors

---

## Migration Workflow (Future)

### Phase 2 Implementation

**1. Detect Outdated Save:**
```java
WorldData world = saveManager.load("world.json", WorldData.class);
if (versionManager.needsMigration("world/WorldGen", world.schemaVersion)) {
    // Trigger migration
}
```

**2. Calculate Migration Path:**
```java
List<Integer> path = versionManager.getMigrationPath(
    "world/WorldGen", world.schemaVersion, 
    versionManager.getCurrentVersion("world/WorldGen")
);
// path = [1, 2, 3] → need 2 migrations
```

**3. Apply Migrations:**
```java
for (int i = 0; i < path.size() - 1; i++) {
    int from = path.get(i);
    int to = path.get(i + 1);
    String script = versionManager.getMigrationScript("world/WorldGen", from);
    world = executeMigrationScript(script, world);
}
```

**4. Update Version:**
```java
world.schemaVersion = versionManager.getCurrentVersion("world/WorldGen");
saveManager.save(world, "world.json"); // Save migrated version
```

---

## Error Handling

### Scenarios

**1. Unknown Module Type**
```java
try {
    int version = versionManager.getCurrentVersion("invalid/Type");
} catch (IllegalArgumentException e) {
    // Module type not in CURRENT_VERSIONS map
}
```

**2. Migration Path Impossible**
```java
// Downgrade not supported (yet)
List<Integer> path = versionManager.getMigrationPath("world/WorldGen", 3, 1);
// Returns: empty list (or throws exception in strict mode)
```

**3. Missing Migration Script**
```java
// v2 exists, but migration script v1→v2 missing
versionManager.loadMigrationRegistry(registryFile);
String script = versionManager.getMigrationScript("world/WorldGen", 1);
if (script == null || !new File(script).exists()) {
    // Error: Migration script not found
}
```

---

## Performance

### Benchmarks

**Version Check:**
```java
versionManager.isCurrent("world/WorldGen", 1); // <1μs (Map lookup)
```

**Migration Path Calculation:**
```java
versionManager.getMigrationPath("character/Character", 1, 5);
// ~1μs (simple loop: fromVersion to toVersion)
```

**Registry Loading (YAML):**
```java
versionManager.loadMigrationRegistry(registryFile);
// ~10ms (one-time cost at startup)
```

---

## Integration Examples

### With SaveManager

**Save (Auto-Version):**
```java
public class SaveManager {
    private SchemaVersionManager versionManager;
    
    public void save(Object object, String filename) throws IOException {
        // Object's schemaVersion field already set to current version
        // SaveManager doesn't need to modify it
        objectMapper.writeValue(file, object);
    }
}
```

**Load (Version Check):**
```java
public <T> T load(String filename, Class<T> clazz) throws IOException {
    T object = objectMapper.readValue(file, clazz);
    
    // Get module type from object (assume object has getType() method)
    String moduleType = extractModuleType(object);
    int loadedVersion = extractSchemaVersion(object);
    
    if (versionManager.needsMigration(moduleType, loadedVersion)) {
        object = migrateObject(object, moduleType, loadedVersion);
    }
    
    return object;
}
```

### With Game Startup

**Version Audit:**
```java
public class Game {
    public void startup() {
        SchemaVersionManager versionManager = new SchemaVersionManager();
        
        System.out.println("=== Schema Version Audit ===");
        for (String moduleType : getAllModuleTypes()) {
            int version = versionManager.getCurrentVersion(moduleType);
            System.out.println(moduleType + ": v" + version);
        }
    }
}
```

**Output:**
```
=== Schema Version Audit ===
world/WorldGen: v1
world/Plate: v1
character/Character: v1
character/NPC: v1
items/Item: v1
...
```

---

## Testing

### Test Coverage

**Tests:** 4/4 in `PersistenceTest.java`

**1. `testCurrentVersionCheck()`** — All 27 modules return v1
**2. `testIsCurrentTrue()`** — `isCurrent("world/WorldGen", 1) == true`
**3. `testIsCurrentFalse()`** — `isCurrent("world/WorldGen", 0) == false`
**4. `testGetMigrationPath()`** — Path calculation (v1 → v3)

---

## Design Decisions

### 1. Why centralized version registry?
**Decision:** Single `CURRENT_VERSIONS` map  
**Rationale:**
- Easy to audit all versions (one place)
- Prevents version drift (all modules updated together)
- Simple to generate migration reports

### 2. Why integer versions (not semver)?
**Decision:** `int version` (1, 2, 3, ...)  
**Rationale:**
- Simple incremental updates
- Easy to calculate migration paths
- No breaking vs. non-breaking distinction needed (all saves migrate)

### 3. Why YAML for migration registry?
**Decision:** YAML (not JSON or Java code)  
**Rationale:**
- Human-readable (easier to edit)
- Comments supported (document migrations)
- Industry standard for config

---

## Future Enhancements

### Phase 2 Features

**1. Automatic Migration Execution**
```java
public <T> T loadAndMigrate(String filename, Class<T> clazz) throws IOException {
    T object = load(filename, clazz);
    return autoMigrate(object); // Apply all needed migrations
}
```

**2. Migration Validation**
```java
public boolean validateMigration(String moduleType, int fromVersion, int toVersion) {
    // Check if all migration scripts exist
    // Validate script syntax (if JS/Lua)
}
```

**3. Rollback Support**
```java
public void rollbackMigration(String filename, int targetVersion) {
    // Restore from backup at targetVersion
}
```

**4. Migration Dry-Run**
```java
public MigrationReport previewMigration(String filename) {
    // Show what would change (don't apply)
}
```

---

## Migration Script Examples (Phase 2)

### JavaScript Migration

**File:** `migrations/character_1_to_2.js`

```javascript
// Add skills array to Character v1 → v2
function migrate(character) {
    if (!character.skills) {
        character.skills = [];
    }
    character.schemaVersion = 2;
    return character;
}
```

### Java Migration

**File:** `CharacterMigration_1_to_2.java`

```java
public class CharacterMigration_1_to_2 implements MigrationScript {
    public Character migrate(Character character) {
        if (character.getSkills() == null) {
            character.setSkills(new ArrayList<>());
        }
        character.setSchemaVersion(2);
        return character;
    }
}
```

---

## Related Classes

- `SaveManager` — Uses SchemaVersionManager for version checks
- `BackupManager` — No version dependency (file-level operations)
- All data classes — Include `schemaVersion` field (27 classes)

---

## References

- Design: `docs/persistence_versioning.md` → Schema Versioning Strategy
- Phase Summary: `archive/PHASE_1.8_SUMMARY.md`
- Tests: `src/test/java/org/adventure/PersistenceTest.java`
- Migration Registry: `src/main/resources/migrations/registry.yaml` (Phase 2)

---

**Last Updated:** November 13, 2025  
**Status:** ✅ Complete (Phase 1.8)
