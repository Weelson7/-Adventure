package org.adventure.quest;

import java.util.Objects;

/**
 * Represents a reward given upon quest completion.
 */
public class QuestReward {
    private final String type; // e.g., "gold", "item", "reputation", "experience"
    private final String targetId; // Item ID, clan ID, etc.
    private final int amount;
    private final String description;
    
    private QuestReward(Builder builder) {
        this.type = builder.type;
        this.targetId = builder.targetId;
        this.amount = builder.amount;
        this.description = builder.description;
    }
    
    // Getters
    public String getType() { return type; }
    public String getTargetId() { return targetId; }
    public int getAmount() { return amount; }
    public String getDescription() { return description; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestReward that = (QuestReward) o;
        return amount == that.amount &&
               Objects.equals(type, that.type) &&
               Objects.equals(targetId, that.targetId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, targetId, amount);
    }
    
    @Override
    public String toString() {
        return String.format("QuestReward{type='%s', amount=%d, description='%s'}",
            type, amount, description);
    }
    
    /**
     * Builder for creating QuestReward instances.
     */
    public static class Builder {
        private String type;
        private String targetId;
        private int amount;
        private String description;
        
        public Builder type(String type) { this.type = type; return this; }
        public Builder targetId(String targetId) { this.targetId = targetId; return this; }
        public Builder amount(int amount) { this.amount = amount; return this; }
        public Builder value(int value) { this.amount = value; return this; } // Alias for amount
        public Builder description(String description) { this.description = description; return this; }
        
        public QuestReward build() {
            Objects.requireNonNull(type, "type cannot be null");
            
            return new QuestReward(this);
        }
    }
}
