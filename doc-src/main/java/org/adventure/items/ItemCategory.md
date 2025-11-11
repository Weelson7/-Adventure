# ItemCategory.java - Item Classification System

**Package:** `org.adventure.items`  
**Source:** [ItemCategory.java](../../../../../src/main/java/org/adventure/items/ItemCategory.java)  
**Phase:** MVP Phase 1.4 (Items & Crafting)

## Overview

`ItemCategory` is an enum that defines all possible categories an item can belong to. Categories are used for:
- **Inventory Organization**: Group items by type (weapons, armor, tools)
- **System Filtering**: Combat system only uses WEAPON category items
- **UI Display**: Show category tabs in inventory
- **Crafting**: Recipes target specific categories
- **Economy**: Different categories have different value curves

This enum is referenced by `ItemPrototype` and is central to the item classification system.

## Design Philosophy

### Why 24 Categories?

From `docs/objects_crafting_legacy.md`:
> "Items are classified into broad categories to enable system specialization while avoiding over-fragmentation."

**Balance**:
- **Too Few** (5 categories): Not granular enough (e.g., "consumable" for food AND potions)
- **Too Many** (50+ categories): Over-engineering, hard to remember
- **Sweet Spot** (24 categories): Covers all MVP needs + room for expansion

### Category Hierarchy (Implicit)

While the enum is flat, categories form **logical groups**:

```
Equipment (direct gameplay impact)
├── WEAPON        - Offensive items
├── ARMOR         - Defensive items
├── TOOL          - Harvesting/crafting
├── ACCESSORY     - Jewelry, belts, cloaks
└── MOUNT_GEAR    - Saddles, horseshoes

Consumables (single-use or limited use)
├── CONSUMABLE    - Generic consumables
├── FOOD          - Restores hunger
├── POTION        - Magical effects
└── SCROLL        - Spell scrolls

Crafting (used in recipes)
├── MATERIAL      - Raw materials
├── COMPONENT     - Crafted components
├── REAGENT       - Alchemical ingredients
└── GEM           - Socketable gems

Economy (trade/value)
├── CURRENCY      - Gold, gems, tokens
├── QUEST_ITEM    - Quest-specific
└── TRADE_GOOD    - Bulk trade items

Storage (containers)
├── CONTAINER     - Bags, chests, boxes
└── AMMO          - Arrows, bolts, bullets

Special (unique mechanics)
├── BOOK          - Skill books, lore
├── KEY           - Opens locks
├── RUNE          - Enchanting runes
├── PET           - Companion creatures
├── VEHICLE       - Boats, carts
└── MISC          - Uncategorized
```

## Enum Values

### Equipment Categories

#### WEAPON
```java
WEAPON(1, "Weapon", "Offensive equipment used in combat")
```
**Examples**: Swords, axes, bows, staves, daggers  
**Properties**: `damage`, `attack_speed`, `range`, `damage_type`  
**Systems**: Combat, Durability, Evolution  
**Stacking**: Never stackable (unique durability)

#### ARMOR
```java
ARMOR(2, "Armor", "Defensive equipment that reduces damage")
```
**Examples**: Helmets, chest plates, boots, shields  
**Properties**: `armor`, `slot` (head/chest/legs/feet), `weight_class`  
**Systems**: Combat (damage reduction), Equipment Slots  
**Stacking**: Never stackable

#### TOOL
```java
TOOL(3, "Tool", "Items used for gathering resources or crafting")
```
**Examples**: Pickaxes, axes, hammers, fishing rods  
**Properties**: `harvest_speed`, `harvest_types`, `efficiency`  
**Systems**: Resource Gathering, Durability  
**Stacking**: Never stackable

#### ACCESSORY
```java
ACCESSORY(4, "Accessory", "Rings, amulets, and other worn items")
```
**Examples**: Rings, amulets, belts, cloaks  
**Properties**: `stat_bonuses`, `special_effects`, `slot`  
**Systems**: Equipment, Stats Modifiers  
**Stacking**: Never stackable

#### MOUNT_GEAR
```java
MOUNT_GEAR(5, "Mount Gear", "Equipment for mounts and vehicles")
```
**Examples**: Saddles, horseshoes, reins, barding  
**Properties**: `mount_type`, `speed_bonus`, `armor_bonus`  
**Systems**: Mounts (future Phase 2)  
**Stacking**: Never stackable

### Consumable Categories

