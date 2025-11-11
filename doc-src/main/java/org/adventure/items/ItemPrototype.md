# ItemPrototype.java - Item Template System

**Package:** `org.adventure.items`  
**Source:** [ItemPrototype.java](../../../../../src/main/java/org/adventure/items/ItemPrototype.java)  
**Phase:** MVP Phase 1.4 (Items & Crafting)

## Overview

`ItemPrototype` serves as the immutable template from which all `Item` instances are created. It defines the default properties, statistics, and behaviors that all instances of a particular item type share. This separation between template (prototype) and instance (item) enables memory-efficient storage, consistent behavior across items, and flexible modding support.

This class implements the Prototype Pattern as specified in `docs/objects_crafting_legacy.md` and serves as the foundation for the game's item system.

## Design Philosophy

### The Prototype Pattern

**Problem**: Without prototypes, each item stores redundant data:
```java
// ❌ BAD: Redundant storage
Item sword1 = new Item("Iron Sword", 200, 50, 5.0f, ...);  // 100 bytes
Item sword2 = new Item("Iron Sword", 200, 50, 5.0f, ...);  // 100 bytes
// 1000 swords = 100 KB of duplicate data
```

**Solution**: Shared template + unique instances:
```java
// ✅ GOOD: Shared prototype
ItemPrototype ironSwordProto = new ItemPrototype.Builder(...)
    .maxDurability(200)
    .baseValue(50)
    .build();  // 100 bytes (shared)

Item sword1 = Item.fromPrototype(ironSwordProto);  // 70 bytes (instance data only)
Item sword2 = Item.fromPrototype(ironSwordProto);  // 70 bytes
// 1000 swords = 100 bytes + (70 × 1000) = 70.1 KB (30% savings)
```

### Immutability by Design

**Why Immutable?**
```java
private final String id;
private final String name;
private final float maxDurability;
// No setters!
```

**Advantages:**
- **Thread-Safe**: Multiple threads can read prototypes without locks
- **Cache-Friendly**: Prototypes never change, stay in CPU cache
- **Predictability**: Item behavior doesn't change mid-game
- **Modding Safety**: Mods can't accidentally corrupt base prototypes

**Alternative Considered:**
- Mutable prototypes with setters → rejected due to race conditions

## Class Structure

### Fields

```java
// Core identity
private final String id;                        // Unique ID ("iron_sword")
private final String name;                      // Display name ("Iron Sword")
private final String description;               // Flavor text
private final ItemCategory category;            // WEAPON, ARMOR, TOOL, etc.
private final ItemRarity rarity;                // COMMON, RARE, LEGENDARY, etc.

// Physical properties
private final float maxDurability;              // Max condition (0 = indestructible)
private final int baseValue;                    // Base gold value
private final float weight;                     // Kilograms (affects carry capacity)

// Stacking
private final boolean stackable;                // Can multiple exist in one slot?
private final int maxStackSize;                 // Max stack size (default 99)

// Extensible properties (for stats, requirements)
private final Map<String, Object> properties;   // Custom data (damage, armor, etc.)

// Asset references (future: 3D models, icons)
private final String iconPath;                  // UI icon ("assets/icons/iron_sword.png")
private final String modelPath;                 // 3D model path (future)

// Modding/persistence
private final int schemaVersion = 1;            // For migrations
```

## Builder Pattern

### Why Builder Pattern?

**Problem**: Many optional parameters lead to constructor hell:
```java
// ❌ BAD: Telescoping constructors
public ItemPrototype(String id, String name, ItemCategory category) { ... }
public ItemPrototype(String id, String name, ItemCategory category, float maxDurability) { ... }
public ItemPrototype(String id, String name, ItemCategory category, float maxDurability, int baseValue) { ... }
// ... 20 more constructors
```

**Solution**: Fluent builder interface:
```java
// ✅ GOOD: Readable, flexible
ItemPrototype sword = new ItemPrototype.Builder("iron_sword", "Iron Sword", ItemCategory.WEAPON)
    .description("A sturdy iron blade")
    .rarity(ItemRarity.COMMON)
    .maxDurability(200.0f)
    .baseValue(50)
    .weight(5.0f)
    .property("damage", 10)
    .property("attack_speed", 1.2f)
    .iconPath("assets/icons/iron_sword.png")
    .build();
```

### Builder Implementation

