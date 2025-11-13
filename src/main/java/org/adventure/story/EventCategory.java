package org.adventure.story;

/**
 * Event categories define the scope and impact level of events.
 */
public enum EventCategory {
    /**
     * Global events affecting the entire world (e.g., apocalyptic prophecy, world boss).
     */
    WORLD,
    
    /**
     * Regional events affecting one or more regions (e.g., plague, festival, war).
     */
    REGIONAL,
    
    /**
     * Personal events targeting specific NPCs or players (e.g., quest offer, vendetta).
     */
    PERSONAL,
    
    /**
     * Random spontaneous events for variety and unpredictability.
     */
    RANDOM,
    
    /**
     * Events triggered by player or NPC actions (e.g., killing a boss, completing a quest).
     */
    TRIGGERED
}
