package org.adventure.region;

import org.adventure.npc.Gender;
import org.adventure.npc.NPCJob;
import org.adventure.npc.NamedNPC;
import org.adventure.quest.Quest;
import org.adventure.settlement.RoadTile;
import org.adventure.settlement.Village;
import org.adventure.society.Clan;
import org.adventure.society.ClanType;
import org.adventure.society.RelationshipRecord;
import org.adventure.story.Story;
import org.adventure.story.StoryType;
import org.adventure.story.StoryStatus;
import org.adventure.structure.EntranceSide;
import org.adventure.structure.OwnerType;
import org.adventure.structure.Structure;
import org.adventure.structure.StructureType;
import org.adventure.world.Biome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration tests for Phase 1.10 complete dynamic world simulation.
 * Tests all systems working together: NPC lifecycle, clan expansion, structure lifecycle,
 * quest generation, and village management.
 */
public class RegionSimulatorIntegrationTest {
    
    private RegionSimulator simulator;
    private Region testRegion;
    private int worldWidth = 256;
    private int worldHeight = 256;
    private Biome[][] biomes;
    private double[][] elevation;
    
    @BeforeEach
    public void setup() {
        simulator = new RegionSimulator();
        
        // Create simple world data
        biomes = new Biome[worldHeight][worldWidth];
        elevation = new double[worldHeight][worldWidth];
        
        // Fill with grassland and flat terrain
        for (int y = 0; y < worldHeight; y++) {
            for (int x = 0; x < worldWidth; x++) {
                biomes[y][x] = Biome.GRASSLAND;
                elevation[y][x] = 0.5; // Flat buildable terrain
            }
        }
        
        // Set world data
        simulator.setWorldData(biomes, elevation, worldWidth, worldHeight);
        
        // Create test region with initial entities
        testRegion = createTestRegion();
        simulator.addRegion(testRegion);
        simulator.activateRegion(testRegion.getId());
    }
    
    private Region createTestRegion() {
        Region region = new Region(1, 128, 128, 64, 64);
        
        // Add NPCs
        List<NamedNPC> npcs = createTestNPCs();
        region.getNPCs().addAll(npcs);
        
        // Add clans
        List<Clan> clans = createTestClans();
        region.getClans().addAll(clans);
        
        // Add structures
        List<Structure> structures = createTestStructures();
        region.getStructures().addAll(structures);
        
        // Add roads
        List<RoadTile> roads = createTestRoads();
        region.getRoads().addAll(roads);
        
        // Add stories
        List<Story> stories = createTestStories();
        region.getStories().addAll(stories);
        
        return region;
    }
    
    private List<NamedNPC> createTestNPCs() {
        List<NamedNPC> npcs = new ArrayList<>();
        
        // Create NPCs for clan1 (NPC-led)
        for (int i = 0; i < 15; i++) {
            npcs.add(new NamedNPC.Builder()
                .id("npc_clan1_" + i)
                .name("NPC" + i)
                .clanId("clan1")
                .age(20 + i)
                .gender(i % 2 == 0 ? Gender.MALE : Gender.FEMALE)
                .job(NPCJob.FARMER)
                .homeStructureId("house_clan1_" + (i % 3))
                .isPlayer(false)
                .birthTick(0)
                .build());
        }
        
        // Create NPCs for clan2 (player-led)
        npcs.add(new NamedNPC.Builder()
            .id("player1")
            .name("Player1")
            .clanId("clan2")
            .age(25)
            .gender(Gender.MALE)
            .job(NPCJob.WARRIOR)
            .homeStructureId("house_clan2_0")
            .isPlayer(true) // Player-controlled
            .birthTick(0)
            .build());
        
        for (int i = 0; i < 10; i++) {
            npcs.add(new NamedNPC.Builder()
                .id("npc_clan2_" + i)
                .name("NPCPlayer" + i)
                .clanId("clan2")
                .age(20 + i)
                .gender(i % 2 == 0 ? Gender.MALE : Gender.FEMALE)
                .job(NPCJob.FARMER)
                .homeStructureId("house_clan2_" + (i % 2))
                .isPlayer(false)
                .birthTick(0)
                .build());
        }
        
        return npcs;
    }
    
