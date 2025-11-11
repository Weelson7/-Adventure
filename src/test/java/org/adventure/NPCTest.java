package org.adventure;

import org.adventure.character.Character;
import org.adventure.character.NPC;
import org.adventure.character.NPC.BehaviorType;
import org.adventure.character.Race;
import org.adventure.world.Biome;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the NPC class.
 * Validates deterministic spawning, biome compatibility, density limits, and NPC behavior.
 */
class NPCTest {

    @Test
    void testNPCCreation() {
        NPC npc = new NPC("npc-001", "Guard", Race.HUMAN, BehaviorType.NEUTRAL, 
                         100, 200, "forest-region");

        assertEquals("npc-001", npc.getId(), "ID should match");
        assertEquals("Guard", npc.getName(), "Name should match");
        assertEquals(Race.HUMAN, npc.getRace(), "Race should match");
        assertEquals(BehaviorType.NEUTRAL, npc.getBehaviorType(), "Behavior should match");
        assertEquals(100, npc.getSpawnX(), "Spawn X should match");
        assertEquals(200, npc.getSpawnY(), "Spawn Y should match");
        assertEquals("forest-region", npc.getBiomeId(), "Biome ID should match");
    }

    @Test
    void testNPCHealthInitialization() {
        NPC npc = new NPC("npc-001", "Warrior", Race.DWARF, BehaviorType.GUARD, 
                         0, 0, "mountain");

        assertTrue(npc.getMaxHealth() > 0, "Max health should be positive");
        assertEquals(npc.getMaxHealth(), npc.getHealth(), "Health should start at max");
    }

    @Test
    void testNPCPosition() {
        NPC npc = new NPC("npc-001", "Scout", Race.ELF, BehaviorType.PEACEFUL, 
                         50, 75, "forest");

        assertEquals(50, npc.getCurrentX(), "Current X should match spawn X");
        assertEquals(75, npc.getCurrentY(), "Current Y should match spawn Y");

        npc.setCurrentX(60);
        npc.setCurrentY(80);

        assertEquals(60, npc.getCurrentX(), "Current X should update");
        assertEquals(80, npc.getCurrentY(), "Current Y should update");
    }

    @Test
    void testDeterministicSpawning() {
        int regionId = 100;
        long worldSeed = 12345L;
        Biome biome = Biome.FOREST;

        List<NPC> npcs1 = NPC.spawnNPCsForRegion(regionId, worldSeed, 0, 0, 100, 100, biome);
        List<NPC> npcs2 = NPC.spawnNPCsForRegion(regionId, worldSeed, 0, 0, 100, 100, biome);

        assertEquals(npcs1.size(), npcs2.size(), "Same seed should spawn same number of NPCs");

        for (int i = 0; i < npcs1.size(); i++) {
            NPC npc1 = npcs1.get(i);
            NPC npc2 = npcs2.get(i);

            assertEquals(npc1.getName(), npc2.getName(), "NPC names should match");
            assertEquals(npc1.getRace(), npc2.getRace(), "NPC races should match");
            assertEquals(npc1.getBehaviorType(), npc2.getBehaviorType(), "NPC behaviors should match");
            assertEquals(npc1.getSpawnX(), npc2.getSpawnX(), "Spawn X should match");
            assertEquals(npc1.getSpawnY(), npc2.getSpawnY(), "Spawn Y should match");
        }
    }

    @Test
    void testSpawningVariability() {
        long worldSeed1 = 12345L;
        long worldSeed2 = 67890L;
        int regionId = 100;
        Biome biome = Biome.FOREST;

        List<NPC> npcs1 = NPC.spawnNPCsForRegion(regionId, worldSeed1, 0, 0, 100, 100, biome);
        List<NPC> npcs2 = NPC.spawnNPCsForRegion(regionId, worldSeed2, 0, 0, 100, 100, biome);

        // Different seeds → likely different results
        boolean different = npcs1.size() != npcs2.size();
        if (npcs1.size() == npcs2.size() && npcs1.size() > 0) {
            for (int i = 0; i < npcs1.size(); i++) {
                if (!npcs1.get(i).getRace().equals(npcs2.get(i).getRace())) {
                    different = true;
                    break;
                }
            }
        }

        assertTrue(different, "Different seeds should produce different NPC spawns");
    }

