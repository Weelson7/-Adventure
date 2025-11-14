# PersistenceTest

**Package:** `org.adventure`  
**Type:** JUnit 5 Test Suite  
**Phase:** 1.8 (Persistence & Save/Load)  
**Tests:** 15 test cases (all passing)

---

## Overview

`PersistenceTest` provides comprehensive test coverage for the persistence module, including save/load operations, backup rotation, checksum validation, schema versioning, and error recovery. All 15 tests verify deterministic behavior and data integrity.

**Test Coverage:**
- SaveManager: save/load roundtrip, checksum validation, atomic writes
- BackupManager: rotation, pruning, retrieval
- SchemaVersionManager: version tracking, migration paths
- Error handling: corruption detection, backup fallback

---

## Test Cases (15 Total)

### SaveManager Tests (7)

1. **`testSaveAndLoad()`** — Basic save/load roundtrip preserves data
2. **`testChecksumValidation()`** — Checksum file created on save
3. **`testChecksumMismatch()`** — Corruption detection throws exception
4. **`testLoadWithBackupFallback()`** — Automatic backup restore on corruption
5. **`testAtomicWrite()`** — Temp file cleanup after save
6. **`testDeleteSaveFile()`** — Delete removes all related files
7. **`testExistsMethod()`** — File existence check works correctly

### BackupManager Tests (4)

8. **`testBackupCreation()`** — Backups created with correct naming
9. **`testBackupRotation()`** — Old backups pruned when limit exceeded (N=3)
10. **`testGetMostRecentBackup()`** — Returns newest backup file
11. **`testDeleteBackups()`** — All backups deleted for a file

### SchemaVersionManager Tests (4)

12. **`testCurrentVersionCheck()`** — All module types return v1
13. **`testIsCurrentTrue()`** — Current version check returns true
14. **`testIsCurrentFalse()`** — Outdated version check returns false
15. **`testGetMigrationPath()`** — Migration path calculation works

---

## Test Results

```
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Coverage:**
- SaveManager: 100% line coverage
- BackupManager: 100% line coverage
- SchemaVersionManager: 100% line coverage
- WorldSerializer: Indirectly tested via SaveManager
- ChecksumMismatchException: 100% coverage

---

## Key Test Patterns

### 1. Deterministic Testing
```java
TestObject obj = new TestObject("FIXED_VALUE");
saveManager.save(obj, "test.json");
TestObject loaded = saveManager.load("test.json", TestObject.class);
assertEquals("FIXED_VALUE", loaded.getValue());
```

### 2. Corruption Detection
```java
saveManager.save(obj, "test.json");
Files.writeString(tempDir.resolve("test.json"), "CORRUPTED DATA");
assertThrows(ChecksumMismatchException.class, () -> {
    saveManager.load("test.json", TestObject.class);
});
```

### 3. Backup Recovery
```java
saveManager.save(v1, "test.json");
Thread.sleep(1000); // Ensure unique timestamp
saveManager.save(v2, "test.json");
Files.writeString(tempDir.resolve("test.json"), "CORRUPTED");
TestObject restored = saveManager.loadWithBackupFallback("test.json", TestObject.class);
assertEquals(v1.getValue(), restored.getValue());
```

---

## References

- **Implementation**: `src/main/java/org/adventure/persistence/`
- **Documentation**: `doc-src/main/java/org/adventure/persistence/`
- **Phase Summary**: `archive/PHASE_1.8_SUMMARY.md`
- **Build Guide**: `BUILD_PHASE1.md` → Phase 1.8

---

**Last Updated:** November 13, 2025  
**Status:** ✅ Complete (Phase 1.8)  
**Test Count:** 15/15 passing
