package org.adventure.story;

/**
 * Status of a story in its lifecycle.
 */
public enum StoryStatus {
    /**
     * Story is actively spreading and affecting the world.
     */
    ACTIVE,
    
    /**
     * Story is dormant, waiting for trigger conditions.
     */
    DORMANT,
    
    /**
     * Story has been completed or concluded.
     */
    RESOLVED,
    
    /**
     * Story has been archived for historical reference.
     */
    ARCHIVED,
    
    /**
     * Story was proven false or abandoned.
     */
    DISCREDITED
}
