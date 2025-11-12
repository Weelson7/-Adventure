package org.adventure.structure;

/**
 * Enumeration of valid owner types for structures.
 * Single-owner model: each structure has exactly one owner of one of these types.
 * 
 * @see Structure
 * @see Ownership
 */
public enum OwnerType {
    /** Individual character ownership */
    CHARACTER,
    
    /** Clan or family ownership */
    CLAN,
    
    /** Society or kingdom ownership */
    SOCIETY,
    
    /** No owner (abandoned or unclaimed) */
    NONE,
    
    /** Government/state ownership */
    GOVERNMENT
}
