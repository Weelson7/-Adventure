package org.adventure.quest;

import java.util.*;

/**
 * Represents a quest/mission that players can undertake.
 * Quests can be generated from world features, stories, or dynamic events.
 */
public class Quest {
    private final String id;
    private final String title;
    private final String description;
    private final QuestType type;
    private QuestStatus status;
    private final List<QuestObjective> objectives;
    private final List<QuestReward> rewards;
    private final String linkedFeatureId;
    private final String linkedStoryId;
    private final int requiredLevel;
    private final long expirationTick;
    
    private Quest(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.description = builder.description;
        this.type = builder.type;
        this.status = builder.status;
        this.objectives = new ArrayList<>(builder.objectives);
        this.rewards = new ArrayList<>(builder.rewards);
        this.linkedFeatureId = builder.linkedFeatureId;
        this.linkedStoryId = builder.linkedStoryId;
        this.requiredLevel = builder.requiredLevel;
        this.expirationTick = builder.expirationTick;
    }
    
    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public QuestType getType() { return type; }
    public QuestStatus getStatus() { return status; }
    public List<QuestObjective> getObjectives() { return new ArrayList<>(objectives); }
    public List<QuestReward> getRewards() { return new ArrayList<>(rewards); }
    public String getLinkedFeatureId() { return linkedFeatureId; }
    public String getLinkedStoryId() { return linkedStoryId; }
    public int getRequiredLevel() { return requiredLevel; }
    public long getExpirationTick() { return expirationTick; }
    
    // Setters
    public void setStatus(QuestStatus status) { this.status = status; }
    
    /**
     * Checks if all objectives are completed.
     * 
     * @return true if quest is completable
     */
    public boolean isCompletable() {
        return objectives.stream().allMatch(QuestObjective::isCompleted);
    }
    
    /**
     * Checks if quest has expired.
     * 
     * @param currentTick Current game tick
     * @return true if expired
     */
    public boolean isExpired(long currentTick) {
        return expirationTick > 0 && currentTick >= expirationTick;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quest quest = (Quest) o;
        return Objects.equals(id, quest.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("Quest{id='%s', title='%s', type=%s, status=%s}",
            id, title, type, status);
    }
    
    /**
     * Builder for creating Quest instances.
     */
    public static class Builder {
        private String id;
        private String title;
        private String description;
        private QuestType type;
        private QuestStatus status = QuestStatus.AVAILABLE;
        private List<QuestObjective> objectives = new ArrayList<>();
        private List<QuestReward> rewards = new ArrayList<>();
        private String linkedFeatureId;
        private String linkedStoryId;
        private int requiredLevel = 0;
        private long expirationTick = 0;
        
        public Builder id(String id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder type(QuestType type) { this.type = type; return this; }
        public Builder status(QuestStatus status) { this.status = status; return this; }
        public Builder objectives(List<QuestObjective> objectives) {
            this.objectives = new ArrayList<>(objectives);
            return this;
        }
        public Builder addObjective(QuestObjective objective) {
            this.objectives.add(objective);
            return this;
        }
        public Builder rewards(List<QuestReward> rewards) {
            this.rewards = new ArrayList<>(rewards);
            return this;
        }
        public Builder addReward(QuestReward reward) {
            this.rewards.add(reward);
            return this;
        }
        public Builder linkedFeatureId(String linkedFeatureId) {
            this.linkedFeatureId = linkedFeatureId;
            return this;
        }
        public Builder linkedStoryId(String linkedStoryId) {
            this.linkedStoryId = linkedStoryId;
            return this;
        }
        public Builder requiredLevel(int requiredLevel) {
            this.requiredLevel = requiredLevel;
            return this;
        }
        public Builder expirationTick(long expirationTick) {
            this.expirationTick = expirationTick;
            return this;
        }
        
        public Builder schemaVersion(int schemaVersion) {
            // For now, ignore schema version in Quest (not in data model)
            // This method exists for API compatibility
            return this;
        }
        
        public Quest build() {
            Objects.requireNonNull(id, "id cannot be null");
            Objects.requireNonNull(title, "title cannot be null");
            Objects.requireNonNull(type, "type cannot be null");
            
            return new Quest(this);
        }
    }
}
