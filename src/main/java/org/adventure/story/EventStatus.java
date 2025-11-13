package org.adventure.story;

/**
 * Status of an event in its lifecycle.
 */
public enum EventStatus {
    /**
     * Event is pending trigger conditions.
     */
    PENDING,
    
    /**
     * Event is actively occurring and affecting the world.
     */
    ACTIVE,
    
    /**
     * Event is propagating to other regions.
     */
    PROPAGATING,
    
    /**
     * Event has completed its effects.
     */
    COMPLETED,
    
    /**
     * Event was interrupted or cancelled.
     */
    CANCELLED
}
