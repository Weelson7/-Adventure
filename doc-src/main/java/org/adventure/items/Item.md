# Item.java - Item Instance System

**Package:** `org.adventure.items`  
**Source:** [Item.java](../../../../../src/main/java/org/adventure/items/Item.java)  
**Phase:** MVP Phase 1.4 (Items & Crafting)

## Overview

`Item` represents a specific instance of an `ItemPrototype` in the game world. Each item has a unique ID, tracks its durability state, manages quantity for stackable items, and supports custom properties for enchantments and modifications. This class is the foundation for inventory management, equipment systems, and item-driven gameplay.

This class implements the item system specified in `docs/objects_crafting_legacy.md` and provides the runtime representation of items that can be owned, damaged, repaired, and evolved through usage.

## Design Philosophy

### Why Item Instances Matter

Items are **not just data** - they are objects with **history and state**:
- **Durability Progression**: Weapons wear down in combat, tools degrade with use
- **Evolution**: Items gain power through usage (evolution points system)
- **Uniqueness**: Even two "Iron Swords" can differ in durability, enchantments, and history
- **Story Integration**: Legendary items track their lineage via `historyReferenceId`
- **Player Attachment**: Named weapons, family heirlooms, cursed artifacts

From `docs/grand_plan.md` MVP matrix:
> "Items are the bridge between player actions and world persistence."

### Prototype vs Instance Pattern

**Why Separate Templates from Instances?**

```java
// ItemPrototype: Template (shared, immutable)
ItemPrototype ironSwordProto = new ItemPrototype.Builder("iron_sword", "Iron Sword", ItemCategory.WEAPON)
    .maxDurability(200.0f)
    .baseValue(50)
    .build();

// Item: Instance (unique, mutable state)
Item sword1 = Item.fromPrototype(ironSwordProto);  // Fresh, 200/200 durability
Item sword2 = Item.fromPrototype(ironSwordProto);  // Another fresh sword

sword1.damage(50);  // Now 150/200
sword2.damage(100); // Now 100/200
// Same template, different states
```

**Advantages:**
- **Memory Efficiency**: 1000 iron swords share 1 prototype (~500 bytes), not 1000 copies
- **Consistency**: All iron swords have same max durability, base value
- **Modding**: Change prototype → all future instances updated
- **Performance**: Cache-friendly (prototypes stay in memory)

## Class Structure

### Fields

```java
// Identity
private final String id;                    // Unique UUID per instance
private final String prototypeId;           // Template reference (e.g., "iron_sword")
private String ownerId;                     // Character/container ID

// Durability tracking
private float currentDurability;            // Current condition (0 = broken)
private final float maxDurability;          // Maximum condition

// Stacking (for materials/consumables)
private int quantity;                       // Stack size (1 for non-stackable)

// Custom properties (enchantments, modifications)
private final Map<String, Object> customProperties;

// Legacy/story tracking (optional)
private String historyReferenceId;          // Story/event linkage

// Evolution tracking
private int evolutionPoints;                // Usage points (capped at 10,000)

// Timestamps
private final long createdAtTick;           // Birth tick
private long lastModifiedTick;              // Last update tick

// Persistence
private final int schemaVersion = 1;        // For migrations
```

## Durability System

### The Problem: Infinite Items

Without durability, items never break → economy stagnates, no demand for crafters.

### The Solution: Wear and Repair

```java
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
```

**Example: Sword in Combat**
```java
Item sword = Item.fromPrototype(ironSwordProto);
// Start: 200/200 durability

sword.damage(5);   // After 1 hit: 195/200
sword.damage(5);   // After 2 hits: 190/200
// ... 40 hits later ...
sword.damage(5);   // After 40 hits: 0/200 → breaks!

if (sword.isBroken()) {
    System.out.println("Your sword shattered!");
    // Remove from inventory
}
```

### Durability Percentage

```java
public float getDurabilityPercent() {
    if (maxDurability <= 0) return 1.0f;
    return Math.max(0.0f, Math.min(1.0f, currentDurability / maxDurability));
}
```

**Usage in UI:**
```java
float percent = sword.getDurabilityPercent();
String bar = "█".repeat((int)(percent * 10)) + "░".repeat(10 - (int)(percent * 10));
System.out.println("Durability: [" + bar + "] " + (int)(percent * 100) + "%");
// Output: "Durability: [████████░░] 80%"
```

### Repair Mechanics

```java
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
```

**Example: Blacksmith Repairs**
```java
Item damagedSword = sword;  // 120/200 durability

float repaired = damagedSword.repair(50);  // Try to repair 50
System.out.println("Repaired " + repaired + " points");  // "Repaired 50 points"
// Now: 170/200

damagedSword.repair(100);  // Try to repair 100
// Caps at max: 200/200, only repaired 30
```

