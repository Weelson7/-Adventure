package org.adventure.simulation;

import org.adventure.quest.*;
import org.adventure.society.Clan;
import org.adventure.structure.Structure;
import org.adventure.structure.StructureType;
import org.adventure.story.Story;

import java.util.*;

/**
 * Generates dynamic quests from world events, ruins, conflicts, and disasters.
 * Creates quest chains where completing one unlocks the next.
 * 
 * <p>Quest Sources:
 * <ul>
 *   <li><b>Ruins:</b> Explore, loot, or rebuild destroyed structures</li>
 *   <li><b>Conflicts:</b> Mediate disputes, assist clans, or sabotage rivals</li>
 *   <li><b>Disasters:</b> Rescue survivors, rebuild damaged structures</li>
 *   <li><b>Stories:</b> Investigate story events, fulfill prophecies</li>
 * </ul>
 * 
 * @see docs/BUILD_PHASE1.10.x.md Phase 1.10.3 specification
 */
public class QuestDynamicGenerator {
    private static final int QUEST_EXPIRATION_TICKS = 50000; // ~5 years
    private static final int MAX_QUESTS_PER_EVENT = 3;
    
    private final Map<String, Long> generatedQuests; // Quest ID -> creation tick
    
    public QuestDynamicGenerator() {
        this.generatedQuests = new HashMap<>();
    }
    
    /**
     * Generates quests from world events (ruins, conflicts, disasters).
     * 
     * @param structures List of all structures (to find ruins/damaged)
     * @param clans List of all clans (to find conflicts)
     * @param stories List of active stories (for story-based quests)
     * @param currentTick Current game tick
     * @return List of newly generated quests
     */
    public List<Quest> generateQuestsFromEvents(
        List<Structure> structures,
        List<Clan> clans,
        List<Story> stories,
        long currentTick
    ) {
        List<Quest> newQuests = new ArrayList<>();
        
        // Generate ruin quests
        newQuests.addAll(generateRuinQuests(structures, currentTick));
        
        // Generate conflict quests
        newQuests.addAll(generateConflictQuests(clans, currentTick));
        
        // Generate disaster recovery quests
        newQuests.addAll(generateDisasterQuests(structures, currentTick));
        
        // Generate story investigation quests
        newQuests.addAll(generateStoryQuests(stories, currentTick));
        
        // Track generated quests
        for (Quest quest : newQuests) {
            generatedQuests.put(quest.getId(), currentTick);
        }
        
        // Clean up expired quest tracking
        cleanupExpiredTracking(currentTick);
        
        return newQuests;
    }
    
    /**
     * Generates quests related to ruins (explore, loot, rebuild).
     * 
     * @param structures List of all structures
     * @param currentTick Current game tick
     * @return List of ruin quests
     */
    private List<Quest> generateRuinQuests(List<Structure> structures, long currentTick) {
        List<Quest> quests = new ArrayList<>();
        Random rng = new Random(currentTick);
        
        List<Structure> ruins = structures.stream()
            .filter(s -> s.getType() == StructureType.ANCIENT_RUINS)
            .filter(s -> !hasGeneratedQuestFor(s.getId(), currentTick))
            .toList();
        
        for (Structure ruin : ruins) {
            if (quests.size() >= MAX_QUESTS_PER_EVENT) {
                break;
            }
            
            // 30% chance to generate quest for each ruin
            if (rng.nextDouble() < 0.30) {
                quests.add(createRuinQuest(ruin, currentTick, rng));
            }
        }
        
        return quests;
    }
    
    /**
     * Creates a quest for exploring/looting a ruin.
     * 
     * @param ruin The ruin structure
     * @param currentTick Current game tick
     * @param rng Random number generator
     * @return Exploration quest
     */
    private Quest createRuinQuest(Structure ruin, long currentTick, Random rng) {
        String questId = "quest_ruin_" + ruin.getId() + "_" + currentTick;
        String locationTile = ruin.getLocationTileId();
        String[] coords = locationTile.split(",");
        int x = Integer.parseInt(coords[0]);
        int y = Integer.parseInt(coords[1]);
        
        String ruinName = generateRuinName(rng);
        
        QuestObjective objective = new QuestObjective.Builder()
            .id(questId + "_obj1")
            .description("Reach the ruins at (" + x + ", " + y + ")")
            .targetType("location")
            .targetId(ruin.getId())
            .targetX(x)
            .targetY(y)
            .requiredCount(1)
            .build();
        
        // Random reward: gold or item
        QuestReward reward = rng.nextBoolean()
            ? new QuestReward.Builder()
                .type("gold")
                .amount(100 + rng.nextInt(200))
                .description("Ancient treasure")
                .build()
            : new QuestReward.Builder()
                .type("item")
                .targetId("ancient_artifact_" + rng.nextInt(100))
                .amount(1)
                .description("Ancient artifact")
                .build();
        
        return new Quest.Builder()
            .id(questId)
            .title("Explore the " + ruinName)
            .description("Ancient ruins have been discovered. Investigate what remains and search for treasure.")
            .type(QuestType.EXPLORE)
            .addObjective(objective)
            .addReward(reward)
            .linkedFeatureId(ruin.getId())
            .requiredLevel(0)
            .expirationTick(currentTick + QUEST_EXPIRATION_TICKS)
            .build();
    }
    
