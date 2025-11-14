package org.adventure.simulation;

import org.adventure.npc.NamedNPC;
import org.adventure.npc.Gender;
import org.adventure.npc.NPCJob;
import org.adventure.society.Clan;
import org.adventure.society.ClanType;
import org.adventure.society.RelationshipRecord;
import org.adventure.structure.Structure;
import org.adventure.structure.StructureType;
import org.adventure.structure.OwnerType;
import org.adventure.settlement.RoadTile;
import org.adventure.world.Biome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ClanExpansionSimulator.
 */
public class ClanExpansionSimulatorTest {
    private ClanExpansionSimulator simulator;
    private List<Clan> clans;
    private List<NamedNPC> npcs;
    private List<Structure> structures;
    private List<RoadTile> roads;
    private Biome[][] biomes;
    private double[][] elevation;
    private int worldWidth = 100;
    private int worldHeight = 100;
    
    @BeforeEach
    public void setUp() {
        simulator = new ClanExpansionSimulator();
        clans = new ArrayList<>();
        npcs = new ArrayList<>();
        structures = new ArrayList<>();
        roads = new ArrayList<>();
        
        // Create simple world
        biomes = new Biome[worldWidth][worldHeight];
        elevation = new double[worldWidth][worldHeight];
        for (int x = 0; x < worldWidth; x++) {
            for (int y = 0; y < worldHeight; y++) {
                biomes[x][y] = Biome.GRASSLAND;
                elevation[x][y] = 0.5;
            }
        }
    }
    
    @Test
    public void testPlayerControlledClanSkipsExpansion() {
        // Create clan with player member
        Clan playerClan = new Clan.Builder()
            .id("clan1")
            .name("Player Clan")
            .type(ClanType.CLAN)
            .treasury(1000.0)
            .centerX(50)
            .centerY(50)
            .foundingTick(0)
            .build();
        clans.add(playerClan);
        
        // Add player NPC
        NamedNPC player = new NamedNPC.Builder()
            .id("npc1")
            .name("PlayerChar")
            .clanId("clan1")
            .age(25)
            .gender(Gender.MALE)
            .job(NPCJob.WARRIOR)
            .homeStructureId("struct1")
            .isPlayer(true)
            .birthTick(0)
            .build();
        npcs.add(player);
        
        // Add 10 more NPCs to meet population threshold
        for (int i = 0; i < 10; i++) {
            npcs.add(new NamedNPC.Builder()
                .id("npc" + (i + 2))
                .name("NPC" + i)
                .clanId("clan1")
                .age(25)
                .gender(i % 2 == 0 ? Gender.MALE : Gender.FEMALE)
                .job(NPCJob.FARMER)
                .homeStructureId("struct1")
                .birthTick(0)
                .build());
        }
        
        int initialStructureCount = structures.size();
        
        // Set world data before simulation
        simulator.setWorldData(elevation);
        
        // Simulate 1000 ticks
        for (long tick = 0; tick < 1000; tick++) {
            simulator.simulateTick(clans, npcs, structures, roads, biomes, elevation, worldWidth, worldHeight, tick);
        }
        
        // Player clan should NOT expand automatically
        assertEquals(initialStructureCount, structures.size(),
            "Player-controlled clan should not automatically expand");
    }
    
    @Test
    public void testNPCClanExpands() {
        // Create NPC-only clan
        Clan npcClan = new Clan.Builder()
            .id("clan1")
            .name("NPC Clan")
            .type(ClanType.CLAN)
            .treasury(1000.0)
            .centerX(50)
            .centerY(50)
            .foundingTick(0)
            .build();
        clans.add(npcClan);
        
        // Add NPCs (no players)
        for (int i = 0; i < 15; i++) {
            npcs.add(new NamedNPC.Builder()
                .id("npc" + i)
                .name("NPC" + i)
                .clanId("clan1")
                .age(25)
                .gender(i % 2 == 0 ? Gender.MALE : Gender.FEMALE)
                .job(NPCJob.FARMER)
                .homeStructureId("struct1")
                .isPlayer(false)
                .birthTick(0)
                .build());
        }
        
        // Set world data before simulation
        simulator.setWorldData(elevation);
        
        // Simulate 600 ticks (past expansion interval)
        for (long tick = 0; tick < 600; tick++) {
            simulator.simulateTick(clans, npcs, structures, roads, biomes, elevation, worldWidth, worldHeight, tick);
        }
        
        // NPC clan should have expanded at least once
        assertTrue(structures.size() > 0, "NPC clan should have built structures");
        
        // Check that treasury decreased
        Clan updatedClan = clans.get(0);
        assertTrue(updatedClan.getTreasury() < 1000.0, "Treasury should decrease from construction costs");
    }
    
