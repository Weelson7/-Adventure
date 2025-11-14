package org.adventure.settlement;

import org.adventure.society.Clan;
import org.adventure.structure.Structure;
import org.adventure.structure.StructureType;
import org.adventure.structure.OwnerType;
import org.adventure.world.Biome;

import java.util.*;

/**
 * Generates initial settlements at worldgen.
 * Each clan gets 1 settlement with clustered structures.
 * 
 * Features:
 * - Deterministic placement from seed
 * - Biome-appropriate naming
 * - Structure clustering (core + residential + commercial)
 * - Minimum 5-tile spacing between structures
 * 
 * Design: BUILD_PHASE1.10.x.md - Phase 1.10.1, deliverable 5
 */
public class SettlementGenerator {
    private static final int MIN_STRUCTURE_SPACING = 5;
    private static final int SETTLEMENT_RADIUS = 20;
    
    // Biome-appropriate name components
    private static final List<String> GRASSLAND_PREFIXES = Arrays.asList(
        "Green", "Sun", "River", "Meadow", "Hill", "Vale", "Oak", "Willow"
    );
    private static final List<String> FOREST_PREFIXES = Arrays.asList(
        "Dark", "Deep", "Elder", "Shadow", "Moss", "Pine", "Cedar", "Ash"
    );
    private static final List<String> DESERT_PREFIXES = Arrays.asList(
        "Sand", "Dune", "Oasis", "Sun", "Mirage", "Gold", "Red", "Amber"
    );
    private static final List<String> MOUNTAIN_PREFIXES = Arrays.asList(
        "High", "Stone", "Peak", "Crag", "Iron", "Silver", "Sky", "Storm"
    );
    private static final List<String> TUNDRA_PREFIXES = Arrays.asList(
        "Frost", "Ice", "Winter", "Snow", "Frozen", "North", "Cold", "White"
    );
    
    private static final List<String> SUFFIXES = Arrays.asList(
        "haven", "ford", "dale", "burg", "ville", "ton", "hold", "rest"
    );
    
    /**
     * Generates initial settlements for all clans.
     * One settlement per clan, placed near clan center with appropriate structures.
     * 
     * @param worldSeed Seed for deterministic generation
     * @param clans List of clans to create settlements for
     * @param biomes World biome map
     * @param worldWidth World width in tiles
     * @param worldHeight World height in tiles
     * @return Map of clanId -> Settlement with generated structures
     */
    public static Map<String, SettlementWithStructures> generateInitialSettlements(
            long worldSeed,
            List<Clan> clans,
            Biome[][] biomes,
            int worldWidth,
            int worldHeight) {
        
        Map<String, SettlementWithStructures> settlements = new HashMap<>();
        Random rng = new Random(worldSeed ^ 0x5E77EEE7L);
        
        for (Clan clan : clans) {
            // Find suitable location for settlement (near clan center)
            int centerX = (int)(clan.getCenterX() * worldWidth);
            int centerY = (int)(clan.getCenterY() * worldHeight);
            
            // Adjust if on unsuitable terrain
            int[] adjustedPos = findSuitableSettlementLocation(
                centerX, centerY, biomes, worldWidth, worldHeight, rng
            );
            centerX = adjustedPos[0];
            centerY = adjustedPos[1];
            
            // Generate settlement name
            Biome biome = biomes[centerY][centerX];
            String name = generateSettlementName(biome, rng);
            
            // Generate structures for this settlement
            List<Structure> structures = generateSettlementStructures(
                clan.getId(),
                centerX,
                centerY,
                biomes,
                worldWidth,
                worldHeight,
                rng
            );
            
            // Create settlement
            List<String> structureIds = new ArrayList<>();
            for (Structure s : structures) {
                structureIds.add(s.getId());
            }
            
            Settlement settlement = new Settlement.Builder()
                .id("settlement_" + clan.getId())
                .name(name)
                .clanId(clan.getId())
                .centerX(centerX)
                .centerY(centerY)
                .type(VillageType.VILLAGE)
                .structureIds(structureIds)
                .foundedTick(0L)
                .schemaVersion(1)
                .build();
            
            settlements.put(clan.getId(), new SettlementWithStructures(settlement, structures));
        }
        
        return settlements;
    }
    
    /**
     * Finds a suitable location for settlement placement.
     * Avoids water and steep mountains.
     */
    private static int[] findSuitableSettlementLocation(
            int startX, int startY,
            Biome[][] biomes,
            int worldWidth, int worldHeight,
            Random rng) {
        
        // Check if starting position is suitable
        if (isSuitableForSettlement(startX, startY, biomes, worldWidth, worldHeight)) {
            return new int[]{startX, startY};
        }
        
        // Search in expanding radius
        for (int radius = 1; radius <= 10; radius++) {
            List<int[]> candidates = new ArrayList<>();
            
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    int x = startX + dx;
                    int y = startY + dy;
                    
                    if (isSuitableForSettlement(x, y, biomes, worldWidth, worldHeight)) {
                        candidates.add(new int[]{x, y});
                    }
                }
            }
            