#### CONSUMABLE
```java
CONSUMABLE(6, "Consumable", "Items that are consumed upon use")
```
**Examples**: Bandages, repair kits, buff items  
**Properties**: `use_time`, `cooldown`, `effect`  
**Systems**: Consumption, Cooldown Management  
**Stacking**: Usually stackable (99 max)

#### FOOD
```java
FOOD(7, "Food", "Consumables that restore hunger or provide buffs")
```
**Examples**: Bread, cooked meat, fruit, cheese  
**Properties**: `hunger_restore`, `buff_duration`, `buff_effect`  
**Systems**: Hunger (future), Buffs  
**Stacking**: Stackable (20-50 max)

#### POTION
```java
POTION(8, "Potion", "Magical consumables with various effects")
```
**Examples**: Health potions, mana potions, antidotes  
**Properties**: `heal_amount`, `mana_restore`, `effect_type`  
**Systems**: Health/Mana, Status Effects  
**Stacking**: Stackable (20 max)

#### SCROLL
```java
SCROLL(9, "Scroll", "Single-use magical items that cast spells")
```
**Examples**: Fireball scroll, teleport scroll, identify scroll  
**Properties**: `spell_id`, `spell_power`, `cast_time`  
**Systems**: Magic System (future Phase 2)  
**Stacking**: Stackable (10 max)

### Crafting Categories

#### MATERIAL
```java
MATERIAL(10, "Material", "Raw materials used in crafting")
```
**Examples**: Iron ingots, wood planks, leather, stone  
**Properties**: `material_type`, `tier`, `quality`  
**Systems**: Crafting, Resource Gathering  
**Stacking**: Stackable (99 max)

#### COMPONENT
```java
COMPONENT(11, "Component", "Crafted components used in recipes")
```
**Examples**: Gears, springs, enchanted thread, bottled essence  
**Properties**: `component_type`, `tier`  
**Systems**: Crafting (complex recipes)  
**Stacking**: Stackable (50 max)

#### REAGENT
```java
REAGENT(12, "Reagent", "Alchemical ingredients for potions")
```
**Examples**: Herbs, mushrooms, crystals, blood  
**Properties**: `alchemical_properties`, `potency`  
**Systems**: Alchemy, Crafting  
**Stacking**: Stackable (50 max)

#### GEM
```java
GEM(13, "Gem", "Precious stones that can be socketed into equipment")
```
**Examples**: Ruby, sapphire, diamond, emerald  
**Properties**: `socket_bonus`, `tier`, `rarity`  
**Systems**: Socketing (future Phase 2)  
**Stacking**: Stackable (99 max)

### Economy Categories

#### CURRENCY
```java
CURRENCY(14, "Currency", "Monetary items used for trading")
```
**Examples**: Gold coins, silver coins, copper coins, tokens  
**Properties**: `value`, `exchange_rate`  
**Systems**: Economy, Trading  
**Stacking**: Stackable (9999 max)

#### QUEST_ITEM
```java
QUEST_ITEM(15, "Quest Item", "Items required for quests")
```
**Examples**: Ancient artifact, letter of recommendation, MacGuffin  
**Properties**: `quest_id`, `unique`, `cannot_drop`  
**Systems**: Quest System (future Phase 1.7)  
**Stacking**: Usually non-stackable (unique items)

#### TRADE_GOOD
```java
TRADE_GOOD(16, "Trade Good", "Items primarily used for trading")
```
**Examples**: Silk, spices, furs, wine  
**Properties**: `base_value`, `regional_modifiers`  
**Systems**: Economy, Trading, Caravans  
**Stacking**: Stackable (99 max)

### Storage Categories

#### CONTAINER
```java
CONTAINER(17, "Container", "Items that can hold other items")
```
**Examples**: Backpacks, chests, pouches, crates  
**Properties**: `capacity`, `weight_reduction`, `restricted_types`  
**Systems**: Inventory Expansion (future Phase 2)  
**Stacking**: Never stackable (each has unique contents)

#### AMMO
```java
AMMO(18, "Ammo", "Projectiles for ranged weapons")
```
**Examples**: Arrows, bolts, bullets, throwing knives  
**Properties**: `damage_modifier`, `ammo_type`  
**Systems**: Combat (ranged weapons)  
**Stacking**: Stackable (99 max)

### Special Categories

