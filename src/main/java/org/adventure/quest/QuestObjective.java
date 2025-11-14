package org.adventure.quest;

import java.util.Objects;

/**
 * Represents a single objective within a quest.
 */
public class QuestObjective {
    private final String id;
    private final String description;
    private final String targetType; // e.g., "location", "item", "npc", "structure"
    private final String targetId;
    private final int targetX; // Target X coordinate for location objectives
    private final int targetY; // Target Y coordinate for location objectives
    private final int requiredCount;
    private int currentCount;
    private boolean completed;
    
    private QuestObjective(Builder builder) {
        this.id = builder.id;
        this.description = builder.description;
        this.targetType = builder.targetType;
        this.targetId = builder.targetId;
        this.targetX = builder.targetX;
        this.targetY = builder.targetY;
        this.requiredCount = builder.requiredCount;
        this.currentCount = builder.currentCount;
        this.completed = builder.completed;
    }
    
    // Getters
    public String getId() { return id; }
    public String getDescription() { return description; }
    public String getTargetType() { return targetType; }
    public String getTargetId() { return targetId; }
    public int getTargetX() { return targetX; }
    public int getTargetY() { return targetY; }
    public int getRequiredCount() { return requiredCount; }
    public int getCurrentCount() { return currentCount; }
    public boolean isCompleted() { return completed; }
    
    // Setters
    public void setCurrentCount(int currentCount) {
        this.currentCount = currentCount;
        if (currentCount >= requiredCount) {
            this.completed = true;
        }
    }
    
    public void incrementCount() {
        setCurrentCount(currentCount + 1);
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    /**
     * Gets completion percentage (0-100).
     * 
     * @return Percentage complete
     */
    public int getCompletionPercentage() {
        if (requiredCount == 0) return 100;
        return Math.min(100, (currentCount * 100) / requiredCount);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestObjective that = (QuestObjective) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("QuestObjective{id='%s', description='%s', progress=%d/%d}",
            id, description, currentCount, requiredCount);
    }
    
    /**
     * Builder for creating QuestObjective instances.
     */
    public static class Builder {
        private String id;
        private String description;
        private String targetType;
        private String targetId;
        private int targetX = -1; // -1 means no specific location
        private int targetY = -1;
        private int requiredCount = 1;
        private int currentCount = 0;
        private boolean completed = false;
        
        public Builder id(String id) { this.id = id; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder targetType(String targetType) { this.targetType = targetType; return this; }
        public Builder targetId(String targetId) { this.targetId = targetId; return this; }
        public Builder targetX(int targetX) { this.targetX = targetX; return this; }
        public Builder targetY(int targetY) { this.targetY = targetY; return this; }
        public Builder requiredCount(int requiredCount) { this.requiredCount = requiredCount; return this; }
        public Builder currentCount(int currentCount) { this.currentCount = currentCount; return this; }
        public Builder completed(boolean completed) { this.completed = completed; return this; }
        
        public QuestObjective build() {
            Objects.requireNonNull(id, "id cannot be null");
            Objects.requireNonNull(description, "description cannot be null");
            
            return new QuestObjective(this);
        }
    }
}
