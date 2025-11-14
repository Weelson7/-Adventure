package org.adventure.settlement;

import org.adventure.structure.Structure;
import org.adventure.structure.StructureType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Detects and manages villages/cities from structure clusters.
 * Uses DBSCAN-like clustering algorithm to identify settlements.
 * Tracks village growth and promotes villages to towns/cities.
 * 
 * Village criteria:
 * - VILLAGE: 3-14 structures within 10-tile radius
 * - TOWN: 15-29 structures OR has MARKET
 * - CITY: 30+ structures OR (20+ structures + 50+ NPCs + TEMPLE/GUILD_HALL)
 * 
 * Design: BUILD_PHASE1.10.x.md → Phase 1.10.2
 */
public class VillageManager {
    private static final int VILLAGE_RADIUS = 10;
    private static final int CITY_RADIUS = 20;
    private static final int MIN_STRUCTURES_VILLAGE = 3;
    private static final int MIN_STRUCTURES_TOWN = 15;
    private static final int MIN_STRUCTURES_CITY = 30;
    private static final int MIN_STRUCTURES_CITY_SPECIAL = 20;
    private static final int MIN_POPULATION_CITY = 50;
    
    /**
     * Detect all villages from the given structures using DBSCAN clustering.
     * 
     * @param structures All structures in the region
     * @return List of detected villages
     */
    public List<Village> detectVillages(List<Structure> structures) {
        if (structures == null || structures.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Create spatial index (simple grid-based for now)
        Map<String, Structure> structureMap = structures.stream()
                .collect(Collectors.toMap(Structure::getId, s -> s));
        
        Set<String> visited = new HashSet<>();
        List<Village> villages = new ArrayList<>();
        int villageCounter = 1;
        
        for (Structure structure : structures) {
            if (visited.contains(structure.getId())) {
                continue;
            }
            
            // Find neighbors within radius
            List<Structure> cluster = findCluster(structure, structures, visited, VILLAGE_RADIUS);
            
            if (cluster.size() >= MIN_STRUCTURES_VILLAGE) {
                // Calculate center
                int centerX = (int) cluster.stream()
                        .mapToInt(s -> parseX(s.getLocationTileId()))
                        .average()
                        .orElse(0);
                int centerY = (int) cluster.stream()
                        .mapToInt(s -> parseY(s.getLocationTileId()))
                        .average()
                        .orElse(0);
                
                // Classify village type
                VillageType type = classifyVillage(cluster, 0); // TODO: get population from NPCs
                
                // Generate name
                String name = generateVillageName(villageCounter, type);
                
                // Find governing clan (most common clan among structures)
                String governingClan = findGoverningClan(cluster);
                
                // Create village
                Village village = new Village.Builder()
                        .id("village_" + villageCounter++)
                        .name(name)
                        .type(type)
                        .centerX(centerX)
                        .centerY(centerY)
                        .structureIds(cluster.stream().map(Structure::getId).collect(Collectors.toList()))
                        .population(0) // TODO: calculate from NPCs
                        .governingClanId(governingClan)
                        .foundedTick(0)
                        .build();
                
                villages.add(village);
            }
        }
        
        return villages;
    }
    
    /**
     * Update village status and check for promotions.
     * 
     * @param village Village to update
     * @param structures All structures
     * @param currentTick Current game tick
     * @return true if village was promoted
     */
    public boolean updateVillageStatus(Village village, List<Structure> structures, long currentTick) {
        if (village == null) {
            return false;
        }
        
        // Get current structures
        List<Structure> villageStructures = structures.stream()
                .filter(s -> village.getStructureIds().contains(s.getId()))
                .collect(Collectors.toList());
        
        VillageType oldType = village.getType();
        VillageType newType = classifyVillage(villageStructures, village.getPopulation());
        
        if (newType != oldType) {
            village.setType(newType);
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if village should be promoted to city.
     * 
     * @param village Village to check
     * @return true if village meets city criteria
     */
    public boolean shouldPromoteToCity(Village village) {
        return village.getType() == VillageType.CITY;
    }
    
    // Helper methods
    
    private List<Structure> findCluster(Structure start, List<Structure> allStructures, 
                                       Set<String> visited, int radius) {
        List<Structure> cluster = new ArrayList<>();
        Queue<Structure> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start.getId());
        
        while (!queue.isEmpty()) {
            Structure current = queue.poll();
            cluster.add(current);
            
            int currentX = parseX(current.getLocationTileId());
            int currentY = parseY(current.getLocationTileId());
            
            for (Structure neighbor : allStructures) {
                if (visited.contains(neighbor.getId())) {
                    continue;
                }
                
                int neighborX = parseX(neighbor.getLocationTileId());
                int neighborY = parseY(neighbor.getLocationTileId());
                
                double distance = Math.sqrt(
                        Math.pow(currentX - neighborX, 2) + 
                        Math.pow(currentY - neighborY, 2));
                
                if (distance <= radius) {
                    visited.add(neighbor.getId());
                    queue.add(neighbor);
                }
            }
        }
        
        return cluster;
    }
    
    private VillageType classifyVillage(List<Structure> structures, int population) {
        int structureCount = structures.size();
        
        // Check for CITY criteria
        if (structureCount >= MIN_STRUCTURES_CITY) {
            return VillageType.CITY;
        }
        
        // Check for CITY with special buildings
        if (structureCount >= MIN_STRUCTURES_CITY_SPECIAL && 
            population >= MIN_POPULATION_CITY &&
            hasSpecialBuilding(structures)) {
            return VillageType.CITY;
        }
        
        // Check for TOWN criteria
        if (structureCount >= MIN_STRUCTURES_TOWN) {
            return VillageType.TOWN;
        }
        
        // Check if has MARKET (automatic town promotion)
        if (hasMarket(structures)) {
            return VillageType.TOWN;
        }
        
        // Default to VILLAGE
        return VillageType.VILLAGE;
    }
    
    private boolean hasSpecialBuilding(List<Structure> structures) {
        return structures.stream()
                .anyMatch(s -> s.getType() == StructureType.TEMPLE || 
                             s.getType() == StructureType.GUILD_HALL);
    }
    
    private boolean hasMarket(List<Structure> structures) {
        return structures.stream()
                .anyMatch(s -> s.getType() == StructureType.MARKET);
    }
    
    private String findGoverningClan(List<Structure> structures) {
        // Find most common clan owner
        Map<String, Long> clanCounts = structures.stream()
                .map(Structure::getOwnerId)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(id -> id, Collectors.counting()));
        
        return clanCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
    
    private String generateVillageName(int counter, VillageType type) {
        String[] prefixes = {"Meadow", "Stone", "River", "Oak", "Silver", "Gold", "Iron", "Copper"};
        String[] suffixes = {"dale", "field", "brook", "wood", "vale", "haven", "crest", "ton"};
        
        Random rng = new Random(counter);
        String prefix = prefixes[rng.nextInt(prefixes.length)];
        String suffix = suffixes[rng.nextInt(suffixes.length)];
        
        return prefix + suffix;
    }
    
    private int parseX(String locationTileId) {
        // Assuming format "x_y" or similar
        // TODO: implement proper tile ID parsing based on actual format
        try {
            String[] parts = locationTileId.split("_");
            return Integer.parseInt(parts[0]);
        } catch (Exception e) {
            return 0;
        }
    }
    
    private int parseY(String locationTileId) {
        // Assuming format "x_y" or similar
        // TODO: implement proper tile ID parsing based on actual format
        try {
            String[] parts = locationTileId.split("_");
            return Integer.parseInt(parts[1]);
        } catch (Exception e) {
            return 0;
        }
    }
}