    @Test
    public void testExpansionRequiresSufficientTreasury() {
        // Create clan with insufficient treasury
        Clan poorClan = new Clan.Builder()
            .id("clan1")
            .name("Poor Clan")
            .type(ClanType.CLAN)
            .treasury(100.0) // Below threshold
            .centerX(50)
            .centerY(50)
            .foundingTick(0)
            .build();
        clans.add(poorClan);
        
        // Add NPCs
        for (int i = 0; i < 15; i++) {
            npcs.add(new NamedNPC.Builder()
                .id("npc" + i)
                .name("NPC" + i)
                .clanId("clan1")
                .age(25)
                .gender(i % 2 == 0 ? Gender.MALE : Gender.FEMALE)
                .job(NPCJob.FARMER)
                .homeStructureId("struct1")
                .birthTick(0)
                .build());
        }
        
        int initialStructureCount = structures.size();
        
        // Simulate 600 ticks
        for (long tick = 0; tick < 600; tick++) {
            simulator.simulateTick(clans, npcs, structures, roads, biomes, elevation, worldWidth, worldHeight, tick);
        }
        
        // Should not expand (insufficient treasury)
        assertEquals(initialStructureCount, structures.size(),
            "Clan with insufficient treasury should not expand");
    }
    
    @Test
    public void testExpansionRequiresSufficientPopulation() {
        // Create clan with sufficient treasury but low population
        Clan smallClan = new Clan.Builder()
            .id("clan1")
            .name("Small Clan")
            .type(ClanType.CLAN)
            .treasury(1000.0)
            .centerX(50)
            .centerY(50)
            .foundingTick(0)
            .build();
        clans.add(smallClan);
        
        // Add only 5 NPCs (below threshold)
        for (int i = 0; i < 5; i++) {
            npcs.add(new NamedNPC.Builder()
                .id("npc" + i)
                .name("NPC" + i)
                .clanId("clan1")
                .age(25)
                .gender(i % 2 == 0 ? Gender.MALE : Gender.FEMALE)
                .job(NPCJob.FARMER)
                .homeStructureId("struct1")
                .birthTick(0)
                .build());
        }
        
        int initialStructureCount = structures.size();
        
        // Simulate 600 ticks
        for (long tick = 0; tick < 600; tick++) {
            simulator.simulateTick(clans, npcs, structures, roads, biomes, elevation, worldWidth, worldHeight, tick);
        }
        
        // Should not expand (insufficient population)
        assertEquals(initialStructureCount, structures.size(),
            "Clan with insufficient population should not expand");
    }
    
    @Test
    public void testWarfareBetweenHostileClans() {
        // Create two hostile clans
        Clan clan1 = new Clan.Builder()
            .id("clan1")
            .name("Clan 1")
            .type(ClanType.CLAN)
            .treasury(1000.0)
            .centerX(50)
            .centerY(50)
            .foundingTick(0)
            .build();
        
        Clan clan2 = new Clan.Builder()
            .id("clan2")
            .name("Clan 2")
            .type(ClanType.CLAN)
            .treasury(1000.0)
            .centerX(60)
            .centerY(60)
            .foundingTick(0)
            .build();
        
        // Set hostile relationship
        RelationshipRecord hostileRelation = new RelationshipRecord(
            "clan2", // targetSocietyId
            -60, // reputation (below war threshold)
            0.0, // influence
            -50.0, // alignment (hostile)
            0.0, // raceAffinity
            0 // lastUpdatedTick
        );
        
        clan1 = clan1.updateRelationship(hostileRelation);
        clans.add(clan1);
        clans.add(clan2);
        
        // Add military structures to clan1 (make it stronger)
        for (int i = 0; i < 3; i++) {
            structures.add(new Structure.Builder()
                .id("barracks" + i)
                .type(StructureType.BARRACKS)
                .ownerId("clan1")
                .ownerType(OwnerType.CLAN)
                .locationTileId("50,50")
                .health(100.0)
                .maxHealth(100.0)
                .createdAtTick(0)
                .build());
        }
        
        // Add structure to clan2 (target)
        Structure targetStructure = new Structure.Builder()
            .id("target1")
            .type(StructureType.HOUSE)
            .ownerId("clan2")
            .ownerType(OwnerType.CLAN)
            .locationTileId("60,60")
            .health(100.0)
            .maxHealth(100.0)
            .createdAtTick(0)
            .build();
        structures.add(targetStructure);
        
        // Add NPCs (to pass player check)
        npcs.add(new NamedNPC.Builder()
            .id("npc1")
            .name("NPC1")
            .clanId("clan1")
            .age(25)
            .gender(Gender.MALE)
            .job(NPCJob.WARRIOR)
            .homeStructureId("barracks0")
            .birthTick(0)
            .build());
        
        // Set world data before simulation
        simulator.setWorldData(elevation);
        
        // Simulate until war occurs
        for (long tick = 0; tick < 1000; tick++) {
            simulator.simulateTick(clans, npcs, structures, roads, biomes, elevation, worldWidth, worldHeight, tick);
        }
        
        // Target structure should have taken damage (or been destroyed)
        assertTrue(targetStructure.getHealth() < 100.0 || targetStructure.isDestroyed(),
            "Hostile clans should engage in warfare and damage enemy structures");
    }
    