            if (!candidates.isEmpty()) {
                return candidates.get(rng.nextInt(candidates.size()));
            }
        }
        
        // Fallback: return original position (will be handled by structure placement)
        return new int[]{startX, startY};
    }
    
    /**
     * Checks if a location is suitable for settlement.
     */
    private static boolean isSuitableForSettlement(
            int x, int y,
            Biome[][] biomes,
            int worldWidth, int worldHeight) {
        
        if (x < 0 || x >= worldWidth || y < 0 || y >= worldHeight) {
            return false;
        }
        
        Biome biome = biomes[y][x];
        
        // Avoid water and ice
        return biome != Biome.OCEAN && 
               biome != Biome.DEEP_OCEAN && 
               biome != Biome.ICE && 
               biome != Biome.GLACIER;
    }
    
    /**
     * Generates structures for a settlement.
     * Layout: 1 core (GUILD_HALL/TEMPLE), 3-5 HOUSE, 1 SHOP
     */
    private static List<Structure> generateSettlementStructures(
            String clanId,
            int centerX, int centerY,
            Biome[][] biomes,
            int worldWidth, int worldHeight,
            Random rng) {
        
        List<Structure> structures = new ArrayList<>();
        List<int[]> occupiedPositions = new ArrayList<>();
        
        // 1. Place core structure at center
        Structure core = createStructure(
            "structure_" + clanId + "_core",
            rng.nextBoolean() ? StructureType.GUILD_HALL : StructureType.TEMPLE,
            clanId,
            centerX, centerY
        );
        structures.add(core);
        occupiedPositions.add(new int[]{centerX, centerY});
        
        // 2. Place 3-5 houses in radial pattern
        int houseCount = 3 + rng.nextInt(3); // 3-5
        for (int i = 0; i < houseCount; i++) {
            int[] pos = findNextStructurePosition(
                centerX, centerY, occupiedPositions, worldWidth, worldHeight, rng
            );
            
            Structure house = createStructure(
                "structure_" + clanId + "_house_" + i,
                StructureType.HOUSE,
                clanId,
                pos[0], pos[1]
            );
            structures.add(house);
            occupiedPositions.add(pos);
        }
        
        // 3. Place 1 shop/market
        int[] shopPos = findNextStructurePosition(
            centerX, centerY, occupiedPositions, worldWidth, worldHeight, rng
        );
        Structure shop = createStructure(
            "structure_" + clanId + "_shop",
            StructureType.SHOP,
            clanId,
            shopPos[0], shopPos[1]
        );
        structures.add(shop);
        
        return structures;
    }
    
    /**
     * Finds next valid structure position with minimum spacing.
     */
    private static int[] findNextStructurePosition(
            int centerX, int centerY,
            List<int[]> occupied,
            int worldWidth, int worldHeight,
            Random rng) {
        
        int maxAttempts = 100;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            // Random position within settlement radius
            double angle = rng.nextDouble() * 2 * Math.PI;
            double distance = 10 + rng.nextDouble() * (SETTLEMENT_RADIUS - 10);
            
            int x = centerX + (int)(Math.cos(angle) * distance);
            int y = centerY + (int)(Math.sin(angle) * distance);
            
            // Check bounds
            if (x < 0 || x >= worldWidth || y < 0 || y >= worldHeight) {
                continue;
            }
            
            // Check minimum spacing
            boolean tooClose = false;
            for (int[] pos : occupied) {
                double dist = Math.sqrt(Math.pow(x - pos[0], 2) + Math.pow(y - pos[1], 2));
                if (dist < MIN_STRUCTURE_SPACING) {
                    tooClose = true;
                    break;
                }
            }
            
            if (!tooClose) {
                return new int[]{x, y};
            }
        }
        
        // Fallback: place at center + offset
        return new int[]{centerX + 10, centerY + 10};
    }
    
    /**
     * Creates a structure instance.
     */
    private static Structure createStructure(
            String id,
            StructureType type,
            String ownerId,
            int x, int y) {
        
        return new Structure.Builder()
            .id(id)
            .type(type)
            .ownerId(ownerId)
            .ownerType(OwnerType.CLAN)
            .locationTileId(x + "," + y)
            .health(100.0)
            .maxHealth(100.0)
            .createdAtTick(0)
            .lastUpdatedTick(0)
            .schemaVersion(1)
            .build();
    }
    
    /**
     * Generates a settlement name based on biome.
     */
    private static String generateSettlementName(Biome biome, Random rng) {
        List<String> prefixes;
        
        switch (biome) {
            case GRASSLAND:
            case SAVANNA:
                prefixes = GRASSLAND_PREFIXES;
                break;
            case FOREST:
            case RAINFOREST:
                prefixes = FOREST_PREFIXES;
                break;
            case DESERT:
            case BADLANDS:
                prefixes = DESERT_PREFIXES;
                break;
            case MOUNTAIN:
            case HIGHLAND:
                prefixes = MOUNTAIN_PREFIXES;
                break;
            case TUNDRA:
            case TAIGA:
                prefixes = TUNDRA_PREFIXES;
                break;
            default:
                prefixes = GRASSLAND_PREFIXES;
        }
        
        String prefix = prefixes.get(rng.nextInt(prefixes.size()));
        String suffix = SUFFIXES.get(rng.nextInt(SUFFIXES.size()));
        
        return prefix + suffix;
    }
    
    /**
     * Container for settlement with its structures.
     */
    public static class SettlementWithStructures {
        private final Settlement settlement;
        private final List<Structure> structures;
        
        public SettlementWithStructures(Settlement settlement, List<Structure> structures) {
            this.settlement = settlement;
            this.structures = structures;
        }
        
        public Settlement getSettlement() {
            return settlement;
        }
        
        public List<Structure> getStructures() {
            return structures;
        }
    }
}
