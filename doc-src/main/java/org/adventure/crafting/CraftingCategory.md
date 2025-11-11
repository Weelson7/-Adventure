# CraftingCategory.java - Specialization System

**Package:** `org.adventure.crafting`  
**Source:** [CraftingCategory.java](../../../../../src/main/java/org/adventure/crafting/CraftingCategory.java)  
**Phase:** MVP Phase 1.4 (Items & Crafting)

## Overview

`CraftingCategory` defines eight specialized crafting disciplines. Each category tracks independent XP progression, allowing characters to master specific crafts while encouraging trade and cooperation for non-specialized items.

From `docs/objects_crafting_legacy.md`:
> "Characters can specialize in up to 2 crafting categories, gaining +20% XP in specialized skills."

## Design Philosophy

### Why 8 Categories?

**Coverage**: Must span all item types
- **Weapons/Armor**: Smithing, Leatherworking, Tailoring
- **Consumables**: Alchemy, Cooking
- **Magical**: Enchanting, Runecrafting
- **Utility**: Engineering

**Balance**:
- **Too Few** (3-4): Forces players into identical builds
- **Too Many** (15+): Overwhelms choice, dilutes progression
- **Sweet Spot** (8): Meaningful specialization, diverse economy

### Specialization Trade-offs

**2 Specialization Limit** enforces choices:
- **Example 1**: Smithing + Enchanting = Weapon master (can't make potions)
- **Example 2**: Alchemy + Cooking = Support crafter (can't make weapons)
- **Example 3**: Engineering + Runecrafting = Tech mage (can't make armor)

**Economic Effect**: Need other players for non-specialized items → trading economy

## Enum Values

### SMITHING
```java
SMITHING(1, "Smithing", "Crafting metal weapons, armor, and tools")
```

**Specialization**: Metalworking

**Crafts:**
- **Weapons**: Swords, axes, maces, polearms
- **Armor**: Plate armor, chainmail, shields
- **Tools**: Pickaxes, hammers, anvils

**Materials:**
- Metal ingots (iron, steel, mithril)
- Coal/fuel
- Whetstones

**Tools Required:**
- Forge (heat source)
- Anvil (shaping)
- Hammer (tool)

**Example Recipes:**
- Iron Sword (Apprentice)
- Steel Axe (Journeyman)
- Plate Helmet (Expert)
- Legendary Blade (Master)

**Synergy Categories:**
- Smithing + Enchanting = Magic weapons
- Smithing + Engineering = Complex mechanisms

### LEATHERWORKING
```java
LEATHERWORKING(2, "Leatherworking", "Crafting leather armor and goods")
```

**Specialization**: Hide processing

**Crafts:**
- **Armor**: Leather armor, boots, gloves, belts
- **Accessories**: Pouches, bags, saddles
- **Goods**: Leather straps, bindings

**Materials:**
- Animal hides (cow, wolf, dragon)
- Tanning agents
- Thread/sinew

**Tools Required:**
- Tanning rack
- Needles
- Awl

**Example Recipes:**
- Leather Armor (Novice)
- Reinforced Boots (Apprentice)
- Dragon Hide Armor (Expert)
- Masterwork Belt (Master)

**Synergy Categories:**
- Leatherworking + Alchemy = Treated leather (resistance)
- Leatherworking + Tailoring = Mixed armor

### ALCHEMY
```java
ALCHEMY(3, "Alchemy", "Creating potions, elixirs, and transmuting materials")
```

**Specialization**: Potion brewing, transmutation

**Crafts:**
- **Potions**: Health, mana, stamina
- **Elixirs**: Stat buffs, resistances
- **Transmutation**: Material conversion
- **Poisons**: Weapon coatings (future)

**Materials:**
- Herbs, mushrooms, flowers
- Reagents (eyes, blood, essences)
- Glass vials, bottles

**Tools Required:**
- Alchemy table
- Mortar & pestle
- Distillation apparatus

**Example Recipes:**
- Health Potion (Apprentice)
- Mana Elixir (Journeyman)
- Transmute Iron → Gold (Expert)
- Philosopher's Stone (Master)

**Synergy Categories:**
- Alchemy + Enchanting = Magical potions
- Alchemy + Cooking = Enhanced foods

### ENCHANTING
```java
ENCHANTING(4, "Enchanting", "Imbuing items with magical properties")
```

**Specialization**: Item enhancement

**Crafts:**
- **Weapon Enchants**: Fire, ice, lightning damage
- **Armor Enchants**: Resistances, stat bonuses
- **Tool Enchants**: Efficiency, durability
- **Scrolls**: Single-use spell scrolls

**Materials:**
- Magical essences
- Soul gems
- Enchanted dust
- Base items to enchant

**Tools Required:**
- Enchanting table
- Arcane focus
- Enchanting scrolls

**Example Recipes:**
- Enchant Weapon (Fire) (Apprentice)
- Enchant Armor (Protection) (Journeyman)
- Fortify Strength Enchant (Expert)
- Legendary Enchantment (Master)

**Synergy Categories:**
- Enchanting + Smithing = Magic weapons/armor
- Enchanting + Runecrafting = Permanent enchants

### COOKING
```java
COOKING(5, "Cooking", "Preparing food and beverages")
```

**Specialization**: Food preparation

**Crafts:**
- **Meals**: Restore hunger, grant buffs
- **Beverages**: Drinks with temporary effects
- **Rations**: Long-lasting food
- **Feasts**: Party-wide buffs

**Materials:**
- Raw food (meat, vegetables, grains)
- Spices, herbs
- Water, alcohol

**Tools Required:**
- Cooking fire/stove
- Cooking pot
- Utensils

**Example Recipes:**
- Cooked Meat (Novice)
- Hearty Stew (Apprentice)
- Dragon Steak (Journeyman)
- Royal Feast (Master)

**Synergy Categories:**
- Cooking + Alchemy = Alchemical cuisine (stronger buffs)
- Cooking + Leatherworking = Water skins, food storage

### ENGINEERING
```java
ENGINEERING(6, "Engineering", "Building mechanisms, traps, and siege weapons")
```

**Specialization**: Mechanical devices

**Crafts:**
- **Mechanisms**: Gears, springs, clockwork
- **Traps**: Snares, spike traps, explosives
- **Siege Weapons**: Catapults, ballistae (future)
- **Gadgets**: Lockpicks, spyglasses

**Materials:**
- Metal components
- Wood, gears
- Explosives (gunpowder)
- Mechanisms

**Tools Required:**
- Workbench
- Tools (wrench, screwdriver)
- Blueprints

**Example Recipes:**
- Bear Trap (Apprentice)
- Mechanical Lock (Journeyman)
- Clockwork Golem (Expert)
- Siege Engine (Master)

**Synergy Categories:**
- Engineering + Smithing = Advanced mechanisms
- Engineering + Alchemy = Explosives

### RUNECRAFTING
```java
RUNECRAFTING(7, "Runecrafting", "Inscribing magical runes and glyphs")
```

**Specialization**: Rune inscription

**Crafts:**
- **Runes**: Socketable magic stones
- **Glyphs**: Permanent inscriptions
- **Wards**: Protective runes (future)
- **Teleportation Runes**: Fast travel (future)

**Materials:**
- Blank runes (stone, metal)
- Magical ink
- Essence (element-specific)

**Tools Required:**
- Runecrafting table
- Inscription tools
- Magical focus

**Example Recipes:**
- Rune of Fire (Apprentice)
- Glyph of Protection (Journeyman)
- Major Rune of Power (Expert)
- Elder Rune (Master)

**Synergy Categories:**
- Runecrafting + Enchanting = Layered magic
- Runecrafting + Smithing = Runic weapons

### TAILORING
```java
TAILORING(8, "Tailoring", "Crafting cloth armor and garments")
```

**Specialization**: Fabric work

**Crafts:**
- **Cloth Armor**: Robes, hoods, cloaks
- **Garments**: Shirts, pants, dresses
- **Accessories**: Scarves, bags, banners
- **Magical Cloth**: Enchanted fabrics (with Enchanting)

**Materials:**
- Cloth (linen, silk, wool)
- Thread, needles
- Dyes (colors)
- Magical thread (for robes)

**Tools Required:**
- Loom
- Sewing kit
- Tailoring table

**Example Recipes:**
- Cloth Robe (Novice)
- Silk Tunic (Apprentice)
- Mage Robes (Journeyman)
- Archmage Vestments (Master)

**Synergy Categories:**
- Tailoring + Enchanting = Magic robes
- Tailoring + Leatherworking = Mixed armor sets

## Specialization System

### Maximum 2 Specializations

```java
public class CraftingSkills {
    private Set<CraftingCategory> specializations = new HashSet<>();
    
    public boolean addSpecialization(CraftingCategory category) {
        if (specializations.size() >= 2) {
            return false;  // Already have 2 specializations
        }
        return specializations.add(category);
    }
}
```

**Design Rationale**:
- **Choice Matters**: Can't be good at everything
- **Trade Economy**: Need other players
- **Replayability**: Different specializations = different playstyles

### +20% XP Bonus

```java
public float getXpMultiplier(CraftingCategory category) {
    return isSpecialized(category) ? 1.2f : 1.0f;
}

// Example
float baseXp = 100;
float specializedXp = baseXp * 1.2f;  // 120 XP
float normalXp = baseXp * 1.0f;       // 100 XP
```

**Impact**:
- Specialized: 1000 XP → Master in ~42 crafts
- Normal: 1000 XP → Master in ~50 crafts
- **~16% faster** progression in specialized categories

### Specialization Choices

**Common Combinations:**

1. **Weapon Smith**: Smithing + Enchanting
   - Craft and enchant weapons
   - Self-sufficient for combat gear

2. **Armor Crafter**: Smithing + Leatherworking
   - Heavy and light armor
   - Covers all armor slots

3. **Potion Master**: Alchemy + Cooking
   - Consumables specialist
   - Support role

4. **Mage Crafter**: Enchanting + Runecrafting
   - Pure magic focus
   - Strongest enchantments

5. **Gadgeteer**: Engineering + Smithing
   - Mechanisms and metal
   - Utility specialist

6. **Merchant**: Leatherworking + Tailoring
   - Bags and storage
   - Economy focus

## API Reference

### Fields
```java
private final int id;              // Category ID (1-8)
private final String name;         // Display name ("Smithing")
private final String description;  // What this category crafts
```

### Methods
```java
int getId()                        // Get numeric ID
String getName()                   // Get display name
String getDescription()            // Get description text
```

### Static Methods
```java
static CraftingCategory fromId(int id)  // Get category by ID
static CraftingCategory[] values()      // Get all categories
```

## Usage Examples

### Checking Specialization

```java
CraftingSkills skills = new CraftingSkills();

// Add first specialization
skills.addSpecialization(CraftingCategory.SMITHING);
System.out.println("Specialized in Smithing!");

// Add second specialization
skills.addSpecialization(CraftingCategory.ENCHANTING);
System.out.println("Specialized in Enchanting!");

// Try to add third (fails)
boolean success = skills.addSpecialization(CraftingCategory.ALCHEMY);
if (!success) {
    System.out.println("Already have 2 specializations!");
}
```

### Calculating XP with Specialization

```java
public void craftItem(CraftingRecipe recipe, CraftingSkills skills) {
    float baseXp = 100;
    CraftingCategory category = recipe.getCategory();
    
    // Check specialization
    float specializationBonus = skills.isSpecialized(category) ? 1.2f : 1.0f;
    
    // Apply other multipliers
    float rarityBonus = recipe.getRarityMultiplier();
    float qualityBonus = result.getQuality().getXpMultiplier();
    
    float finalXp = baseXp * specializationBonus * rarityBonus * qualityBonus;
    
    skills.addXp(category, (int) finalXp);
}
```

### UI Display

```java
public void displayCraftingCategories(CraftingSkills skills) {
    for (CraftingCategory category : CraftingCategory.values()) {
        CraftingProficiency prof = skills.getProficiency(category);
        boolean specialized = skills.isSpecialized(category);
        
        String marker = specialized ? "★" : " ";
        System.out.println(marker + " " + category.getName() + ": " + prof.getName());
    }
}

// Output:
// ★ Smithing: Expert
// ★ Enchanting: Journeyman
//   Alchemy: Apprentice
//   Cooking: Novice
// ... etc
```

## Integration with Other Systems

### CraftingSkills
```java
public class CraftingSkills {
    private Map<CraftingCategory, Integer> xpMap = new HashMap<>();
    private Set<CraftingCategory> specializations = new HashSet<>();
    
    public void addXp(CraftingCategory category, int amount) {
        boolean isSpecialized = specializations.contains(category);
        float multiplier = isSpecialized ? 1.2f : 1.0f;
        int finalAmount = (int) (amount * multiplier);
        
        xpMap.merge(category, finalAmount, Integer::sum);
    }
}
```

### CraftingRecipe
```java
public class CraftingRecipe {
    private CraftingCategory category;
    
    public CraftingCategory getCategory() {
        return category;
    }
}
```

### Character (future)
```java
public class Character {
    private CraftingSkills craftingSkills;
    
    public void specializeCrafting(CraftingCategory category) {
        boolean success = craftingSkills.addSpecialization(category);
        if (success) {
            System.out.println("You are now specialized in " + category.getName() + "!");
        }
    }
}
```

## Performance Considerations

### Enum Performance
- **Memory**: 8 enum constants × ~70 bytes = ~560 bytes
- **Lookup**: `O(1)` by ordinal
- **Comparison**: Reference equality (fastest)

### Specialization Check
```java
// Fast: HashSet lookup O(1)
boolean isSpecialized = specializations.contains(category);
```

## Design Decisions

### Why 8 Categories Instead of 12+?

**Alternative**: More granular categories
- Weaponsmithing, Armorsmithing, Toolsmithing (instead of just Smithing)

**Rejected Because**:
- Too much specialization fragmentation
- Players would feel forced into narrow builds
- Trading economy too complex
- 8 categories already covers full item spectrum

### Why 2 Specialization Limit?

**Alternatives Considered:**
- **1 specialization**: Too restrictive, limits build diversity
- **3 specializations**: Too permissive, defeats purpose
- **Unlimited**: Everyone masters everything, no economy

**2 Specializations Wins**:
- Allows hybrid builds (Smithing + Enchanting)
- Still forces meaningful choices
- Creates interdependency (need 3rd category from others)

### Why +20% XP Bonus?

**Formula Analysis:**
- +10% bonus: Not impactful enough (50 crafts → 45 crafts)
- +20% bonus: Noticeable (50 crafts → 42 crafts), sweet spot
- +50% bonus: Too strong, non-specialized becomes worthless

**Playtesting Target**: Specialized path ~15-20% faster

## Known Limitations

1. **No Category Synergies**: Smithing + Enchanting doesn't unlock special recipes
2. **No Respec**: Can't change specializations once chosen
3. **No Sub-Categories**: Smithing covers all metal (weapons, armor, tools)
4. **Fixed Categories**: Mods can't add custom categories

## Future Enhancements

### Phase 1.6: Category Synergies
```java
// Unlock special recipes with 2 specializations
if (skills.hasSpecializations(SMITHING, ENCHANTING)) {
    unlockRecipe("runic_blade");  // Requires both
}
```

### Phase 1.7: Respec System
```java
// Reset specializations (costs gold/materials)
public boolean resetSpecializations(int goldCost) {
    if (hasGold(goldCost)) {
        specializations.clear();
        return true;
    }
    return false;
}
```

### Phase 2: Master Artisan Title
```java
// Master in all 8 categories = achievement
if (isMasterInAllCategories()) {
    grantTitle("Master Artisan");
    unlockRecipe("ultimate_artifact");
}
```

## Testing

See [CraftingCategoryTest.md](../../../../../test/java/org/adventure/CraftingCategoryTest.md) for test coverage:
- All 8 categories defined
- Specialization limits (max 2)
- XP bonus calculations
- Category-specific progression
- Edge cases (duplicate specializations)

## References

- **Design Docs**: `docs/objects_crafting_legacy.md` → Specialization System
- **Data Models**: `docs/data_models.md` → CraftingSkills Schema
- **Grand Plan**: `docs/grand_plan.md` → Phase 1.4 Economy
- **Related Classes**: [CraftingSkills.md](CraftingSkills.md), [CraftingProficiency.md](CraftingProficiency.md)
- **Tests**: [CraftingTest.md](../../../../../test/java/org/adventure/CraftingTest.md)

---

**Last Updated:** Phase 1.4 Implementation (November 2025)  
**Status:** ✅ Complete - 8 categories, specialization system tested