**Design Decision: Cannot Repair Broken Items**
```java
Item brokenSword = sword;  // 0/200 durability
brokenSword.repair(100);   // ❌ IllegalStateException: "Cannot repair broken items"
```

**Rationale:**
- **Risk Management**: Players must maintain items *before* breaking
- **Economy**: Broken items → need new items → crafters stay relevant
- **Realism**: Shattered sword can't be fixed, only melted down for materials
- **Future**: Salvage system to recover materials from broken items

### Repair Fully

```java
public float repairFully() {
    float amount = maxDurability - currentDurability;
    currentDurability = maxDurability;
    lastModifiedTick = System.currentTimeMillis();
    return amount;
}
```

**Usage:**
```java
Item sword = damagedSword;  // 120/200
float cost = sword.getDurabilityPercent();  // 0.6
float materialCost = (1.0f - cost) * baseRepairCost;  // 40% missing → 40% of materials

float repaired = sword.repairFully();
System.out.println("Fully repaired " + repaired + " points for " + materialCost + " iron");
```

## Stacking System

### Stackable Items (Materials, Consumables)

```java
// Create stack of 10 iron ingots
ItemPrototype ironIngotProto = /* stackable prototype */;
Item ironStack = Item.fromPrototype(ironIngotProto, 10);

System.out.println(ironStack.getQuantity());  // 10

// Add more to stack
ironStack.increaseQuantity(5);  // Now 15

// Consume from stack
boolean success = ironStack.decreaseQuantity(3);  // Now 12
if (!success) {
    System.out.println("Not enough items!");
}
```

### Non-Stackable Items (Equipment)

```java
// Iron sword cannot stack
Item sword = Item.fromPrototype(ironSwordProto, 5);  // ❌ IllegalArgumentException
// Each sword has unique durability state
```

**Quantity Management:**

```java
public boolean decreaseQuantity(int amount) {
    if (amount < 0) {
        throw new IllegalArgumentException("Amount must be non-negative");
    }
    if (amount > quantity) {
        return false;  // Not enough items
    }
    quantity -= amount;
    lastModifiedTick = System.currentTimeMillis();
    return true;
}
```

**Usage in Crafting:**
```java
// Consume 3 iron ingots for recipe
if (ironStack.decreaseQuantity(3)) {
    craftItem();
} else {
    System.out.println("Insufficient materials!");
}
```

## Custom Properties (Enchantments & Modifications)

### Extensible Property System

```java
private final Map<String, Object> customProperties;

public void setCustomProperty(String key, Object value) {
    this.customProperties.put(key, value);
    this.lastModifiedTick = System.currentTimeMillis();
}

public Object getCustomProperty(String key) {
    return customProperties.get(key);
}
```

### Example: Enchanted Sword

```java
Item enchantedSword = Item.fromPrototype(ironSwordProto);

// Add fire enchantment
enchantedSword.setCustomProperty("enchantment", "fire");
enchantedSword.setCustomProperty("fire_damage", 10);
enchantedSword.setCustomProperty("enchantment_level", 2);

// Later: Check in combat
if ("fire".equals(enchantedSword.getCustomProperty("enchantment"))) {
    int bonusDamage = (int) enchantedSword.getCustomProperty("fire_damage");
    applyBurnEffect(target, bonusDamage);
}
```

### Example: Crafted Quality

```java
// Masterwork sword from crafting
Item masterworkSword = Item.fromPrototype(ironSwordProto);
masterworkSword.setCustomProperty("quality", "masterwork");
masterworkSword.setCustomProperty("damage_bonus", 5);
masterworkSword.setCustomProperty("crafted_by", "Thorin Ironforge");
```

### Example: Cursed Item

```java
Item cursedDagger = Item.fromPrototype(daggerProto);
cursedDagger.setCustomProperty("cursed", true);
cursedDagger.setCustomProperty("curse_effect", "blood_drain");
cursedDagger.setCustomProperty("cannot_unequip", true);
```

## Evolution Points System

### Item Growth Through Usage

From `docs/objects_crafting_legacy.md`:
> "Objects evolve through usage, exposure to magic, or story events."

```java
public void addEvolutionPoints(int points) {
    if (points < 0) {
        throw new IllegalArgumentException("Evolution points must be non-negative");
    }
    // Cap at 10,000 as per design doc
    this.evolutionPoints = Math.min(10000, this.evolutionPoints + points);
    lastModifiedTick = System.currentTimeMillis();
}
```

**Usage Tracking:**
```java
// In combat system
public void onSuccessfulHit(Item weapon, Enemy target) {
    weapon.addEvolutionPoints(1);  // +1 per hit
    
    // Check for evolution milestones
    int evo = weapon.getEvolutionPoints();
    if (evo == 500) {
        applyEvolution(weapon, "veteran");  // +5 damage
    } else if (evo == 1000) {
        applyEvolution(weapon, "legendary");  // +10 damage, special ability
    } else if (evo == 10000) {
        applyEvolution(weapon, "mythical");  // Maximum power
    }
}
```

