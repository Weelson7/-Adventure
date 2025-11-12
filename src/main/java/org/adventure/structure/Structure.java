package org.adventure.structure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.*;

/**
 * Represents a physical structure in the game world.
 * Structures can be owned, damaged, upgraded, and contain rooms.
 * Follows single-owner model with separate access permissions.
 * 
 * Design: docs/structures_ownership.md
 * Data Model: docs/data_models.md
 * 
 * @see StructureType
 * @see OwnerType
 * @see Room
 * @see Upgrade
 * @see Permission
 */
public final class Structure {
    private final String id;
    private final StructureType type;
    private String ownerId;
    private OwnerType ownerType;
    private final String locationTileId;
    private double health;
    private final double maxHealth;
    private final List<Room> rooms;
    private final List<Upgrade> upgrades;
    private final Map<AccessRole, AccessLevel> permissions;
    private final int createdAtTick;
    private int lastUpdatedTick;
    private final int schemaVersion;
    
    @JsonCreator
    public Structure(
            @JsonProperty("id") String id,
            @JsonProperty("type") StructureType type,
            @JsonProperty("ownerId") String ownerId,
            @JsonProperty("ownerType") OwnerType ownerType,
            @JsonProperty("locationTileId") String locationTileId,
            @JsonProperty("health") double health,
            @JsonProperty("maxHealth") double maxHealth,
            @JsonProperty("rooms") List<Room> rooms,
            @JsonProperty("upgrades") List<Upgrade> upgrades,
            @JsonProperty("permissions") Map<AccessRole, AccessLevel> permissions,
            @JsonProperty("createdAtTick") int createdAtTick,
            @JsonProperty("lastUpdatedTick") int lastUpdatedTick,
            @JsonProperty("schemaVersion") int schemaVersion) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Structure ID cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Structure type cannot be null");
        }
        if (locationTileId == null || locationTileId.isEmpty()) {
            throw new IllegalArgumentException("Location tile ID cannot be null or empty");
        }
        if (maxHealth <= 0) {
            throw new IllegalArgumentException("Max health must be positive");
        }
        if (health < 0) {
            throw new IllegalArgumentException("Health cannot be negative");
        }
        if (health > maxHealth) {
            throw new IllegalArgumentException("Health cannot exceed max health");
        }
        
        this.id = id;
        this.type = type;
        this.ownerId = ownerId;
        this.ownerType = ownerType != null ? ownerType : OwnerType.NONE;
        this.locationTileId = locationTileId;
        this.health = health;
        this.maxHealth = maxHealth;
        this.rooms = rooms != null ? new ArrayList<>(rooms) : new ArrayList<>();
        this.upgrades = upgrades != null ? new ArrayList<>(upgrades) : new ArrayList<>();
        this.permissions = permissions != null ? new HashMap<>(permissions) : new HashMap<>();
        this.createdAtTick = createdAtTick;
        this.lastUpdatedTick = lastUpdatedTick;
        this.schemaVersion = schemaVersion;
        
        // Ensure owner always has FULL access
        if (this.ownerId != null && !this.ownerId.isEmpty()) {
            this.permissions.put(AccessRole.OWNER, AccessLevel.FULL);
        }
    }
    
    /**
     * Builder for creating Structure instances.
     */
    public static class Builder {
        private String id;
        private StructureType type;
        private String ownerId;
        private OwnerType ownerType = OwnerType.NONE;
        private String locationTileId;
        private double health = 100.0;
        private double maxHealth = 100.0;
        private List<Room> rooms = new ArrayList<>();
        private List<Upgrade> upgrades = new ArrayList<>();
        private Map<AccessRole, AccessLevel> permissions = new HashMap<>();
        private int createdAtTick = 0;
        private int lastUpdatedTick = 0;
        private int schemaVersion = 1;
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder type(StructureType type) {
            this.type = type;
            return this;
        }
        
        public Builder ownerId(String ownerId) {
            this.ownerId = ownerId;
            return this;
        }
        
        public Builder ownerType(OwnerType ownerType) {
            this.ownerType = ownerType;
            return this;
        }
        
        public Builder locationTileId(String locationTileId) {
            this.locationTileId = locationTileId;
            return this;
        }
        
        public Builder health(double health) {
            this.health = health;
            return this;
        }
        
        public Builder maxHealth(double maxHealth) {
            this.maxHealth = maxHealth;
            return this;
        }
        
        public Builder rooms(List<Room> rooms) {
            this.rooms = new ArrayList<>(rooms);
            return this;
        }
        
        public Builder addRoom(Room room) {
            this.rooms.add(room);
            return this;
        }
        
        public Builder upgrades(List<Upgrade> upgrades) {
            this.upgrades = new ArrayList<>(upgrades);
            return this;
        }
        
        public Builder addUpgrade(Upgrade upgrade) {
            this.upgrades.add(upgrade);
            return this;
        }
        
        public Builder permissions(Map<AccessRole, AccessLevel> permissions) {
            this.permissions = new HashMap<>(permissions);
            return this;
        }
        
        public Builder permission(AccessRole role, AccessLevel level) {
            this.permissions.put(role, level);
            return this;
        }
        
        public Builder createdAtTick(int createdAtTick) {
            this.createdAtTick = createdAtTick;
            return this;
        }
        
        public Builder lastUpdatedTick(int lastUpdatedTick) {
            this.lastUpdatedTick = lastUpdatedTick;
            return this;
        }
        
        public Builder schemaVersion(int schemaVersion) {
            this.schemaVersion = schemaVersion;
            return this;
        }
        
        public Structure build() {
            return new Structure(id, type, ownerId, ownerType, locationTileId,
                               health, maxHealth, rooms, upgrades, permissions,
                               createdAtTick, lastUpdatedTick, schemaVersion);
        }
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public StructureType getType() {
        return type;
    }
    
    public String getOwnerId() {
        return ownerId;
    }
    
    public OwnerType getOwnerType() {
        return ownerType;
    }
    
    public String getLocationTileId() {
        return locationTileId;
    }
    
    public double getHealth() {
        return health;
    }
    
    public double getMaxHealth() {
        return maxHealth;
    }
    
    public List<Room> getRooms() {
        return Collections.unmodifiableList(rooms);
    }
    
    public List<Upgrade> getUpgrades() {
        return Collections.unmodifiableList(upgrades);
    }
    
    public Map<AccessRole, AccessLevel> getPermissions() {
        return Collections.unmodifiableMap(permissions);
    }
    
    public int getCreatedAtTick() {
        return createdAtTick;
    }
    
    public int getLastUpdatedTick() {
        return lastUpdatedTick;
    }
    
    public int getSchemaVersion() {
        return schemaVersion;
    }
    
    /**
     * Check if structure is destroyed (health at 0).
     */
    public boolean isDestroyed() {
        return health <= 0;
    }
    
    /**
     * Check if structure is damaged (health below max).
     */
    public boolean isDamaged() {
        return health < maxHealth;
    }
    
    /**
     * Get the health percentage (0.0 to 1.0).
     */
    public double getHealthPercentage() {
        return health / maxHealth;
    }
    
    /**
     * Apply damage to the structure.
     * Health cannot go below 0.
     * 
     * @param amount Amount of damage to apply (positive value)
     * @param currentTick Current game tick
     */
    public void takeDamage(double amount, int currentTick) {
        if (amount < 0) {
            throw new IllegalArgumentException("Damage amount cannot be negative");
        }
        health = Math.max(0, health - amount);
        lastUpdatedTick = currentTick;
    }
    
    /**
     * Repair the structure.
     * Health cannot exceed maxHealth.
     * 
     * @param amount Amount of health to restore (positive value)
     * @param currentTick Current game tick
     */
    public void repair(double amount, int currentTick) {
        if (amount < 0) {
            throw new IllegalArgumentException("Repair amount cannot be negative");
        }
        if (isDestroyed()) {
            throw new IllegalStateException("Cannot repair destroyed structure");
        }
        health = Math.min(maxHealth, health + amount);
        lastUpdatedTick = currentTick;
    }
    
    /**
     * Add a room to the structure.
     * 
     * @param room The room to add
     * @param currentTick Current game tick
     */
    public void addRoom(Room room, int currentTick) {
        if (room == null) {
            throw new IllegalArgumentException("Room cannot be null");
        }
        rooms.add(room);
        lastUpdatedTick = currentTick;
    }
    
    /**
     * Apply an upgrade to the structure.
     * 
     * @param upgrade The upgrade to apply
     * @param currentTick Current game tick
     */
    public void applyUpgrade(Upgrade upgrade, int currentTick) {
        if (upgrade == null) {
            throw new IllegalArgumentException("Upgrade cannot be null");
        }
        upgrades.add(upgrade);
        lastUpdatedTick = currentTick;
    }
    
    /**
     * Transfer ownership to a new owner.
     * Updates ownerId, ownerType, and resets permissions to grant new owner FULL access.
     * 
     * @param newOwnerId New owner ID
     * @param newOwnerType New owner type
     * @param currentTick Current game tick
     */
    public void transferOwnership(String newOwnerId, OwnerType newOwnerType, int currentTick) {
        if (newOwnerId == null || newOwnerId.isEmpty()) {
            throw new IllegalArgumentException("New owner ID cannot be null or empty");
        }
        if (newOwnerType == null) {
            throw new IllegalArgumentException("New owner type cannot be null");
        }
        
        this.ownerId = newOwnerId;
        this.ownerType = newOwnerType;
        this.permissions.clear();
        this.permissions.put(AccessRole.OWNER, AccessLevel.FULL);
        this.lastUpdatedTick = currentTick;
    }
    
    /**
     * Set permission for a specific role.
     * 
     * @param role The access role
     * @param level The access level to grant
     * @param currentTick Current game tick
     */
    public void setPermission(AccessRole role, AccessLevel level, int currentTick) {
        if (role == null || level == null) {
            throw new IllegalArgumentException("Role and level cannot be null");
        }
        // Owner always has FULL access (cannot be changed)
        if (role == AccessRole.OWNER) {
            permissions.put(role, AccessLevel.FULL);
        } else {
            permissions.put(role, level);
        }
        lastUpdatedTick = currentTick;
    }
    
    /**
     * Check if a given role has the required access level.
     * 
     * @param role The role to check
     * @param requiredLevel The minimum required access level
     * @return true if the role has at least the required access level
     */
    public boolean hasAccess(AccessRole role, AccessLevel requiredLevel) {
        AccessLevel grantedLevel = permissions.getOrDefault(role, AccessLevel.NONE);
        return grantedLevel.allows(requiredLevel);
    }
    
    /**
     * Get the access level for a specific role.
     * 
     * @param role The access role
     * @return The access level granted to this role
     */
    public AccessLevel getAccessLevel(AccessRole role) {
        return permissions.getOrDefault(role, AccessLevel.NONE);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Structure)) return false;
        Structure other = (Structure) obj;
        return Objects.equals(id, other.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Structure{id='" + id + "', type=" + type + 
               ", owner=" + ownerId + " (" + ownerType + ")" +
               ", health=" + health + "/" + maxHealth +
               ", location='" + locationTileId + "'}";
    }
}
