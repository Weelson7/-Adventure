package org.adventure;

import org.adventure.persistence.BackupManager;
import org.adventure.persistence.ChecksumMismatchException;
import org.adventure.persistence.SaveManager;
import org.adventure.persistence.SchemaVersionManager;
import org.adventure.persistence.SchemaVersionManager.MigrationStep;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for persistence system (Phase 1.8).
 * Covers save/load, backup rotation, checksum validation, schema versioning.
 */
public class PersistenceTest {
    
    @TempDir
    Path tempDir;
    
    private SaveManager saveManager;
    
    @BeforeEach
    void setUp() {
        saveManager = new SaveManager(tempDir.toString(), 3); // Keep 3 backups for testing
    }
    
    @AfterEach
    void tearDown() {
        // Cleanup handled by @TempDir
    }
    
    // ==================== SaveManager Tests ====================
    
    @Test
    void testSaveAndLoad() throws IOException {
        TestData data = new TestData("test-id", "test-value", 42);
        
        saveManager.save(data, "test.json");
        
        TestData loaded = saveManager.load("test.json", TestData.class);
        
        assertEquals(data.id, loaded.id);
        assertEquals(data.value, loaded.value);
        assertEquals(data.number, loaded.number);
    }
    
    @Test
    void testChecksumValidation() throws IOException {
        TestData data = new TestData("id-1", "value-1", 100);
        
        saveManager.save(data, "checksum.json");
        
        // File should have accompanying checksum file
        Path checksumPath = tempDir.resolve("checksum.json.checksum");
        assertTrue(Files.exists(checksumPath));
        
        // Load should succeed with valid checksum
        TestData loaded = saveManager.load("checksum.json", TestData.class);
        assertNotNull(loaded);
    }
    
    @Test
    void testChecksumMismatch() throws IOException {
        TestData data = new TestData("id-2", "value-2", 200);
        
        saveManager.save(data, "corrupted.json");
        
        // Corrupt the file by modifying it
        Path targetPath = tempDir.resolve("corrupted.json");
        String content = Files.readString(targetPath);
        Files.writeString(targetPath, content + "\n// corrupted");
        
        // Load should throw ChecksumMismatchException
        assertThrows(ChecksumMismatchException.class, () -> {
            saveManager.load("corrupted.json", TestData.class);
        });
    }
    
    @Test
    void testLoadWithBackupFallback() throws IOException {
        TestData data = new TestData("id-3", "value-3", 300);
        
        // Save original
        saveManager.save(data, "fallback.json");
        
        // Modify data and save again (creates backup)
        data.value = "value-3-modified";
        saveManager.save(data, "fallback.json");
        
        // Corrupt current file
        Path targetPath = tempDir.resolve("fallback.json");
        String content = Files.readString(targetPath);
        Files.writeString(targetPath, content + "\n// corrupted");
        
        // Load with fallback should restore from backup
        TestData loaded = saveManager.loadWithBackupFallback("fallback.json", TestData.class);
        assertNotNull(loaded);
    }
    
    @Test
    void testAtomicWrite() throws IOException {
        TestData data = new TestData("id-4", "value-4", 400);
        
        saveManager.save(data, "atomic.json");
        
        // Temp file should not exist after save
        Path tempPath = tempDir.resolve("atomic.json.tmp");
        assertFalse(Files.exists(tempPath));
        
        // Target file should exist
        Path targetPath = tempDir.resolve("atomic.json");
        assertTrue(Files.exists(targetPath));
    }
    
    @Test
    void testDeleteSaveFile() throws IOException {
        TestData data = new TestData("id-5", "value-5", 500);
        
        saveManager.save(data, "delete.json");
        assertTrue(saveManager.exists("delete.json"));
        
        saveManager.delete("delete.json");
        assertFalse(saveManager.exists("delete.json"));
        
        // Checksum should also be deleted
        Path checksumPath = tempDir.resolve("delete.json.checksum");
        assertFalse(Files.exists(checksumPath));
    }
    
    // ==================== BackupManager Tests ====================
    
