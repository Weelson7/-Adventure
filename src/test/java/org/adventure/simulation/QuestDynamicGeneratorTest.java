package org.adventure.simulation;

import org.adventure.quest.Quest;
import org.adventure.quest.QuestType;
import org.adventure.society.Clan;
import org.adventure.society.ClanType;
import org.adventure.society.RelationshipRecord;
import org.adventure.structure.Structure;
import org.adventure.structure.StructureType;
import org.adventure.structure.OwnerType;
import org.adventure.story.Story;
import org.adventure.story.StoryType;
import org.adventure.story.StoryStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for QuestDynamicGenerator.
 */
public class QuestDynamicGeneratorTest {
    private QuestDynamicGenerator generator;
    private List<Structure> structures;
    private List<Clan> clans;
    private List<Story> stories;
    
    @BeforeEach
    public void setUp() {
        generator = new QuestDynamicGenerator();
        structures = new ArrayList<>();
        clans = new ArrayList<>();
        stories = new ArrayList<>();
    }
    
    @Test
    public void testRuinQuestsGenerated() {
        // Create ruin structures
        for (int i = 0; i < 5; i++) {
            structures.add(new Structure.Builder()
                .id("ruin" + i)
                .type(StructureType.ANCIENT_RUINS)
                .ownerId("")
                .ownerType(OwnerType.NONE)
                .locationTileId(i + "," + i)
                .health(0.0)
                .maxHealth(100.0)
                .createdAtTick(0)
                .build());
        }
        
        // Generate quests
        List<Quest> quests = generator.generateQuestsFromEvents(structures, clans, stories, 1000);
        
        // Should generate some ruin quests (not necessarily all, due to randomness)
        assertTrue(quests.size() > 0, "Should generate at least one quest from ruins");
        
        // Check quest properties
        Quest ruinQuest = quests.stream()
            .filter(q -> q.getType() == QuestType.EXPLORE)
            .findFirst()
            .orElse(null);
        
        assertNotNull(ruinQuest, "Should have an exploration quest for ruins");
        assertTrue(ruinQuest.getTitle().contains("Explore"), "Ruin quest should mention exploration");
        assertFalse(ruinQuest.getObjectives().isEmpty(), "Quest should have objectives");
    }
    
    @Test
    public void testConflictQuestsGenerated() {
        // Create two hostile clans
        Clan clan1 = new Clan.Builder()
            .id("clan1")
            .name("Clan One")
            .type(ClanType.CLAN)
            .treasury(500.0)
            .centerX(50)
            .centerY(50)
            .leaderId("leader1")
            .foundingTick(0)
            .build();
        
        Clan clan2 = new Clan.Builder()
            .id("clan2")
            .name("Clan Two")
            .type(ClanType.CLAN)
            .treasury(500.0)
            .centerX(60)
            .centerY(60)
            .leaderId("leader2")
            .foundingTick(0)
            .build();
        
        // Set hostile relationship
        RelationshipRecord hostileRelation = new RelationshipRecord(
            "clan2", // targetSocietyId
            -40, // reputation
            0.0, // influence
            -30.0, // alignment (hostile)
            0.0, // raceAffinity
            0 // lastUpdatedTick
        );
        
        clan1 = clan1.updateRelationship(hostileRelation);
        clans.add(clan1);
        clans.add(clan2);
        
        // Generate quests multiple times with widely spaced ticks to avoid cooldown
        List<Quest> allQuests = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            // Use fresh generator each time to avoid cooldown tracking
            QuestDynamicGenerator freshGenerator = new QuestDynamicGenerator();
            allQuests.addAll(freshGenerator.generateQuestsFromEvents(structures, clans, stories, 1000 + i * 100));
        }
        
        // Should eventually generate a conflict quest
        Quest conflictQuest = allQuests.stream()
            .filter(q -> q.getTitle().contains("Mediate"))
            .findFirst()
            .orElse(null);
        
