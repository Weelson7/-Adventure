package org.adventure.settlement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.*;

/**
 * Represents a village, town, or city formed from structure clusters.
 * Villages are automatically detected when 3+ structures are within 10-tile radius.
 * Can be promoted to towns (15+ structures) or cities (30+ structures + population).
 * 
 * Design: BUILD_PHASE1.10.x.md → Phase 1.10.2
 */
public final class Village {
    private final String id;
    private String name;
    private VillageType type;
    private final int centerX;
    private final int centerY;
    private final List<String> structureIds;
    private int population;
    private String governingClanId;
    private final long foundedTick;
    private final int schemaVersion;
    
    @JsonCreator
    public Village(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("type") VillageType type,
            @JsonProperty("centerX") int centerX,
            @JsonProperty("centerY") int centerY,
            @JsonProperty("structureIds") List<String> structureIds,
            @JsonProperty("population") int population,
            @JsonProperty("governingClanId") String governingClanId,
            @JsonProperty("foundedTick") long foundedTick,
            @JsonProperty("schemaVersion") int schemaVersion) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Village ID cannot be null or empty");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Village name cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Village type cannot be null");
        }
        if (structureIds == null || structureIds.isEmpty()) {
            throw new IllegalArgumentException("Village must have at least one structure");
        }
        if (population < 0) {
            throw new IllegalArgumentException("Population cannot be negative");
        }
        if (foundedTick < 0) {
            throw new IllegalArgumentException("Founded tick cannot be negative");
        }
        
        this.id = id;
        this.name = name;
        this.type = type;
        this.centerX = centerX;
        this.centerY = centerY;
        this.structureIds = new ArrayList<>(structureIds);
        this.population = population;
        this.governingClanId = governingClanId;
        this.foundedTick = foundedTick;
        this.schemaVersion = schemaVersion > 0 ? schemaVersion : 1;
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public VillageType getType() { return type; }
    public int getCenterX() { return centerX; }
    public int getCenterY() { return centerY; }
    public List<String> getStructureIds() { return new ArrayList<>(structureIds); }
    public int getPopulation() { return population; }
    public String getGoverningClanId() { return governingClanId; }
    public long getFoundedTick() { return foundedTick; }
    public int getSchemaVersion() { return schemaVersion; }
    
    // Setters for mutable fields
    public void setName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Village name cannot be null or empty");
        }
        this.name = name;
    }
    
    public void setType(VillageType type) {
        if (type == null) {
            throw new IllegalArgumentException("Village type cannot be null");
        }
        this.type = type;
    }
    
    public void setPopulation(int population) {
        if (population < 0) {
            throw new IllegalArgumentException("Population cannot be negative");
        }
        this.population = population;
    }
    
    public void setGoverningClanId(String clanId) {
        this.governingClanId = clanId;
    }
    
    public void addStructure(String structureId) {
        if (structureId != null && !structureId.isEmpty() && !structureIds.contains(structureId)) {
            structureIds.add(structureId);
        }
    }
    
    public void removeStructure(String structureId) {
        structureIds.remove(structureId);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Village village = (Village) o;
        return Objects.equals(id, village.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Village{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", center=(" + centerX + "," + centerY + ")" +
                ", structures=" + structureIds.size() +
                ", population=" + population +
                ", clan='" + governingClanId + '\'' +
                '}';
    }
    
    /**
     * Builder for creating Village instances.
     */
    public static class Builder {
        private String id;
        private String name;
        private VillageType type = VillageType.VILLAGE;
        private int centerX;
        private int centerY;
        private List<String> structureIds = new ArrayList<>();
        private int population = 0;
        private String governingClanId;
        private long foundedTick = 0;
        private int schemaVersion = 1;
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder type(VillageType type) {
            this.type = type;
            return this;
        }
        
        public Builder centerX(int centerX) {
            this.centerX = centerX;
            return this;
        }
        
        public Builder centerY(int centerY) {
            this.centerY = centerY;
            return this;
        }
        
        public Builder structureIds(List<String> structureIds) {
            this.structureIds = structureIds != null ? new ArrayList<>(structureIds) : new ArrayList<>();
            return this;
        }
        
        public Builder addStructure(String structureId) {
            if (structureId != null && !structureId.isEmpty()) {
                this.structureIds.add(structureId);
            }
            return this;
        }
        
        public Builder population(int population) {
            this.population = population;
            return this;
        }
        
        public Builder governingClanId(String clanId) {
            this.governingClanId = clanId;
            return this;
        }
        
        public Builder foundedTick(long tick) {
            this.foundedTick = tick;
            return this;
        }
        
        public Builder schemaVersion(int version) {
            this.schemaVersion = version;
            return this;
        }
        
        public Village build() {
            return new Village(id, name, type, centerX, centerY, structureIds, 
                    population, governingClanId, foundedTick, schemaVersion);
        }
    }
}