    @Test
    void testBackupCreation() throws IOException {
        BackupManager backupManager = saveManager.getBackupManager();
        
        // Create a file
        Path testFile = tempDir.resolve("backup-test.json");
        Files.writeString(testFile, "{\"test\":\"data\"}");
        
        // Create backup
        backupManager.createBackup(testFile.toFile());
        
        // Verify backup exists
        List<File> backups = backupManager.getBackups("backup-test.json");
        assertEquals(1, backups.size());
    }
    
    @Test
    void testBackupRotation() throws IOException {
        BackupManager backupManager = new BackupManager(tempDir.toString(), 3);
        
        Path testFile = tempDir.resolve("rotation-test.json");
        Files.writeString(testFile, "{\"version\":0}");
        
        // Create 5 backups (should keep only 3)
        for (int i = 0; i < 5; i++) {
            Files.writeString(testFile, "{\"version\":" + i + "}");
            backupManager.createBackup(testFile.toFile());
            try {
                Thread.sleep(50); // Ensure different timestamps
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        List<File> backups = backupManager.getBackups("rotation-test.json");
        assertTrue(backups.size() <= 3, "Should keep at most 3 backups, found: " + backups.size());
    }
    
    @Test
    void testGetMostRecentBackup() throws IOException {
        BackupManager backupManager = saveManager.getBackupManager();
        
        Path testFile = tempDir.resolve("recent-test.json");
        
        // Create multiple backups
        for (int i = 0; i < 3; i++) {
            Files.writeString(testFile, "{\"version\":" + i + "}");
            backupManager.createBackup(testFile.toFile());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        File mostRecent = backupManager.getMostRecentBackup("recent-test.json");
        assertNotNull(mostRecent);
        assertTrue(mostRecent.exists());
    }
    
    @Test
    void testDeleteBackups() throws IOException {
        BackupManager backupManager = saveManager.getBackupManager();
        
        Path testFile = tempDir.resolve("delete-backups.json");
        Files.writeString(testFile, "{\"version\":0}");
        
        // Create backups
        for (int i = 0; i < 3; i++) {
            Files.writeString(testFile, "{\"version\":" + i + "}");
            backupManager.createBackup(testFile.toFile());
        }
        
        assertTrue(backupManager.getBackups("delete-backups.json").size() >= 1);
        
        // Delete all backups
        backupManager.deleteBackups("delete-backups.json");
        
        assertEquals(0, backupManager.getBackups("delete-backups.json").size());
    }
    
    // ==================== SchemaVersionManager Tests ====================
    
    @Test
    void testCurrentVersionCheck() {
        SchemaVersionManager versionManager = new SchemaVersionManager();
        
        assertTrue(versionManager.isCurrent("world/Chunk", 1));
        assertFalse(versionManager.isCurrent("world/Chunk", 0));
    }
    
    @Test
    void testGetCurrentVersion() {
        SchemaVersionManager versionManager = new SchemaVersionManager();
        
        assertEquals(1, versionManager.getCurrentVersion("world/Chunk"));
        assertEquals(1, versionManager.getCurrentVersion("character/Character"));
        assertEquals(1, versionManager.getCurrentVersion("structure/Structure"));
    }
    
    @Test
    void testUnknownModule() {
        SchemaVersionManager versionManager = new SchemaVersionManager();
        
        assertThrows(IllegalArgumentException.class, () -> {
            versionManager.getCurrentVersion("unknown/Module");
        });
    }
    
    @Test
    void testMigrationPathEmpty() {
        SchemaVersionManager versionManager = new SchemaVersionManager();
        
        // No migration needed if already at current version
        List<MigrationStep> path = versionManager.getMigrationPath("world/Chunk", 1);
        assertTrue(path.isEmpty());
    }
    
    @Test
    void testVersionTooNew() {
        SchemaVersionManager versionManager = new SchemaVersionManager();
        
        assertThrows(IllegalArgumentException.class, () -> {
            versionManager.getMigrationPath("world/Chunk", 999);
        });
    }
    
    // ==================== Test Data Class ====================
    
    /**
     * Simple POJO for testing JSON serialization.
     */
    public static class TestData {
        public String id;
        public String value;
        public int number;
        
        // Jackson requires no-arg constructor
        public TestData() {}
        
        public TestData(String id, String value, int number) {
            this.id = id;
            this.value = value;
            this.number = number;
        }
    }
}