**Evolution Thresholds (from design docs):**
- **500 points**: Veteran tier (+minor stats)
- **1000 points**: Legendary tier (+major stats)
- **2000 points**: Epic tier (+special ability)
- **10,000 points**: Mythical tier (max power, diminishing returns beyond)

**Example: Sword Evolution**
```java
Item ironSword = Item.fromPrototype(ironSwordProto);
// Initial: 10 base damage

// After 500 successful hits
ironSword.addEvolutionPoints(500);
ironSword.setCustomProperty("damage_bonus", 5);  // Now 15 damage
ironSword.setCustomProperty("tier", "veteran");

// After 1000 total hits
ironSword.addEvolutionPoints(500);
ironSword.setCustomProperty("damage_bonus", 10);  // Now 20 damage
ironSword.setCustomProperty("special_ability", "cleave");  // Hit 2 enemies
ironSword.setCustomProperty("tier", "legendary");
```

## History Reference ID (Story Integration)

### Linking Items to Stories

```java
private String historyReferenceId;

public void setHistoryReferenceId(String historyReferenceId) {
    this.historyReferenceId = historyReferenceId;
    this.lastModifiedTick = System.currentTimeMillis();
}
```

**Example: Legendary Artifact**
```java
Item excalibur = Item.fromPrototype(legendaryProto);
excalibur.setHistoryReferenceId("story_arthur_pulls_sword");

// Later: Check if player has story item
if ("story_arthur_pulls_sword".equals(excalibur.getHistoryReferenceId())) {
    unlockQuestline("king_arthur_saga");
    grantAbility("rightful_king");
}
```

**Legacy Persistence (from design docs):**
> "Objects with historyReferenceId always store full legacy state (evolution points, active effects, trigger history)."

**Storage Rules:**
- **With `historyReferenceId`**: Always save full state (for quest items, legendary artifacts)
- **Without `historyReferenceId`**: Save only if `evolutionPoints > 100` (to save storage)

## Factory Methods

### Creating Items from Prototypes

```java
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
```

**Usage:**
```java
// Single item
Item sword = Item.fromPrototype(ironSwordProto);

// Stack of items
Item ironIngots = Item.fromPrototype(ironIngotProto, 50);
```

## Builder Pattern

### Flexible Construction

```java
Item customItem = new Item.Builder("custom_sword", 300.0f)
    .id("sword_of_destiny")                // Custom ID
    .ownerId("player_001")                  // Assign to player
    .currentDurability(150.0f)              // Partially damaged
    .quantity(1)                             // Single item
    .customProperty("enchantment", "holy")  // Enchanted
    .customProperty("glow", true)           // Glows in dark
    .historyReferenceId("quest_holy_sword") // Quest item
    .evolutionPoints(1500)                  // Already evolved
    .createdAtTick(1000)                    // Creation time
    .build();
```

**Validation:**
```java
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
```

## API Reference

### Constructor (via Builder)

```java
Item item = new Item.Builder(String prototypeId, float maxDurability)
    .id(String id)                              // Optional: custom UUID
    .ownerId(String ownerId)                    // Optional: owner
    .currentDurability(float durability)        // Optional: starts at max
    .quantity(int quantity)                     // Optional: default 1
    .customProperty(String key, Object value)   // Optional: add properties
    .historyReferenceId(String storyId)         // Optional: link to story
    .evolutionPoints(int points)                // Optional: default 0
    .build();
```

### Durability Methods

```java
boolean isBroken()                          // Check if durability <= 0
float getDurabilityPercent()                // Get durability as 0.0-1.0
boolean damage(float amount)                // Reduce durability, returns if broken
float repair(float amount)                  // Increase durability, returns actual repaired
float repairFully()                         // Repair to max, returns amount repaired
```

### Stacking Methods

```java
int getQuantity()                           // Get stack size
boolean increaseQuantity(int amount)        // Add to stack
boolean decreaseQuantity(int amount)        // Remove from stack, returns success
```

### Property Methods

```java
void setCustomProperty(String key, Object value)
Object getCustomProperty(String key)
Map<String, Object> getCustomProperties()   // Returns copy
```

### Evolution Methods

```java
int getEvolutionPoints()
void addEvolutionPoints(int points)         // Capped at 10,000
```

### Ownership Methods

```java
String getOwnerId()
void setOwnerId(String ownerId)
String getHistoryReferenceId()
void setHistoryReferenceId(String storyId)
```

## Testing

