package org.adventure.items;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ItemPrototype defines the template for creating item instances.
 * Each prototype represents a type of item with default properties.
 */
public class ItemPrototype {
    private final String id;
    private final String name;
    private final ItemCategory category;
    private final String description;
    
    // Base stats
    private final float maxDurability;
    private final int baseValue; // in currency units
    private final float weight;
    
    // Item properties (damage, armor rating, tool efficiency, etc.)
    private final Map<String, Object> properties;
    
    // Rarity affects crafting, drops, and value
    private final ItemRarity rarity;
    
    // Can this item be repaired, stacked, enchanted, etc.
    private final boolean repairable;
    private final boolean stackable;
    private final int maxStackSize;
    private final boolean enchantable;
    
    private ItemPrototype(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.category = builder.category;
        this.description = builder.description;
        this.maxDurability = builder.maxDurability;
        this.baseValue = builder.baseValue;
        this.weight = builder.weight;
        this.properties = new HashMap<>(builder.properties);
        this.rarity = builder.rarity;
        this.repairable = builder.repairable;
        this.stackable = builder.stackable;
        this.maxStackSize = builder.maxStackSize;
        this.enchantable = builder.enchantable;
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public ItemCategory getCategory() { return category; }
    public String getDescription() { return description; }
    public float getMaxDurability() { return maxDurability; }
    public int getBaseValue() { return baseValue; }
    public float getWeight() { return weight; }
    public Map<String, Object> getProperties() { return new HashMap<>(properties); }
    public Object getProperty(String key) { return properties.get(key); }
    public ItemRarity getRarity() { return rarity; }
    public boolean isRepairable() { return repairable; }
    public boolean isStackable() { return stackable; }
    public int getMaxStackSize() { return maxStackSize; }
    public boolean isEnchantable() { return enchantable; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPrototype that = (ItemPrototype) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("ItemPrototype{id='%s', name='%s', category=%s, rarity=%s}",
                id, name, category, rarity);
    }
    
    // Builder pattern for flexible construction
    public static class Builder {
        private String id;
        private String name;
        private ItemCategory category;
        private String description = "";
        private float maxDurability = 100.0f;
        private int baseValue = 1;
        private float weight = 1.0f;
        private Map<String, Object> properties = new HashMap<>();
        private ItemRarity rarity = ItemRarity.COMMON;
        private boolean repairable = true;
        private boolean stackable = false;
        private int maxStackSize = 1;
        private boolean enchantable = false;
        
        public Builder(String id, String name, ItemCategory category) {
            this.id = id;
            this.name = name;
            this.category = category;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder maxDurability(float maxDurability) {
            this.maxDurability = maxDurability;
            return this;
        }
        
        public Builder baseValue(int baseValue) {
            this.baseValue = baseValue;
            return this;
        }
        
        public Builder weight(float weight) {
            this.weight = weight;
            return this;
        }
        
        public Builder property(String key, Object value) {
            this.properties.put(key, value);
            return this;
        }
        
        public Builder properties(Map<String, Object> properties) {
            this.properties.putAll(properties);
            return this;
        }
        
        public Builder rarity(ItemRarity rarity) {
            this.rarity = rarity;
            return this;
        }
        
        public Builder repairable(boolean repairable) {
            this.repairable = repairable;
            return this;
        }
        
        public Builder stackable(boolean stackable, int maxStackSize) {
            this.stackable = stackable;
            this.maxStackSize = maxStackSize;
            return this;
        }
        
        public Builder enchantable(boolean enchantable) {
            this.enchantable = enchantable;
            return this;
        }
        
        public ItemPrototype build() {
            Objects.requireNonNull(id, "id cannot be null");
            Objects.requireNonNull(name, "name cannot be null");
            Objects.requireNonNull(category, "category cannot be null");
            
            if (maxDurability < 0) {
                throw new IllegalArgumentException("maxDurability must be non-negative");
            }
            if (baseValue < 0) {
                throw new IllegalArgumentException("baseValue must be non-negative");
            }
            if (weight < 0) {
                throw new IllegalArgumentException("weight must be non-negative");
            }
            if (maxStackSize < 1) {
                throw new IllegalArgumentException("maxStackSize must be at least 1");
            }
            
            return new ItemPrototype(this);
        }
    }
}