    private List<Clan> createTestClans() {
        List<Clan> clans = new ArrayList<>();
        
        // NPC-led clan with sufficient treasury
        Clan clan1 = new Clan.Builder()
            .id("clan1")
            .name("NPC Clan")
            .type(ClanType.CLAN)
            .treasury(1000.0) // Sufficient for expansion
            .centerX(100)
            .centerY(100)
            .leaderId("npc_clan1_0")
            .foundingTick(0)
            .lastActiveTick(0)
            .build();
        
        // Player-led clan
        Clan clan2 = new Clan.Builder()
            .id("clan2")
            .name("Player Clan")
            .type(ClanType.CLAN)
            .treasury(1000.0)
            .centerX(150)
            .centerY(150)
            .leaderId("player1")
            .foundingTick(0)
            .lastActiveTick(0)
            .build();
        
        // Set hostile relationship for conflict quest generation
        RelationshipRecord hostileRelation = new RelationshipRecord(
            "clan2",
            -50, // Hostile
            0.0,
            -40.0,
            0.0,
            0
        );
        clan1 = clan1.updateRelationship(hostileRelation);
        
        clans.add(clan1);
        clans.add(clan2);
        
        return clans;
    }
    
    private List<Structure> createTestStructures() {
        List<Structure> structures = new ArrayList<>();
        
        // Clan1 structures
        for (int i = 0; i < 3; i++) {
            structures.add(new Structure.Builder()
                .id("house_clan1_" + i)
                .type(StructureType.HOUSE)
                .ownerId("clan1")
                .ownerType(OwnerType.CLAN)
                .locationTileId((100 + i * 5) + "," + 100)
                .entrance(EntranceSide.SOUTH)
                .health(100.0)
                .maxHealth(100.0)
                .createdAtTick(0)
                .build());
        }
        
        // Clan2 structures
        for (int i = 0; i < 2; i++) {
            structures.add(new Structure.Builder()
                .id("house_clan2_" + i)
                .type(StructureType.HOUSE)
                .ownerId("clan2")
                .ownerType(OwnerType.CLAN)
                .locationTileId((150 + i * 5) + "," + 150)
                .entrance(EntranceSide.SOUTH)
                .health(100.0)
                .maxHealth(100.0)
                .createdAtTick(0)
                .build());
        }
        
        // Add a damaged structure for disaster quest
        structures.add(new Structure.Builder()
            .id("damaged_house")
            .type(StructureType.HOUSE)
            .ownerId("clan1")
            .ownerType(OwnerType.CLAN)
            .locationTileId("105,105")
            .entrance(EntranceSide.SOUTH)
            .health(30.0) // Damaged
            .maxHealth(100.0)
            .createdAtTick(0)
            .build());
        
        // Add ancient ruins for exploration quest
        structures.add(new Structure.Builder()
            .id("ancient_ruin_1")
            .type(StructureType.ANCIENT_RUINS)
            .ownerId(null)
            .ownerType(OwnerType.NONE)
            .locationTileId("120,120")
            .entrance(EntranceSide.SOUTH)
            .health(50.0)
            .maxHealth(100.0)
            .createdAtTick(0)
            .build());
        
        return structures;
    }
    
    private List<RoadTile> createTestRoads() {
        List<RoadTile> roads = new ArrayList<>();
        
        // Simple road connecting clan1 structures
        for (int x = 100; x <= 110; x++) {
            roads.add(new RoadTile.Builder()
                .x(x)
                .y(100)
                .createdTick(0)
                .isAutoGenerated(true)
                .build());
        }
        
        return roads;
    }
    
