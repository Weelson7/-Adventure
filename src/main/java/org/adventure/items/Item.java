package org.adventure.items;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Item represents a specific instance of an ItemPrototype.
 * Each item has a unique ID, durability state, and can have custom properties.
 */
public class Item {
    private final String id;
    private final String prototypeId;
    private String ownerId; // Character or container ID
    
    // Durability tracking
    private float currentDurability;
    private final float maxDurability;
    
    // Stack tracking for stackable items
    private int quantity;
    
    // Custom properties (enchantments, modifications, etc.)
    private final Map<String, Object> customProperties;
    
    // Legacy/story tracking (optional)
    private String historyReferenceId;
    
    // Evolution tracking
    private int evolutionPoints;
    
    // Timestamps
    private final long createdAtTick;
    private long lastModifiedTick;
    
    // Schema version for persistence
    private final int schemaVersion = 1;
    
    private Item(Builder builder) {
        this.id = builder.id;
        this.prototypeId = builder.prototypeId;
        this.ownerId = builder.ownerId;
        this.currentDurability = builder.currentDurability;
        this.maxDurability = builder.maxDurability;
        this.quantity = builder.quantity;
        this.customProperties = new HashMap<>(builder.customProperties);
        this.historyReferenceId = builder.historyReferenceId;
        this.evolutionPoints = builder.evolutionPoints;
        this.createdAtTick = builder.createdAtTick;
        this.lastModifiedTick = builder.lastModifiedTick;
    }
    
    // Getters
    public String getId() { return id; }
    public String getPrototypeId() { return prototypeId; }
    public String getOwnerId() { return ownerId; }
    public float getCurrentDurability() { return currentDurability; }
    public float getMaxDurability() { return maxDurability; }
    public int getQuantity() { return quantity; }
    public Map<String, Object> getCustomProperties() { return new HashMap<>(customProperties); }
    public Object getCustomProperty(String key) { return customProperties.get(key); }
    public String getHistoryReferenceId() { return historyReferenceId; }
    public int getEvolutionPoints() { return evolutionPoints; }
    public long getCreatedAtTick() { return createdAtTick; }
    public long getLastModifiedTick() { return lastModifiedTick; }
    public int getSchemaVersion() { return schemaVersion; }
    
