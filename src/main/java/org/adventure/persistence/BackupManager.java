package org.adventure.persistence;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Manages backup rotation for save files.
 * Keeps N most recent backups and automatically prunes old ones.
 * 
 * <p>Backup naming: {@code filename.backup.YYYYMMDD_HHmmss}
 * 
 * <p>Design: docs/persistence_versioning.md → Backup Rotation Policy
 * 
 * @see SaveManager
 */
public class BackupManager {
    
    private final Path backupDirectory;
    private final int maxBackups;
    
    private static final String BACKUP_SUFFIX = ".backup.";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = 
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").withZone(ZoneOffset.UTC);
    
    /**
     * Create BackupManager.
     * 
     * @param saveDirectory Base save directory
     * @param maxBackups Maximum number of backups to keep per file
     */
    public BackupManager(String saveDirectory, int maxBackups) {
        if (maxBackups < 1) {
            throw new IllegalArgumentException("maxBackups must be at least 1");
        }
        
        this.backupDirectory = Paths.get(saveDirectory, "backups");
        this.maxBackups = maxBackups;
        
        // Create backup directory if not exists
        try {
            Files.createDirectories(backupDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create backup directory: " + backupDirectory, e);
        }
    }
    
    /**
     * Create a timestamped backup of the given file.
     * Automatically prunes old backups if count exceeds maxBackups.
     * 
     * @param sourceFile File to backup
     * @throws IOException If backup fails
     */
    public void createBackup(File sourceFile) throws IOException {
        if (!sourceFile.exists()) {
            return; // Nothing to backup
        }
        
        String filename = sourceFile.getName();
        String timestamp = TIMESTAMP_FORMAT.format(Instant.now());
        String backupFilename = filename + BACKUP_SUFFIX + timestamp;
        
        Path backupPath = backupDirectory.resolve(backupFilename);
        Files.copy(sourceFile.toPath(), backupPath, StandardCopyOption.REPLACE_EXISTING);
        
        // Prune old backups
        pruneOldBackups(filename);
    }
    
    /**
     * Delete backups older than maxBackups limit.
     * 
     * @param filename Base filename (without backup suffix)
     * @throws IOException If pruning fails
     */
    private void pruneOldBackups(String filename) throws IOException {
        List<File> backups = getBackups(filename);
        
        if (backups.size() > maxBackups) {
            // Sort by timestamp (oldest first)
            backups.sort(Comparator.comparing(File::getName));
            
            // Delete oldest backups
            int toDelete = backups.size() - maxBackups;
            for (int i = 0; i < toDelete; i++) {
                Files.deleteIfExists(backups.get(i).toPath());
            }
        }
    }
    
    /**
     * Get all backups for a given filename.
     * 
     * @param filename Base filename (without backup suffix)
     * @return List of backup files (sorted newest first)
     */
    public List<File> getBackups(String filename) {
        File[] files = backupDirectory.toFile().listFiles();
        if (files == null) {
            return new ArrayList<>();
        }
        
        String prefix = filename + BACKUP_SUFFIX;
        List<File> backups = new ArrayList<>();
        for (File file : files) {
            if (file.getName().startsWith(prefix)) {
                backups.add(file);
            }
        }
        
        // Sort newest first
        backups.sort(Comparator.comparing(File::getName).reversed());
        return backups;
    }
    
    /**
     * Get most recent backup for a file.
     * 
     * @param filename Base filename
     * @return Most recent backup, or null if none exist
     */
    public File getMostRecentBackup(String filename) {
        List<File> backups = getBackups(filename);
        return backups.isEmpty() ? null : backups.get(0);
    }
    
    /**
     * Delete all backups for a file.
     * 
     * @param filename Base filename
     * @throws IOException If deletion fails
     */
    public void deleteBackups(String filename) throws IOException {
        List<File> backups = getBackups(filename);
        for (File backup : backups) {
            Files.deleteIfExists(backup.toPath());
        }
    }
    
    /**
     * Get backup directory path.
     */
    public Path getBackupDirectory() {
        return backupDirectory;
    }
    
    /**
     * Get configured max backups.
     */
    public int getMaxBackups() {
        return maxBackups;
    }
}