#### BOOK
```java
BOOK(19, "Book", "Readable items that may grant knowledge or skills")
```
**Examples**: Skill books, recipe books, lore tomes  
**Properties**: `skill_id`, `xp_grant`, `one_time_use`  
**Systems**: Skills, Lore  
**Stacking**: Never stackable (each book unique)

#### KEY
```java
KEY(20, "Key", "Items that unlock doors, chests, or other locked objects")
```
**Examples**: Iron key, master key, lockpick  
**Properties**: `lock_id`, `durability`, `skeleton_key`  
**Systems**: Lock/Key System (future Phase 2)  
**Stacking**: Usually non-stackable (specific locks)

#### RUNE
```java
RUNE(21, "Rune", "Magical runes used for enchanting")
```
**Examples**: Rune of Fire, Rune of Protection, Rune of Speed  
**Properties**: `enchantment_type`, `power_level`, `slot`  
**Systems**: Enchanting (future Phase 2)  
**Stacking**: Stackable (20 max)

#### PET
```java
PET(22, "Pet", "Companion creatures that follow the player")
```
**Examples**: Pet egg, pet whistle, tamed creature  
**Properties**: `creature_type`, `level`, `abilities`  
**Systems**: Pet System (future Phase 2)  
**Stacking**: Never stackable (unique creatures)

#### VEHICLE
```java
VEHICLE(23, "Vehicle", "Transportation items")
```
**Examples**: Boat, cart, sled, flying carpet  
**Properties**: `speed`, `capacity`, `terrain_types`  
**Systems**: Transportation (future Phase 2)  
**Stacking**: Never stackable

#### MISC
```java
MISC(24, "Miscellaneous", "Items that don't fit other categories")
```
**Examples**: Junk items, decorations, trophies  
**Properties**: Varies wildly  
**Systems**: None (flavor items)  
**Stacking**: Case-by-case

## Usage Examples

### Creating Items by Category

```java
// Weapon
ItemPrototype sword = new ItemPrototype.Builder("iron_sword", "Iron Sword", ItemCategory.WEAPON)
    .property("damage", 10)
    .build();

// Material
ItemPrototype ironIngot = new ItemPrototype.Builder("iron_ingot", "Iron Ingot", ItemCategory.MATERIAL)
    .stackable(true)
    .build();

// Potion
ItemPrototype healthPotion = new ItemPrototype.Builder("health_potion", "Health Potion", ItemCategory.POTION)
    .stackable(true)
    .property("heal_amount", 50)
    .build();
```

### Filtering by Category

```java
// Get all weapons from inventory
List<Item> weapons = inventory.stream()
    .filter(item -> itemRegistry.getPrototype(item.getPrototypeId()).getCategory() == ItemCategory.WEAPON)
    .collect(Collectors.toList());

// Get all stackable materials
List<Item> materials = inventory.stream()
    .filter(item -> {
        ItemPrototype proto = itemRegistry.getPrototype(item.getPrototypeId());
        return proto.getCategory() == ItemCategory.MATERIAL && proto.isStackable();
    })
    .collect(Collectors.toList());
```

### Category-Based Logic

```java
// Combat system: Only weapons can be equipped in weapon slot
public void equipWeapon(Item item) {
    ItemPrototype proto = itemRegistry.getPrototype(item.getPrototypeId());
    if (proto.getCategory() != ItemCategory.WEAPON) {
        throw new IllegalArgumentException("Cannot equip non-weapon as weapon!");
    }
    this.equippedWeapon = item;
}

// Crafting system: Tools required for some recipes
public boolean hasRequiredTool(CraftingRecipe recipe) {
    String toolId = recipe.getRequiredToolId();
    ItemPrototype toolProto = itemRegistry.getPrototype(toolId);
    
    if (toolProto.getCategory() != ItemCategory.TOOL) {
        throw new IllegalStateException("Recipe requires tool, not " + toolProto.getCategory());
    }
    
    return inventory.contains(toolId);
}
```

### UI Organization

```java
// Inventory tabs by category
Map<ItemCategory, List<Item>> categorizedInventory = inventory.stream()
    .collect(Collectors.groupingBy(item -> 
        itemRegistry.getPrototype(item.getPrototypeId()).getCategory()
    ));

// Display tabs
for (ItemCategory category : ItemCategory.values()) {
    List<Item> items = categorizedInventory.getOrDefault(category, List.of());
    if (!items.isEmpty()) {
        System.out.println("[" + category.getName() + "] (" + items.size() + " items)");
    }
}
```

