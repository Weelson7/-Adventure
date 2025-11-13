package org.adventure.story;

/**
 * Story types define the category and flavor of stories in the world.
 * Each type has different characteristics and can spawn in different contexts.
 */
public enum StoryType {
    /**
     * Legendary tales of heroes, artifacts, and world-shaping events.
     * High priority, long-lasting, wide propagation.
     */
    LEGEND,
    
    /**
     * Unverified information, gossip, and hearsay.
     * Lower priority, may be false, spreads quickly but fades fast.
     */
    RUMOR,
    
    /**
     * Player or NPC objectives, tasks, and missions.
     * Medium priority, targeted propagation, resolution-based.
     */
    QUEST,
    
    /**
     * Predictions and foreshadowing of future events.
     * High priority, cryptic, triggers world events.
     */
    PROPHECY,
    
    /**
     * Dark tales of loss, betrayal, and downfall.
     * Medium priority, can affect NPC morale and behavior.
     */
    TRAGEDY,
    
    /**
     * Humorous anecdotes and lighthearted tales.
     * Low priority, morale boost, fast propagation in peaceful regions.
     */
    COMEDY,
    
    /**
     * Unsolved puzzles, hidden treasures, and enigmas.
     * Medium-high priority, triggers investigation events.
     */
    MYSTERY
}
