package org.adventure;

import org.adventure.items.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Item class functionality including durability, repair, and stacking.
 */
public class ItemTest {
    
    @Test
    public void testItemCreationFromPrototype() {
        ItemPrototype proto = new ItemPrototype.Builder("test_sword", "Test Sword", ItemCategory.WEAPON)
                .maxDurability(100.0f)
                .baseValue(50)
                .build();
        
        Item item = Item.fromPrototype(proto);
        
        assertNotNull(item);
        assertEquals("test_sword", item.getPrototypeId());
        assertEquals(100.0f, item.getMaxDurability());
        assertEquals(100.0f, item.getCurrentDurability());
        assertEquals(1, item.getQuantity());
    }
    
    @Test
    public void testItemDurabilityDamage() {
        ItemPrototype proto = new ItemPrototype.Builder("test_item", "Test Item", ItemCategory.WEAPON)
                .maxDurability(100.0f)
                .build();
        
        Item item = Item.fromPrototype(proto);
        
        boolean broken = item.damage(30.0f);
        
        assertFalse(broken);
        assertEquals(70.0f, item.getCurrentDurability());
        assertEquals(0.7f, item.getDurabilityPercent(), 0.01f);
    }
    
    @Test
    public void testItemBreaksAtZeroDurability() {
        ItemPrototype proto = new ItemPrototype.Builder("test_item", "Test Item", ItemCategory.WEAPON)
                .maxDurability(100.0f)
                .build();
        
        Item item = Item.fromPrototype(proto);
        
        boolean broken = item.damage(150.0f);
        
        assertTrue(broken);
        assertTrue(item.isBroken());
        assertEquals(0.0f, item.getCurrentDurability());
    }
    
    @Test
    public void testItemRepair() {
        ItemPrototype proto = new ItemPrototype.Builder("test_item", "Test Item", ItemCategory.WEAPON)
                .maxDurability(100.0f)
                .build();
        
        Item item = Item.fromPrototype(proto);
        item.damage(40.0f);
        
        float repaired = item.repair(20.0f);
        
        assertEquals(20.0f, repaired);
        assertEquals(80.0f, item.getCurrentDurability());
    }
    
    @Test
    public void testItemRepairCappedAtMax() {
        ItemPrototype proto = new ItemPrototype.Builder("test_item", "Test Item", ItemCategory.WEAPON)
                .maxDurability(100.0f)
                .build();
        
        Item item = Item.fromPrototype(proto);
        item.damage(20.0f);
        
        float repaired = item.repair(50.0f);
        
        assertEquals(20.0f, repaired); // Only 20 needed to reach max
        assertEquals(100.0f, item.getCurrentDurability());
    }
    
    @Test
    public void testCannotRepairBrokenItem() {
        ItemPrototype proto = new ItemPrototype.Builder("test_item", "Test Item", ItemCategory.WEAPON)
                .maxDurability(100.0f)
                .build();
        
        Item item = Item.fromPrototype(proto);
        item.damage(100.0f);
        
        assertTrue(item.isBroken());
        assertThrows(IllegalStateException.class, () -> item.repair(10.0f));
    }
    
    @Test
    public void testRepairFully() {
        ItemPrototype proto = new ItemPrototype.Builder("test_item", "Test Item", ItemCategory.WEAPON)
                .maxDurability(100.0f)
                .build();
        
        Item item = Item.fromPrototype(proto);
        item.damage(65.0f);
        
        float repaired = item.repairFully();
        
        assertEquals(65.0f, repaired);
        assertEquals(100.0f, item.getCurrentDurability());
    }
    
    @Test
    public void testStackableItems() {
        ItemPrototype proto = new ItemPrototype.Builder("test_material", "Test Material", ItemCategory.MATERIAL)
                .maxDurability(1.0f)
                .stackable(true, 99)
                .build();
        
        Item item = Item.fromPrototype(proto, 10);
        
        assertEquals(10, item.getQuantity());
        assertTrue(proto.isStackable());
        assertEquals(99, proto.getMaxStackSize());
    }
    
    @Test
    public void testIncreaseQuantity() {
        ItemPrototype proto = new ItemPrototype.Builder("test_material", "Test Material", ItemCategory.MATERIAL)
                .stackable(true, 99)
                .build();
        
        Item item = Item.fromPrototype(proto, 10);
        
        boolean success = item.increaseQuantity(5);
        
        assertTrue(success);
        assertEquals(15, item.getQuantity());
    }
    
    @Test
    public void testDecreaseQuantity() {
        ItemPrototype proto = new ItemPrototype.Builder("test_material", "Test Material", ItemCategory.MATERIAL)
                .stackable(true, 99)
                .build();
        
        Item item = Item.fromPrototype(proto, 10);
        
        boolean success = item.decreaseQuantity(3);
        
        assertTrue(success);
        assertEquals(7, item.getQuantity());
    }
    
    @Test
    public void testDecreaseQuantityFailsIfInsufficient() {
        ItemPrototype proto = new ItemPrototype.Builder("test_material", "Test Material", ItemCategory.MATERIAL)
                .stackable(true, 99)
                .build();
        
        Item item = Item.fromPrototype(proto, 5);
        
        boolean success = item.decreaseQuantity(10);
        
        assertFalse(success);
        assertEquals(5, item.getQuantity()); // Unchanged
    }
    