See [ItemTest.md](../../../../../test/java/org/adventure/ItemTest.md) for comprehensive test coverage:
- 20 unit tests covering all core functionality
- Durability damage/repair mechanics
- Cannot repair broken items
- Stackable item quantity management
- Custom properties (enchantments)
- Evolution points tracking (capped at 10,000)
- Prototype creation and validation

## Integration with Other Systems

### Crafting System (Phase 1.4)
```java
// Create item from crafting
CraftingResult result = craftingSystem.craft(recipe, skills, materials, tools);
if (result.isSuccess()) {
    Item craftedItem = result.getItem();
    // Apply quality modifiers
    if (result.getQuality() == CraftingQuality.MASTERWORK) {
        craftedItem.setCustomProperty("quality", "masterwork");
        craftedItem.setCustomProperty("damage_bonus", 5);
    }
}
```

### Character Inventory (Phase 1.3)
```java
Character player = new Character("player_1", "Hero", Race.HUMAN);
player.addItem(sword.getId());  // Add to inventory

// Later: Equip weapon
String swordId = player.getInventory().get(0);
Item sword = itemRegistry.getItem(swordId);
```

### Combat System (Phase 1.5)
```java
// Use weapon in combat
Item weapon = player.getEquippedWeapon();
weapon.damage(5);  // Durability loss per hit
weapon.addEvolutionPoints(1);  // Gain experience

if (weapon.isBroken()) {
    player.unequipWeapon();
    notifyPlayer("Your " + weapon.getPrototypeId() + " shattered!");
}
```

### Story System (Phase 1.7)
```java
// Check for quest item
Item questItem = findItemInInventory("holy_grail");
if (questItem != null && "quest_grail".equals(questItem.getHistoryReferenceId())) {
    triggerStoryEvent("grail_found");
}
```

## Performance Considerations

### Memory Footprint

**Per Item:**
- UUID: 36 bytes
- Prototype ID: 16 bytes (string reference)
- Owner ID: 16 bytes (string reference)
- Durability: 8 bytes (2× float)
- Quantity: 4 bytes (int)
- Custom properties: ~50 bytes (avg 5 properties)
- History ref: 16 bytes (string reference)
- Evolution: 4 bytes (int)
- Timestamps: 16 bytes (2× long)
- Schema version: 4 bytes (int)
- **Total**: ~170 bytes per item

**World Scale:**
- 1,000 items = 170 KB
- 10,000 items = 1.7 MB
- 100,000 items = 17 MB (acceptable)

### Update Frequency

Items only update when:
- Damaged in combat (per-hit)
- Repaired by crafter (manual action)
- Quantity changed (crafting/consumption)
- Properties modified (enchanting/evolution)

**No per-tick updates** → minimal CPU cost.

## Known Limitations

1. **No Equipment Slots**: Item doesn't track which slot it occupies (head, chest, weapon)
2. **No Weight System**: Future: weight-based carry capacity
3. **No Item Sets**: Future: set bonuses for matching armor pieces
4. **No Sockets**: Future: gem/rune socket system
5. **Simple Stacking**: No partial stack splitting UI yet

## Design Decisions

### Why Float for Durability?

```java
private float currentDurability;
```

**Advantages:**
- **Precision**: Gradual wear (0.1 damage per action) vs integer steps
- **Percentage Calculations**: Clean 0.0-1.0 scaling
- **Future**: Exponential decay formulas

**Alternative Considered:**
- `int` durability (0-100) - rejected for lack of precision

### Why Cap Evolution at 10,000?

From design docs:
> "Evolution points capped at 10,000 (legendary tier). Further usage provides diminishing XP."

**Rationale:**
- **Balance**: Prevents infinite power creep
- **Diminishing Returns**: Encourages using multiple items vs grinding one
- **Goal**: Legendary items are achievements, not inevitable

### Why Store Timestamps as Long?

```java
private final long createdAtTick;
private long lastModifiedTick;
```

**Current:** System.currentTimeMillis() (milliseconds since epoch)  
**Future:** Game tick count (for simulation consistency)

**Migration Path:**
```java
// Phase 1.8: Switch to game ticks
private long createdAtTick;  // Game tick, not real-world time
```

## References

- **Design Docs**: `docs/objects_crafting_legacy.md`
- **Data Models**: `docs/data_models.md` → Item Schema
- **Grand Plan**: `docs/grand_plan.md` → Phase 1.4 Requirements
- **Related Classes**: [ItemPrototype.md](ItemPrototype.md), [ItemCategory.md](ItemCategory.md), [ItemRarity.md](ItemRarity.md)
- **Tests**: [ItemTest.md](../../../../../test/java/org/adventure/ItemTest.md)
- **Crafting Integration**: [../crafting/CraftingSystem.md](../crafting/CraftingSystem.md)

---

**Last Updated:** Phase 1.4 Implementation (November 2025)  
**Status:** ✅ Complete - 20 tests passing, ready for Phase 1.5 integration
