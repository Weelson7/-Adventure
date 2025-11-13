package org.adventure.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.yaml.snakeyaml.Yaml;

/**
 * Manages schema versioning and migration scripts.
 * Loads migration registry from YAML and validates version compatibility.
 * 
 * <p>Migration Registry Format (YAML):
 * <pre>{@code
 * migrations:
 *   - module: world/Chunk
 *     from: 1
 *     to: 2
 *     script: migrations/world/chunk_v1_to_v2.py
 *     isBreaking: false
 * }</pre>
 * 
 * <p>Design: docs/persistence_versioning.md → Migration Guidance
 * <p>Specs: docs/specs_summary.md → Persistence Format & Migration
 * 
 * @see SaveManager
 */
public class SchemaVersionManager {
    
    private final Map<String, List<MigrationStep>> migrations;
    private static final String DEFAULT_REGISTRY_PATH = "migrations/registry.yml";
    
    /**
     * Current schema versions for all modules (canonical source).
     */
    public static final Map<String, Integer> CURRENT_VERSIONS = Map.ofEntries(
        Map.entry("world/WorldGrid", 1),
        Map.entry("world/Chunk", 1),
        Map.entry("world/Tile", 1),
        Map.entry("world/Plate", 1),
        Map.entry("world/Biome", 1),
        Map.entry("world/River", 1),
        Map.entry("world/RegionalFeature", 1),
        Map.entry("region/Region", 1),
        Map.entry("region/ResourceNode", 1),
        Map.entry("character/Character", 1),
        Map.entry("character/NPC", 1),
        Map.entry("character/Trait", 1),
        Map.entry("character/Skill", 1),
        Map.entry("character/Race", 1),
        Map.entry("items/Item", 1),
        Map.entry("items/ItemPrototype", 1),
        Map.entry("crafting/CraftingRecipe", 1),
        Map.entry("crafting/CraftingProficiency", 1),
        Map.entry("structure/Structure", 1),
        Map.entry("structure/TaxRecord", 1),
        Map.entry("structure/TransferRecord", 1),
        Map.entry("structure/ContestedOwnership", 1),
        Map.entry("structure/Room", 1),
        Map.entry("structure/Upgrade", 1),
        Map.entry("society/Clan", 1),
        Map.entry("society/RelationshipRecord", 1),
        Map.entry("story/Story", 1),
        Map.entry("story/Event", 1)
    );
    
    /**
     * Create SchemaVersionManager with default registry.
     */
    public SchemaVersionManager() {
        this.migrations = new HashMap<>();
        loadDefaultMigrations();
    }
    
    /**
     * Create SchemaVersionManager with custom registry file.
     * 
     * @param registryPath Path to YAML migration registry
     * @throws IOException If registry cannot be loaded
     */
    public SchemaVersionManager(String registryPath) throws IOException {
        this.migrations = new HashMap<>();
        loadMigrationsFromFile(Paths.get(registryPath));
    }
    
    /**
     * Load default migrations (empty for Phase 1.8, populated in future).
     */
    private void loadDefaultMigrations() {
        // Phase 1.8: No migrations yet (all at v1)
        // Future phases will add migration steps here
    }
    
    /**
     * Load migrations from YAML file.
     * 
     * @param path Path to registry file
     * @throws IOException If file cannot be read
     */
    @SuppressWarnings("unchecked")
    private void loadMigrationsFromFile(Path path) throws IOException {
        if (!Files.exists(path)) {
            // No registry file → no migrations (valid for fresh installs)
            return;
        }
        
        Yaml yaml = new Yaml();
        try (InputStream input = Files.newInputStream(path)) {
            Map<String, Object> data = yaml.load(input);
            if (data == null || !data.containsKey("migrations")) {
                return;
            }
            
            List<Map<String, Object>> migrationList = 
                (List<Map<String, Object>>) data.get("migrations");
            
            for (Map<String, Object> migrationData : migrationList) {
                MigrationStep step = parseMigrationStep(migrationData);
                migrations.computeIfAbsent(step.module, k -> new ArrayList<>()).add(step);
            }
            
            // Sort migrations by version order
            for (List<MigrationStep> steps : migrations.values()) {
                steps.sort(Comparator.comparingInt(s -> s.fromVersion));
            }
        }
    }
    
    /**
     * Parse migration step from YAML map.
     */
    @SuppressWarnings("unchecked")
    private MigrationStep parseMigrationStep(Map<String, Object> data) {
        String module = (String) data.get("module");
        int from = (Integer) data.get("from");
        int to = (Integer) data.get("to");
        String script = (String) data.get("script");
        boolean isBreaking = (Boolean) data.getOrDefault("isBreaking", false);
        
        return new MigrationStep(module, from, to, script, isBreaking);
    }
    
    /**
     * Check if object version is current.
     * 
     * @param module Module identifier (e.g., "world/Chunk")
     * @param version Object schema version
     * @return true if version matches current
     */
    public boolean isCurrent(String module, int version) {
        Integer currentVersion = CURRENT_VERSIONS.get(module);
        if (currentVersion == null) {
            throw new IllegalArgumentException("Unknown module: " + module);
        }
        return version == currentVersion;
    }
    
    /**
     * Get migration path from old version to current.
     * 
     * @param module Module identifier
     * @param fromVersion Starting version
     * @return Ordered list of migration steps (empty if already current)
     * @throws IllegalArgumentException If no migration path exists
     */
    public List<MigrationStep> getMigrationPath(String module, int fromVersion) {
        Integer currentVersion = CURRENT_VERSIONS.get(module);
        if (currentVersion == null) {
            throw new IllegalArgumentException("Unknown module: " + module);
        }
        
        if (fromVersion == currentVersion) {
            return List.of(); // Already current
        }
        
        if (fromVersion > currentVersion) {
            throw new IllegalArgumentException(
                "Version " + fromVersion + " is newer than current " + currentVersion + 
                " for module " + module
            );
        }
        
        // Build migration path
        List<MigrationStep> path = new ArrayList<>();
        List<MigrationStep> moduleSteps = migrations.getOrDefault(module, List.of());
        
        int version = fromVersion;
        while (version < currentVersion) {
            int finalVersion = version;
            MigrationStep step = moduleSteps.stream()
                .filter(s -> s.fromVersion == finalVersion)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    "No migration from version " + finalVersion + " for module " + module
                ));
            
            path.add(step);
            version = step.toVersion;
        }
        
        return path;
    }
    
    /**
     * Get current version for module.
     * 
     * @param module Module identifier
     * @return Current schema version
     */
    public int getCurrentVersion(String module) {
        Integer version = CURRENT_VERSIONS.get(module);
        if (version == null) {
            throw new IllegalArgumentException("Unknown module: " + module);
        }
        return version;
    }
    
    /**
     * Represents a single migration step.
     */
    public static class MigrationStep {
        public final String module;
        public final int fromVersion;
        public final int toVersion;
        public final String scriptPath;
        public final boolean isBreaking;
        
        public MigrationStep(String module, int fromVersion, int toVersion, 
                           String scriptPath, boolean isBreaking) {
            this.module = module;
            this.fromVersion = fromVersion;
            this.toVersion = toVersion;
            this.scriptPath = scriptPath;
            this.isBreaking = isBreaking;
        }
        
        @Override
        public String toString() {
            return module + " v" + fromVersion + "→v" + toVersion + 
                   (isBreaking ? " (BREAKING)" : "");
        }
    }
}
