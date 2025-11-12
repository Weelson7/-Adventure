package org.adventure;

import org.adventure.structure.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Structure class and related components.
 * 
 * Tests cover:
 * - Structure creation and validation
 * - Health management (damage, repair, destruction)
 * - Ownership and permissions
 * - Rooms and upgrades
 * - Edge cases and error conditions
 * 
 * Target: 85%+ line coverage for structures module
 */
public class StructureTest {
    
    @Test
    public void testStructureCreation() {
        Structure structure = new Structure.Builder()
                .id("struct-001")
                .type(StructureType.HOUSE)
                .ownerId("char-001")
                .ownerType(OwnerType.CHARACTER)
                .locationTileId("100:200:0")
                .health(100.0)
                .maxHealth(100.0)
                .createdAtTick(0)
                .build();
        
        assertNotNull(structure);
        assertEquals("struct-001", structure.getId());
        assertEquals(StructureType.HOUSE, structure.getType());
        assertEquals("char-001", structure.getOwnerId());
        assertEquals(OwnerType.CHARACTER, structure.getOwnerType());
        assertEquals("100:200:0", structure.getLocationTileId());
        assertEquals(100.0, structure.getHealth());
        assertEquals(100.0, structure.getMaxHealth());
        assertFalse(structure.isDestroyed());
        assertFalse(structure.isDamaged());
    }
    
