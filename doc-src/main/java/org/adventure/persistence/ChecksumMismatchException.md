# ChecksumMismatchException

**Package:** `org.adventure.persistence`  
**Type:** Custom Exception (extends `IOException`)  
**Phase:** 1.8 (Persistence & Save/Load)

---

## Overview

`ChecksumMismatchException` is thrown when a loaded save file's checksum doesn't match the expected value, indicating file corruption, tampering, or incomplete writes. This exception triggers automatic backup recovery in `SaveManager`.

**Use Cases:**
- Detect bit-flip errors (hardware failure)
- Prevent loading corrupted saves (crash prevention)
- Trigger backup restore (data recovery)
- Security: Detect manual edits (anti-cheat)

---

## Class Structure

### Inheritance

```
java.lang.Exception
  └── java.io.IOException
        └── ChecksumMismatchException
```

**Why extend IOException?**
- Checksum errors are file integrity issues
- Consistent with `save()` / `load()` method signatures
- Standard Java convention for file-related errors

---

## Constructors

### `ChecksumMismatchException(String message)`

**Purpose:** Create exception with descriptive error message.

**Parameters:**
- `message` — Human-readable error description

**Example:**
```java
throw new ChecksumMismatchException(
    "Checksum mismatch for world.json: expected ABC123, got DEF456"
);
```

---

### `ChecksumMismatchException(String message, Throwable cause)`

**Purpose:** Create exception with error message and underlying cause.

**Parameters:**
- `message` — Error description
- `cause` — Underlying exception (e.g., `FileNotFoundException`)

**Example:**
```java
try {
    String checksum = readChecksumFile(file);
} catch (IOException e) {
    throw new ChecksumMismatchException("Failed to read checksum file", e);
}
```

---

## Usage Patterns

### 1. Detection (SaveManager)

**When Thrown:**
```java
public <T> T load(String filename, Class<T> clazz) throws IOException {
    File file = saveDirectory.resolve(filename).toFile();
    File checksumFile = new File(file.getAbsolutePath() + CHECKSUM_SUFFIX);
    
    // Read expected checksum
    String expectedChecksum = Files.readString(checksumFile.toPath()).trim();
    
    // Calculate actual checksum
    String actualChecksum = calculateChecksum(file);
    
    // Compare
    if (!expectedChecksum.equals(actualChecksum)) {
        throw new ChecksumMismatchException(
            "Checksum mismatch for " + filename + 
            ": expected " + expectedChecksum + ", got " + actualChecksum
        );
    }
    
    // ... deserialize object ...
}
```

---

### 2. Recovery (Automatic Backup Restore)

**Catch and Recover:**
```java
public <T> T loadWithBackupFallback(String filename, Class<T> clazz) throws IOException {
    try {
        return load(filename, clazz);
    } catch (ChecksumMismatchException e) {
        System.err.println("Corruption detected: " + e.getMessage());
        System.out.println("Attempting backup restore...");
        
        // Get most recent backup
        File backup = backupManager.getMostRecentBackup(filename);
        if (backup == null) {
            throw new IOException("No valid backup available", e);
        }
        
        // Restore backup
        Path targetPath = saveDirectory.resolve(filename);
        Files.copy(backup.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        // Recalculate checksum
        String newChecksum = calculateChecksum(targetPath.toFile());
        Files.writeString(
            Paths.get(targetPath + CHECKSUM_SUFFIX), 
            newChecksum
        );
        
        // Retry load
        return load(filename, clazz);
    }
}
```

---

### 3. User Notification

**Inform User:**
```java
public void loadGame() {
    SaveManager saveManager = new SaveManager("saves/");
    
    try {
        WorldData world = saveManager.load("world.json", WorldData.class);
        System.out.println("World loaded successfully");
    } catch (ChecksumMismatchException e) {
        System.err.println("ERROR: Save file corrupted!");
        System.err.println("Details: " + e.getMessage());
        System.out.println("Attempting recovery from backup...");
        
        try {
            WorldData world = saveManager.loadWithBackupFallback("world.json", WorldData.class);
            System.out.println("Recovery successful!");
        } catch (IOException ex) {
            System.err.println("FATAL: No valid backups available");
            System.err.println("Please regenerate world or restore from external backup");
        }
    } catch (IOException e) {
        System.err.println("ERROR: Failed to load save file");
        e.printStackTrace();
    }
}
```

---

## Error Messages

### Format

**Template:**
```
Checksum mismatch for <filename>: expected <expected>, got <actual>
```

**Example:**
```
Checksum mismatch for world.json: 
expected rTmQp9F3K8J2L5M7N1O9P3Q6R8S0T4U2V7W9X1Y5Z3A==, 
got 8X4W2V0T6S4R2Q0P9N7M5L3K1J9H7G5F3E1D9C7B5A3Z==
```

### Interpretation

**Expected != Actual → Corruption**
- Bit flip (hardware error)
- Incomplete write (crash during save)
- Manual edit (cheating)
- Disk error (bad sector)

**Checksum File Missing → Warning (not error)**
```java
if (!checksumFile.exists()) {
    System.out.println("Warning: No checksum file, skipping validation");
    // Load without validation
}
```

---

## Checksum Algorithm

### SHA-256

**Why SHA-256?**
- Cryptographically secure (collision-resistant)
- Fast enough for save files (~5ms for 2 MB)
- Standard Java library support
- Prevents tampering (intentional edits)

**Implementation:**
```java
private String calculateChecksum(File file) throws IOException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] fileBytes = Files.readAllBytes(file.toPath());
    byte[] hashBytes = digest.digest(fileBytes);
    return Base64.getEncoder().encodeToString(hashBytes);
}
```

