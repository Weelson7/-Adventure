package org.adventure.world;

import org.adventure.npc.NamedNPC;
import org.adventure.npc.NPCGenerator;
import org.adventure.society.Clan;
import org.adventure.society.ClanType;
import org.adventure.structure.Structure;

import java.util.*;

/**
 * Generates initial clans at worldgen.
 * 
 * Features:
 * - Deterministic clan placement from seed
 * - Scale with world size (1 clan per 20k tiles, min 3, max 50)
 * - Biome-appropriate clan types
 * - Variable starting sizes (1 large @ 20-30, rest @ 5-15)
 * - Named NPC generation for all clans
 * 
 * Design: BUILD_PHASE1.10.x.md - Phase 1.10.1, deliverable 4
 */
public class ClanGenerator {
    private static final int TILES_PER_CLAN = 20000;
    private static final int MIN_CLANS = 3;
    private static final int MAX_CLANS = 50;
    private static final int LARGE_CLAN_MIN = 20;
    private static final int LARGE_CLAN_MAX = 30;
    private static final int SMALL_CLAN_MIN = 5;
    private static final int SMALL_CLAN_MAX = 15;
    
    /**
     * Generates initial clans for the world.
     * 
     * @param worldSeed Seed for deterministic generation
     * @param worldWidth World width in tiles
     * @param worldHeight World height in tiles
     * @param biomes World biome map
     * @return List of generated clans
     */
    public static List<Clan> generateInitialClans(
            long worldSeed,
            int worldWidth,
            int worldHeight,
            Biome[][] biomes) {
        
        List<Clan> clans = new ArrayList<>();
        Random rng = new Random(worldSeed ^ 0xC1A4);
        
        // Calculate clan count
        int totalTiles = worldWidth * worldHeight;
        int clanCount = Math.max(MIN_CLANS, Math.min(MAX_CLANS, totalTiles / TILES_PER_CLAN));
        
        // Generate clans
        for (int i = 0; i < clanCount; i++) {
            // Find suitable spawn location
            int[] location = findSuitableLocation(worldWidth, worldHeight, biomes, clans, rng);
            int x = location[0];
            int y = location[1];
            
            // Determine clan type based on biome
            Biome biome = biomes[y][x];
            ClanType type = selectClanTypeForBiome(biome, rng);
            
            // Determine clan size (first clan is large, rest are small)
            int memberCount = (i == 0) 
                ? LARGE_CLAN_MIN + rng.nextInt(LARGE_CLAN_MAX - LARGE_CLAN_MIN + 1)
                : SMALL_CLAN_MIN + rng.nextInt(SMALL_CLAN_MAX - SMALL_CLAN_MIN + 1);
            
            // Generate placeholder member IDs (actual NPCs generated later)
            List<String> memberIds = new ArrayList<>();
            for (int j = 0; j < memberCount; j++) {
                memberIds.add("placeholder_" + i + "_" + j);
            }
            
            // Calculate starting treasury
            int treasury = calculateStartingTreasury(type, rng);
            
            // Create clan
            Clan clan = new Clan.Builder()
                .id("clan_" + i)
                .name(generateClanName(type, biome, rng))
                .type(type)
                .members(memberIds)
                .leaderId(memberIds.get(0))
                .treasury(treasury)
                .centerX((double)x / worldWidth)
                .centerY((double)y / worldHeight)
                .foundingTick(0L)
                .schemaVersion(1)
                .build();
            
            clans.add(clan);
        }
        
        return clans;
    }
    
    /**
     * Generates Named NPCs for all clans.
     * Should be called AFTER settlements are generated so NPCs can be assigned homes.
     * 
     * @param clans List of clans
     * @param clanStructures Map of clanId -> structures (including homes)
     * @param worldSeed Seed for deterministic generation
     * @param currentTick Current game tick (usually 0 at worldgen)
     * @return List of all generated NPCs
     */
    public static List<NamedNPC> generateNPCsForClans(
            List<Clan> clans,
            Map<String, List<Structure>> clanStructures,
            long worldSeed,
            long currentTick) {
        
        List<NamedNPC> allNpcs = new ArrayList<>();
        Random rng = new Random(worldSeed ^ 0x4ABCL);
        
        for (Clan clan : clans) {
            List<Structure> structures = clanStructures.getOrDefault(clan.getId(), new ArrayList<>());
            
            // Generate NPCs for this clan
            List<NamedNPC> clanNpcs = NPCGenerator.generateInitialClanPopulation(
                clan,
                structures,
                currentTick,
                rng
            );
            
            allNpcs.addAll(clanNpcs);
        }
        
        return allNpcs;
    }
    