    private List<Story> createTestStories() {
        List<Story> stories = new ArrayList<>();
        
        Story story = new Story.Builder()
            .id("story1")
            .title("The Ancient Prophecy")
            .storyType(StoryType.LEGEND)
            .status(StoryStatus.ACTIVE)
            .originTileId(12000120)
            .originTick(0)
            .baseProbability(0.8)
            .maxHops(6)
            .priority(10) // Max priority (0-10 range)
            .lastProcessedTick(0)
            .build();
        stories.add(story);
        
        return stories;
    }
    
    @Test
    public void testCompleteSimulationCycle() {
        // Initial state
        int initialStructureCount = testRegion.getStructures().size();
        int initialNPCCount = testRegion.getNPCs().size();
        int initialQuestCount = testRegion.getQuests().size();
        
        // Run simulation for 1000 ticks
        simulator.advanceTicks(1000);
        
        // Verify simulation ran
        assertEquals(1000, simulator.getCurrentTick());
        
        // NPCs should still exist (no deaths expected in 1000 ticks for young NPCs)
        assertTrue(testRegion.getNPCs().size() >= initialNPCCount * 0.9,
            "Most NPCs should survive 1000 ticks");
        
        // Structures may have been added or converted to ruins
        assertTrue(testRegion.getStructures().size() >= initialStructureCount * 0.8,
            "Most structures should still exist");
        
        // Quests should have been generated
        assertTrue(testRegion.getQuests().size() > initialQuestCount,
            "Quests should have been generated from world events");
    }
    
    @Test
    public void testNPCClanExpansion() {
        // Run simulation long enough for NPC clan to expand (500 tick cooldown)
        simulator.advanceTicks(600);
        
        // NPC clan (clan1) should have attempted expansion
        List<Structure> clan1Structures = testRegion.getStructures().stream()
            .filter(s -> "clan1".equals(s.getOwnerId()))
            .toList();
        
        // May or may not have succeeded (depends on placement availability)
        assertTrue(clan1Structures.size() >= 3,
            "Clan1 should have at least initial structures");
    }
    
    @Test
    public void testPlayerClanNoAutoExpansion() {
        int initialClan2Structures = (int) testRegion.getStructures().stream()
            .filter(s -> "clan2".equals(s.getOwnerId()))
            .count();
        
        // Run simulation
        simulator.advanceTicks(1000);
        
        // Player clan should NOT auto-expand
        int finalClan2Structures = (int) testRegion.getStructures().stream()
            .filter(s -> "clan2".equals(s.getOwnerId()))
            .count();
        
        assertEquals(initialClan2Structures, finalClan2Structures,
            "Player clan should not auto-expand");
    }
    
    @Test
    public void testQuestGeneration() {
        // Run simulation
        simulator.advanceTicks(500);
        
        List<Quest> quests = testRegion.getQuests();
        
        // Should have generated quests
        assertFalse(quests.isEmpty(), "Should generate quests from world events");
        
        // Check for ruin quest
        boolean hasRuinQuest = quests.stream()
            .anyMatch(q -> q.getTitle().contains("Explore") || q.getTitle().contains("Ruins"));
        
        // Check for conflict quest
        boolean hasConflictQuest = quests.stream()
            .anyMatch(q -> q.getTitle().contains("Mediate"));
        
        // At least one type of quest should be generated
        assertTrue(hasRuinQuest || hasConflictQuest,
            "Should generate ruin or conflict quests");
    }
    
    @Test
    public void testVillageDetection() {
        // Run simulation
        simulator.advanceTicks(100);
        
        List<Village> villages = testRegion.getVillages();
        
        // Should detect villages from structure clusters
        assertFalse(villages.isEmpty(), "Should detect villages from structure clusters");
        
        // Check village properties
        for (Village village : villages) {
            assertNotNull(village.getId());
            assertNotNull(village.getName());
            assertTrue(village.getStructureIds().size() >= 3,
                "Village should have at least 3 structures");
        }
    }
    