**Example Checksums:**
```
world.json (2 MB):        rTmQp9F3K8J2L5M7N1O9P3Q6R8S0T4U2V7W9X1Y5Z3A==
character.json (50 KB):   7B5D9F1H3J5L7N9P1R3T5V7X9Z1A3C5E7G9I1K3M5O==
```

---

## Testing

### Test Coverage

**Tests:** 2/2 in `PersistenceTest.java`

**1. `testChecksumValidation()`**
- Save file → checksum created
- Load file → checksum validated
- No exception thrown

**2. `testChecksumMismatch()`**
- Save file with valid checksum
- Manually corrupt file (modify bytes)
- Load file → `ChecksumMismatchException` thrown

**Test Code:**
```java
@Test
public void testChecksumMismatch() throws IOException {
    SaveManager saveManager = new SaveManager(tempDir.toString());
    
    // Save valid file
    TestObject obj = new TestObject("test");
    saveManager.save(obj, "test.json");
    
    // Corrupt file
    Path filePath = tempDir.resolve("test.json");
    Files.writeString(filePath, "CORRUPTED DATA");
    
    // Attempt load
    assertThrows(ChecksumMismatchException.class, () -> {
        saveManager.load("test.json", TestObject.class);
    });
}
```

---

## Error Handling Strategies

### 1. Automatic Recovery (Recommended)

**Strategy:** Always use `loadWithBackupFallback()` for critical saves.

```java
try {
    world = saveManager.loadWithBackupFallback("world.json", WorldData.class);
} catch (IOException e) {
    // No valid save or backup → regenerate
    world = WorldGen.generate(defaultSeed);
}
```

**Pros:**
- Zero user intervention
- Data loss minimized

**Cons:**
- May restore old version (lose progress)

---

### 2. User Choice (Interactive)

**Strategy:** Ask user to choose backup or regenerate.

```java
try {
    world = saveManager.load("world.json", WorldData.class);
} catch (ChecksumMismatchException e) {
    System.out.println("Save file corrupted. Options:");
    System.out.println("1. Restore from backup (may lose progress)");
    System.out.println("2. Regenerate world (lose all progress)");
    System.out.println("3. Exit");
    
    int choice = getUserInput();
    if (choice == 1) {
        world = saveManager.loadWithBackupFallback("world.json", WorldData.class);
    } else if (choice == 2) {
        world = WorldGen.generate(defaultSeed);
    } else {
        System.exit(1);
    }
}
```

**Pros:**
- User control

**Cons:**
- Requires UI
- Breaks automation

---

### 3. Logging Only (Non-Critical Saves)

**Strategy:** Log error, continue with default values.

```java
try {
    settings = saveManager.load("settings.json", Settings.class);
} catch (ChecksumMismatchException e) {
    logger.warn("Settings corrupted, using defaults: " + e.getMessage());
    settings = new Settings(); // Default settings
}
```

**Use Case:** User preferences, UI state (non-critical data)

---

## Security Implications

### Anti-Cheat

**Detection:**
```java
try {
    Character character = saveManager.load("player.json", Character.class);
} catch (ChecksumMismatchException e) {
    // Player manually edited save file (cheating)
    System.err.println("CHEATING DETECTED: Save file modified");
    // Ban player or reset to backup
}
```

**Limitations:**
- Checksums can be recalculated by cheaters
- Use encryption (Phase 2) for stronger protection

---

### Data Integrity

**Tamper Detection:**
- File edited with text editor → checksum mismatch
- Prevents accidental/malicious changes
- Preserves game balance (no stat editing)

**Example:**
```
Original: {"gold": 100}  → Checksum: ABC123
Edited:   {"gold": 9999} → Checksum: DEF456 → MISMATCH!
```

---

## Design Decisions

### 1. Why custom exception (not generic IOException)?
**Decision:** Dedicated `ChecksumMismatchException` class  
**Rationale:**
- Specific error handling (backup restore)
- Clear intent in logs
- Easy to catch separately

### 2. Why include expected/actual checksums?
**Decision:** Detailed error message  
**Rationale:**
- Debugging (compare checksums)
- Audit logs (track corruption patterns)

### 3. Why IOException (not RuntimeException)?
**Decision:** Checked exception  
**Rationale:**
- Forces explicit error handling
- Consistent with file I/O methods
- Prevents silent failures

---

## Future Enhancements

### Phase 2 Features

**1. Corruption Metadata**
```java
public class ChecksumMismatchException extends IOException {
    private final String filename;
    private final String expectedChecksum;
    private final String actualChecksum;
    
    // Getters for metadata
}
```

**2. Auto-Repair**
```java
// Attempt partial recovery (parse valid JSON sections)
public <T> T loadWithRepair(String filename, Class<T> clazz)
```

**3. Detailed Diagnostics**
```java
public CorruptionReport diagnoseCorruption(File file) {
    // Return: corruption type, affected bytes, recovery options
}
```

---

## Related Classes

- `SaveManager` — Throws and catches this exception
- `BackupManager` — Used for recovery after checksum mismatch
- `SchemaVersionManager` — No direct interaction (version != checksum)

---

## References

- Design: `docs/persistence_versioning.md` → Checksum Validation
- Phase Summary: `archive/PHASE_1.8_SUMMARY.md`
- Tests: `src/test/java/org/adventure/PersistenceTest.java`

---

**Last Updated:** November 13, 2025  
**Status:** ✅ Complete (Phase 1.8)
