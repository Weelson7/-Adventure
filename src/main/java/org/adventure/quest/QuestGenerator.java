package org.adventure.quest;

import org.adventure.story.Story;
import org.adventure.world.RegionalFeature;
import org.adventure.world.RegionalFeature.FeatureType;

import java.util.*;

/**
 * Generates quests from world features and stories.
 * 
 * Features:
 * - Link quests to RegionalFeatures
 * - Quest types: EXPLORE, RETRIEVE, DEFEAT, INVESTIGATE
 * - Multi-step quest chains
 * 
 * Design: BUILD_PHASE1.10.x.md - Phase 1.10.1, deliverable 7
 */
public class QuestGenerator {
    
    /**
     * Generates feature-based quests at worldgen.
     * 
     * @param worldSeed Seed for deterministic generation
     * @param features List of regional features
     * @param stories List of initial stories
     * @return List of generated quests
     */
    public static List<Quest> generateFeatureQuests(
            long worldSeed,
            List<RegionalFeature> features,
            List<Story> stories) {
        
        List<Quest> quests = new ArrayList<>();
        Random rng = new Random(worldSeed ^ 0x00E57);
        
        // Generate quests from notable features
        for (RegionalFeature feature : features) {
            if (shouldGenerateQuest(feature, rng)) {
                Quest quest = createQuestForFeature(feature, rng);
                if (quest != null) {
                    quests.add(quest);
                }
            }
        }
        
        // Generate quests from stories (10% chance per story)
        for (Story story : stories) {
            if (rng.nextDouble() < 0.1) {
                Quest quest = createQuestForStory(story, rng);
                if (quest != null) {
                    quests.add(quest);
                }
            }
        }
        
        return quests;
    }
    
    /**
     * Determines if a feature should generate a quest.
     */
    private static boolean shouldGenerateQuest(RegionalFeature feature, Random rng) {
        FeatureType type = feature.getType();
        if (type == FeatureType.MAGIC_ZONE || 
            type == FeatureType.ANCIENT_RUINS || 
            type == FeatureType.SUBMERGED_CITY) {
            return true; // Always generate quests for special features
        } else if (type == FeatureType.VOLCANO || type == FeatureType.CRYSTAL_CAVE) {
            return rng.nextDouble() < 0.5; // 50% chance
        } else {
            return rng.nextDouble() < 0.2; // 20% chance for others
        }
    }
    
    /**
     * Creates a quest for a regional feature.
     */
    private static Quest createQuestForFeature(RegionalFeature feature, Random rng) {
        QuestType questType = selectQuestType(feature.getType(), rng);
        
        String title = generateQuestTitle(questType, feature);
        String description = generateQuestDescription(questType, feature);
        List<QuestObjective> objectives = generateObjectives(questType, feature, rng);
        List<QuestReward> rewards = generateRewards(questType, feature, rng);
        
        return new Quest.Builder()
            .id("quest_feature_" + feature.getId())
            .title(title)
            .description(description)
            .type(questType)
            .objectives(objectives)
            .rewards(rewards)
            .linkedFeatureId(String.valueOf(feature.getId()))
            .linkedStoryId(null)
            .status(QuestStatus.AVAILABLE)
            .schemaVersion(1)
            .build();
    }
    
    /**
     * Creates a quest for a story.
     */
    private static Quest createQuestForStory(Story story, Random rng) {
        QuestType questType = QuestType.INVESTIGATE;
        
        String title = "Investigate: " + story.getTitle();
        String description = "Learn more about the tale of " + story.getTitle().toLowerCase() + ".";
        
        List<QuestObjective> objectives = new ArrayList<>();
        objectives.add(new QuestObjective.Builder()
            .id("obj_" + story.getId() + "_0")
            .description("Reach the location of the story")
            .targetX(story.getOriginTileX())
            .targetY(story.getOriginTileY())
            .completed(false)
            .build());
        
        List<QuestReward> rewards = new ArrayList<>();
        rewards.add(new QuestReward.Builder()
            .type("reputation")
            .value(10)
            .description("+10 reputation")
            .build());
        
        return new Quest.Builder()
            .id("quest_story_" + story.getId())
            .title(title)
            .description(description)
            .type(questType)
            .objectives(objectives)
            .rewards(rewards)
            .linkedFeatureId(null)
            .linkedStoryId(story.getId())
            .status(QuestStatus.AVAILABLE)
            .schemaVersion(1)
            .build();
    }
    
    /**
     * Selects appropriate quest type for feature.
     */
    private static QuestType selectQuestType(FeatureType featureType, Random rng) {
        if (featureType == FeatureType.MAGIC_ZONE) {
            return QuestType.INVESTIGATE;
        } else if (featureType == FeatureType.ANCIENT_RUINS || featureType == FeatureType.SUBMERGED_CITY) {
            return rng.nextBoolean() ? QuestType.EXPLORE : QuestType.RETRIEVE;
        } else if (featureType == FeatureType.VOLCANO || featureType == FeatureType.CRYSTAL_CAVE) {
            return QuestType.DEFEAT;
        } else {
            return QuestType.values()[rng.nextInt(QuestType.values().length)];
        }
    }
    
