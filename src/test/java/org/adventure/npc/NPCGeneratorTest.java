package org.adventure.npc;

import org.adventure.society.Clan;
import org.adventure.structure.Structure;
import org.adventure.structure.StructureType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for NPCGenerator.
 */
public class NPCGeneratorTest {
    
    @Test
    public void testGenerateSingleNPC() {
        Random rng = new Random(42L);
        
        NamedNPC npc = NPCGenerator.generateNPC(
            "clan-1",
            Gender.MALE,
            25,
            NPCJob.FARMER,
            "house-1",
            0L,
            rng
        );
        
        assertNotNull(npc);
        assertEquals("clan-1", npc.getClanId());
        assertEquals(Gender.MALE, npc.getGender());
        assertEquals(25, npc.getAge());
        assertEquals(NPCJob.FARMER, npc.getJob());
        assertEquals("house-1", npc.getHomeStructureId());
        assertFalse(npc.isPlayer());
        assertTrue(npc.getFertility() > 0); // 25-year-old should be fertile
    }
    
    @Test
    public void testGenerateClanPopulation() {
        Random rng = new Random(123L);
        
        // Create test clan with 20 members
        List<String> members = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            members.add("member-" + i);
        }
        
        Clan clan = new Clan.Builder()
            .id("clan-1")
            .name("TestClan")
            .members(members)
            .build();
        
        // Create test structures (5 houses)
        List<Structure> structures = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            structures.add(createHouse("house-" + i, "clan-1"));
        }
        
        // Generate population
        List<NamedNPC> npcs = NPCGenerator.generateInitialClanPopulation(
            clan,
            structures,
            0L,
            rng
        );
        
        // Verify population count
        assertEquals(20, npcs.size());
        
        // Verify age distribution (approximately)
        long children = npcs.stream().filter(NamedNPC::isChild).count();
        long adults = npcs.stream().filter(n -> !n.isChild() && n.getAge() < 60).count();
        long elders = npcs.stream().filter(n -> n.getAge() >= 60).count();
        
        assertTrue(children >= 2 && children <= 6, "Expected ~20% children (4), got " + children);
        assertTrue(adults >= 8 && adults <= 12, "Expected ~50% adults (10), got " + adults);
        assertTrue(elders >= 4 && elders <= 8, "Expected ~30% elders (6), got " + elders);
        
        // Verify some adults are married
        long married = npcs.stream().filter(NamedNPC::isMarried).count();
        assertTrue(married > 0, "At least some NPCs should be married");
        
        // Verify all NPCs have homes
        assertTrue(npcs.stream().allMatch(n -> n.getHomeStructureId() != null));
        
        // Verify all NPCs belong to same clan
        assertTrue(npcs.stream().allMatch(n -> n.getClanId().equals("clan-1")));
    }
    
    @Test
    public void testDeterministicGeneration() {
        long seed = 42L;
        
        List<String> members = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            members.add("member-" + i);
        }
        
        Clan clan = new Clan.Builder()
            .id("clan-1")
            .name("TestClan")
            .members(members)
            .build();
        
        List<Structure> structures = List.of(
            createHouse("house-1", "clan-1"),
            createHouse("house-2", "clan-1")
        );
        
        // Generate twice with same seed
        List<NamedNPC> npcs1 = NPCGenerator.generateInitialClanPopulation(
            clan,
            structures,
            0L,
            new Random(seed)
        );
        
        List<NamedNPC> npcs2 = NPCGenerator.generateInitialClanPopulation(
            clan,
            structures,
            0L,
            new Random(seed)
        );
        
        // Verify same count
        assertEquals(npcs1.size(), npcs2.size());
        
        // Verify same genders (order matters)
        for (int i = 0; i < npcs1.size(); i++) {
            assertEquals(npcs1.get(i).getGender(), npcs2.get(i).getGender());
            assertEquals(npcs1.get(i).getAge(), npcs2.get(i).getAge());
            assertEquals(npcs1.get(i).getJob(), npcs2.get(i).getJob());
        }
    }
    
    @Test
    public void testEmptyClan() {
        Clan emptyClan = new Clan.Builder()
            .id("clan-1")
            .name("EmptyClan")
            .build();
        
        List<Structure> structures = List.of(createHouse("house-1", "clan-1"));
        
        List<NamedNPC> npcs = NPCGenerator.generateInitialClanPopulation(
            emptyClan,
            structures,
            0L,
            new Random(42L)
        );
        
        assertTrue(npcs.isEmpty());
    }
    
    @Test
    public void testNoHouses() {
        List<String> members = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            members.add("member-" + i);
        }
        
        Clan clan = new Clan.Builder()
            .id("clan-1")
            .name("TestClan")
            .members(members)
            .build();
        
        // No houses, but other structures
        List<Structure> structures = List.of(
            createStructure("shop-1", StructureType.SHOP, "clan-1")
        );
        
        List<NamedNPC> npcs = NPCGenerator.generateInitialClanPopulation(
            clan,
            structures,
            0L,
            new Random(42L)
        );
        
        // Should still generate NPCs (using fallback to any structure)
        assertEquals(10, npcs.size());
        assertTrue(npcs.stream().allMatch(n -> n.getHomeStructureId() != null));
    }
    
    private Structure createHouse(String id, String ownerId) {
        return createStructure(id, StructureType.HOUSE, ownerId);
    }
    
    private Structure createStructure(String id, StructureType type, String ownerId) {
        return new Structure.Builder()
            .id(id)
            .type(type)
            .locationTileId("0,0")
            .ownerId(ownerId)
            .health(100)
            .build();
    }
}