    @Test
    void testForestBiomeSpawning() {
        long worldSeed = 11111L;
        int regionId = 10;
        Biome biome = Biome.FOREST;

        List<NPC> npcs = NPC.spawnNPCsForRegion(regionId, worldSeed, 0, 0, 100, 100, biome);

        assertFalse(npcs.isEmpty(), "Forest should spawn NPCs");
        
        // Verify all NPCs have valid properties
        for (NPC npc : npcs) {
            assertNotNull(npc.getId(), "NPC should have ID");
            assertNotNull(npc.getName(), "NPC should have name");
            assertNotNull(npc.getRace(), "NPC should have race");
            assertNotNull(npc.getBehaviorType(), "NPC should have behavior");
            assertEquals(biome.name(), npc.getBiomeId(), "NPC should be from correct biome");
        }
    }

    @Test
    void testMountainBiomeSpawning() {
        long worldSeed = 22222L;
        int regionId = 20;
        Biome biome = Biome.MOUNTAIN;

        List<NPC> npcs = NPC.spawnNPCsForRegion(regionId, worldSeed, 0, 0, 100, 100, biome);

        // Mountain is harsh, may have fewer NPCs
        for (NPC npc : npcs) {
            assertNotNull(npc.getId(), "NPC should have ID");
            assertEquals(biome.name(), npc.getBiomeId(), "NPC should be from mountain biome");
        }
    }

    @Test
    void testDesertBiomeSpawning() {
        long worldSeed = 33333L;
        int regionId = 30;
        Biome biome = Biome.DESERT;

        List<NPC> npcs = NPC.spawnNPCsForRegion(regionId, worldSeed, 0, 0, 100, 100, biome);

        // Desert is harsh, should have low density
        for (NPC npc : npcs) {
            assertNotNull(npc.getId(), "NPC should have ID");
            assertEquals(biome.name(), npc.getBiomeId(), "NPC should be from desert biome");
        }
    }

    @Test
    void testGrasslandBiomeSpawning() {
        long worldSeed = 44444L;
        int regionId = 40;
        Biome biome = Biome.GRASSLAND;

        List<NPC> npcs = NPC.spawnNPCsForRegion(regionId, worldSeed, 0, 0, 100, 100, biome);

        // Grassland is habitable, should have moderate-high NPC density
        assertFalse(npcs.isEmpty(), "Grassland should spawn NPCs");
        
        for (NPC npc : npcs) {
            assertEquals(biome.name(), npc.getBiomeId(), "NPC should be from grassland biome");
        }
    }

    @Test
    void testSwampBiomeSpawning() {
        long worldSeed = 55555L;
        int regionId = 50;
        Biome biome = Biome.SWAMP;

        List<NPC> npcs = NPC.spawnNPCsForRegion(regionId, worldSeed, 0, 0, 100, 100, biome);

        for (NPC npc : npcs) {
            assertEquals(biome.name(), npc.getBiomeId(), "NPC should be from swamp biome");
        }
    }

    @Test
    void testTundraBiomeSpawning() {
        long worldSeed = 66666L;
        int regionId = 60;
        Biome biome = Biome.TUNDRA;

        List<NPC> npcs = NPC.spawnNPCsForRegion(regionId, worldSeed, 0, 0, 100, 100, biome);

        // Tundra is very harsh, should have very low NPC density
        for (NPC npc : npcs) {
            assertEquals(biome.name(), npc.getBiomeId(), "NPC should be from tundra biome");
        }
    }

    @Test
    void testNPCIdFormat() {
        int regionId = 100;
        long worldSeed = 77777L;
        Biome biome = Biome.FOREST;

        List<NPC> npcs = NPC.spawnNPCsForRegion(regionId, worldSeed, 0, 0, 100, 100, biome);

        for (int i = 0; i < npcs.size(); i++) {
            String expectedId = "npc_r" + regionId + "_" + i;
            assertEquals(expectedId, npcs.get(i).getId(), "NPC ID should follow format npc_r{regionId}_{index}");
        }
    }

