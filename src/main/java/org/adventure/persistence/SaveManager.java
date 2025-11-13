package org.adventure.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

/**
 * Main entry point for save/load operations.
 * Coordinates serialization, backup management, and checksum validation.
 * 
 * <p>Features:
 * <ul>
 *   <li>Atomic writes (temp file → rename)</li>
 *   <li>Automatic backup rotation (N=5 by default)</li>
 *   <li>SHA-256 checksum validation</li>
 *   <li>Schema version tracking</li>
 * </ul>
 * 
 * <p>Design: docs/persistence_versioning.md
 * <p>Specs: docs/specs_summary.md → Persistence Format & Migration
 * 
 * @see BackupManager
 * @see SchemaVersionManager
 */
public class SaveManager {
    
    private final ObjectMapper objectMapper;
    private final BackupManager backupManager;
    private final SchemaVersionManager schemaVersionManager;
    private final Path saveDirectory;
    
    private static final String TEMP_SUFFIX = ".tmp";
    private static final String CHECKSUM_SUFFIX = ".checksum";
    
    /**
     * Create SaveManager with default configuration.
     * 
     * @param saveDirectory Directory where save files are stored
     */
    public SaveManager(String saveDirectory) {
        this(saveDirectory, 5); // Default: keep 5 backups
    }
    
    /**
     * Create SaveManager with custom backup count.
     * 
     * @param saveDirectory Directory where save files are stored
     * @param maxBackups Maximum number of backups to retain
     */
    public SaveManager(String saveDirectory, int maxBackups) {
        this.saveDirectory = Paths.get(saveDirectory);
        this.objectMapper = createObjectMapper();
        this.backupManager = new BackupManager(saveDirectory, maxBackups);
        this.schemaVersionManager = new SchemaVersionManager();
        
        // Create save directory if not exists
        try {
            Files.createDirectories(this.saveDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create save directory: " + saveDirectory, e);
        }
    }
    
    /**
     * Configure Jackson ObjectMapper for JSON serialization.
     */
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty-print for human readability
        mapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS); // Deterministic ordering
        return mapper;
    }
    
    /**
     * Save object to file with atomic write and checksum.
     * 
     * @param object Object to save
     * @param filename Target filename (without path)
     * @throws IOException If save fails
     */
    public void save(Object object, String filename) throws IOException {
        Path targetPath = saveDirectory.resolve(filename);
        Path tempPath = saveDirectory.resolve(filename + TEMP_SUFFIX);
        
        // Backup existing file before overwriting
        if (Files.exists(targetPath)) {
            backupManager.createBackup(targetPath.toFile());
        }
        
        // Write to temp file
        objectMapper.writeValue(tempPath.toFile(), object);
        
        // Calculate checksum
        String checksum = calculateChecksum(tempPath.toFile());
        Path checksumPath = saveDirectory.resolve(filename + CHECKSUM_SUFFIX);
        Files.writeString(checksumPath, checksum);
        
        // Atomic rename (replaces existing file)
        Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }
    
    /**
     * Load object from file with checksum validation.
     * 
     * @param filename Filename to load (without path)
     * @param clazz Target class type
     * @param <T> Type parameter
     * @return Loaded object
     * @throws IOException If load fails or checksum mismatch
     */
    public <T> T load(String filename, Class<T> clazz) throws IOException {
        Path targetPath = saveDirectory.resolve(filename);
        Path checksumPath = saveDirectory.resolve(filename + CHECKSUM_SUFFIX);
        
        if (!Files.exists(targetPath)) {
            throw new IOException("Save file not found: " + filename);
        }
        
        // Validate checksum if exists
        if (Files.exists(checksumPath)) {
            String expectedChecksum = Files.readString(checksumPath).trim();
            String actualChecksum = calculateChecksum(targetPath.toFile());
            
            if (!expectedChecksum.equals(actualChecksum)) {
                // Checksum mismatch - attempt backup restore
                throw new ChecksumMismatchException(
                    "Checksum mismatch for " + filename + 
                    " (expected: " + expectedChecksum + ", actual: " + actualChecksum + ")"
                );
            }
        }
        
        // Load and return
        return objectMapper.readValue(targetPath.toFile(), clazz);
    }
    
    /**
     * Load object with automatic backup restore on corruption.
     * 
     * @param filename Filename to load
     * @param clazz Target class type
     * @param <T> Type parameter
     * @return Loaded object (may be from backup)
     * @throws IOException If load fails and all backups corrupted
     */
    public <T> T loadWithBackupFallback(String filename, Class<T> clazz) throws IOException {
        try {
            return load(filename, clazz);
        } catch (ChecksumMismatchException e) {
            // Try to restore from backup
            File backup = backupManager.getMostRecentBackup(filename);
            if (backup != null && backup.exists()) {
                Path targetPath = saveDirectory.resolve(filename);
                Files.copy(backup.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                
                // Recalculate checksum for restored backup
                String checksum = calculateChecksum(targetPath.toFile());
                Path checksumPath = saveDirectory.resolve(filename + CHECKSUM_SUFFIX);
                Files.writeString(checksumPath, checksum);
                
                // Retry load
                return load(filename, clazz);
            } else {
                throw new IOException("No valid backup found for corrupted file: " + filename, e);
            }
        }
    }
    
    /**
     * Calculate SHA-256 checksum for file.
     * 
     * @param file File to checksum
     * @return Base64-encoded checksum
     * @throws IOException If file cannot be read
     */
    private String calculateChecksum(File file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            byte[] hash = digest.digest(fileBytes);
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    /**
     * Delete save file and its backups.
     * 
     * @param filename Filename to delete
     * @throws IOException If deletion fails
     */
    public void delete(String filename) throws IOException {
        Path targetPath = saveDirectory.resolve(filename);
        Path checksumPath = saveDirectory.resolve(filename + CHECKSUM_SUFFIX);
        
        Files.deleteIfExists(targetPath);
        Files.deleteIfExists(checksumPath);
        
        backupManager.deleteBackups(filename);
    }
    
    /**
     * Check if save file exists.
     * 
     * @param filename Filename to check
     * @return true if file exists
     */
    public boolean exists(String filename) {
        return Files.exists(saveDirectory.resolve(filename));
    }
    
    /**
     * Get ObjectMapper for direct serialization (advanced use).
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
    
    /**
     * Get BackupManager for manual backup operations.
     */
    public BackupManager getBackupManager() {
        return backupManager;
    }
    
    /**
     * Get SchemaVersionManager for migration operations.
     */
    public SchemaVersionManager getSchemaVersionManager() {
        return schemaVersionManager;
    }
}
