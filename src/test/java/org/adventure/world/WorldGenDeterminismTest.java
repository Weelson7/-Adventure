package org.adventure.world;

import org.adventure.npc.NamedNPC;
import org.adventure.prophecy.Prophecy;
import org.adventure.quest.Quest;
import org.adventure.settlement.Settlement;
import org.adventure.society.Clan;
import org.adventure.story.Story;
import org.adventure.structure.Structure;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Phase 1.10.1 determinism requirements.
 * Verifies that same seed produces identical worldgen outputs including NPCs.
 * 
 * Quality Gates (BUILD_PHASE1.10.x.md):
 * - Same seed produces same clans (IDs, positions, types)
 * - Same seed produces same settlements (structures, layouts)
 * - Same seed produces same NPCs (names, ages, jobs, marriages)
 * - Same seed produces same prophecies
 * - Same seed produces same quests
 */
public class WorldGenDeterminismTest {
    
    @Test
    public void testWorldgenDeterminism_Geography() {
        long seed = 42L;
        
        WorldGen gen1 = new WorldGen(128, 128);
        gen1.generate(seed);
        
        WorldGen gen2 = new WorldGen(128, 128);
        gen2.generate(seed);
        
        // Geography checksums must match
        assertEquals(gen1.checksum(), gen2.checksum(),
            "Same seed must produce identical geography");
    }
    
    @Test
    public void testWorldgenDeterminism_Clans() {
        long seed = 123L;
        
        WorldGen gen1 = new WorldGen(256, 256);
        gen1.generate(seed);
        
        WorldGen gen2 = new WorldGen(256, 256);
        gen2.generate(seed);
        
        List<Clan> clans1 = gen1.getClans();
        List<Clan> clans2 = gen2.getClans();
        
        // Same number of clans
        assertEquals(clans1.size(), clans2.size(),
            "Same seed must produce same number of clans");
        
        assertTrue(clans1.size() >= 3,
            "World must have at least 3 clans");
        
        // Same clan IDs, names, types, treasury
        for (int i = 0; i < clans1.size(); i++) {
            Clan c1 = clans1.get(i);
            Clan c2 = clans2.get(i);
            
            assertEquals(c1.getId(), c2.getId(),
                "Clan " + i + " must have same ID");
            assertEquals(c1.getName(), c2.getName(),
                "Clan " + i + " must have same name");
            assertEquals(c1.getType(), c2.getType(),
                "Clan " + i + " must have same type");
            assertEquals(c1.getTreasury(), c2.getTreasury(),
                "Clan " + i + " must have same starting treasury");
            assertEquals(c1.getMembers().size(), c2.getMembers().size(),
                "Clan " + i + " must have same number of members");
        }
    }
    
    @Test
    public void testWorldgenDeterminism_Settlements() {
        long seed = 456L;
        
        WorldGen gen1 = new WorldGen(256, 256);
        gen1.generate(seed);
        
        WorldGen gen2 = new WorldGen(256, 256);
        gen2.generate(seed);
        
        List<Settlement> settlements1 = gen1.getSettlements();
        List<Settlement> settlements2 = gen2.getSettlements();
        
        // Same number of settlements
        assertEquals(settlements1.size(), settlements2.size(),
            "Same seed must produce same number of settlements");
        
        // Same settlement properties
        for (int i = 0; i < settlements1.size(); i++) {
            Settlement s1 = settlements1.get(i);
            Settlement s2 = settlements2.get(i);
            
            assertEquals(s1.getId(), s2.getId(),
                "Settlement " + i + " must have same ID");
            assertEquals(s1.getName(), s2.getName(),
                "Settlement " + i + " must have same name");
            assertEquals(s1.getCenterX(), s2.getCenterX(),
                "Settlement " + i + " must have same center X");
            assertEquals(s1.getCenterY(), s2.getCenterY(),
                "Settlement " + i + " must have same center Y");
            assertEquals(s1.getStructureIds().size(), s2.getStructureIds().size(),
                "Settlement " + i + " must have same number of structures");
        }
    }
    
    @Test
    public void testWorldgenDeterminism_Structures() {
        long seed = 789L;
        
        WorldGen gen1 = new WorldGen(256, 256);
        gen1.generate(seed);
        
        WorldGen gen2 = new WorldGen(256, 256);
        gen2.generate(seed);
        
        List<Structure> structures1 = gen1.getStructures();
        List<Structure> structures2 = gen2.getStructures();
        
        // Same number of structures
        assertEquals(structures1.size(), structures2.size(),
            "Same seed must produce same number of structures");
        
        // Same structure properties
        for (int i = 0; i < structures1.size(); i++) {
            Structure st1 = structures1.get(i);
            Structure st2 = structures2.get(i);
            
            assertEquals(st1.getId(), st2.getId(),
                "Structure " + i + " must have same ID");
            assertEquals(st1.getType(), st2.getType(),
                "Structure " + i + " must have same type");
            assertEquals(st1.getOwnerId(), st2.getOwnerId(),
                "Structure " + i + " must have same owner");
            assertEquals(st1.getLocationTileId(), st2.getLocationTileId(),
                "Structure " + i + " must have same location");
        }
    }
    
