package org.adventure.story;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * Story represents a narrative element in the game world.
 * Stories are seeded at worldgen and can propagate across regions through events.
 * They affect NPC behavior, player interactions, and world state.
 */
public class Story {
    @JsonProperty private String id;
    @JsonProperty private String type = "story/Story";
    @JsonProperty private int schemaVersion = 1;
    @JsonProperty private StoryType storyType;
    @JsonProperty private StoryStatus status;
    @JsonProperty private String title;
    @JsonProperty private String description;
    @JsonProperty private int originTileId;
    @JsonProperty private long originTick;
    @JsonProperty private double baseProbability;
    @JsonProperty private int hopCount;
    @JsonProperty private int maxHops;
    @JsonProperty private int priority;
    @JsonProperty private long lastProcessedTick;
    @JsonProperty private Set<String> affectedRegions;
    @JsonProperty private Map<String, Object> metadata;

    /**
     * Default constructor for Jackson deserialization.
     */
    public Story() {
        this.affectedRegions = new HashSet<>();
        this.metadata = new HashMap<>();
    }

    /**
     * Private constructor for builder pattern.
     */
    private Story(Builder builder) {
        this.id = builder.id;
        this.storyType = builder.storyType;
        this.status = builder.status;
        this.title = builder.title;
        this.description = builder.description;
        this.originTileId = builder.originTileId;
        this.originTick = builder.originTick;
        this.baseProbability = builder.baseProbability;
        this.hopCount = builder.hopCount;
        this.maxHops = builder.maxHops;
        this.priority = builder.priority;
        this.lastProcessedTick = builder.lastProcessedTick;
        this.affectedRegions = new HashSet<>(builder.affectedRegions);
        this.metadata = new HashMap<>(builder.metadata);
    }

    // Getters
    public String getId() { return id; }
    public String getType() { return type; }
    public int getSchemaVersion() { return schemaVersion; }
    public StoryType getStoryType() { return storyType; }
    public StoryStatus getStatus() { return status; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getOriginTileId() { return originTileId; }
    public long getOriginTick() { return originTick; }
    public double getBaseProbability() { return baseProbability; }
    public int getHopCount() { return hopCount; }
    public int getMaxHops() { return maxHops; }
    public int getPriority() { return priority; }
    public long getLastProcessedTick() { return lastProcessedTick; }
    public Set<String> getAffectedRegions() { return new HashSet<>(affectedRegions); }
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }

    /**
     * Extract X coordinate from originTileId (assuming format like "x,y").
     * Returns 0 if originTileId cannot be parsed.
     */
    public int getOriginTileX() {
        try {
            // For now, just return originTileId as coordinate
            // In a real implementation, you'd parse it from a composite ID
            return originTileId % 10000; // Simple conversion
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Extract Y coordinate from originTileId (assuming format like "x,y").
     * Returns 0 if originTileId cannot be parsed.
     */
    public int getOriginTileY() {
        try {
            // For now, just return originTileId as coordinate
            // In a real implementation, you'd parse it from a composite ID
            return originTileId / 10000; // Simple conversion
        } catch (Exception e) {
            return 0;
        }
    }

    // Setters for mutable fields
    public void setStatus(StoryStatus status) { this.status = status; }
    public void setHopCount(int hopCount) { this.hopCount = hopCount; }
    public void setLastProcessedTick(long tick) { this.lastProcessedTick = tick; }
    public void addAffectedRegion(String regionId) { this.affectedRegions.add(regionId); }
    public void setMetadata(String key, Object value) { this.metadata.put(key, value); }

    /**
     * Builder for creating Story instances.
     */
    public static class Builder {
        private String id;
        private StoryType storyType;
        private StoryStatus status = StoryStatus.ACTIVE;
        private String title;
        private String description;
        private int originTileId;
        private long originTick;
        private double baseProbability = 0.9;
        private int hopCount = 0;
        private int maxHops = 6;
        private int priority = 5;
        private long lastProcessedTick = 0;
        private Set<String> affectedRegions = new HashSet<>();
        private Map<String, Object> metadata = new HashMap<>();

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder storyType(StoryType storyType) {
            this.storyType = storyType;
            return this;
        }

        public Builder status(StoryStatus status) {
            this.status = status;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
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

        public Builder addAffectedRegion(String regionId) {
            this.affectedRegions.add(regionId);
            return this;
        }

        public Builder metadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public Story build() {
            if (id == null || id.isEmpty()) {
                throw new IllegalArgumentException("Story must have a valid id");
            }
            if (storyType == null) {
                throw new IllegalArgumentException("Story must have a storyType");
            }
            if (title == null || title.isEmpty()) {
                throw new IllegalArgumentException("Story must have a title");
            }
            if (baseProbability < 0.0 || baseProbability > 1.0) {
                throw new IllegalArgumentException("baseProbability must be between 0.0 and 1.0");
            }
            if (priority < 0 || priority > 10) {
                throw new IllegalArgumentException("priority must be between 0 and 10");
            }
            return new Story(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Story story = (Story) o;
        return Objects.equals(id, story.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Story{" +
                "id='" + id + '\'' +
                ", storyType=" + storyType +
                ", status=" + status +
                ", title='" + title + '\'' +
                ", priority=" + priority +
                ", hopCount=" + hopCount +
                ", maxHops=" + maxHops +
                '}';
    }
}