## API Reference

### Fields

```java
private final int id;              // Unique ID (for database storage)
private final String name;         // Display name (e.g., "Weapon")
private final String description;  // Explanation
```

### Methods

```java
int getId()                        // Get numeric ID (1-24)
String getName()                   // Get display name ("Weapon")
String getDescription()            // Get description text
```

### Static Methods

```java
static ItemCategory fromId(int id)  // Get category by ID (throws if invalid)
static ItemCategory[] values()      // Get all categories (standard enum method)
```

## Integration with Other Systems

### ItemPrototype
```java
private final ItemCategory category;

public ItemCategory getCategory() {
    return category;
}
```

### Crafting System
```java
// Recipe validation
CraftingRecipe recipe = new CraftingRecipe.Builder("recipe_sword", "iron_sword")
    .addMaterial("iron_ingot", 3)  // Must be MATERIAL category
    .addTool("forge")               // Must be TOOL category
    .build();
```

### Inventory System (future Phase 1.5)
```java
// Category-based inventory tabs
public List<Item> getItemsByCategory(ItemCategory category) {
    return inventory.stream()
        .filter(item -> getPrototype(item).getCategory() == category)
        .collect(Collectors.toList());
}
```

## Performance Considerations

### Enum Performance

- **Memory**: 24 enum constants × ~50 bytes = ~1.2 KB (negligible)
- **Lookup**: `O(1)` by ordinal, `O(log n)` by name
- **Comparison**: Direct reference equality (fastest)

### Filtering Performance

```java
// ❌ SLOW: String comparison
if ("WEAPON".equals(proto.getCategory().name())) { ... }

// ✅ FAST: Reference equality
if (ItemCategory.WEAPON == proto.getCategory()) { ... }
```

## Design Decisions

### Why Not Hierarchical Enum?

**Alternative**: Nested categories
```java
enum ItemCategory {
    EQUIPMENT(WEAPON, ARMOR, TOOL),
    CONSUMABLE(FOOD, POTION, SCROLL),
    ...
}
```

**Rejected Because:**
- **Complexity**: Harder to filter ("give me all EQUIPMENT" vs specific subcategories)
- **Flexibility**: Flat structure easier to extend
- **Simplicity**: Simpler enum → simpler serialization

**Compromise**: Logical grouping in documentation (see hierarchy above), flat in code

### Why IDs for Categories?

```java
private final int id;
```

**Use Case**: Database persistence
```sql
CREATE TABLE items (
    item_id VARCHAR(36),
    category_id INT,  -- Faster than VARCHAR
    ...
);
```

**Advantage**: Integers faster than strings for indexing/sorting

### Why Descriptions?

```java
private final String description;
```

**Use Case**: Tooltips, help text, modding documentation

**Example:**
```java
// In UI
System.out.println(category.getName() + ": " + category.getDescription());
// "Weapon: Offensive equipment used in combat"
```

## Known Limitations

1. **No Subcategories**: WEAPON doesn't distinguish swords/axes/bows
2. **No Multi-Category**: Item can't be both TOOL and WEAPON (e.g., combat pickaxe)
3. **No Custom Categories**: Mods can't add new categories without recompiling

## Future Enhancements

### Phase 1.5: Tags System
```java
// Supplement categories with flexible tags
ItemPrototype combatPickaxe = new ItemPrototype.Builder(...)
    .category(ItemCategory.TOOL)  // Primary category
    .tags("weapon", "mining", "two_handed")  // Secondary classifications
    .build();

// Multi-system usage
if (item.hasTag("weapon")) {
    useInCombat(item);
}
if (item.hasTag("mining")) {
    useForMining(item);
}
```

### Phase 2: Modding Support
```java
// Register custom categories
ItemCategory.registerModCategory("magic_staff", "Magic Staff", "Magical weapons");
```

## References

- **Design Docs**: `docs/objects_crafting_legacy.md` → Item Classification
- **Data Models**: `docs/data_models.md` → ItemPrototype Schema
- **Related Classes**: [ItemPrototype.md](ItemPrototype.md), [Item.md](Item.md)
- **Usage**: [../crafting/ItemRegistry.md](../crafting/ItemRegistry.md)

---

**Last Updated:** Phase 1.4 Implementation (November 2025)  
**Status:** ✅ Complete - 24 categories covering MVP scope