    @Test
    public void testWorldgenDeterminism_NamedNPCs() {
        long seed = 999L;
        
        WorldGen gen1 = new WorldGen(256, 256);
        gen1.generate(seed);
        
        WorldGen gen2 = new WorldGen(256, 256);
        gen2.generate(seed);
        
        List<NamedNPC> npcs1 = gen1.getNPCs();
        List<NamedNPC> npcs2 = gen2.getNPCs();
        
        // Same number of NPCs
        assertEquals(npcs1.size(), npcs2.size(),
            "Same seed must produce same number of NPCs");
        
        assertTrue(npcs1.size() > 0,
            "World must have at least some NPCs");
        
        // Same NPC properties
        for (int i = 0; i < npcs1.size(); i++) {
            NamedNPC npc1 = npcs1.get(i);
            NamedNPC npc2 = npcs2.get(i);
            
            assertEquals(npc1.getId(), npc2.getId(),
                "NPC " + i + " must have same ID");
            assertEquals(npc1.getName(), npc2.getName(),
                "NPC " + i + " must have same name");
            assertEquals(npc1.getAge(), npc2.getAge(),
                "NPC " + i + " must have same age");
            assertEquals(npc1.getGender(), npc2.getGender(),
                "NPC " + i + " must have same gender");
            assertEquals(npc1.getJob(), npc2.getJob(),
                "NPC " + i + " must have same job");
            assertEquals(npc1.getClanId(), npc2.getClanId(),
                "NPC " + i + " must belong to same clan");
            assertEquals(npc1.getSpouseId(), npc2.getSpouseId(),
                "NPC " + i + " must have same marriage status");
            assertEquals(npc1.getChildrenIds().size(), npc2.getChildrenIds().size(),
                "NPC " + i + " must have same number of children");
        }
    }
    
    @Test
    public void testWorldgenDeterminism_Prophecies() {
        long seed = 111L;
        
        WorldGen gen1 = new WorldGen(256, 256);
        gen1.generate(seed);
        
        WorldGen gen2 = new WorldGen(256, 256);
        gen2.generate(seed);
        
        List<Prophecy> prophecies1 = gen1.getProphecies();
        List<Prophecy> prophecies2 = gen2.getProphecies();
        
        // Same number of prophecies
        assertEquals(prophecies1.size(), prophecies2.size(),
            "Same seed must produce same number of prophecies");
        
        // Same prophecy properties
        for (int i = 0; i < prophecies1.size(); i++) {
            Prophecy p1 = prophecies1.get(i);
            Prophecy p2 = prophecies2.get(i);
            
            assertEquals(p1.getId(), p2.getId(),
                "Prophecy " + i + " must have same ID");
            assertEquals(p1.getTitle(), p2.getTitle(),
                "Prophecy " + i + " must have same title");
            assertEquals(p1.getType(), p2.getType(),
                "Prophecy " + i + " must have same type");
        }
    }
    
    @Test
    public void testWorldgenDeterminism_Quests() {
        long seed = 222L;
        
        WorldGen gen1 = new WorldGen(256, 256);
        gen1.generate(seed);
        
        WorldGen gen2 = new WorldGen(256, 256);
        gen2.generate(seed);
        
        List<Quest> quests1 = gen1.getQuests();
        List<Quest> quests2 = gen2.getQuests();
        
        // Same number of quests
        assertEquals(quests1.size(), quests2.size(),
            "Same seed must produce same number of quests");
        
        // Same quest properties
        for (int i = 0; i < quests1.size(); i++) {
            Quest q1 = quests1.get(i);
            Quest q2 = quests2.get(i);
            
            assertEquals(q1.getId(), q2.getId(),
                "Quest " + i + " must have same ID");
            assertEquals(q1.getTitle(), q2.getTitle(),
                "Quest " + i + " must have same title");
            assertEquals(q1.getType(), q2.getType(),
                "Quest " + i + " must have same type");
        }
    }
    
