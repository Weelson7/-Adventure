package org.adventure.world;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.adventure.npc.NamedNPC;
import org.adventure.prophecy.Prophecy;
import org.adventure.prophecy.ProphecyGenerator;
import org.adventure.quest.Quest;
import org.adventure.quest.QuestGenerator;
import org.adventure.settlement.Settlement;
import org.adventure.settlement.SettlementGenerator;
import org.adventure.society.Clan;
import org.adventure.story.Story;
import org.adventure.story.StoryGenerator;
import org.adventure.structure.Structure;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class WorldGen {
    private final int width;
    private final int height;
    private final double[][] elevation;
    private final double[][] temperature;  // In Celsius
    private final double[][] moisture;     // 0.0 = dry, 1.0 = wet
    private final Biome[][] biomes;
    private final int[][] plateIds;        // Plate assignment per tile
    private long seed;
    private java.util.List<Plate> plates;
    private java.util.List<River> rivers;
    private java.util.List<RegionalFeature> features;
    private java.util.List<Story> stories;
    private java.util.List<Clan> clans;
    private java.util.List<Settlement> settlements;
    private java.util.List<Structure> structures;
    private java.util.List<NamedNPC> npcs;
    private java.util.List<Prophecy> prophecies;
    private java.util.List<Quest> quests;

    public WorldGen(int width, int height) {
        this.width = width;
        this.height = height;
        this.elevation = new double[width][height];
        this.temperature = new double[width][height];
        this.moisture = new double[width][height];
        this.biomes = new Biome[width][height];
        this.plateIds = new int[width][height];
        this.plates = new java.util.ArrayList<>();
        this.rivers = new java.util.ArrayList<>();
        this.features = new java.util.ArrayList<>();
        this.stories = new java.util.ArrayList<>();
        this.clans = new java.util.ArrayList<>();
        this.settlements = new java.util.ArrayList<>();
        this.structures = new java.util.ArrayList<>();
        this.npcs = new java.util.ArrayList<>();
        this.prophecies = new java.util.ArrayList<>();
        this.quests = new java.util.ArrayList<>();
    }

    public void generate(long seed) {
        this.seed = seed;
        
        // Phase 1: Generate tectonic plates
        generatePlates(seed);
        
        // Phase 2: Assign tiles to plates (Voronoi partitioning)
        assignTilesToPlates();
        
        // Phase 3: Generate elevation from plates and noise
        generateElevation(seed);
        
        // Phase 4: Calculate temperature based on latitude
        generateTemperature();
        
        // Phase 5: Calculate moisture based on proximity to water
        generateMoisture(seed);
        
        // Phase 6: Assign biomes based on elevation, temperature, moisture
        assignBiomes();
        
        // Phase 7: Generate rivers from highlands to ocean
        generateRivers(seed);
        
        // Phase 8: Place regional features (volcanoes, magic zones, etc.)
        generateRegionalFeatures(seed);
        
        // Phase 9: Generate initial stories
        generateStories(seed);
        
        // Phase 10: Generate initial clans/societies
        generateClans(seed);
        
        // Phase 11: Generate initial settlements (1 per clan)
        generateSettlements(seed);
        
        // Phase 12: Generate Named NPCs for all clans
        generateNamedNPCs(seed);
        
        // Phase 13: Generate prophecies
        generateProphecies(seed);
        
        // Phase 14: Generate feature-based quests
        generateQuests(seed);
    }

    private void generatePlates(long seed) {
        // Number of plates based on world size (1 plate per ~10000 tiles)
        int numPlates = Math.max(4, (width * height) / 10000);
        
        java.util.Random rng = new java.util.Random(seed);
        plates = new java.util.ArrayList<>();
        
        for (int i = 0; i < numPlates; i++) {
            Plate plate = Plate.createRandomPlate(i, width, height, seed, rng);
            plates.add(plate);
        }
    }

    private void assignTilesToPlates() {
        // Voronoi partitioning: assign each tile to nearest plate center
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int nearestPlate = 0;
                double minDist = Double.MAX_VALUE;
                
                for (int i = 0; i < plates.size(); i++) {
                    Plate plate = plates.get(i);
                    double dx = x - plate.getCenterX();
                    double dy = y - plate.getCenterY();
                    double dist = dx * dx + dy * dy;  // Squared distance (faster)
                    
                    if (dist < minDist) {
                        minDist = dist;
                        nearestPlate = i;
                    }
                }
                
                plateIds[x][y] = nearestPlate;
                plates.get(nearestPlate).addTile(x, y);
            }
        }
    }

    private void generateElevation(long seed) {
        // Base elevation from plate type + layered noise for detail
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Plate plate = plates.get(plateIds[x][y]);
                
                // Base elevation from plate type
                double baseElevation = plate.getType() == Plate.PlateType.CONTINENTAL ? 0.5 : 0.15;
                
                // Add layered noise for natural variation
                double e1 = RandomUtil.valueNoise(seed, x, y);
                double e2 = RandomUtil.valueNoise(seed + 0x9e3779b97f4a7c15L, x * 2, y * 2) * 0.5;
                double e3 = RandomUtil.valueNoise(seed + 0xC2B2AE3D27D4EB4FL, x * 4, y * 4) * 0.25;
                double noise = e1 * 0.6 + e2 * 0.3 + e3 * 0.1;
                
                // Combine base + noise (0.7 base, 0.3 noise)
                elevation[x][y] = baseElevation * 0.7 + noise * 0.3;
                
                // Add collision mountains at plate boundaries
                elevation[x][y] += getCollisionUplift(x, y);
                
                // Clamp to [0, 1]
                elevation[x][y] = Math.max(0.0, Math.min(1.0, elevation[x][y]));
            }
        }
    }

    private double getCollisionUplift(int x, int y) {
        int myPlate = plateIds[x][y];
        double maxUplift = 0.0;
        
        // Check 4-neighbors for plate boundaries
        int[][] neighbors = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] dir : neighbors) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            
            if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                int neighborPlate = plateIds[nx][ny];
                if (neighborPlate != myPlate) {
                    // Plate boundary detected
                    Plate p1 = plates.get(myPlate);
                    Plate p2 = plates.get(neighborPlate);
                    
                    if (p1.isColliding(p2)) {
                        // Collision creates mountains
                        double intensity = p1.collisionIntensity(p2);
                        maxUplift = Math.max(maxUplift, intensity * 0.3);  // Up to +0.3 elevation
                    }
                }
            }
        }
        
        return maxUplift;
    }

    private void generateTemperature() {
        // Temperature based on latitude (y-coordinate)
        // Equator (middle) = warm, poles (top/bottom) = cold
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Normalize latitude to [-1, 1] where 0 is equator
                double latitude = 2.0 * (y / (double) height) - 1.0;
                
                // Base temperature: hot at equator, cold at poles
                double baseTemp = 25.0 - Math.abs(latitude) * 35.0;  // 25°C to -10°C
                
                // Elevation cooling: -6°C per 1000m (elevation * 10000m)
                double elevationEffect = -elevation[x][y] * 60.0;
                
                temperature[x][y] = baseTemp + elevationEffect;
            }
        }
    }

    private void generateMoisture(long seed) {
        // Moisture influenced by proximity to water + noise
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Base moisture from noise
                double moistureNoise = RandomUtil.valueNoise(seed + 0xABCDEF123456L, x / 4, y / 4);
                
                // Increase moisture near water
                double waterProximity = getWaterProximity(x, y);
                
                // Combine (60% noise, 40% water proximity)
                moisture[x][y] = moistureNoise * 0.6 + waterProximity * 0.4;
            }
        }
    }

    private double getWaterProximity(int x, int y) {
        // Check if this is water
        if (elevation[x][y] < 0.2) {
            return 1.0;
        }
        
        // Find nearest water within 10 tiles
        int searchRadius = 10;
        double minDist = Double.MAX_VALUE;
        
        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dy = -searchRadius; dy <= searchRadius; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                
                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                    if (elevation[nx][ny] < 0.2) {  // Water threshold
                        double dist = Math.sqrt(dx * dx + dy * dy);
                        minDist = Math.min(minDist, dist);
                    }
                }
            }
        }
        
        if (minDist == Double.MAX_VALUE) {
            return 0.1;  // No water nearby
        }
        
        // Inverse distance: closer = more moisture
        return Math.max(0.1, 1.0 - (minDist / searchRadius));
    }

    private void assignBiomes() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                biomes[x][y] = Biome.assign(elevation[x][y], temperature[x][y], moisture[x][y]);
            }
        }
    }

    private void generateRivers(long seed) {
        // Calculate number of rivers based on world size (1 per ~8000 tiles)
        int numRivers = Math.max(3, (width * height) / 8000);
        
        rivers = River.generateRivers(elevation, seed + 0x1234567890ABCDEFL, width, height, numRivers);
    }

    private void generateRegionalFeatures(long seed) {
        // Standard feature density (can be adjusted)
        double density = 1.0;
        
        features = RegionalFeature.generateFeatures(elevation, biomes, seed + 0xFEDCBA0987654321L, 
                                                     width, height, density);
    }

    public String checksum() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    byte[] row = Double.toString(elevation[x][y]).getBytes(StandardCharsets.UTF_8);
                    md.update(row);
                }
            }
            byte[] digest = md.digest();
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeChunkJson(File out) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ChunkData data = new ChunkData();
        data.width = width;
        data.height = height;
        data.seed = seed;
        data.elevation = elevation;
        mapper.writerWithDefaultPrettyPrinter().writeValue(out, data);
    }

    // Accessors for prototypes and external tooling
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public long getSeed() {
        return seed;
    }

    public double getElevation(int x, int y) {
        return elevation[x][y];
    }

    public double getTemperature(int x, int y) {
        return temperature[x][y];
    }

    public double getMoisture(int x, int y) {
        return moisture[x][y];
    }

    public Biome getBiome(int x, int y) {
        return biomes[x][y];
    }

    public int getPlateId(int x, int y) {
        return plateIds[x][y];
    }

    public Plate getPlate(int plateId) {
        return plates.get(plateId);
    }

    public java.util.List<River> getRivers() {
        return new java.util.ArrayList<>(rivers);
    }

    public java.util.List<RegionalFeature> getFeatures() {
        return new java.util.ArrayList<>(features);
    }

    public java.util.List<Story> getStories() {
        return new java.util.ArrayList<>(stories);
    }

    public java.util.List<Clan> getClans() {
        return new java.util.ArrayList<>(clans);
    }

    public java.util.List<Settlement> getSettlements() {
        return new java.util.ArrayList<>(settlements);
    }

    public java.util.List<Structure> getStructures() {
        return new java.util.ArrayList<>(structures);
    }

    public java.util.List<NamedNPC> getNPCs() {
        return new java.util.ArrayList<>(npcs);
    }

    public java.util.List<Prophecy> getProphecies() {
        return new java.util.ArrayList<>(prophecies);
    }

    public java.util.List<Quest> getQuests() {
        return new java.util.ArrayList<>(quests);
    }

    // Phase 9: Generate initial stories
    private void generateStories(long seed) {
        StoryGenerator generator = new StoryGenerator(seed, width, height);
        this.stories = generator.generateStories(biomes);
    }

    // Phase 10: Generate initial clans
    private void generateClans(long seed) {
        this.clans = ClanGenerator.generateInitialClans(seed, width, height, biomes);
    }

    // Phase 11: Generate settlements (1 per clan)
    private void generateSettlements(long seed) {
        java.util.Map<String, SettlementGenerator.SettlementWithStructures> settlementMap = 
            SettlementGenerator.generateInitialSettlements(seed, clans, biomes, width, height);
        
        // Extract settlements and structures
        for (SettlementGenerator.SettlementWithStructures sws : settlementMap.values()) {
            this.settlements.add(sws.getSettlement());
            this.structures.addAll(sws.getStructures());
        }
    }

    // Phase 12: Generate Named NPCs
    private void generateNamedNPCs(long seed) {
        // Build clan -> structures map
        java.util.Map<String, java.util.List<Structure>> clanStructures = new java.util.HashMap<>();
        for (Structure structure : structures) {
            String clanId = structure.getOwnerId();
            clanStructures.computeIfAbsent(clanId, k -> new java.util.ArrayList<>())
                .add(structure);
        }
        
        // Generate NPCs
        this.npcs = ClanGenerator.generateNPCsForClans(
            clans, 
            clanStructures, 
            seed, 
            0L // currentTick = 0 at worldgen
        );
    }

    // Phase 13: Generate prophecies
    private void generateProphecies(long seed) {
        this.prophecies = ProphecyGenerator.generateProphecies(seed, features, biomes);
    }

    // Phase 14: Generate quests
    private void generateQuests(long seed) {
        this.quests = QuestGenerator.generateFeatureQuests(seed, features, stories);
    }

    public static class ChunkData {
        public int width;
        public int height;
        public long seed;
        public double[][] elevation;
    }
}