```java
public static class Builder {
    // Required fields
    private final String id;
    private final String name;
    private final ItemCategory category;
    
    // Optional fields with defaults
    private String description = "";
    private ItemRarity rarity = ItemRarity.COMMON;
    private float maxDurability = 100.0f;
    private int baseValue = 1;
    private float weight = 1.0f;
    private boolean stackable = false;
    private int maxStackSize = 99;
    private Map<String, Object> properties = new HashMap<>();
    private String iconPath = "";
    private String modelPath = "";
    
    public Builder(String id, String name, ItemCategory category) {
        this.id = id;
        this.name = name;
        this.category = category;
    }
    
    public Builder description(String description) {
        this.description = description;
        return this;
    }
    
    // ... more fluent setters ...
    
    public ItemPrototype build() {
        validate();
        return new ItemPrototype(this);
    }
}
```

### Validation

```java
private void validate() {
    Objects.requireNonNull(id, "id cannot be null");
    Objects.requireNonNull(name, "name cannot be null");
    Objects.requireNonNull(category, "category cannot be null");
    Objects.requireNonNull(rarity, "rarity cannot be null");
    
    if (id.trim().isEmpty()) {
        throw new IllegalArgumentException("id cannot be empty");
    }
    if (name.trim().isEmpty()) {
        throw new IllegalArgumentException("name cannot be empty");
    }
    if (maxDurability < 0) {
        throw new IllegalArgumentException("maxDurability must be non-negative");
    }
    if (baseValue < 0) {
        throw new IllegalArgumentException("baseValue must be non-negative");
    }
    if (weight < 0) {
        throw new IllegalArgumentException("weight must be non-negative");
    }
    if (stackable && maxStackSize < 1) {
        throw new IllegalArgumentException("stackable items must have maxStackSize >= 1");
    }
}
```

## Property System

### Extensible Properties Map

```java
private final Map<String, Object> properties;

public Object getProperty(String key) {
    return properties.get(key);
}

public Map<String, Object> getProperties() {
    return new HashMap<>(properties);  // Defensive copy
}
```

**Why `Map<String, Object>`?**

1. **Flexibility**: Different items have different stats
2. **Modding**: Mods can add custom properties without code changes
3. **Future-Proof**: New systems (enchanting, sockets) just add properties

### Example: Weapon Properties

```java
ItemPrototype ironSword = new ItemPrototype.Builder("iron_sword", "Iron Sword", ItemCategory.WEAPON)
    .property("damage", 10)                 // Integer damage
    .property("attack_speed", 1.2f)         // Float attacks/second
    .property("crit_chance", 0.05f)         // 5% crit chance
    .property("damage_type", "slashing")    // String type
    .property("two_handed", false)          // Boolean flag
    .build();

// Later: Read properties
int damage = (int) ironSword.getProperty("damage");
float attackSpeed = (float) ironSword.getProperty("attack_speed");
```

### Example: Armor Properties

```java
ItemPrototype plateHelm = new ItemPrototype.Builder("plate_helm", "Plate Helm", ItemCategory.ARMOR)
    .property("armor", 15)                  // Damage reduction
    .property("slot", "head")               // Equipment slot
    .property("material", "iron")           // Material type
    .property("weight_class", "heavy")      // Affects movement speed
    .build();
```

### Example: Tool Properties

```java
ItemPrototype ironPickaxe = new ItemPrototype.Builder("iron_pickaxe", "Iron Pickaxe", ItemCategory.TOOL)
    .property("harvest_speed", 1.5f)        // Mining speed multiplier
    .property("harvest_types", List.of("stone", "ore"))  // Can mine these
    .property("efficiency", 1.2f)           // Resource yield bonus
    .build();
```

### Example: Consumable Properties

```java
ItemPrototype healthPotion = new ItemPrototype.Builder("health_potion", "Health Potion", ItemCategory.CONSUMABLE)
    .stackable(true)
    .maxStackSize(20)
    .property("heal_amount", 50)            // HP restored
    .property("use_time", 2.0f)             // Seconds to consume
    .property("cooldown", 5.0f)             // Seconds before next use
    .build();
```

## Stacking System

### Stackable vs Non-Stackable

```java
// ❌ Equipment: Each item has unique durability/enchantments
ItemPrototype ironSword = new ItemPrototype.Builder(...)
    .stackable(false)  // Default
    .build();

// ✅ Materials: Identical items stack
ItemPrototype ironIngot = new ItemPrototype.Builder(...)
    .stackable(true)
    .maxStackSize(99)
    .build();
```