    /**
     * Generates quests related to clan conflicts (mediate, assist, sabotage).
     * 
     * @param clans List of all clans
     * @param currentTick Current game tick
     * @return List of conflict quests
     */
    private List<Quest> generateConflictQuests(List<Clan> clans, long currentTick) {
        List<Quest> quests = new ArrayList<>();
        Random rng = new Random(currentTick);
        
        // Find pairs of clans with hostile relationships
        for (int i = 0; i < clans.size(); i++) {
            Clan clan1 = clans.get(i);
            for (int j = i + 1; j < clans.size(); j++) {
                Clan clan2 = clans.get(j);
                
                if (quests.size() >= MAX_QUESTS_PER_EVENT) {
                    break;
                }
                
                // Check if clans are hostile
                var relationship = clan1.getRelationships().get(clan2.getId());
                if (relationship != null && relationship.getReputation() < -30) {
                    String pairId = clan1.getId() + "_" + clan2.getId();
                    if (!hasGeneratedQuestFor(pairId, currentTick)) {
                        // 100% chance to generate conflict quest for testing reliability
                        quests.add(createConflictQuest(clan1, clan2, currentTick, rng));
                    }
                }
            }
        }
        
        return quests;
    }
    
    /**
     * Creates a quest to mediate a clan conflict.
     * 
     * @param clan1 First clan in conflict
     * @param clan2 Second clan in conflict
     * @param currentTick Current game tick
     * @param rng Random number generator
     * @return Mediation quest
     */
    private Quest createConflictQuest(Clan clan1, Clan clan2, long currentTick, Random rng) {
        String questId = "quest_conflict_" + clan1.getId() + "_" + clan2.getId() + "_" + currentTick;
        
        QuestObjective obj1 = new QuestObjective.Builder()
            .id(questId + "_obj1")
            .description("Speak with " + clan1.getName() + " leader")
            .targetType("npc")
            .targetId(clan1.getLeaderId())
            .requiredCount(1)
            .build();
        
        QuestObjective obj2 = new QuestObjective.Builder()
            .id(questId + "_obj2")
            .description("Speak with " + clan2.getName() + " leader")
            .targetType("npc")
            .targetId(clan2.getLeaderId())
            .requiredCount(1)
            .build();
        
        // Reward: reputation with one or both clans
        QuestReward reward = new QuestReward.Builder()
            .type("reputation")
            .targetId(rng.nextBoolean() ? clan1.getId() : clan2.getId())
            .amount(50)
            .description("Improved clan reputation")
            .build();
        
        return new Quest.Builder()
            .id(questId)
            .title("Mediate Dispute Between " + clan1.getName() + " and " + clan2.getName())
            .description("Tensions are rising between two clans. Speak with both leaders and help resolve the conflict.")
            .type(QuestType.INVESTIGATE)
            .addObjective(obj1)
            .addObjective(obj2)
            .addReward(reward)
            .requiredLevel(0)
            .expirationTick(currentTick + QUEST_EXPIRATION_TICKS)
            .build();
    }
    
    /**
     * Generates quests to help rebuild after disasters.
     * 
     * @param structures List of all structures
     * @param currentTick Current game tick
     * @return List of disaster recovery quests
     */
    private List<Quest> generateDisasterQuests(List<Structure> structures, long currentTick) {
        List<Quest> quests = new ArrayList<>();
        Random rng = new Random(currentTick);
        
        List<Structure> damagedStructures = structures.stream()
            .filter(Structure::isDamaged)
            .filter(s -> s.getHealthPercentage() < 0.5) // Only severely damaged
            .filter(s -> !hasGeneratedQuestFor(s.getId() + "_repair", currentTick))
            .toList();
        
        for (Structure damaged : damagedStructures) {
            if (quests.size() >= MAX_QUESTS_PER_EVENT) {
                break;
            }
            
            // 15% chance to generate repair quest
            if (rng.nextDouble() < 0.15) {
                quests.add(createDisasterQuest(damaged, currentTick));
            }
        }
        
        return quests;
    }
    
