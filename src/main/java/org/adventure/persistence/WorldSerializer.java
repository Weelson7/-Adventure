package org.adventure.persistence;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.adventure.world.Biome;
import org.adventure.world.Plate;
import org.adventure.world.RegionalFeature;
import org.adventure.world.River;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Serializes/deserializes world generation data.
 * Handles chunk-based storage for efficient loading.
 * 
 * <p>Design: docs/persistence_versioning.md
 * <p>Data Model: docs/data_models.md → WorldGrid, Chunk, Tile
 * 
 * @see SaveManager
 */
public class WorldSerializer {
    
    private final ObjectMapper objectMapper;
    
    public WorldSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * Serialize complete world data to JSON.
     * 
     * @param width World width
     * @param height World height
     * @param seed Generation seed
     * @param elevation Elevation map
     * @param temperature Temperature map
     * @param moisture Moisture map
     * @param biomes Biome map
     * @param plates Tectonic plates
     * @param rivers Generated rivers
     * @param features Regional features
     * @param outputFile Target file
     * @throws IOException If write fails
     */
    public void saveWorld(
            int width, int height, long seed,
            double[][] elevation, double[][] temperature, double[][] moisture,
            Biome[][] biomes,
            List<Plate> plates,
            List<River> rivers,
            List<RegionalFeature> features,
            File outputFile) throws IOException {
        
        WorldData world = new WorldData();
        world.schemaVersion = 1;
        world.type = "world/WorldGrid";
        world.width = width;
        world.height = height;
        world.seed = seed;
        world.elevation = elevation;
        world.temperature = temperature;
        world.moisture = moisture;
        world.biomes = biomes;
        world.plates = plates;
        world.rivers = rivers;
        world.features = features;
        
        objectMapper.writeValue(outputFile, world);
    }
    
    /**
     * Deserialize world data from JSON.
     * 
     * @param inputFile Source file
     * @return Loaded world data
     * @throws IOException If read fails
     */
    public WorldData loadWorld(File inputFile) throws IOException {
        return objectMapper.readValue(inputFile, WorldData.class);
    }
    
    /**
     * Data transfer object for world persistence.
     * 
     * <p>Schema Version 1 fields:
     * <ul>
     *   <li>type: "world/WorldGrid"</li>
     *   <li>schemaVersion: 1</li>
     *   <li>width, height, seed</li>
     *   <li>elevation, temperature, moisture (2D arrays)</li>
     *   <li>biomes (2D enum array)</li>
     *   <li>plates, rivers, features (lists)</li>
     * </ul>
     */
    public static class WorldData {
        @JsonProperty("type")
        public String type;
        
        @JsonProperty("schemaVersion")
        public int schemaVersion;
        
        @JsonProperty("width")
        public int width;
        
        @JsonProperty("height")
        public int height;
        
        @JsonProperty("seed")
        public long seed;
        
        @JsonProperty("elevation")
        public double[][] elevation;
        
        @JsonProperty("temperature")
        public double[][] temperature;
        
        @JsonProperty("moisture")
        public double[][] moisture;
        
        @JsonProperty("biomes")
        public Biome[][] biomes;
        
        @JsonProperty("plates")
        public List<Plate> plates;
        
        @JsonProperty("rivers")
        public List<River> rivers;
        
        @JsonProperty("features")
        public List<RegionalFeature> features;
    }
}