    /**
     * Generates quest title.
     */
    private static String generateQuestTitle(QuestType type, RegionalFeature feature) {
        String featureName = feature.getType().toString().toLowerCase().replace('_', ' ');
        
        switch (type) {
            case EXPLORE:
                return "Explore the " + featureName;
            case RETRIEVE:
                return "Retrieve artifact from the " + featureName;
            case DEFEAT:
                return "Defeat creatures near the " + featureName;
            case INVESTIGATE:
                return "Investigate the " + featureName;
            default:
                return "Quest: " + featureName;
        }
    }
    
    /**
     * Generates quest description.
     */
    private static String generateQuestDescription(QuestType type, RegionalFeature feature) {
        String featureName = feature.getType().toString().toLowerCase().replace('_', ' ');
        
        switch (type) {
            case EXPLORE:
                return "Venture into the " + featureName + " and discover its secrets.";
            case RETRIEVE:
                return "A powerful artifact is said to be hidden in the " + featureName + 
                       ". Retrieve it and bring it back safely.";
            case DEFEAT:
                return "Dangerous creatures have been spotted near the " + featureName + 
                       ". Defeat them to protect nearby settlements.";
            case INVESTIGATE:
                return "Strange phenomena have been reported at the " + featureName + 
                       ". Investigate and report your findings.";
            default:
                return "Complete the quest related to the " + featureName + ".";
        }
    }
    
    /**
     * Generates quest objectives.
     */
    private static List<QuestObjective> generateObjectives(
            QuestType type,
            RegionalFeature feature,
            Random rng) {
        
        List<QuestObjective> objectives = new ArrayList<>();
        
        // Primary objective: reach the feature
        objectives.add(new QuestObjective.Builder()
            .id("obj_" + feature.getId() + "_0")
            .description("Travel to the " + feature.getType().toString().toLowerCase().replace('_', ' '))
            .targetX(feature.getX())
            .targetY(feature.getY())
            .completed(false)
            .build());
        
        // Secondary objectives based on quest type
        switch (type) {
            case EXPLORE:
                objectives.add(new QuestObjective.Builder()
                    .id("obj_" + feature.getId() + "_1")
                    .description("Map the area")
                    .targetX(feature.getX())
                    .targetY(feature.getY())
                    .completed(false)
                    .build());
                break;
            case RETRIEVE:
                objectives.add(new QuestObjective.Builder()
                    .id("obj_" + feature.getId() + "_1")
                    .description("Find the artifact")
                    .targetX(feature.getX())
                    .targetY(feature.getY())
                    .completed(false)
                    .build());
                objectives.add(new QuestObjective.Builder()
                    .id("obj_" + feature.getId() + "_2")
                    .description("Return the artifact safely")
                    .targetX(-1)
                    .targetY(-1)
                    .completed(false)
                    .build());
                break;
            case DEFEAT:
                objectives.add(new QuestObjective.Builder()
                    .id("obj_" + feature.getId() + "_1")
                    .description("Defeat the hostile creatures")
                    .targetX(feature.getX())
                    .targetY(feature.getY())
                    .completed(false)
                    .build());
                break;
            case INVESTIGATE:
                objectives.add(new QuestObjective.Builder()
                    .id("obj_" + feature.getId() + "_1")
                    .description("Gather information")
                    .targetX(feature.getX())
                    .targetY(feature.getY())
                    .completed(false)
                    .build());
                break;
            case ESCORT:
            case DELIVER:
            case CRAFT:
                // Not implemented yet, add placeholder
                break;
        }
        
        return objectives;
    }
    
    /**
     * Generates quest rewards.
     */
    private static List<QuestReward> generateRewards(
            QuestType type,
            RegionalFeature feature,
            Random rng) {
        
        List<QuestReward> rewards = new ArrayList<>();
        
        // Gold reward
        int goldAmount = 50 + rng.nextInt(200);
        rewards.add(new QuestReward.Builder()
            .type("gold")
            .value(goldAmount)
            .description(goldAmount + " gold")
            .build());
        
        // Reputation reward
        int reputation = 10 + rng.nextInt(20);
        rewards.add(new QuestReward.Builder()
            .type("reputation")
            .value(reputation)
            .description("+" + reputation + " reputation")
            .build());
        
        // Additional rewards for harder quests
        if (type == QuestType.DEFEAT || type == QuestType.RETRIEVE) {
            rewards.add(new QuestReward.Builder()
                .type("item")
                .value(1)
                .description("Rare item")
                .build());
        }
        
        return rewards;
    }
}