    @Test
    public void testStructureRequiresId() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Structure.Builder()
                    .type(StructureType.HOUSE)
                    .locationTileId("100:200:0")
                    .maxHealth(100.0)
                    .build();
        });
    }
    
    @Test
    public void testStructureRequiresType() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Structure.Builder()
                    .id("struct-001")
                    .locationTileId("100:200:0")
                    .maxHealth(100.0)
                    .build();
        });
    }
    
    @Test
    public void testStructureRequiresLocationTileId() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Structure.Builder()
                    .id("struct-001")
                    .type(StructureType.HOUSE)
                    .maxHealth(100.0)
                    .build();
        });
    }
    
    @Test
    public void testStructureHealthCannotExceedMax() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Structure.Builder()
                    .id("struct-001")
                    .type(StructureType.HOUSE)
                    .locationTileId("100:200:0")
                    .health(150.0)
                    .maxHealth(100.0)
                    .build();
        });
    }
    
    @Test
    public void testStructureHealthCannotBeNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Structure.Builder()
                    .id("struct-001")
                    .type(StructureType.HOUSE)
                    .locationTileId("100:200:0")
                    .health(-10.0)
                    .maxHealth(100.0)
                    .build();
        });
    }
    
    @Test
    public void testTakeDamage() {
        Structure structure = new Structure.Builder()
                .id("struct-001")
                .type(StructureType.HOUSE)
                .locationTileId("100:200:0")
                .health(100.0)
                .maxHealth(100.0)
                .build();
        
        structure.takeDamage(30.0, 100);
        assertEquals(70.0, structure.getHealth());
        assertTrue(structure.isDamaged());
        assertFalse(structure.isDestroyed());
        assertEquals(100, structure.getLastUpdatedTick());
    }
    
    @Test
    public void testTakeDamageCannotGoNegative() {
        Structure structure = new Structure.Builder()
                .id("struct-001")
                .type(StructureType.HOUSE)
                .locationTileId("100:200:0")
                .health(100.0)
                .maxHealth(100.0)
                .build();
        
        structure.takeDamage(150.0, 100);
        assertEquals(0.0, structure.getHealth());
        assertTrue(structure.isDestroyed());
    }
    
    @Test
    public void testTakeDamageRejectsNegativeAmount() {
        Structure structure = new Structure.Builder()
                .id("struct-001")
                .type(StructureType.HOUSE)
                .locationTileId("100:200:0")
                .health(100.0)
                .maxHealth(100.0)
                .build();
        
        assertThrows(IllegalArgumentException.class, () -> {
            structure.takeDamage(-10.0, 100);
        });
    }
    
    @Test
    public void testRepair() {
        Structure structure = new Structure.Builder()
                .id("struct-001")
                .type(StructureType.HOUSE)
                .locationTileId("100:200:0")
                .health(50.0)
                .maxHealth(100.0)
                .build();
        
        assertTrue(structure.isDamaged());
        structure.repair(30.0, 200);
        assertEquals(80.0, structure.getHealth());
        assertTrue(structure.isDamaged());
        assertEquals(200, structure.getLastUpdatedTick());
        
        structure.repair(20.0, 300);
        assertEquals(100.0, structure.getHealth());
        assertFalse(structure.isDamaged());
    }
    
    @Test
    public void testRepairCannotExceedMaxHealth() {
        Structure structure = new Structure.Builder()
                .id("struct-001")
                .type(StructureType.HOUSE)
                .locationTileId("100:200:0")
                .health(90.0)
                .maxHealth(100.0)
                .build();
        
        structure.repair(50.0, 100);
        assertEquals(100.0, structure.getHealth());
    }
    
    @Test
    public void testCannotRepairDestroyedStructure() {
        Structure structure = new Structure.Builder()
                .id("struct-001")
                .type(StructureType.HOUSE)
                .locationTileId("100:200:0")
                .health(0.0)
                .maxHealth(100.0)
                .build();
        
        assertTrue(structure.isDestroyed());
        assertThrows(IllegalStateException.class, () -> {
            structure.repair(50.0, 100);
        });
    }
    
    @Test
    public void testRepairRejectsNegativeAmount() {
        Structure structure = new Structure.Builder()
                .id("struct-001")
                .type(StructureType.HOUSE)
                .locationTileId("100:200:0")
                .health(50.0)
                .maxHealth(100.0)
                .build();
        
        assertThrows(IllegalArgumentException.class, () -> {
            structure.repair(-10.0, 100);
        });
    }
    
    @Test
    public void testHealthPercentage() {
        Structure structure = new Structure.Builder()
                .id("struct-001")
                .type(StructureType.HOUSE)
                .locationTileId("100:200:0")
                .health(75.0)
                .maxHealth(100.0)
                .build();
        
        assertEquals(0.75, structure.getHealthPercentage(), 0.001);
        
        structure.takeDamage(25.0, 100);
        assertEquals(0.50, structure.getHealthPercentage(), 0.001);
        
        structure.takeDamage(50.0, 200);
        assertEquals(0.0, structure.getHealthPercentage(), 0.001);
    }
    
    @Test
    public void testOwnerAlwaysHasFullAccess() {
        Structure structure = new Structure.Builder()
                .id("struct-001")
                .type(StructureType.HOUSE)
                .ownerId("char-001")
                .ownerType(OwnerType.CHARACTER)
                .locationTileId("100:200:0")
                .maxHealth(100.0)
                .build();
        
        assertTrue(structure.hasAccess(AccessRole.OWNER, AccessLevel.FULL));
        assertEquals(AccessLevel.FULL, structure.getAccessLevel(AccessRole.OWNER));
    }
    
    @Test
    public void testSetPermission() {
        Structure structure = new Structure.Builder()
                .id("struct-001")
                .type(StructureType.HOUSE)
                .ownerId("char-001")
                .ownerType(OwnerType.CHARACTER)
                .locationTileId("100:200:0")
                .maxHealth(100.0)
                .build();
        
        structure.setPermission(AccessRole.PUBLIC, AccessLevel.READ, 100);
        assertTrue(structure.hasAccess(AccessRole.PUBLIC, AccessLevel.READ));
        assertFalse(structure.hasAccess(AccessRole.PUBLIC, AccessLevel.USE));
        assertEquals(100, structure.getLastUpdatedTick());
    }
    
    @Test
    public void testOwnerPermissionCannotBeChanged() {
        Structure structure = new Structure.Builder()
                .id("struct-001")
                .type(StructureType.HOUSE)
                .ownerId("char-001")
                .ownerType(OwnerType.CHARACTER)
                .locationTileId("100:200:0")
                .maxHealth(100.0)
                .build();
        
        // Try to set owner permission to READ (should remain FULL)
        structure.setPermission(AccessRole.OWNER, AccessLevel.READ, 100);
        assertEquals(AccessLevel.FULL, structure.getAccessLevel(AccessRole.OWNER));
    }
    
    @Test
    public void testAccessLevelHierarchy() {
        Structure structure = new Structure.Builder()
                .id("struct-001")
                .type(StructureType.HOUSE)
                .locationTileId("100:200:0")
                .maxHealth(100.0)
                .build();
        
        structure.setPermission(AccessRole.CLAN_MEMBER, AccessLevel.MODIFY, 100);
        
        // MODIFY (level 3) allows READ (level 1) and USE (level 2)
        assertTrue(structure.hasAccess(AccessRole.CLAN_MEMBER, AccessLevel.READ));
        assertTrue(structure.hasAccess(AccessRole.CLAN_MEMBER, AccessLevel.USE));
        assertTrue(structure.hasAccess(AccessRole.CLAN_MEMBER, AccessLevel.MODIFY));
        
        // But not MANAGE (level 4) or FULL (level 5)
        assertFalse(structure.hasAccess(AccessRole.CLAN_MEMBER, AccessLevel.MANAGE));
        assertFalse(structure.hasAccess(AccessRole.CLAN_MEMBER, AccessLevel.FULL));
    }
    
    @Test
    public void testDefaultAccessIsNone() {
        Structure structure = new Structure.Builder()
                .id("struct-001")
                .type(StructureType.HOUSE)
                .locationTileId("100:200:0")
                .maxHealth(100.0)
                .build();
        
        assertEquals(AccessLevel.NONE, structure.getAccessLevel(AccessRole.PUBLIC));
        assertFalse(structure.hasAccess(AccessRole.PUBLIC, AccessLevel.READ));
    }
    
    @Test
    public void testTransferOwnership() {
        Structure structure = new Structure.Builder()
                .id("struct-001")
                .type(StructureType.HOUSE)
                .ownerId("char-001")
                .ownerType(OwnerType.CHARACTER)
                .locationTileId("100:200:0")
                .maxHealth(100.0)
                .permission(AccessRole.PUBLIC, AccessLevel.READ)
                .build();
        
        assertEquals("char-001", structure.getOwnerId());
        assertEquals(OwnerType.CHARACTER, structure.getOwnerType());
        
        structure.transferOwnership("char-002", OwnerType.CHARACTER, 500);
        
        assertEquals("char-002", structure.getOwnerId());
        assertEquals(OwnerType.CHARACTER, structure.getOwnerType());
        assertEquals(500, structure.getLastUpdatedTick());
        
        // Old permissions should be cleared
        assertEquals(AccessLevel.NONE, structure.getAccessLevel(AccessRole.PUBLIC));
        
        // New owner should have FULL access
        assertEquals(AccessLevel.FULL, structure.getAccessLevel(AccessRole.OWNER));
    }
    
    @Test
    public void testTransferOwnershipRequiresValidOwner() {
        Structure structure = new Structure.Builder()
                .id("struct-001")
                .type(StructureType.HOUSE)
                .ownerId("char-001")
                .ownerType(OwnerType.CHARACTER)
                .locationTileId("100:200:0")
                .maxHealth(100.0)
                .build();
        
        assertThrows(IllegalArgumentException.class, () -> {
            structure.transferOwnership("", OwnerType.CHARACTER, 100);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            structure.transferOwnership(null, OwnerType.CHARACTER, 100);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            structure.transferOwnership("char-002", null, 100);
        });
    }
    
    @Test
    public void testAddRoom() {
        Structure structure = new Structure.Builder()
                .id("struct-001")
                .type(StructureType.HOUSE)
                .locationTileId("100:200:0")
                .maxHealth(100.0)
                .build();
        
        assertEquals(0, structure.getRooms().size());
        
        Room room = new Room.Builder()
                .id("room-001")
                .category(RoomCategory.LIVING_QUARTERS)
                .size(10)
                .build();
        
        structure.addRoom(room, 100);
        
        assertEquals(1, structure.getRooms().size());
        assertTrue(structure.getRooms().contains(room));
        assertEquals(100, structure.getLastUpdatedTick());
    }
    
    @Test
    public void testAddRoomRejectsNull() {
        Structure structure = new Structure.Builder()
                .id("struct-001")
                .type(StructureType.HOUSE)
                .locationTileId("100:200:0")
                .maxHealth(100.0)
                .build();
        
        assertThrows(IllegalArgumentException.class, () -> {
            structure.addRoom(null, 100);
        });
    }
    
    @Test
    public void testApplyUpgrade() {
        Structure structure = new Structure.Builder()
                .id("struct-001")
                .type(StructureType.HOUSE)
                .locationTileId("100:200:0")
                .maxHealth(100.0)
                .build();
        
        assertEquals(0, structure.getUpgrades().size());
        
        Upgrade upgrade = new Upgrade.Builder()
                .id("upgrade-001")
                .name("Reinforced Walls")
                .timeRequiredTicks(1000)
                .build();
        
        structure.applyUpgrade(upgrade, 100);
        
        assertEquals(1, structure.getUpgrades().size());
        assertTrue(structure.getUpgrades().contains(upgrade));
        assertEquals(100, structure.getLastUpdatedTick());
    }
    
    @Test
    public void testApplyUpgradeRejectsNull() {
        Structure structure = new Structure.Builder()
                .id("struct-001")
                .type(StructureType.HOUSE)
                .locationTileId("100:200:0")
                .maxHealth(100.0)
                .build();
        
        assertThrows(IllegalArgumentException.class, () -> {
            structure.applyUpgrade(null, 100);
        });
    }
    
    @Test
    public void testStructureEquality() {
        Structure s1 = new Structure.Builder()
                .id("struct-001")
                .type(StructureType.HOUSE)
                .locationTileId("100:200:0")
                .maxHealth(100.0)
                .build();
        
        Structure s2 = new Structure.Builder()
                .id("struct-001")
                .type(StructureType.CASTLE)  // Different type
                .locationTileId("200:300:0")  // Different location
                .maxHealth(200.0)             // Different health
                .build();
        
        Structure s3 = new Structure.Builder()
                .id("struct-002")  // Different ID
                .type(StructureType.HOUSE)
                .locationTileId("100:200:0")
                .maxHealth(100.0)
                .build();
        
        // Equality based on ID only
        assertEquals(s1, s2);
        assertNotEquals(s1, s3);
        assertEquals(s1.hashCode(), s2.hashCode());
    }
    
    @Test
    public void testStructureTypeCategories() {
        assertTrue(StructureType.HOUSE.isResidential());
        assertFalse(StructureType.HOUSE.isMilitary());
        
        assertTrue(StructureType.FORTRESS.isMilitary());
        assertFalse(StructureType.FORTRESS.isCommercial());
        
        assertTrue(StructureType.SHOP.isCommercial());
        assertFalse(StructureType.SHOP.isMagical());
        
        assertTrue(StructureType.WIZARD_TOWER.isMagical());
        assertFalse(StructureType.WIZARD_TOWER.isRuins());
        
        assertTrue(StructureType.ANCIENT_RUINS.isRuins());
        assertFalse(StructureType.ANCIENT_RUINS.isSpecial());
        
        assertTrue(StructureType.TEMPLE.isSpecial());
        assertFalse(StructureType.TEMPLE.isResidential());
    }
}
