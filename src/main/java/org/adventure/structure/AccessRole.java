package org.adventure.structure;

/**
 * Enumeration of access roles for structure permissions.
 * Access is separate from ownership - these roles determine who can do what.
 * 
 * @see Permission
 * @see AccessLevel
 */
public enum AccessRole {
    /** The structure owner (full control) */
    OWNER,
    
    /** Member of owner's clan */
    CLAN_MEMBER,
    
    /** Allied faction or individual */
    ALLY,
    
    /** General public access */
    PUBLIC,
    
    /** Explicitly granted guest access */
    GUEST,
    
    /** Hostile or banned entity */
    HOSTILE
}
