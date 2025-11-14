package org.adventure.quest;

/**
 * Represents the current status of a quest.
 */
public enum QuestStatus {
    /** Quest is available to be accepted */
    AVAILABLE,
    
    /** Quest has been accepted and is in progress */
    ACTIVE,
    
    /** Quest has been successfully completed */
    COMPLETED,
    
    /** Quest has failed or expired */
    FAILED,
    
    /** Quest is locked/hidden until prerequisites met */
    LOCKED
}