    @Test
    public void testCannotStackNonStackableItems() {
        ItemPrototype proto = new ItemPrototype.Builder("test_sword", "Test Sword", ItemCategory.WEAPON)
                .maxDurability(100.0f)
                .stackable(false, 1)
                .build();
        
        assertFalse(proto.isStackable());
        assertThrows(IllegalArgumentException.class, () -> Item.fromPrototype(proto, 5));
    }
    
    @Test
    public void testCustomProperties() {
        ItemPrototype proto = new ItemPrototype.Builder("test_item", "Test Item", ItemCategory.WEAPON)
                .maxDurability(100.0f)
                .build();
        
        Item item = Item.fromPrototype(proto);
        item.setCustomProperty("enchantment", "fire");
        item.setCustomProperty("bonus_damage", 5);
        
        assertEquals("fire", item.getCustomProperty("enchantment"));
        assertEquals(5, item.getCustomProperty("bonus_damage"));
    }
    
    @Test
    public void testEvolutionPoints() {
        ItemPrototype proto = new ItemPrototype.Builder("test_sword", "Test Sword", ItemCategory.WEAPON)
                .maxDurability(100.0f)
                .build();
        
        Item item = Item.fromPrototype(proto);
        
        assertEquals(0, item.getEvolutionPoints());
        
        item.addEvolutionPoints(100);
        assertEquals(100, item.getEvolutionPoints());
        
        item.addEvolutionPoints(50);
        assertEquals(150, item.getEvolutionPoints());
    }
    
    @Test
    public void testEvolutionPointsCappedAt10000() {
        ItemPrototype proto = new ItemPrototype.Builder("test_sword", "Test Sword", ItemCategory.WEAPON)
                .maxDurability(100.0f)
                .build();
        
        Item item = Item.fromPrototype(proto);
        item.addEvolutionPoints(9000);
        item.addEvolutionPoints(2000); // Should cap at 10000
        
        assertEquals(10000, item.getEvolutionPoints());
    }
    
    @Test
    public void testItemPrototypeProperties() {
        ItemPrototype proto = new ItemPrototype.Builder("iron_sword", "Iron Sword", ItemCategory.WEAPON)
                .description("A sharp iron sword")
                .maxDurability(200.0f)
                .baseValue(50)
                .weight(3.5f)
                .property("damage", 10)
                .property("attackSpeed", 1.2)
                .rarity(ItemRarity.UNCOMMON)
                .enchantable(true)
                .build();
        
        assertEquals("iron_sword", proto.getId());
        assertEquals("Iron Sword", proto.getName());
        assertEquals(ItemCategory.WEAPON, proto.getCategory());
        assertEquals("A sharp iron sword", proto.getDescription());
        assertEquals(200.0f, proto.getMaxDurability());
        assertEquals(50, proto.getBaseValue());
        assertEquals(3.5f, proto.getWeight());
        assertEquals(10, proto.getProperty("damage"));
        assertEquals(1.2, proto.getProperty("attackSpeed"));
        assertEquals(ItemRarity.UNCOMMON, proto.getRarity());
        assertTrue(proto.isEnchantable());
    }
    
    @Test
    public void testItemRarityMultipliers() {
        assertEquals(1.0f, ItemRarity.COMMON.getValueMultiplier());
        assertEquals(1.5f, ItemRarity.UNCOMMON.getValueMultiplier());
        assertEquals(2.0f, ItemRarity.RARE.getValueMultiplier());
        assertEquals(3.0f, ItemRarity.LEGENDARY.getValueMultiplier());
    }
    
    @Test
    public void testItemCategoryEnum() {
        assertEquals("weapon", ItemCategory.WEAPON.getId());
        assertEquals("armor", ItemCategory.ARMOR.getId());
        assertEquals("tool", ItemCategory.TOOL.getId());
        assertEquals("material", ItemCategory.MATERIAL.getId());
    }
    
    @Test
    public void testItemOwnership() {
        ItemPrototype proto = new ItemPrototype.Builder("test_item", "Test Item", ItemCategory.WEAPON)
                .maxDurability(100.0f)
                .build();
        
        Item item = new Item.Builder(proto.getId(), proto.getMaxDurability())
                .ownerId("player_123")
                .build();
        
        assertEquals("player_123", item.getOwnerId());
        
        item.setOwnerId("player_456");
        assertEquals("player_456", item.getOwnerId());
    }
    
    @Test
    public void testHistoryReferenceId() {
        ItemPrototype proto = new ItemPrototype.Builder("legendary_sword", "Legendary Sword", ItemCategory.WEAPON)
                .maxDurability(300.0f)
                .rarity(ItemRarity.LEGENDARY)
                .build();
        
        Item item = new Item.Builder(proto.getId(), proto.getMaxDurability())
                .historyReferenceId("story_001")
                .build();
        
        assertEquals("story_001", item.getHistoryReferenceId());
    }
}