    /**
     * Finds a suitable location for clan placement.
     * Avoids water and ensures minimum spacing from other clans.
     */
    private static int[] findSuitableLocation(
            int worldWidth, int worldHeight,
            Biome[][] biomes,
            List<Clan> existingClans,
            Random rng) {
        
        int maxAttempts = 1000;
        int minClanSpacing = 20; // Minimum 20 tiles between clan centers
        
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int x = rng.nextInt(worldWidth);
            int y = rng.nextInt(worldHeight);
            
            Biome biome = biomes[y][x];
            
            // Avoid water and ice
            if (biome == Biome.OCEAN || biome == Biome.DEEP_OCEAN || 
                biome == Biome.ICE || biome == Biome.GLACIER) {
                continue;
            }
            
            // Check spacing from existing clans
            boolean tooClose = false;
            for (Clan existing : existingClans) {
                double existingX = existing.getCenterX() * worldWidth;
                double existingY = existing.getCenterY() * worldHeight;
                double distance = Math.sqrt(
                    Math.pow(x - existingX, 2) + Math.pow(y - existingY, 2)
                );
                
                if (distance < minClanSpacing) {
                    tooClose = true;
                    break;
                }
            }
            
            if (!tooClose) {
                return new int[]{x, y};
            }
        }
        
        // Fallback: return random position
        return new int[]{rng.nextInt(worldWidth), rng.nextInt(worldHeight)};
    }
    
    /**
     * Selects appropriate clan type based on biome.
     */
    private static ClanType selectClanTypeForBiome(Biome biome, Random rng) {
        switch (biome) {
            case GRASSLAND:
            case FOREST:
                return rng.nextBoolean() ? ClanType.AGRICULTURAL : ClanType.MERCANTILE;
            case DESERT:
            case SAVANNA:
                return ClanType.NOMADIC;
            case MOUNTAIN:
            case HIGHLAND:
                return rng.nextBoolean() ? ClanType.MILITARY : ClanType.MINING;
            case TUNDRA:
            case TAIGA:
                return ClanType.NOMADIC;
            default:
                return ClanType.AGRICULTURAL;
        }
    }
    
    /**
     * Calculates starting treasury based on clan type.
     */
    private static int calculateStartingTreasury(ClanType type, Random rng) {
        int base;
        
        switch (type) {
            case MERCANTILE:
                base = 150;
                break;
            case MINING:
                base = 120;
                break;
            case AGRICULTURAL:
                base = 100;
                break;
            case MILITARY:
                base = 80;
                break;
            case NOMADIC:
                base = 60;
                break;
            default:
                base = 100;
        }
        
        // Add randomness: ±50%
        int variance = (int)(base * 0.5);
        return base - variance/2 + rng.nextInt(variance);
    }
    
    /**
     * Generates clan name based on type and biome.
     */
    private static String generateClanName(ClanType type, Biome biome, Random rng) {
        List<String> prefixes = new ArrayList<>();
        List<String> suffixes = new ArrayList<>();
        
        // Biome-based prefixes
        switch (biome) {
            case GRASSLAND:
                prefixes.addAll(Arrays.asList("Green", "Sun", "Meadow", "Plains"));
                break;
            case FOREST:
                prefixes.addAll(Arrays.asList("Dark", "Deep", "Elder", "Woodland"));
                break;
            case DESERT:
                prefixes.addAll(Arrays.asList("Sand", "Dune", "Sun", "Scorpion"));
                break;
            case MOUNTAIN:
                prefixes.addAll(Arrays.asList("Stone", "Peak", "Iron", "Sky"));
                break;
            case TUNDRA:
                prefixes.addAll(Arrays.asList("Frost", "Ice", "Snow", "Winter"));
                break;
            default:
                prefixes.addAll(Arrays.asList("North", "South", "East", "West"));
        }
        
        // Type-based suffixes
        switch (type) {
            case AGRICULTURAL:
                suffixes.addAll(Arrays.asList("Farmers", "Harvesters", "Tillers", "Growers"));
                break;
            case MERCANTILE:
                suffixes.addAll(Arrays.asList("Traders", "Merchants", "Caravan", "Guild"));
                break;
            case MILITARY:
                suffixes.addAll(Arrays.asList("Warriors", "Defenders", "Legion", "Guard"));
                break;
            case MINING:
                suffixes.addAll(Arrays.asList("Miners", "Diggers", "Delvers", "Forgers"));
                break;
            case NOMADIC:
                suffixes.addAll(Arrays.asList("Wanderers", "Rovers", "Nomads", "Drifters"));
                break;
            default:
                suffixes.addAll(Arrays.asList("Clan", "Tribe", "Folk", "People"));
        }
        
        String prefix = prefixes.get(rng.nextInt(prefixes.size()));
        String suffix = suffixes.get(rng.nextInt(suffixes.size()));
        
        return prefix + " " + suffix;
    }
}