### Stack Size Limits

**Design Decision**: Why cap at 99?

```java
private int maxStackSize = 99;
```

**Rationale:**
- **UI Constraints**: 2-digit display fits in inventory slots
- **Balance**: Prevents hoarding (must manage multiple stacks)
- **Trade**: Encourages bulk trading (100 ingots = 2 slots)

**Special Cases:**
```java
// Quest items: Stack to 1 (unique)
.stackable(true)
.maxStackSize(1)

// Currency: Stack to 9999
.stackable(true)
.maxStackSize(9999)
```

## Durability System

### Indestructible Items

```java
ItemPrototype questItem = new ItemPrototype.Builder(...)
    .maxDurability(0)  // 0 = indestructible
    .build();
```

**Use Cases:**
- **Quest Items**: Can't break before quest completion
- **Currency**: Gold coins don't wear out
- **Keys**: Must survive to unlock doors

### Durability Scaling by Rarity

```java
// Common iron sword
.maxDurability(200.0f)
.rarity(ItemRarity.COMMON)

// Legendary iron sword
.maxDurability(500.0f)  // 2.5× durability
.rarity(ItemRarity.LEGENDARY)
```

**Pattern**: `maxDurability *= rarity.getDurabilityMultiplier()`

## Item Rarity Integration

```java
private final ItemRarity rarity;
```

**Effects:**
- **Value**: `finalValue = baseValue * rarity.getValueMultiplier()`
- **Drop Rate**: Legendary items 0.1% drop chance
- **Crafting XP**: Higher rarity = more XP
- **Visual**: Color-coded in UI (grey/green/blue/purple/orange)

**Example:**
```java
ItemPrototype commonSword = new ItemPrototype.Builder(...)
    .baseValue(50)
    .rarity(ItemRarity.COMMON)  // 1.0× multiplier
    .build();
// Final value: 50 gold

ItemPrototype legendarySword = new ItemPrototype.Builder(...)
    .baseValue(50)
    .rarity(ItemRarity.LEGENDARY)  // 5.0× multiplier
    .build();
// Final value: 250 gold
```

## Asset Paths (Future: Graphics)

```java
private final String iconPath;
private final String modelPath;
```

**Current State**: Not used yet (Phase 1 is text-based)

**Future (Phase 2+):**
```java
ItemPrototype sword = new ItemPrototype.Builder(...)
    .iconPath("assets/textures/items/iron_sword.png")  // 32×32 inventory icon
    .modelPath("assets/models/weapons/iron_sword.obj")  // 3D world model
    .build();

// In renderer
Texture icon = textureManager.load(sword.getIconPath());
Model model = modelManager.load(sword.getModelPath());
```

## API Reference

### Constructor (via Builder)

```java
ItemPrototype proto = new ItemPrototype.Builder(
    String id,              // Unique ID (e.g., "iron_sword")
    String name,            // Display name (e.g., "Iron Sword")
    ItemCategory category   // WEAPON, ARMOR, TOOL, etc.
)
.description(String desc)           // Optional: flavor text
.rarity(ItemRarity rarity)          // Optional: COMMON (default)
.maxDurability(float dur)           // Optional: 100.0f (default)
.baseValue(int value)               // Optional: 1 (default)
.weight(float weight)               // Optional: 1.0f (default)
.stackable(boolean stack)           // Optional: false (default)
.maxStackSize(int size)             // Optional: 99 (default)
.property(String key, Object value) // Optional: custom properties
.iconPath(String path)              // Optional: icon file path
.modelPath(String path)             // Optional: 3D model path
.build();
```

### Getters

```java
String getId()
String getName()
String getDescription()
ItemCategory getCategory()
ItemRarity getRarity()
float getMaxDurability()
int getBaseValue()
float getWeight()
boolean isStackable()
int getMaxStackSize()
Object getProperty(String key)
Map<String, Object> getProperties()
String getIconPath()
String getModelPath()
int getSchemaVersion()
```

### Utility Methods

```java
boolean hasProperty(String key)         // Check if property exists
int getFinalValue()                     // baseValue × rarity multiplier
```

## MVP Item Prototypes

From `ItemRegistry.java`:

