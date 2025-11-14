package org.adventure;

import org.adventure.settlement.Village;
import org.adventure.settlement.VillageType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tests for Village data model.
 * Validates Builder pattern, field validation, and structure management.
 */
public class VillageTest {
    
    @Test
    public void testVillageBuilder() {
        Village village = new Village.Builder()
                .id("village_1")
                .name("Meadowdale")
                .type(VillageType.VILLAGE)
                .centerX(100)
                .centerY(200)
                .structureIds(Arrays.asList("structure_1", "structure_2", "structure_3"))
                .population(25)
                .governingClanId("clan_1")
                .foundedTick(1000)
                .build();
        
        assertEquals("village_1", village.getId());
        assertEquals("Meadowdale", village.getName());
        assertEquals(VillageType.VILLAGE, village.getType());
        assertEquals(100, village.getCenterX());
        assertEquals(200, village.getCenterY());
        assertEquals(3, village.getStructureIds().size());
        assertEquals(25, village.getPopulation());
        assertEquals("clan_1", village.getGoverningClanId());
        assertEquals(1000, village.getFoundedTick());
    }
    
    @Test
    public void testVillageBuilderRequiredFields() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Village.Builder()
                    .name("Test")
                    .type(VillageType.VILLAGE)
                    .centerX(0)
                    .centerY(0)
                    .build(); // Missing id
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Village.Builder()
                    .id("village_1")
                    .type(VillageType.VILLAGE)
                    .centerX(0)
                    .centerY(0)
                    .build(); // Missing name
        });
    }
    
    @Test
    public void testVillageBuilderValidation() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Village.Builder()
                    .id("")
                    .name("Test")
                    .type(VillageType.VILLAGE)
                    .centerX(0)
                    .centerY(0)
                    .build(); // Empty id
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Village.Builder()
                    .id("village_1")
                    .name("Test")
                    .type(VillageType.VILLAGE)
                    .centerX(0)
                    .centerY(0)
                    .population(-5)
                    .build(); // Negative population
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Village.Builder()
                    .id("village_1")
                    .name("Test")
                    .type(VillageType.VILLAGE)
                    .centerX(0)
                    .centerY(0)
                    .foundedTick(-100)
                    .build(); // Negative founded tick
        });
    }
    
    @Test
    public void testAddStructure() {
        Village village = new Village.Builder()
                .id("village_1")
                .name("Testville")
                .type(VillageType.VILLAGE)
                .centerX(0)
                .centerY(0)
                .structureIds(new ArrayList<>(Arrays.asList("initial_structure")))
                .build();
        
        assertEquals(1, village.getStructureIds().size());
        
        village.addStructure("structure_1");
        assertEquals(2, village.getStructureIds().size());
        assertTrue(village.getStructureIds().contains("structure_1"));
        
        village.addStructure("structure_2");
        assertEquals(3, village.getStructureIds().size());
    }
    
    @Test
    public void testAddStructureNoDuplicates() {
        Village village = new Village.Builder()
                .id("village_1")
                .name("Testville")
                .type(VillageType.VILLAGE)
                .centerX(0)
                .centerY(0)
                .structureIds(new ArrayList<>(Arrays.asList("initial_structure")))
                .build();
        
        village.addStructure("structure_1");
        village.addStructure("structure_1"); // Duplicate
        
        assertEquals(2, village.getStructureIds().size()); // initial + structure_1
    }
    
    @Test
    public void testRemoveStructure() {
        List<String> structureIds = new ArrayList<>(Arrays.asList("structure_1", "structure_2"));
        
        Village village = new Village.Builder()
                .id("village_1")
                .name("Testville")
                .type(VillageType.VILLAGE)
                .centerX(0)
                .centerY(0)
                .structureIds(structureIds)
                .build();
        
        assertEquals(2, village.getStructureIds().size());
        
        village.removeStructure("structure_1");
        assertEquals(1, village.getStructureIds().size());
        assertFalse(village.getStructureIds().contains("structure_1"));
    }
    
    @Test
    public void testRemoveNonexistentStructure() {
        Village village = new Village.Builder()
                .id("village_1")
                .name("Testville")
                .type(VillageType.VILLAGE)
                .centerX(0)
                .centerY(0)
                .structureIds(new ArrayList<>(Arrays.asList("initial_structure")))
                .build();
        
        village.removeStructure("nonexistent");
        assertEquals(1, village.getStructureIds().size()); // Still has initial structure
    }
    
    @Test
    public void testSetName() {
        Village village = new Village.Builder()
                .id("village_1")
                .name("Testville")
                .type(VillageType.VILLAGE)
                .centerX(0)
                .centerY(0)
                .structureIds(new ArrayList<>(Arrays.asList("initial_structure")))
                .build();
        
        village.setName("NewName");
        assertEquals("NewName", village.getName());
        
        assertThrows(IllegalArgumentException.class, () -> {
            village.setName(null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            village.setName("");
        });
    }
    
    @Test
    public void testSetPopulation() {
        Village village = new Village.Builder()
                .id("village_1")
                .name("Testville")
                .type(VillageType.VILLAGE)
                .centerX(0)
                .centerY(0)
                .structureIds(new ArrayList<>(Arrays.asList("initial_structure")))
                .build();
        
        village.setPopulation(50);
        assertEquals(50, village.getPopulation());
        
        assertThrows(IllegalArgumentException.class, () -> {
            village.setPopulation(-10);
        });
    }
    
    @Test
    public void testSetType() {
        Village village = new Village.Builder()
                .id("village_1")
                .name("Testville")
                .type(VillageType.VILLAGE)
                .centerX(0)
                .centerY(0)
                .structureIds(new ArrayList<>(Arrays.asList("initial_structure")))
                .build();
        
        village.setType(VillageType.TOWN);
        assertEquals(VillageType.TOWN, village.getType());
        
        village.setType(VillageType.CITY);
        assertEquals(VillageType.CITY, village.getType());
    }
    
    @Test
    public void testSchemaVersion() {
        Village village = new Village.Builder()
                .id("village_1")
                .name("Testville")
                .type(VillageType.VILLAGE)
                .centerX(0)
                .centerY(0)
                .structureIds(new ArrayList<>(Arrays.asList("initial_structure")))
                .build();
        
        assertEquals(1, village.getSchemaVersion());
    }
}