    // Setters for mutable state
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
        this.lastModifiedTick = System.currentTimeMillis();
    }
    
    public void setHistoryReferenceId(String historyReferenceId) {
        this.historyReferenceId = historyReferenceId;
        this.lastModifiedTick = System.currentTimeMillis();
    }
    
    public void setCustomProperty(String key, Object value) {
        this.customProperties.put(key, value);
        this.lastModifiedTick = System.currentTimeMillis();
    }
    
    /**
     * Checks if the item is broken (durability at or below 0).
     */
    public boolean isBroken() {
        return currentDurability <= 0;
    }
    
    /**
     * Gets the durability as a percentage (0.0 to 1.0).
     */
    public float getDurabilityPercent() {
        if (maxDurability <= 0) return 1.0f;
        return Math.max(0.0f, Math.min(1.0f, currentDurability / maxDurability));
    }
    
    /**
     * Damages the item, reducing its durability.
     * Returns true if the item breaks (reaches 0 durability).
     */
    public boolean damage(float amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Damage amount must be non-negative");
        }
        
        currentDurability = Math.max(0, currentDurability - amount);
        lastModifiedTick = System.currentTimeMillis();
        
        return isBroken();
    }
    
    /**
     * Repairs the item, increasing its durability up to max.
     * Returns the actual amount repaired.
     */
    public float repair(float amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Repair amount must be non-negative");
        }
        if (isBroken()) {
            throw new IllegalStateException("Cannot repair broken items");
        }
        
        float oldDurability = currentDurability;
        currentDurability = Math.min(maxDurability, currentDurability + amount);
        lastModifiedTick = System.currentTimeMillis();
        
        return currentDurability - oldDurability;
    }
    
    /**
     * Repairs the item to full durability.
     * Returns the amount repaired.
     */
    public float repairFully() {
        float amount = maxDurability - currentDurability;
        currentDurability = maxDurability;
        lastModifiedTick = System.currentTimeMillis();
        return amount;
    }
    
    /**
     * Increases quantity for stackable items.
     * Returns true if successful.
     */
    public boolean increaseQuantity(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }
        quantity += amount;
        lastModifiedTick = System.currentTimeMillis();
        return true;
    }
    
    /**
     * Decreases quantity for stackable items.
     * Returns true if successful, false if not enough quantity.
     */
    public boolean decreaseQuantity(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }
        if (amount > quantity) {
            return false;
        }
        quantity -= amount;
        lastModifiedTick = System.currentTimeMillis();
        return true;
    }
    
    /**
     * Adds evolution points from usage.
     */
    public void addEvolutionPoints(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Evolution points must be non-negative");
        }
        // Cap at 10,000 as per design doc
        this.evolutionPoints = Math.min(10000, this.evolutionPoints + points);
        lastModifiedTick = System.currentTimeMillis();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(id, item.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("Item{id='%s', prototype='%s', durability=%.1f/%.1f, quantity=%d}",
                id, prototypeId, currentDurability, maxDurability, quantity);
    }
    
    // Builder pattern
    public static class Builder {
        private String id = UUID.randomUUID().toString();
        private String prototypeId;
        private String ownerId;
        private float currentDurability;
        private float maxDurability;
        private int quantity = 1;
        private Map<String, Object> customProperties = new HashMap<>();
        private String historyReferenceId;
        private int evolutionPoints = 0;
        private long createdAtTick;
        private long lastModifiedTick;
        
        public Builder(String prototypeId, float maxDurability) {
            this.prototypeId = prototypeId;
            this.maxDurability = maxDurability;
            this.currentDurability = maxDurability; // Start at full durability
            long now = System.currentTimeMillis();
            this.createdAtTick = now;
            this.lastModifiedTick = now;
        }
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder ownerId(String ownerId) {
            this.ownerId = ownerId;
            return this;
        }
        
        public Builder currentDurability(float currentDurability) {
            this.currentDurability = currentDurability;
            return this;
        }
        
        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }
        
        public Builder customProperty(String key, Object value) {
            this.customProperties.put(key, value);
            return this;
        }
        
        public Builder customProperties(Map<String, Object> properties) {
            this.customProperties.putAll(properties);
            return this;
        }
        
        public Builder historyReferenceId(String historyReferenceId) {
            this.historyReferenceId = historyReferenceId;
            return this;
        }
        
        public Builder evolutionPoints(int evolutionPoints) {
            this.evolutionPoints = evolutionPoints;
            return this;
        }
        
        public Builder createdAtTick(long createdAtTick) {
            this.createdAtTick = createdAtTick;
            return this;
        }
        
        public Builder lastModifiedTick(long lastModifiedTick) {
            this.lastModifiedTick = lastModifiedTick;
            return this;
        }
        
        public Item build() {
            Objects.requireNonNull(prototypeId, "prototypeId cannot be null");
            
            if (maxDurability < 0) {
                throw new IllegalArgumentException("maxDurability must be non-negative");
            }
            if (currentDurability < 0) {
                throw new IllegalArgumentException("currentDurability must be non-negative");
            }
            if (quantity < 1) {
                throw new IllegalArgumentException("quantity must be at least 1");
            }
            
            return new Item(this);
        }
    }
    
    /**
     * Factory method to create an item from a prototype.
     */
    public static Item fromPrototype(ItemPrototype prototype) {
        return new Builder(prototype.getId(), prototype.getMaxDurability())
                .quantity(1)
                .build();
    }
    
    /**
     * Factory method to create a stackable item from a prototype.
     */
    public static Item fromPrototype(ItemPrototype prototype, int quantity) {
        if (!prototype.isStackable()) {
            throw new IllegalArgumentException("Cannot create stacked items from non-stackable prototype");
        }
        if (quantity > prototype.getMaxStackSize()) {
            throw new IllegalArgumentException("Quantity exceeds max stack size");
        }
        
        return new Builder(prototype.getId(), prototype.getMaxDurability())
                .quantity(quantity)
                .build();
    }
}