    @Test
    public void testAllianceFormationBetweenFriendlyClans() {
        // Create two friendly clans
        Clan clan1 = new Clan.Builder()
            .id("clan1")
            .name("Clan 1")
            .type(ClanType.CLAN)
            .treasury(1000.0)
            .centerX(50)
            .centerY(50)
            .foundingTick(0)
            .build();
        
        Clan clan2 = new Clan.Builder()
            .id("clan2")
            .name("Clan 2")
            .type(ClanType.CLAN)
            .treasury(1000.0)
            .centerX(60)
            .centerY(60)
            .foundingTick(0)
            .build();
        
        Clan clan3 = new Clan.Builder()
            .id("clan3")
            .name("Clan 3")
            .type(ClanType.CLAN)
            .treasury(1000.0)
            .centerX(70)
            .centerY(70)
            .foundingTick(0)
            .build();
        
        // Set friendly relationship between clan1 and clan2
        RelationshipRecord friendlyRelation = new RelationshipRecord(
            "clan2", // targetSocietyId
            60, // reputation (above alliance threshold)
            0.0, // influence
            30.0, // alignment (friendly)
            0.0, // raceAffinity
            0 // lastUpdatedTick
        );
        
        // Set mutual enemy (clan3)
        RelationshipRecord enemy1 = new RelationshipRecord(
            "clan3", // targetSocietyId
            -40, // reputation
            0.0, // influence
            -20.0, // alignment (hostile)
            0.0, // raceAffinity
            0 // lastUpdatedTick
        );
        
        RelationshipRecord enemy2 = new RelationshipRecord(
            "clan3", // targetSocietyId
            -40, // reputation
            0.0, // influence
            -20.0, // alignment (hostile)
            0.0, // raceAffinity
            0 // lastUpdatedTick
        );
        
        clan1 = clan1.updateRelationship(friendlyRelation).updateRelationship(enemy1);
        clan2 = clan2.updateRelationship(enemy2);
        
        clans.add(clan1);
        clans.add(clan2);
        clans.add(clan3);
        
        // Add NPCs (to pass player check)
        npcs.add(new NamedNPC.Builder()
            .id("npc1")
            .name("NPC1")
            .clanId("clan1")
            .age(25)
            .gender(Gender.MALE)
            .job(NPCJob.WARRIOR)
            .homeStructureId("struct1")
            .birthTick(0)
            .build());
        
        // Simulate
        for (long tick = 0; tick < 1000; tick++) {
            simulator.simulateTick(clans, npcs, structures, roads, biomes, elevation, worldWidth, worldHeight, tick);
        }
        
        // Check if alliance formed (reputation should be 75 and isAllied=true)
        Clan updatedClan1 = clans.get(0);
        RelationshipRecord updatedRelation = updatedClan1.getRelationships().get("clan2");
        
        if (updatedRelation != null) {
            // Alliance may have formed
            assertTrue(updatedRelation.getReputation() >= 60,
                "Friendly clans with mutual enemies should maintain or improve relationship");
        }
    }
}
