package org.adventure.structure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a room within a structure.
 * Rooms provide various gameplay effects and can be upgraded.
 * 
 * @see Structure
 * @see RoomCategory
 */
public final class Room {
    private final String id;
    private final RoomCategory category;
    private final int size;
    private final Map<String, Object> properties;
    private final int createdAtTick;
    private final int schemaVersion;
    
    @JsonCreator
    public Room(
            @JsonProperty("id") String id,
            @JsonProperty("category") RoomCategory category,
            @JsonProperty("size") int size,
            @JsonProperty("properties") Map<String, Object> properties,
            @JsonProperty("createdAtTick") int createdAtTick,
            @JsonProperty("schemaVersion") int schemaVersion) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Room ID cannot be null or empty");
        }
        if (category == null) {
            throw new IllegalArgumentException("Room category cannot be null");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Room size must be positive");
        }
        
        this.id = id;
        this.category = category;
        this.size = size;
        this.properties = properties != null ? new HashMap<>(properties) : new HashMap<>();
        this.createdAtTick = createdAtTick;
        this.schemaVersion = schemaVersion;
    }
    
    /**
     * Builder for creating Room instances.
     */
    public static class Builder {
        private String id;
        private RoomCategory category;
        private int size = 1;
        private Map<String, Object> properties = new HashMap<>();
        private int createdAtTick = 0;
        private int schemaVersion = 1;
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder category(RoomCategory category) {
            this.category = category;
            return this;
        }
        
        public Builder size(int size) {
            this.size = size;
            return this;
        }
        
        public Builder properties(Map<String, Object> properties) {
            this.properties = new HashMap<>(properties);
            return this;
        }
        
        public Builder property(String key, Object value) {
            this.properties.put(key, value);
            return this;
        }
        
        public Builder createdAtTick(int createdAtTick) {
            this.createdAtTick = createdAtTick;
            return this;
        }
        
        public Builder schemaVersion(int schemaVersion) {
            this.schemaVersion = schemaVersion;
            return this;
        }
        
        public Room build() {
            return new Room(id, category, size, properties, createdAtTick, schemaVersion);
        }
    }
    
    public String getId() {
        return id;
    }
    
    public RoomCategory getCategory() {
        return category;
    }
    
    public int getSize() {
        return size;
    }
    
    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }
    
    public int getCreatedAtTick() {
        return createdAtTick;
    }
    
    public int getSchemaVersion() {
        return schemaVersion;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Room)) return false;
        Room other = (Room) obj;
        return Objects.equals(id, other.id) && category == other.category;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, category);
    }
    
    @Override
    public String toString() {
        return "Room{id='" + id + "', category=" + category + ", size=" + size + "}";
    }
}