    @Test
    void testNPCSpawnWithinRegionBounds() {
        int regionId = 80;
        long worldSeed = 88888L;
        Biome biome = Biome.GRASSLAND;
        int regionCenterX = 500;
        int regionCenterY = 300;
        int regionWidth = 200;
        int regionHeight = 150;

        List<NPC> npcs = NPC.spawnNPCsForRegion(regionId, worldSeed, regionCenterX, regionCenterY, 
                                                regionWidth, regionHeight, biome);

        for (NPC npc : npcs) {
            int minX = regionCenterX - regionWidth / 2;
            int maxX = regionCenterX + regionWidth / 2;
            int minY = regionCenterY - regionHeight / 2;
            int maxY = regionCenterY + regionHeight / 2;

            assertTrue(npc.getSpawnX() >= minX && npc.getSpawnX() < maxX, 
                      "NPC spawn X should be within region bounds");
            assertTrue(npc.getSpawnY() >= minY && npc.getSpawnY() < maxY, 
                      "NPC spawn Y should be within region bounds");
        }
    }

    @Test
    void testNPCStatsInheritance() {
        // NPCs should inherit race base stats
        NPC dwarf = new NPC("npc-001", "Dwarf Smith", Race.DWARF, BehaviorType.NEUTRAL, 0, 0, "mountain");

        assertEquals(Race.DWARF, dwarf.getRace());
        // Dwarves have high CON and STR
        assertTrue(dwarf.getStat(Character.CoreStat.CONSTITUTION) >= 12, "Dwarf should have high CON");
        assertTrue(dwarf.getStat(Character.CoreStat.STRENGTH) >= 12, "Dwarf should have high STR");
    }

    @Test
    void testNPCSeededRandomness() {
        // Same seed + different region → different results
        long worldSeed = 99999L;
        Biome biome = Biome.FOREST;

        List<NPC> region1 = NPC.spawnNPCsForRegion(1, worldSeed, 0, 0, 100, 100, biome);
        List<NPC> region2 = NPC.spawnNPCsForRegion(2, worldSeed, 0, 0, 100, 100, biome);

        // Very unlikely to be identical
        boolean different = region1.size() != region2.size();
        if (!different && region1.size() > 0) {
            different = !region1.get(0).getRace().equals(region2.get(0).getRace()) ||
                       region1.get(0).getSpawnX() != region2.get(0).getSpawnX();
        }

        assertTrue(different, "Different regions should produce different NPC spawns");
    }

    @Test
    void testBehaviorTypeEnum() {
        // Verify all behavior types have display names and descriptions
        for (BehaviorType type : BehaviorType.values()) {
            assertNotNull(type.getDisplayName(), "Behavior type should have display name");
            assertNotNull(type.getDescription(), "Behavior type should have description");
            assertFalse(type.getDisplayName().isEmpty(), "Display name should not be empty");
            assertFalse(type.getDescription().isEmpty(), "Description should not be empty");
        }
    }

    @Test
    void testNPCDamage() {
        NPC npc = new NPC("npc-001", "Warrior", Race.HUMAN, BehaviorType.AGGRESSIVE, 0, 0, "plains");
        
        int initialHealth = npc.getHealth();
        int damage = 10;
        
        npc.takeDamage(damage);
        
        assertEquals(initialHealth - damage, npc.getHealth(), "Health should decrease by damage amount");
    }

    @Test
    void testNPCLethalDamage() {
        NPC npc = new NPC("npc-001", "Goblin", Race.GOBLIN, BehaviorType.AGGRESSIVE, 0, 0, "swamp");
        
        int overkillDamage = npc.getMaxHealth() + 100;
        npc.takeDamage(overkillDamage);
        
        assertTrue(npc.getHealth() <= 0, "Health should be 0 or negative after lethal damage");
    }
}

