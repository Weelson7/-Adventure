package org.adventure.crafting;

import org.adventure.items.ItemCategory;
import org.adventure.items.ItemPrototype;
import org.adventure.items.ItemRarity;

import java.util.ArrayList;
import java.util.List;

/**
 * ItemRegistry defines all item prototypes for the MVP.
 * Provides factory methods for creating standard item prototypes.
 */
public class ItemRegistry {
    
    /**
     * Gets all MVP item prototypes.
     */
    public static List<ItemPrototype> getMvpItemPrototypes() {
        List<ItemPrototype> prototypes = new ArrayList<>();
        
        // === WEAPONS ===
        prototypes.add(new ItemPrototype.Builder("iron_sword", "Iron Sword", ItemCategory.WEAPON)
                .description("A standard iron sword, well-balanced for combat")
                .maxDurability(200.0f)
                .baseValue(50)
                .weight(3.5f)
                .property("damage", 10)
                .property("attackSpeed", 1.0)
                .rarity(ItemRarity.COMMON)
                .repairable(true)
                .enchantable(true)
                .build());
        
        prototypes.add(new ItemPrototype.Builder("steel_axe", "Steel Axe", ItemCategory.WEAPON)
                .description("A heavy steel axe, effective against armor")
                .maxDurability(180.0f)
                .baseValue(70)
                .weight(5.0f)
                .property("damage", 14)
                .property("attackSpeed", 0.8)
                .rarity(ItemRarity.UNCOMMON)
                .repairable(true)
                .enchantable(true)
                .build());
        
        // === TOOLS ===
        prototypes.add(new ItemPrototype.Builder("iron_pickaxe", "Iron Pickaxe", ItemCategory.TOOL)
                .description("Used for mining ore and stone")
                .maxDurability(300.0f)
                .baseValue(40)
                .weight(4.0f)
                .property("miningSpeed", 1.0)
                .property("efficiency", 1.0)
                .rarity(ItemRarity.COMMON)
                .repairable(true)
                .build());
        
        prototypes.add(new ItemPrototype.Builder("steel_hammer", "Steel Hammer", ItemCategory.TOOL)
                .description("A smithing hammer for crafting metal items")
                .maxDurability(400.0f)
                .baseValue(60)
                .weight(3.0f)
                .property("smithingBonus", 0.2)
                .rarity(ItemRarity.UNCOMMON)
                .repairable(true)
                .build());
        
        // === ARMOR ===
        prototypes.add(new ItemPrototype.Builder("leather_armor", "Leather Armor", ItemCategory.ARMOR)
                .description("Light armor offering basic protection")
                .maxDurability(150.0f)
                .baseValue(30)
                .weight(5.0f)
                .property("armorRating", 5)
                .property("movementPenalty", 0.0)
                .rarity(ItemRarity.COMMON)
                .repairable(true)
                .enchantable(true)
                .build());
        
        prototypes.add(new ItemPrototype.Builder("iron_chainmail", "Iron Chainmail", ItemCategory.ARMOR)
                .description("Medium armor with good protection")
                .maxDurability(250.0f)
                .baseValue(80)
                .weight(15.0f)
                .property("armorRating", 12)
                .property("movementPenalty", 0.1)
                .rarity(ItemRarity.UNCOMMON)
                .repairable(true)
                .enchantable(true)
                .build());
        
        // === MATERIALS ===
        prototypes.add(new ItemPrototype.Builder("iron_ore", "Iron Ore", ItemCategory.MATERIAL)
                .description("Raw iron ore, can be smelted")
                .maxDurability(1.0f) // Materials don't degrade
                .baseValue(5)
                .weight(1.0f)
                .rarity(ItemRarity.COMMON)
                .repairable(false)
                .stackable(true, 99)
                .build());
        
        prototypes.add(new ItemPrototype.Builder("iron_ingot", "Iron Ingot", ItemCategory.MATERIAL)
                .description("Smelted iron, ready for smithing")
                .maxDurability(1.0f)
                .baseValue(10)
                .weight(0.5f)
                .rarity(ItemRarity.COMMON)
                .repairable(false)
                .stackable(true, 99)
                .build());
        
        prototypes.add(new ItemPrototype.Builder("steel_ingot", "Steel Ingot", ItemCategory.MATERIAL)
                .description("High-quality steel for crafting")
                .maxDurability(1.0f)
                .baseValue(20)
                .weight(0.5f)
                .rarity(ItemRarity.UNCOMMON)
                .repairable(false)
                .stackable(true, 99)
                .build());
        
        prototypes.add(new ItemPrototype.Builder("leather", "Leather", ItemCategory.MATERIAL)
                .description("Tanned animal hide")
                .maxDurability(1.0f)
                .baseValue(8)
                .weight(0.3f)
                .rarity(ItemRarity.COMMON)
                .repairable(false)
                .stackable(true, 99)
                .build());
        
        prototypes.add(new ItemPrototype.Builder("wood_plank", "Wood Plank", ItemCategory.MATERIAL)
                .description("Processed wood for crafting")
                .maxDurability(1.0f)
                .baseValue(2)
                .weight(0.5f)
                .rarity(ItemRarity.COMMON)
                .repairable(false)
                .stackable(true, 99)
                .build());
        
        // === CONSUMABLES ===
        prototypes.add(new ItemPrototype.Builder("healing_potion", "Healing Potion", ItemCategory.POTION)
                .description("Restores health when consumed")
                .maxDurability(1.0f)
                .baseValue(25)
                .weight(0.2f)
                .property("healAmount", 50)
                .rarity(ItemRarity.COMMON)
                .repairable(false)
                .stackable(true, 10)
                .build());
        
        return prototypes;
    }
    