        assertNotNull(conflictQuest, "Should generate mediation quest for hostile clans");
        assertEquals(QuestType.INVESTIGATE, conflictQuest.getType());
        assertEquals(2, conflictQuest.getObjectives().size(),
            "Mediation quest should have 2 objectives (talk to both leaders)");
    }
    
    @Test
    public void testDisasterRecoveryQuestsGenerated() {
        // Create damaged structures
        for (int i = 0; i < 3; i++) {
            structures.add(new Structure.Builder()
                .id("damaged" + i)
                .type(StructureType.HOUSE)
                .ownerId("clan1")
                .ownerType(OwnerType.CLAN)
                .locationTileId(i + "," + i)
                .health(30.0) // Severely damaged (< 50%)
                .maxHealth(100.0)
                .createdAtTick(0)
                .build());
        }
        
        // Generate quests multiple times
        List<Quest> allQuests = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            allQuests.addAll(generator.generateQuestsFromEvents(structures, clans, stories, 1000 + i * 100));
        }
        
        // Should generate repair quest(s)
        Quest repairQuest = allQuests.stream()
            .filter(q -> q.getTitle().contains("Rebuild"))
            .findFirst()
            .orElse(null);
        
        assertNotNull(repairQuest, "Should generate repair quest for damaged structures");
        assertEquals(QuestType.DELIVER, repairQuest.getType());
        assertFalse(repairQuest.getRewards().isEmpty(), "Repair quest should have rewards");
    }
    
    @Test
    public void testStoryInvestigationQuestsGenerated() {
        // Create active stories
        for (int i = 0; i < 3; i++) {
            stories.add(new Story.Builder()
                .id("story" + i)
                .storyType(StoryType.MYSTERY)
                .status(StoryStatus.ACTIVE)
                .title("The Mystery of Place " + i)
                .description("Strange events have been occurring...")
                .originTileId(i * 10 + i * 10 * 10000) // Encode coords
                .originTick(0)
                .priority(5)
                .build());
        }
        
        // Generate quests multiple times
        List<Quest> allQuests = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            allQuests.addAll(generator.generateQuestsFromEvents(structures, clans, stories, 1000 + i * 100));
        }
        
        // Should generate story investigation quest(s)
        Quest storyQuest = allQuests.stream()
            .filter(q -> q.getTitle().contains("Investigate"))
            .findFirst()
            .orElse(null);
        
        assertNotNull(storyQuest, "Should generate investigation quest for stories");
        assertEquals(QuestType.INVESTIGATE, storyQuest.getType());
        assertNotNull(storyQuest.getLinkedStoryId(), "Story quest should link to a story");
    }
    
    @Test
    public void testQuestGenerationRespectsCooldown() {
        // Create ruin
        Structure ruin = new Structure.Builder()
            .id("ruin1")
            .type(StructureType.ANCIENT_RUINS)
            .ownerId("")
            .ownerType(OwnerType.NONE)
            .locationTileId("50,50")
            .health(0.0)
            .maxHealth(100.0)
            .createdAtTick(0)
            .build();
        structures.add(ruin);
        
        // Generate quest at tick 1000
        List<Quest> quests1 = generator.generateQuestsFromEvents(structures, clans, stories, 1000);
        int firstCount = quests1.size();
        
        // Try to generate again at tick 2000 (within cooldown)
        List<Quest> quests2 = generator.generateQuestsFromEvents(structures, clans, stories, 2000);
        
        // Should not generate duplicate quest for same ruin
        assertEquals(firstCount, quests1.size() + quests2.size(),
            "Should not generate duplicate quests within cooldown period");
    }
    
    @Test
    public void testQuestExpiration() {
        // Create ruin
        Structure ruin = new Structure.Builder()
            .id("ruin1")
            .type(StructureType.ANCIENT_RUINS)
            .ownerId("")
            .ownerType(OwnerType.NONE)
            .locationTileId("50,50")
            .health(0.0)
            .maxHealth(100.0)
            .createdAtTick(0)
            .build();
        structures.add(ruin);
        
        // Generate quest
        List<Quest> quests = generator.generateQuestsFromEvents(structures, clans, stories, 1000);
        
        if (!quests.isEmpty()) {
            Quest quest = quests.get(0);
            
            // Quest should have expiration tick
            assertTrue(quest.getExpirationTick() > 0, "Quest should have expiration tick");
            assertTrue(quest.getExpirationTick() > 1000, "Quest expiration should be in the future");
            
            // Check if quest is expired
            assertFalse(quest.isExpired(1000), "Quest should not be expired at creation time");
            assertTrue(quest.isExpired(quest.getExpirationTick() + 1),
                "Quest should be expired after expiration tick");
        }
    }
    
    @Test
    public void testMaxQuestsPerEventRespected() {
        // Create many ruins (more than max quests per event)
        for (int i = 0; i < 20; i++) {
            structures.add(new Structure.Builder()
                .id("ruin" + i)
                .type(StructureType.ANCIENT_RUINS)
                .ownerId("")
                .ownerType(OwnerType.NONE)
                .locationTileId(i + "," + i)
                .health(0.0)
                .maxHealth(100.0)
                .createdAtTick(0)
                .build());
        }
        
        // Generate quests
        List<Quest> quests = generator.generateQuestsFromEvents(structures, clans, stories, 1000);
        
        // Should not exceed max quests per event (3)
        assertTrue(quests.size() <= 3 * 4, // 3 per event type, 4 event types
            "Should not exceed maximum quests per event type");
    }
}
