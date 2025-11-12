package org.adventure.structure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a permission entry mapping an access role to an access level.
 * Immutable value object.
 * 
 * @see AccessRole
 * @see AccessLevel
 * @see Structure
 */
public final class Permission {
    private final AccessRole role;
    private final AccessLevel level;
    
    @JsonCreator
    public Permission(
            @JsonProperty("role") AccessRole role,
            @JsonProperty("level") AccessLevel level) {
        if (role == null || level == null) {
            throw new IllegalArgumentException("Role and level cannot be null");
        }
        this.role = role;
        this.level = level;
    }
    
    public AccessRole getRole() {
        return role;
    }
    
    public AccessLevel getLevel() {
        return level;
    }
    
    /**
     * Check if this permission allows the given access level.
     */
    public boolean allows(AccessLevel requiredLevel) {
        return level.allows(requiredLevel);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Permission)) return false;
        Permission other = (Permission) obj;
        return role == other.role && level == other.level;
    }
    
    @Override
    public int hashCode() {
        return 31 * role.hashCode() + level.hashCode();
    }
    
    @Override
    public String toString() {
        return "Permission{role=" + role + ", level=" + level + "}";
    }
}