    /**
     * Gets all MVP crafting recipes.
     */
    public static List<CraftingRecipe> getMvpRecipes() {
        List<CraftingRecipe> recipes = new ArrayList<>();
        
        // === SMITHING RECIPES ===
        
        // Iron Sword
        recipes.add(new CraftingRecipe.Builder("recipe_iron_sword", "Iron Sword", CraftingCategory.SMITHING)
                .description("Craft an iron sword")
                .addMaterial("iron_ingot", 3)
                .addMaterial("wood_plank", 1)
                .addTool("steel_hammer")
                .minProficiency(CraftingProficiency.NOVICE)
                .output("iron_sword", 1)
                .craftingTime(20)
                .baseXp(20)
                .baseDifficulty(0.2f)
                .build());
        
        // Steel Axe
        recipes.add(new CraftingRecipe.Builder("recipe_steel_axe", "Steel Axe", CraftingCategory.SMITHING)
                .description("Craft a steel axe")
                .addMaterial("steel_ingot", 4)
                .addMaterial("wood_plank", 2)
                .addTool("steel_hammer")
                .minProficiency(CraftingProficiency.APPRENTICE)
                .output("steel_axe", 1)
                .craftingTime(30)
                .baseXp(30)
                .baseDifficulty(0.3f)
                .build());
        
        // Iron Pickaxe
        recipes.add(new CraftingRecipe.Builder("recipe_iron_pickaxe", "Iron Pickaxe", CraftingCategory.SMITHING)
                .description("Craft an iron pickaxe")
                .addMaterial("iron_ingot", 3)
                .addMaterial("wood_plank", 2)
                .addTool("steel_hammer")
                .minProficiency(CraftingProficiency.NOVICE)
                .output("iron_pickaxe", 1)
                .craftingTime(20)
                .baseXp(20)
                .baseDifficulty(0.15f)
                .build());
        
        // Steel Hammer (tool to make tools!)
        recipes.add(new CraftingRecipe.Builder("recipe_steel_hammer", "Steel Hammer", CraftingCategory.SMITHING)
                .description("Craft a steel hammer")
                .addMaterial("steel_ingot", 2)
                .addMaterial("wood_plank", 1)
                .addTool("steel_hammer") // Requires existing hammer
                .minProficiency(CraftingProficiency.APPRENTICE)
                .output("steel_hammer", 1)
                .craftingTime(25)
                .baseXp(25)
                .baseDifficulty(0.25f)
                .build());
        
        // Iron Chainmail
        recipes.add(new CraftingRecipe.Builder("recipe_iron_chainmail", "Iron Chainmail", CraftingCategory.SMITHING)
                .description("Craft iron chainmail armor")
                .addMaterial("iron_ingot", 8)
                .addTool("steel_hammer")
                .minProficiency(CraftingProficiency.JOURNEYMAN)
                .output("iron_chainmail", 1)
                .craftingTime(40)
                .baseXp(40)
                .baseDifficulty(0.35f)
                .build());
        
        // === TAILORING RECIPES ===
        
        // Leather Armor
        recipes.add(new CraftingRecipe.Builder("recipe_leather_armor", "Leather Armor", CraftingCategory.TAILORING)
                .description("Craft leather armor")
                .addMaterial("leather", 10)
                .minProficiency(CraftingProficiency.NOVICE)
                .output("leather_armor", 1)
                .craftingTime(30)
                .baseXp(25)
                .baseDifficulty(0.2f)
                .build());
        
        // === ALCHEMY RECIPES ===
        
        // Healing Potion
        recipes.add(new CraftingRecipe.Builder("recipe_healing_potion", "Healing Potion", CraftingCategory.ALCHEMY)
                .description("Brew a healing potion")
                .addMaterial("iron_ore", 1) // Placeholder materials
                .minProficiency(CraftingProficiency.NOVICE)
                .output("healing_potion", 1)
                .craftingTime(15)
                .baseXp(15)
                .baseDifficulty(0.25f)
                .build());
        
        return recipes;
    }
}