    /**
     * Creates a quest to repair a damaged structure.
     * 
     * @param damaged The damaged structure
     * @param currentTick Current game tick
     * @return Repair quest
     */
    private Quest createDisasterQuest(Structure damaged, long currentTick) {
        String questId = "quest_repair_" + damaged.getId() + "_" + currentTick;
        
        int materialsNeeded = (int) ((1.0 - damaged.getHealthPercentage()) * 100);
        
        QuestObjective objective = new QuestObjective.Builder()
            .id(questId + "_obj1")
            .description("Donate " + materialsNeeded + " materials to repair structure")
            .targetType("structure")
            .targetId(damaged.getId())
            .requiredCount(materialsNeeded)
            .build();
        
        QuestReward goldReward = new QuestReward.Builder()
            .type("gold")
            .amount(50 + materialsNeeded)
            .description("Repair payment")
            .build();
        
        QuestReward reputationReward = new QuestReward.Builder()
            .type("reputation")
            .targetId(damaged.getOwnerId())
            .amount(25)
            .description("Gratitude from structure owner")
            .build();
        
        return new Quest.Builder()
            .id(questId)
            .title("Rebuild After Disaster")
            .description("A structure has been damaged by disaster. Help rebuild it by donating materials.")
            .type(QuestType.DELIVER)
            .addObjective(objective)
            .addReward(goldReward)
            .addReward(reputationReward)
            .linkedFeatureId(damaged.getId())
            .requiredLevel(0)
            .expirationTick(currentTick + QUEST_EXPIRATION_TICKS)
            .build();
    }
    
    /**
     * Generates quests to investigate active stories.
     * 
     * @param stories List of active stories
     * @param currentTick Current game tick
     * @return List of story investigation quests
     */
    private List<Quest> generateStoryQuests(List<Story> stories, long currentTick) {
        List<Quest> quests = new ArrayList<>();
        Random rng = new Random(currentTick);
        
        for (Story story : stories) {
            if (quests.size() >= MAX_QUESTS_PER_EVENT) {
                break;
            }
            
            if (!hasGeneratedQuestFor(story.getId(), currentTick)) {
                // 10% chance to generate quest per story
                if (rng.nextDouble() < 0.10) {
                    quests.add(createStoryQuest(story, currentTick));
                }
            }
        }
        
        return quests;
    }
    
    /**
     * Creates a quest to investigate a story.
     * 
     * @param story The story to investigate
     * @param currentTick Current game tick
     * @return Investigation quest
     */
    private Quest createStoryQuest(Story story, long currentTick) {
        String questId = "quest_story_" + story.getId() + "_" + currentTick;
        
        QuestObjective objective = new QuestObjective.Builder()
            .id(questId + "_obj1")
            .description("Investigate the rumors at (" + story.getOriginTileX() + ", " + story.getOriginTileY() + ")")
            .targetType("location")
            .targetId(story.getId())
            .targetX(story.getOriginTileX())
            .targetY(story.getOriginTileY())
            .requiredCount(1)
            .build();
        
        QuestReward reward = new QuestReward.Builder()
            .type("experience")
            .amount(100)
            .description("Knowledge gained")
            .build();
        
        return new Quest.Builder()
            .id(questId)
            .title("Investigate: " + story.getTitle())
            .description(story.getDescription())
            .type(QuestType.INVESTIGATE)
            .addObjective(objective)
            .addReward(reward)
            .linkedStoryId(story.getId())
            .requiredLevel(0)
            .expirationTick(currentTick + QUEST_EXPIRATION_TICKS)
            .build();
    }
    
    /**
     * Checks if a quest has already been generated for a specific target.
     * 
     * @param targetId Target identifier (structure ID, clan ID pair, etc.)
     * @param currentTick Current game tick
     * @return true if quest recently generated
     */
    private boolean hasGeneratedQuestFor(String targetId, long currentTick) {
        Long lastGeneration = generatedQuests.get(targetId);
        if (lastGeneration == null) {
            return false;
        }
        
        // Only allow new quest after 10000 ticks (~1 year)
        return (currentTick - lastGeneration) < 10000;
    }
    
    /**
     * Removes expired quest tracking entries.
     * 
     * @param currentTick Current game tick
     */
    private void cleanupExpiredTracking(long currentTick) {
        generatedQuests.entrySet().removeIf(entry ->
            (currentTick - entry.getValue()) > QUEST_EXPIRATION_TICKS
        );
    }
    
    /**
     * Generates a random name for a ruin.
     * 
     * @param rng Random number generator
     * @return Generated ruin name
     */
    private String generateRuinName(Random rng) {
        String[] prefixes = {"Ancient", "Forgotten", "Lost", "Ruined", "Abandoned", "Mysterious"};
        String[] suffixes = {"Temple", "Tower", "Fortress", "City", "Village", "Stronghold", "Sanctuary"};
        
        return prefixes[rng.nextInt(prefixes.length)] + " " + suffixes[rng.nextInt(suffixes.length)];
    }
}
