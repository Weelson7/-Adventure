package org.adventure.structure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an upgrade applied to a structure.
 * Upgrades improve structure capabilities and require resources.
 * 
 * @see Structure
 */
public final class Upgrade {
    private final String id;
    private final String name;
    private final String description;
    private final Map<String, Integer> resourceCosts;
    private final int timeRequiredTicks;
    private final Map<String, Object> effects;
    private final int appliedAtTick;
    private final int schemaVersion;
    
    @JsonCreator
    public Upgrade(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("resourceCosts") Map<String, Integer> resourceCosts,
            @JsonProperty("timeRequiredTicks") int timeRequiredTicks,
            @JsonProperty("effects") Map<String, Object> effects,
            @JsonProperty("appliedAtTick") int appliedAtTick,
            @JsonProperty("schemaVersion") int schemaVersion) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Upgrade ID cannot be null or empty");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Upgrade name cannot be null or empty");
        }
        if (timeRequiredTicks < 0) {
            throw new IllegalArgumentException("Time required cannot be negative");
        }
        
        this.id = id;
        this.name = name;
        this.description = description != null ? description : "";
        this.resourceCosts = resourceCosts != null ? new HashMap<>(resourceCosts) : new HashMap<>();
        this.timeRequiredTicks = timeRequiredTicks;
        this.effects = effects != null ? new HashMap<>(effects) : new HashMap<>();
        this.appliedAtTick = appliedAtTick;
        this.schemaVersion = schemaVersion;
    }
    
    /**
     * Builder for creating Upgrade instances.
     */
    public static class Builder {
        private String id;
        private String name;
        private String description = "";
        private Map<String, Integer> resourceCosts = new HashMap<>();
        private int timeRequiredTicks = 0;
        private Map<String, Object> effects = new HashMap<>();
        private int appliedAtTick = 0;
        private int schemaVersion = 1;
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder resourceCosts(Map<String, Integer> resourceCosts) {
            this.resourceCosts = new HashMap<>(resourceCosts);
            return this;
        }
        
        public Builder resourceCost(String resource, int amount) {
            this.resourceCosts.put(resource, amount);
            return this;
        }
        
        public Builder timeRequiredTicks(int timeRequiredTicks) {
            this.timeRequiredTicks = timeRequiredTicks;
            return this;
        }
        
        public Builder effects(Map<String, Object> effects) {
            this.effects = new HashMap<>(effects);
            return this;
        }
        
        public Builder effect(String key, Object value) {
            this.effects.put(key, value);
            return this;
        }
        
        public Builder appliedAtTick(int appliedAtTick) {
            this.appliedAtTick = appliedAtTick;
            return this;
        }
        
        public Builder schemaVersion(int schemaVersion) {
            this.schemaVersion = schemaVersion;
            return this;
        }
        
        public Upgrade build() {
            return new Upgrade(id, name, description, resourceCosts, 
                             timeRequiredTicks, effects, appliedAtTick, schemaVersion);
        }
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Map<String, Integer> getResourceCosts() {
        return Collections.unmodifiableMap(resourceCosts);
    }
    
    public int getTimeRequiredTicks() {
        return timeRequiredTicks;
    }
    
    public Map<String, Object> getEffects() {
        return Collections.unmodifiableMap(effects);
    }
    
    public int getAppliedAtTick() {
        return appliedAtTick;
    }
    
    public int getSchemaVersion() {
        return schemaVersion;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Upgrade)) return false;
        Upgrade other = (Upgrade) obj;
        return Objects.equals(id, other.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Upgrade{id='" + id + "', name='" + name + "'}";
    }
}
