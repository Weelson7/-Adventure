package org.adventure.story;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * Event represents a gameplay occurrence that can trigger, propagate, and affect the world.
 * Events are associated with stories and can create cascading effects across regions.
 */
public class Event {
    @JsonProperty private String id;
    @JsonProperty private String type = "story/Event";
    @JsonProperty private int schemaVersion = 1;
    @JsonProperty private EventCategory category;
    @JsonProperty private EventStatus status;
    @JsonProperty private String name;
    @JsonProperty private String description;
    @JsonProperty private int originTileId;
    @JsonProperty private long originTick;
    @JsonProperty private double baseProbability;
    @JsonProperty private int hopCount;
    @JsonProperty private int maxHops;
    @JsonProperty private int priority;
    @JsonProperty private long lastProcessedTick;
    @JsonProperty private String linkedStoryId;
    @JsonProperty private Map<String, String> triggerConditions;
    @JsonProperty private Map<String, Object> effects;
    @JsonProperty private Set<String> affectedRegions;
    @JsonProperty private Map<String, Object> metadata;

    /**
     * Default constructor for Jackson deserialization.
     */
    public Event() {
        this.triggerConditions = new HashMap<>();
        this.effects = new HashMap<>();
        this.affectedRegions = new HashSet<>();
        this.metadata = new HashMap<>();
    }

    /**
     * Private constructor for builder pattern.
     */
    private Event(Builder builder) {
        this.id = builder.id;
        this.category = builder.category;
        this.status = builder.status;
        this.name = builder.name;
        this.description = builder.description;
        this.originTileId = builder.originTileId;
        this.originTick = builder.originTick;
        this.baseProbability = builder.baseProbability;
        this.hopCount = builder.hopCount;
        this.maxHops = builder.maxHops;
        this.priority = builder.priority;
        this.lastProcessedTick = builder.lastProcessedTick;
        this.linkedStoryId = builder.linkedStoryId;
        this.triggerConditions = new HashMap<>(builder.triggerConditions);
        this.effects = new HashMap<>(builder.effects);
        this.affectedRegions = new HashSet<>(builder.affectedRegions);
        this.metadata = new HashMap<>(builder.metadata);
    }

    // Getters
    public String getId() { return id; }
    public String getType() { return type; }
    public int getSchemaVersion() { return schemaVersion; }
    public EventCategory getCategory() { return category; }
    public EventStatus getStatus() { return status; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getOriginTileId() { return originTileId; }
    public long getOriginTick() { return originTick; }
    public double getBaseProbability() { return baseProbability; }
    public int getHopCount() { return hopCount; }
    public int getMaxHops() { return maxHops; }
    public int getPriority() { return priority; }
    public long getLastProcessedTick() { return lastProcessedTick; }
    public String getLinkedStoryId() { return linkedStoryId; }
    public Map<String, String> getTriggerConditions() { return new HashMap<>(triggerConditions); }
    public Map<String, Object> getEffects() { return new HashMap<>(effects); }
    public Set<String> getAffectedRegions() { return new HashSet<>(affectedRegions); }
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }

    // Setters for mutable fields
    public void setStatus(EventStatus status) { this.status = status; }
    public void setHopCount(int hopCount) { this.hopCount = hopCount; }
    public void setLastProcessedTick(long tick) { this.lastProcessedTick = tick; }
    public void addAffectedRegion(String regionId) { this.affectedRegions.add(regionId); }
    public void setEffect(String key, Object value) { this.effects.put(key, value); }
    public void setMetadata(String key, Object value) { this.metadata.put(key, value); }

    /**
     * Check if trigger conditions are met.
     * This is a placeholder for future condition evaluation logic.
     */
    public boolean isTriggered() {
        return status == EventStatus.ACTIVE || status == EventStatus.PROPAGATING;
    }

    /**
     * Builder for creating Event instances.
     */
    public static class Builder {
        private String id;
        private EventCategory category;
        private EventStatus status = EventStatus.PENDING;
        private String name;
        private String description;
        private int originTileId;
        private long originTick;
        private double baseProbability = 0.9;
        private int hopCount = 0;
        private int maxHops = 6;
        private int priority = 5;
        private long lastProcessedTick = 0;
        private String linkedStoryId;
        private Map<String, String> triggerConditions = new HashMap<>();
        private Map<String, Object> effects = new HashMap<>();
        private Set<String> affectedRegions = new HashSet<>();
        private Map<String, Object> metadata = new HashMap<>();

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder category(EventCategory category) {
            this.category = category;
            return this;
        }

        public Builder status(EventStatus status) {
            this.status = status;
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

        public Builder originTileId(int originTileId) {
            this.originTileId = originTileId;
            return this;
        }

        public Builder originTick(long originTick) {
            this.originTick = originTick;
            return this;
        }

        public Builder baseProbability(double baseProbability) {
            this.baseProbability = baseProbability;
            return this;
        }

        public Builder hopCount(int hopCount) {
            this.hopCount = hopCount;
            return this;
        }

        public Builder maxHops(int maxHops) {
            this.maxHops = maxHops;
            return this;
        }

        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder lastProcessedTick(long lastProcessedTick) {
            this.lastProcessedTick = lastProcessedTick;
            return this;
        }

        public Builder linkedStoryId(String linkedStoryId) {
            this.linkedStoryId = linkedStoryId;
            return this;
        }

        public Builder triggerCondition(String key, String value) {
            this.triggerConditions.put(key, value);
            return this;
        }

        public Builder effect(String key, Object value) {
            this.effects.put(key, value);
            return this;
        }

        public Builder addAffectedRegion(String regionId) {
            this.affectedRegions.add(regionId);
            return this;
        }

        public Builder metadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public Event build() {
            if (id == null || id.isEmpty()) {
                throw new IllegalArgumentException("Event must have a valid id");
            }
            if (category == null) {
                throw new IllegalArgumentException("Event must have a category");
            }
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Event must have a name");
            }
            if (baseProbability < 0.0 || baseProbability > 1.0) {
                throw new IllegalArgumentException("baseProbability must be between 0.0 and 1.0");
            }
            if (priority < 0 || priority > 10) {
                throw new IllegalArgumentException("priority must be between 0 and 10");
            }
            return new Event(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(id, event.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Event{" +
                "id='" + id + '\'' +
                ", category=" + category +
                ", status=" + status +
                ", name='" + name + '\'' +
                ", priority=" + priority +
                ", hopCount=" + hopCount +
                ", maxHops=" + maxHops +
                '}';
    }
}
