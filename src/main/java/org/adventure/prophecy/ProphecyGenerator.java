package org.adventure.prophecy;

import org.adventure.world.Biome;
import org.adventure.world.RegionalFeature;
import org.adventure.world.RegionalFeature.FeatureType;

import java.util.*;

/**
 * Generates major world prophecies at worldgen.
 * 
 * Features:
 * - 1-3 prophecies per world
 * - Linked to world features (MAGIC_ZONE, ANCIENT_RUINS)
 * - Hybrid fulfillment (trigger conditions + time limit)
 * 
 * Design: BUILD_PHASE1.10.x.md - Phase 1.10.1, deliverable 6
 */
public class ProphecyGenerator {
    private static final int MIN_PROPHECIES = 1;
    private static final int MAX_PROPHECIES = 3;
    private static final long MIN_TRIGGER_TICK = 50000L; // ~5 in-game years
    private static final long MAX_TRIGGER_TICK = 500000L; // ~50 in-game years
    
    /**
     * Generates prophecies based on world features.
     * 
     * @param worldSeed Seed for deterministic generation
     * @param features List of regional features
     * @param biomes World biome map
     * @return List of generated prophecies
     */
    public static List<Prophecy> generateProphecies(
            long worldSeed,
            List<RegionalFeature> features,
            Biome[][] biomes) {
        
        List<Prophecy> prophecies = new ArrayList<>();
        Random rng = new Random(worldSeed ^ 0x9209ABCD);
        
        // Determine prophecy count (1-3)
        int count = MIN_PROPHECIES + rng.nextInt(MAX_PROPHECIES - MIN_PROPHECIES + 1);
        
        // Filter features suitable for prophecies
        List<RegionalFeature> magicFeatures = new ArrayList<>();
        List<RegionalFeature> ruinFeatures = new ArrayList<>();
        List<RegionalFeature> dangerFeatures = new ArrayList<>();
        
        for (RegionalFeature feature : features) {
            FeatureType type = feature.getType();
            if (type == FeatureType.MAGIC_ZONE) {
                magicFeatures.add(feature);
            } else if (type == FeatureType.ANCIENT_RUINS || type == FeatureType.SUBMERGED_CITY) {
                ruinFeatures.add(feature);
            } else if (type == FeatureType.VOLCANO || type == FeatureType.CRYSTAL_CAVE) {
                dangerFeatures.add(feature);
            }
        }
        
        // Generate prophecies
        for (int i = 0; i < count; i++) {
            ProphecyType type = ProphecyType.values()[rng.nextInt(ProphecyType.values().length)];
            
            RegionalFeature linkedFeature = selectFeatureForProphecy(
                type, magicFeatures, ruinFeatures, dangerFeatures, rng
            );
            
            if (linkedFeature == null) {
                continue; // Skip if no suitable feature
            }
            
            long triggerTick = MIN_TRIGGER_TICK + 
                (long)(rng.nextDouble() * (MAX_TRIGGER_TICK - MIN_TRIGGER_TICK));
            
            Prophecy prophecy = new Prophecy.Builder()
                .id("prophecy_" + i)
                .title(generateTitle(type, rng))
                .description(generateDescription(type, linkedFeature, rng))
                .type(type)
                .triggerTick(triggerTick)
                .triggerConditions(generateTriggerConditions(type, rng))
                .linkedFeatureId(String.valueOf(linkedFeature.getId()))
                .fulfilled(false)
                .build();
            
            prophecies.add(prophecy);
        }
        
        return prophecies;
    }
    
    /**
     * Selects an appropriate feature for the prophecy type.
     */
    private static RegionalFeature selectFeatureForProphecy(
            ProphecyType type,
            List<RegionalFeature> magic,
            List<RegionalFeature> ruins,
            List<RegionalFeature> danger,
            Random rng) {
        
        List<RegionalFeature> candidates;
        
        switch (type) {
            case DOOM:
                candidates = danger.isEmpty() ? magic : danger;
                break;
            case SALVATION:
                candidates = magic.isEmpty() ? ruins : magic;
                break;
            case TRANSFORMATION:
                candidates = magic;
                break;
            case AWAKENING:
                candidates = ruins;
                break;
            default:
                candidates = magic;
        }
        
        if (candidates.isEmpty()) {
            return null;
        }
        
        return candidates.get(rng.nextInt(candidates.size()));
    }
    
    /**
     * Generates prophecy title.
     */
    private static String generateTitle(ProphecyType type, Random rng) {
        List<String> titles;
        
        switch (type) {
            case DOOM:
                titles = Arrays.asList(
                    "The Coming Catastrophe",
                    "Flames of Destruction",
                    "The Dark Awakening",
                    "End of Days"
                );
                break;
            case SALVATION:
                titles = Arrays.asList(
                    "Rise of the Hero",
                    "The Chosen One",
                    "Dawn of Hope",
                    "The Savior's Prophecy"
                );
                break;
            case TRANSFORMATION:
                titles = Arrays.asList(
                    "The Great Change",
                    "Metamorphosis of the Land",
                    "The World Transformed",
                    "Evolution's Call"
                );
                break;
            case AWAKENING:
                titles = Arrays.asList(
                    "Return of the Ancients",
                    "Lost Civilization Restored",
                    "The Old Ones Rise",
                    "Forgotten Legacy"
                );
                break;
            default:
                titles = Arrays.asList("The Prophecy");
        }
        
        return titles.get(rng.nextInt(titles.size()));
    }
    
    /**
     * Generates prophecy description.
     */
    private static String generateDescription(
            ProphecyType type,
            RegionalFeature feature,
            Random rng) {
        
        String featureName = feature.getType().toString().toLowerCase().replace('_', ' ');
        
        switch (type) {
            case DOOM:
                return "A great disaster will emerge from the " + featureName + 
                       ", bringing destruction to all who dwell nearby.";
            case SALVATION:
                return "A hero will rise from the " + featureName + 
                       ", bringing hope and unity to the scattered clans.";
            case TRANSFORMATION:
                return "The " + featureName + " will expand, transforming the land " +
                       "and all who inhabit it into something new.";
            case AWAKENING:
                return "The " + featureName + " will reveal its secrets, " +
                       "awakening a lost civilization of great power.";
            default:
                return "A prophecy concerning the " + featureName + ".";
        }
    }
    
    /**
     * Generates trigger conditions for the prophecy.
     */
    private static List<String> generateTriggerConditions(ProphecyType type, Random rng) {
        List<String> conditions = new ArrayList<>();
        
        switch (type) {
            case DOOM:
                conditions.add("clan_population_reaches_100");
                conditions.add("structure_count_exceeds_50");
                break;
            case SALVATION:
                conditions.add("player_completes_quest");
                conditions.add("clan_relationship_reaches_75");
                break;
            case TRANSFORMATION:
                conditions.add("magic_zone_explored");
                conditions.add("artifact_discovered");
                break;
            case AWAKENING:
                conditions.add("ancient_ruins_explored");
                conditions.add("artifact_activated");
                break;
            case PROSPERITY:
                conditions.add("treasury_reaches_10000");
                conditions.add("trade_routes_established");
                break;
            case WAR:
                conditions.add("clan_relationship_below_25");
                conditions.add("military_strength_sufficient");
                break;
        }
        
        return conditions;
    }
}