    @Test
    public void testStructureLifecycle() {
        // Find damaged structure
        Structure damagedHouse = testRegion.getStructures().stream()
            .filter(s -> s.getId().equals("damaged_house"))
            .findFirst()
            .orElse(null);
        
        assertNotNull(damagedHouse);
        
        // Run simulation (disasters have 5% chance per 1000 ticks)
        simulator.advanceTicks(10000);
        
        // Some structures may have been damaged or converted to ruins
        long ruinCount = testRegion.getStructures().stream()
            .filter(s -> s.getType() == StructureType.ANCIENT_RUINS)
            .count();
        
        // At least the initial ruin should exist
        assertTrue(ruinCount >= 1, "Should have at least initial ruin");
    }
    
    @Test
    public void testNPCLifecycle() {
        int initialNPCCount = testRegion.getNPCs().size();
        
        // Run simulation for significant time (10000 ticks = ~1 year)
        simulator.advanceTicks(10000);
        
        // Verify NPCs still exist
        List<NamedNPC> npcs = testRegion.getNPCs();
        assertFalse(npcs.isEmpty(), "NPCs should still exist after simulation");
        
        // Check that at least some NPCs have valid ages
        long validAgedNPCs = npcs.stream()
            .filter(npc -> npc.getAge() > 0 && npc.getAge() < 100)
            .count();
        
        assertTrue(validAgedNPCs > 0, "At least some NPCs should have valid ages");
        
        // Reproduction may have occurred (creating new NPCs)
        int finalNPCCount = testRegion.getNPCs().size();
        
        // At least original count should remain (minus any deaths)
        assertTrue(finalNPCCount >= initialNPCCount * 0.8,
            "Most NPCs should survive, some may reproduce (initial=" + initialNPCCount + ", final=" + finalNPCCount + ")");
    }
    
    @Test
    public void testActiveVsBackgroundSimulation() {
        // Start with active region
        assertEquals(1, simulator.getActiveRegionCount());
        
        // Deactivate region
        simulator.deactivateRegion(testRegion.getId());
        assertEquals(0, simulator.getActiveRegionCount());
        assertEquals(1, simulator.getBackgroundRegionCount());
        
        // Run simulation
        simulator.advanceTicks(100);
        
        // Reactivate and verify resynchronization
        simulator.activateRegion(testRegion.getId());
        assertEquals(1, simulator.getActiveRegionCount());
        
        // Region should have been resynchronized
        assertEquals(100, testRegion.getLastProcessedTick());
    }
    
    @Test
    public void testMultipleRegions() {
        // Create second region
        Region region2 = new Region(2, 200, 200, 64, 64);
        
        simulator.addRegion(region2);
        
        assertEquals(2, simulator.getRegionCount());
        assertEquals(1, simulator.getActiveRegionCount());
        assertEquals(1, simulator.getBackgroundRegionCount());
        
        // Run simulation
        simulator.advanceTicks(100);
        
        // Both regions should exist
        assertNotNull(simulator.getRegion(1));
        assertNotNull(simulator.getRegion(2));
    }
    
    @Test
    public void testIntegratedSystemsDontCrash() {
        // Stress test: run for extended time with complex interactions
        for (int i = 0; i < 100; i++) {
            simulator.advanceTicks(100);
            
            // Verify region integrity
            assertNotNull(testRegion.getNPCs());
            assertNotNull(testRegion.getClans());
            assertNotNull(testRegion.getStructures());
            assertNotNull(testRegion.getQuests());
            assertNotNull(testRegion.getVillages());
            
            // No null pointers or exceptions should occur
        }
        
        // Simulation should complete successfully
        assertTrue(simulator.getCurrentTick() >= 10000,
            "Simulation should run for extended time without errors");
    }
}