    @Test
    public void testWorldgenDeterminism_Stories() {
        long seed = 333L;
        
        WorldGen gen1 = new WorldGen(256, 256);
        gen1.generate(seed);
        
        WorldGen gen2 = new WorldGen(256, 256);
        gen2.generate(seed);
        
        List<Story> stories1 = gen1.getStories();
        List<Story> stories2 = gen2.getStories();
        
        // Same number of stories
        assertEquals(stories1.size(), stories2.size(),
            "Same seed must produce same number of stories");
        
        // Same story properties
        for (int i = 0; i < stories1.size(); i++) {
            Story s1 = stories1.get(i);
            Story s2 = stories2.get(i);
            
            assertEquals(s1.getId(), s2.getId(),
                "Story " + i + " must have same ID");
            assertEquals(s1.getTitle(), s2.getTitle(),
                "Story " + i + " must have same title");
            assertEquals(s1.getType(), s2.getType(),
                "Story " + i + " must have same type");
        }
    }
    
    @Test
    public void testNPCGenerationDistribution() {
        long seed = 444L;
        
        WorldGen gen = new WorldGen(256, 256);
        gen.generate(seed);
        
        List<NamedNPC> npcs = gen.getNPCs();
        assertTrue(npcs.size() > 0, "World must have NPCs");
        
        // Count by age category
        int children = 0;
        int adults = 0;
        int elders = 0;
        
        for (NamedNPC npc : npcs) {
            if (npc.getAge() < 18) {
                children++;
            } else if (npc.getAge() < 60) {
                adults++;
            } else {
                elders++;
            }
        }
        
        // Verify distribution roughly matches 20% children, 50% adults, 30% elders
        double childRatio = (double) children / npcs.size();
        double adultRatio = (double) adults / npcs.size();
        double elderRatio = (double) elders / npcs.size();
        
        assertTrue(childRatio >= 0.10 && childRatio <= 0.30,
            "Children should be ~20% of population (was " + childRatio + ")");
        assertTrue(adultRatio >= 0.40 && adultRatio <= 0.60,
            "Adults should be ~50% of population (was " + adultRatio + ")");
        assertTrue(elderRatio >= 0.20 && elderRatio <= 0.40,
            "Elders should be ~30% of population (was " + elderRatio + ")");
    }
    
    @Test
    public void testNPCMarriageDistribution() {
        long seed = 555L;
        
        WorldGen gen = new WorldGen(256, 256);
        gen.generate(seed);
        
        List<NamedNPC> npcs = gen.getNPCs();
        
        // Count married adults (age 18-60 inclusive, matching marriage logic)
        int marriedAdults = 0;
        int totalAdults = 0;
        
        for (NamedNPC npc : npcs) {
            if (npc.getAge() >= 18 && npc.getAge() <= 60) {
                totalAdults++;
                if (npc.getSpouseId() != null) {
                    marriedAdults++;
                }
            }
        }
        
        assertTrue(totalAdults > 0, "Must have adult NPCs");
        
        // Verify ~50% of adults are married (allow 30-70% range for variance)
        double marriageRate = (double) marriedAdults / totalAdults;
        assertTrue(marriageRate >= 0.30 && marriageRate <= 0.70,
            "Marriage rate should be ~50% (was " + marriageRate + ")");
    }
    
    @Test
    public void testNPCHomeAssignment() {
        long seed = 666L;
        
        WorldGen gen = new WorldGen(256, 256);
        gen.generate(seed);
        
        List<NamedNPC> npcs = gen.getNPCs();
        
        // All NPCs must have homes
        for (NamedNPC npc : npcs) {
            assertNotNull(npc.getHomeStructureId(),
                "NPC " + npc.getName() + " must have a home");
            assertFalse(npc.getHomeStructureId().isEmpty(),
                "NPC " + npc.getName() + " must have a valid home ID");
        }
    }
    
    @Test
    public void testClanScalingWithWorldSize() {
        // Small world
        WorldGen genSmall = new WorldGen(128, 128);
        genSmall.generate(100L);
        
        // Large world
        WorldGen genLarge = new WorldGen(512, 512);
        genLarge.generate(100L);
        
        int smallClans = genSmall.getClans().size();
        int largeClans = genLarge.getClans().size();
        
        // Larger world should have more clans
        assertTrue(largeClans > smallClans,
            "Larger world should have more clans");
        
        // Verify minimum clan count
        assertTrue(smallClans >= 3,
            "Even small world should have at least 3 clans");
    }
    
    @Test
    public void testOneSettlementPerClan() {
        long seed = 777L;
        
        WorldGen gen = new WorldGen(256, 256);
        gen.generate(seed);
        
        List<Clan> clans = gen.getClans();
        List<Settlement> settlements = gen.getSettlements();
        
        // Should be 1 settlement per clan
        assertEquals(clans.size(), settlements.size(),
            "Should have exactly 1 settlement per clan");
        
        // Each clan should have exactly 1 settlement
        for (Clan clan : clans) {
            long count = settlements.stream()
                .filter(s -> s.getClanId().equals(clan.getId()))
                .count();
            assertEquals(1, count,
                "Clan " + clan.getName() + " should have exactly 1 settlement");
        }
    }
}