### Weapons
```java
// Iron Sword: Basic melee weapon
new ItemPrototype.Builder("iron_sword", "Iron Sword", ItemCategory.WEAPON)
    .description("A sturdy iron blade")
    .rarity(ItemRarity.COMMON)
    .maxDurability(200.0f)
    .baseValue(50)
    .weight(5.0f)
    .property("damage", 10)
    .property("attack_speed", 1.2f)
    .build();

// Steel Axe: Heavy damage, slow
new ItemPrototype.Builder("steel_axe", "Steel Axe", ItemCategory.WEAPON)
    .description("A heavy steel axe")
    .rarity(ItemRarity.UNCOMMON)
    .maxDurability(250.0f)
    .baseValue(100)
    .weight(8.0f)
    .property("damage", 15)
    .property("attack_speed", 0.8f)
    .build();
```

### Tools
```java
// Iron Pickaxe: Mining tool
new ItemPrototype.Builder("iron_pickaxe", "Iron Pickaxe", ItemCategory.TOOL)
    .description("A mining tool")
    .rarity(ItemRarity.COMMON)
    .maxDurability(150.0f)
    .baseValue(30)
    .weight(4.0f)
    .property("harvest_speed", 1.5f)
    .build();
```

### Armor
```java
// Leather Armor: Light protection
new ItemPrototype.Builder("leather_armor", "Leather Armor", ItemCategory.ARMOR)
    .description("Light protective gear")
    .rarity(ItemRarity.COMMON)
    .maxDurability(100.0f)
    .baseValue(40)
    .weight(3.0f)
    .property("armor", 5)
    .build();

// Plate Helmet: Heavy protection
new ItemPrototype.Builder("plate_helmet", "Plate Helmet", ItemCategory.ARMOR)
    .description("Heavy head protection")
    .rarity(ItemRarity.RARE)
    .maxDurability(300.0f)
    .baseValue(150)
    .weight(6.0f)
    .property("armor", 15)
    .build();
```

### Materials
```java
// Iron Ingot: Smithing material
new ItemPrototype.Builder("iron_ingot", "Iron Ingot", ItemCategory.MATERIAL)
    .description("A bar of iron")
    .rarity(ItemRarity.COMMON)
    .stackable(true)
    .maxStackSize(99)
    .baseValue(10)
    .weight(0.5f)
    .build();

// Wood Plank: Building material
new ItemPrototype.Builder("wood_plank", "Wood Plank", ItemCategory.MATERIAL)
    .description("A plank of wood")
    .rarity(ItemRarity.COMMON)
    .stackable(true)
    .maxStackSize(99)
    .baseValue(2)
    .weight(0.2f)
    .build();
```

## Integration with Other Systems

### Crafting System
```java
// Register prototype
craftingSystem.registerItemPrototype(ironSwordProto);

// Create recipe
CraftingRecipe swordRecipe = new CraftingRecipe.Builder("recipe_iron_sword", ironSwordProto.getId())
    .addMaterial("iron_ingot", 3)
    .addTool("forge")
    .requiredProficiency(CraftingProficiency.APPRENTICE)
    .build();

// Craft
CraftingResult result = craftingSystem.craft(swordRecipe, skills, materials, tools);
Item newSword = result.getItem();  // Created from prototype
```

### Item Creation
```java
// Standard creation
Item sword = Item.fromPrototype(ironSwordProto);

// Custom creation with modifications
Item enchantedSword = Item.fromPrototype(ironSwordProto);
enchantedSword.setCustomProperty("enchantment", "fire");
enchantedSword.setCustomProperty("damage_bonus", 5);
```

### Loot Generation
```java
// Random loot based on rarity
public Item generateLoot(float rarityRoll) {
    for (ItemPrototype proto : allPrototypes) {
        float chance = proto.getRarity().getDropChance();
        if (rarityRoll <= chance) {
            return Item.fromPrototype(proto);
        }
    }
    return null;
}
```

## Performance Considerations

### Memory Efficiency

**Prototype Storage:**
- Average prototype: ~500 bytes
- 100 prototypes: 50 KB
- 1,000 prototypes: 500 KB (negligible)

**Shared vs Duplicated:**
```
Without prototypes: 1000 swords × 100 bytes = 100 KB
With prototypes: 1 proto (500 bytes) + 1000 instances (70 bytes each) = 70.5 KB
Savings: 29.5% (scales with more items)
```

### Property Map Overhead

