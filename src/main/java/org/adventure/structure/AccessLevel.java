package org.adventure.structure;

/**
 * Enumeration of access levels for structure permissions.
 * Defines what actions can be performed by a given role.
 * 
 * @see Permission
 * @see AccessRole
 */
public enum AccessLevel {
    /** No access - cannot interact with structure */
    NONE(0),
    
    /** Read-only - can view structure details */
    READ(1),
    
    /** Use - can interact with structure features (e.g., rest at inn) */
    USE(2),
    
    /** Modify - can change contents, perform repairs */
    MODIFY(3),
    
    /** Manage - can change permissions, initiate upgrades */
    MANAGE(4),
    
    /** Full control - can transfer ownership, destroy structure */
    FULL(5);
    
    private final int level;
    
    AccessLevel(int level) {
        this.level = level;
    }
    
    public int getLevel() {
        return level;
    }
    
    /**
     * Check if this access level allows at least the given level.
     * @param required The minimum required access level
     * @return true if this level >= required level
     */
    public boolean allows(AccessLevel required) {
        return this.level >= required.level;
    }
}
