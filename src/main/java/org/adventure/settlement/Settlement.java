package org.adventure.settlement;

import org.adventure.structure.Structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a settlement (village/town/city) formed from clustered structures.
 * Settlements can grow from villages to towns to cities based on size and features.
 */
public class Settlement {
    private final String id;
    private String name;
    private final String clanId;
    private final int centerX;
    private final int centerY;
    private List<String> structureIds;
    private VillageType type;
    private int population;
    private final long foundedTick;
    private final int schemaVersion;
    
    private Settlement(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.clanId = builder.clanId;
        this.centerX = builder.centerX;
        this.centerY = builder.centerY;
        this.structureIds = new ArrayList<>(builder.structureIds);
        this.type = builder.type;
        this.population = builder.population;
        this.foundedTick = builder.foundedTick;
        this.schemaVersion = builder.schemaVersion;
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getClanId() { return clanId; }
    public int getCenterX() { return centerX; }
    public int getCenterY() { return centerY; }
    public List<String> getStructureIds() { return new ArrayList<>(structureIds); }
    public VillageType getType() { return type; }
    public int getPopulation() { return population; }
    public long getFoundedTick() { return foundedTick; }
    public int getSchemaVersion() { return schemaVersion; }
    
    // Setters for mutable fields
    public void setName(String name) { this.name = name; }
    public void setType(VillageType type) { this.type = type; }
    public void setPopulation(int population) { this.population = population; }
    public void addStructure(String structureId) { this.structureIds.add(structureId); }
    public void removeStructure(String structureId) { this.structureIds.remove(structureId); }
    
    /**
     * Gets the number of structures in this settlement.
     * 
     * @return Structure count
     */
    public int getStructureCount() {
        return structureIds.size();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Settlement that = (Settlement) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("Settlement{id='%s', name='%s', type=%s, structures=%d, population=%d}",
            id, name, type, structureIds.size(), population);
    }
    
    /**
     * Builder for creating Settlement instances.
     */
    public static class Builder {
        private String id;
        private String name;
        private String clanId;
        private int centerX;
        private int centerY;
        private List<String> structureIds = new ArrayList<>();
        private VillageType type = VillageType.VILLAGE;
        private int population = 0;
        private long foundedTick = 0;
        private int schemaVersion = 1;
        
        public Builder id(String id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder clanId(String clanId) { this.clanId = clanId; return this; }
        public Builder centerX(int centerX) { this.centerX = centerX; return this; }
        public Builder centerY(int centerY) { this.centerY = centerY; return this; }
        public Builder structureIds(List<String> structureIds) {
            this.structureIds = new ArrayList<>(structureIds);
            return this;
        }
        public Builder type(VillageType type) { this.type = type; return this; }
        public Builder population(int population) { this.population = population; return this; }
        public Builder foundedTick(long foundedTick) { this.foundedTick = foundedTick; return this; }
        public Builder schemaVersion(int schemaVersion) { this.schemaVersion = schemaVersion; return this; }
        
        public Settlement build() {
            Objects.requireNonNull(id, "id cannot be null");
            Objects.requireNonNull(name, "name cannot be null");
            Objects.requireNonNull(clanId, "clanId cannot be null");
            
            return new Settlement(this);
        }
    }
}