```java
private final Map<String, Object> properties;
```

**Cost**: ~16 bytes + (key size + value size) per entry

**Typical Weapon:**
- "damage" → 10: ~25 bytes
- "attack_speed" → 1.2f: ~30 bytes
- "crit_chance" → 0.05f: ~30 bytes
- **Total**: ~85 bytes for properties

**Optimization**: Use primitives where possible (int, float) vs objects (Integer, Float)

### Immutability Benefits

- **No Synchronization**: Thread-safe reads without locks
- **CPU Cache**: Prototypes stay in L1 cache (hot data)
- **GC Friendly**: No mutations = no write barriers

## Known Limitations

1. **No Property Validation**: Properties are `Object`, type errors caught at runtime
2. **No Property Schema**: Mods could add invalid properties (e.g., "damage" as String)
3. **No Localization**: Names/descriptions hardcoded (future: translation keys)
4. **No Dynamic Prototypes**: Can't change prototypes after creation (requires restart)

## Design Decisions

### Why String Keys for Properties?

**Alternative**: Enum keys for type safety
```java
enum PropertyKey { DAMAGE, ATTACK_SPEED, ARMOR }
.property(PropertyKey.DAMAGE, 10)
```

**Rejected Because:**
- **Modding**: Mods can't add enum values without recompiling
- **Flexibility**: Different items have vastly different properties
- **Extensibility**: Future systems add properties without core changes

**Compromise**: Runtime validation in systems that read properties
```java
// Combat system validates weapon properties
Object damageObj = weapon.getProperty("damage");
if (!(damageObj instanceof Integer)) {
    throw new IllegalStateException("Weapon damage must be Integer");
}
```

### Why Immutable Prototypes?

**Alternative**: Mutable prototypes with setters

**Rejected Because:**
- **Thread Safety**: Mutable prototypes need locks → performance cost
- **Predictability**: Item behavior shouldn't change mid-game
- **Save Integrity**: Changing prototype invalidates saved instances

**Hot-Reload Workaround (future):**
```java
// Reload all prototypes from JSON
ItemRegistry.reloadPrototypes();
// Old Item instances still reference old prototypes
// New Item instances use new prototypes
```

### Why Builder Pattern Instead of Factory?

**Alternative**: Factory methods
```java
ItemPrototype sword = ItemPrototypeFactory.createWeapon("iron_sword", ...);
```

**Builder Wins Because:**
- **Readability**: Fluent API reads like configuration
- **Flexibility**: Any combination of optional parameters
- **Validation**: Centralized in builder, not scattered in factories

## Testing

See [ItemPrototypeTest.md](../../../../../test/java/org/adventure/ItemPrototypeTest.md) for test coverage:
- Builder pattern validation
- Required field enforcement
- Stackable/non-stackable behavior
- Property system
- Rarity integration
- Edge cases (empty names, negative values)

## Future Enhancements

### Phase 1.8: Persistence
```java
// JSON serialization
{
  "id": "iron_sword",
  "name": "Iron Sword",
  "category": "WEAPON",
  "max_durability": 200.0,
  "properties": {
    "damage": 10,
    "attack_speed": 1.2
  }
}
```

### Phase 2: Localization
```java
.name("item.iron_sword.name")  // Translation key
.description("item.iron_sword.desc")

// In lang file
"item.iron_sword.name": "Iron Sword",
"item.iron_sword.desc": "A sturdy iron blade"
```

### Phase 3: Dynamic Prototypes
```java
// Hot-reload from mod files
ItemRegistry.loadPrototypesFromMod("MyMod/items.json");
```

## References

- **Design Docs**: `docs/objects_crafting_legacy.md` → Prototype Pattern
- **Data Models**: `docs/data_models.md` → ItemPrototype Schema
- **Grand Plan**: `docs/grand_plan.md` → Phase 1.4 Item System
- **Related Classes**: [Item.md](Item.md), [ItemCategory.md](ItemCategory.md), [ItemRarity.md](ItemRarity.md)
- **Registry**: [../crafting/ItemRegistry.md](../crafting/ItemRegistry.md)
- **Tests**: [ItemPrototypeTest.md](../../../../../test/java/org/adventure/ItemPrototypeTest.md)

---

**Last Updated:** Phase 1.4 Implementation (November 2025)  
**Status:** ✅ Complete - 12 MVP prototypes, ready for Phase 1.5 integration
