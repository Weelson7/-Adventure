package org.adventure.prophecy;

import java.util.*;

/**
 * Represents a major world prophecy that can be fulfilled or prevented.
 * Prophecies are seeded at worldgen and linked to world features.
 */
public class Prophecy {
    private final String id;
    private final String title;
    private final String description;
    private final ProphecyType type;
    private ProphecyStatus status;
    private final long triggerTick;
    private final List<String> triggerConditions;
    private final String linkedFeatureId;
    private final int linkedTileId;
    private boolean fulfilled;
    private long revealedTick;
    private final Map<String, Object> metadata;
    private final int schemaVersion;
    
    private Prophecy(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.description = builder.description;
        this.type = builder.type;
        this.status = builder.status;
        this.triggerTick = builder.triggerTick;
        this.triggerConditions = new ArrayList<>(builder.triggerConditions);
        this.linkedFeatureId = builder.linkedFeatureId;
        this.linkedTileId = builder.linkedTileId;
        this.fulfilled = builder.fulfilled;
        this.revealedTick = builder.revealedTick;
        this.metadata = new HashMap<>(builder.metadata);
        this.schemaVersion = builder.schemaVersion;
    }
    
    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public ProphecyType getType() { return type; }
    public ProphecyStatus getStatus() { return status; }
    public long getTriggerTick() { return triggerTick; }
    public List<String> getTriggerConditions() { return new ArrayList<>(triggerConditions); }
    public String getLinkedFeatureId() { return linkedFeatureId; }
    public int getLinkedTileId() { return linkedTileId; }
    public boolean isFulfilled() { return fulfilled; }
    public long getRevealedTick() { return revealedTick; }
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
    public int getSchemaVersion() { return schemaVersion; }
    
    // Setters
    public void setStatus(ProphecyStatus status) { this.status = status; }
    public void setFulfilled(boolean fulfilled) { this.fulfilled = fulfilled; }
    public void setRevealedTick(long revealedTick) { this.revealedTick = revealedTick; }
    public void setMetadata(String key, Object value) { this.metadata.put(key, value); }
    
    /**
     * Checks if prophecy should trigger at current tick.
     * 
     * @param currentTick Current game tick
     * @return true if trigger tick reached
     */
    public boolean shouldTrigger(long currentTick) {
        return currentTick >= triggerTick && status == ProphecyStatus.REVEALED;
    }
    
    /**
     * Checks if all trigger conditions are met.
     * 
     * @param worldState Current world state (for condition checking)
     * @return true if conditions met
     */
    public boolean areConditionsMet(Map<String, Object> worldState) {
        // TODO: Implement condition checking logic
        // For now, return false (manual triggering only)
        return false;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prophecy prophecy = (Prophecy) o;
        return Objects.equals(id, prophecy.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("Prophecy{id='%s', title='%s', type=%s, status=%s}",
            id, title, type, status);
    }
    
    /**
     * Builder for creating Prophecy instances.
     */
    public static class Builder {
        private String id;
        private String title;
        private String description;
        private ProphecyType type;
        private ProphecyStatus status = ProphecyStatus.HIDDEN;
        private long triggerTick;
        private List<String> triggerConditions = new ArrayList<>();
        private String linkedFeatureId;
        private int linkedTileId;
        private boolean fulfilled = false;
        private long revealedTick = 0;
        private Map<String, Object> metadata = new HashMap<>();
        private int schemaVersion = 1;
        
        public Builder id(String id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder type(ProphecyType type) { this.type = type; return this; }
        public Builder status(ProphecyStatus status) { this.status = status; return this; }
        public Builder triggerTick(long triggerTick) { this.triggerTick = triggerTick; return this; }
        public Builder triggerConditions(List<String> triggerConditions) {
            this.triggerConditions = new ArrayList<>(triggerConditions);
            return this;
        }
        public Builder addTriggerCondition(String condition) {
            this.triggerConditions.add(condition);
            return this;
        }
        public Builder linkedFeatureId(String linkedFeatureId) {
            this.linkedFeatureId = linkedFeatureId;
            return this;
        }
        public Builder linkedTileId(int linkedTileId) {
            this.linkedTileId = linkedTileId;
            return this;
        }
        public Builder fulfilled(boolean fulfilled) { this.fulfilled = fulfilled; return this; }
        public Builder revealedTick(long revealedTick) { this.revealedTick = revealedTick; return this; }
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = new HashMap<>(metadata);
            return this;
        }
        
        public Builder schemaVersion(int schemaVersion) {
            this.schemaVersion = schemaVersion;
            return this;
        }
        
        public Prophecy build() {
            Objects.requireNonNull(id, "id cannot be null");
            Objects.requireNonNull(title, "title cannot be null");
            Objects.requireNonNull(type, "type cannot be null");
            
            return new Prophecy(this);
        }
    }
}
