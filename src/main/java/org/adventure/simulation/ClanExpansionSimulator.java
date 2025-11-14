package org.adventure.simulation;

import org.adventure.npc.NamedNPC;
import org.adventure.society.Clan;
import org.adventure.society.RelationshipRecord;
import org.adventure.structure.Structure;
import org.adventure.structure.StructurePlacementRules;
import org.adventure.structure.StructureType;
import org.adventure.settlement.RoadGenerator;
import org.adventure.settlement.RoadTile;
import org.adventure.world.Biome;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Simulates NPC-driven clan expansion, conflict, and diplomacy.
 * Player-controlled clans bypass automatic simulation.
 * 
 * <p>NPC Clan AI Rules:
 * <ul>
 *   <li><b>Expansion:</b> Build structures when treasury > 500 AND population > 10</li>
 *   <li><b>War:</b> Attack when relationship < -50 AND military strength > 1.5x rival</li>
 *   <li><b>Alliance:</b> Propose when relationship > 50 AND mutual enemies exist</li>
 *   <li><b>Trade:</b> Establish routes when relationship > 0 AND settlements within 50 tiles</li>
 *   <li><b>Split:</b> Divide clan when size > 50 members</li>
 * </ul>
 * 
 * <p>Player clans have full control and skip all automatic behavior.
 * 
 * @see docs/BUILD_PHASE1.10.x.md Phase 1.10.3 specification
 */
public class ClanExpansionSimulator {
    private static final double EXPANSION_TREASURY_THRESHOLD = 500.0;
    private static final int EXPANSION_POPULATION_THRESHOLD = 10;
    private static final int EXPANSION_COST_MIN = 50;
    private static final int EXPANSION_COST_MAX = 200;
    private static final int EXPANSION_INTERVAL_TICKS = 500;
    
    private static final int WAR_RELATIONSHIP_THRESHOLD = -50;
    private static final double WAR_STRENGTH_MULTIPLIER = 1.5;
    private static final int WAR_COOLDOWN_TICKS = 500;
    
    private static final int ALLIANCE_RELATIONSHIP_THRESHOLD = 50;
    private static final int ALLIANCE_ENEMY_THRESHOLD = -30;
    
    private static final int TRADE_RELATIONSHIP_THRESHOLD = 0;
    private static final double TRADE_DISTANCE_THRESHOLD = 50.0;
    private static final double TRADE_BONUS_PER_100_TICKS = 10.0;
    
    private static final int SPLIT_SIZE_THRESHOLD = 50;
    
    private final Map<String, Long> lastExpansionTick;
    private final Map<String, Long> lastWarTick;
    private StructurePlacementRules placementRules;
    private RoadGenerator roadGenerator;
    
    public ClanExpansionSimulator() {
        this.lastExpansionTick = new HashMap<>();
        this.lastWarTick = new HashMap<>();
    }
    
    /**
     * Sets world data for placement rules and road generation.
     * Must be called before simulateTick().
     */
    public void setWorldData(double[][] elevation) {
        this.placementRules = new StructurePlacementRules(elevation);
        this.roadGenerator = new RoadGenerator(elevation);
    }
    
    /**
     * Simulates one tick of clan expansion for all clans.
     * Player-controlled clans are skipped.
     * 
     * @param clans List of all clans
     * @param npcs List of all NPCs
     * @param structures List of all structures
     * @param roads List of all roads
     * @param biomes World biomes (for placement rules)
     * @param elevation World elevation (for placement rules)
     * @param worldWidth World width
     * @param worldHeight World height
     * @param currentTick Current game tick
     */
    public void simulateTick(
        List<Clan> clans,
        List<NamedNPC> npcs,
        List<Structure> structures,
        List<RoadTile> roads,
        Biome[][] biomes,
        double[][] elevation,
        int worldWidth,
        int worldHeight,
        long currentTick
    ) {
        for (Clan clan : clans) {
            if (isPlayerControlled(clan, npcs)) {
                continue; // Skip player-controlled clans
            }
            
            // Process NPC clan AI
            processNPCExpansion(clan, npcs, structures, roads, biomes, elevation, worldWidth, worldHeight, clans, currentTick);
            processNPCDiplomacy(clan, clans, currentTick);
            processNPCTrade(clan, clans, structures, currentTick);
            processNPCWarfare(clan, clans, structures, currentTick);
            
            // Check for clan split
            checkForSplit(clan, npcs, structures, clans, currentTick);
        }
    }
    
    /**
     * Checks if a clan is controlled by any player.
     * 
     * @param clan The clan to check
     * @param npcs List of all NPCs
     * @return true if clan has at least one player member
     */
    private boolean isPlayerControlled(Clan clan, List<NamedNPC> npcs) {
        return npcs.stream()
            .filter(NamedNPC::isPlayer)
            .anyMatch(npc -> npc.getClanId().equals(clan.getId()));
    }
    
    /**
     * Processes NPC clan expansion (building new structures).
     * 
     * @param clan The clan attempting expansion
     * @param npcs List of all NPCs
     * @param structures List of all structures
     * @param roads List of all roads
     * @param biomes World biomes
     * @param elevation World elevation
     * @param worldWidth World width
     * @param worldHeight World height
     * @param clans List of all clans (for updating)
     * @param currentTick Current game tick
     */
    private void processNPCExpansion(
        Clan clan,
        List<NamedNPC> npcs,
        List<Structure> structures,
        List<RoadTile> roads,
        Biome[][] biomes,
        double[][] elevation,
        int worldWidth,
        int worldHeight,
        List<Clan> clans,
        long currentTick
    ) {
        // Check expansion conditions
        if (clan.getTreasury() < EXPANSION_TREASURY_THRESHOLD) {
            return; // Not enough gold
        }
        
        int population = (int) npcs.stream()
            .filter(npc -> npc.getClanId().equals(clan.getId()))
            .count();
        
        if (population < EXPANSION_POPULATION_THRESHOLD) {
            return; // Not enough population
        }
        
        // Check expansion cooldown
        long lastExpansion = lastExpansionTick.getOrDefault(clan.getId(), 0L);
        if (currentTick - lastExpansion < EXPANSION_INTERVAL_TICKS) {
            return; // Too soon since last expansion
        }
        
        // Determine structure type based on population phase
        StructureType structureType = selectStructureType(population, new Random(currentTick ^ clan.getId().hashCode()));
        
        // Find suitable location near clan center
        int[] location = findSuitableLocation(
            clan.getCenterX(),
            clan.getCenterY(),
            structures,
            roads,
            biomes,
            elevation,
            worldWidth,
            worldHeight,
            new Random(currentTick ^ clan.getId().hashCode())
        );
        
        if (location == null) {
            return; // No suitable location found
        }
        
        // Calculate construction cost
        Random rng = new Random(currentTick ^ clan.getId().hashCode());
        int cost = EXPANSION_COST_MIN + rng.nextInt(EXPANSION_COST_MAX - EXPANSION_COST_MIN);
        
        // Deduct cost from treasury
        Clan updatedClan = clan.withdraw(cost);
        
        // Create new structure
        String structureId = "structure_" + clan.getId() + "_" + currentTick;
        String locationTileId = location[0] + "," + location[1];
        
        Structure newStructure = new Structure.Builder()
            .id(structureId)
            .type(structureType)
            .ownerId(clan.getId())
            .locationTileId(locationTileId)
            .health(100.0)
            .maxHealth(100.0)
            .createdAtTick((int) currentTick)
            .lastUpdatedTick((int) currentTick)
            .build();
        
        structures.add(newStructure);
        
        // Generate automatic roads
        List<RoadTile> newRoads = roadGenerator.generateAutomaticRoads(
            structures,
            currentTick
        );
        roads.addAll(newRoads);
        
        // Update expansion timestamp
        lastExpansionTick.put(clan.getId(), currentTick);
        
        // Replace clan in list (immutable update)
        int clanIndex = clans.indexOf(clan);
        if (clanIndex >= 0) {
            clans.set(clanIndex, updatedClan);
        }
    }
    
    /**
     * Selects structure type based on population phase.
     * 
     * @param population Current clan population
     * @param rng Random number generator
     * @return Structure type to build
     */
    private StructureType selectStructureType(int population, Random rng) {
        double roll = rng.nextDouble();
        
        if (population < 20) {
            // Phase 1: Focus on residential (80% HOUSE, 20% SHOP)
            return roll < 0.80 ? StructureType.HOUSE : StructureType.SHOP;
        } else if (population < 50) {
            // Phase 2: Balanced (50% residential, 30% commercial, 20% special)
            if (roll < 0.50) {
                return StructureType.HOUSE;
            } else if (roll < 0.80) {
                return rng.nextBoolean() ? StructureType.SHOP : StructureType.MARKET;
            } else {
                return selectSpecialStructure(rng);
            }
        } else {
            // Phase 3: Diverse (30% residential, 40% commercial, 20% special, 10% military)
            if (roll < 0.30) {
                return StructureType.HOUSE;
            } else if (roll < 0.70) {
                return rng.nextBoolean() ? StructureType.SHOP : StructureType.MARKET;
            } else if (roll < 0.90) {
                return selectSpecialStructure(rng);
            } else {
                return rng.nextBoolean() ? StructureType.BARRACKS : StructureType.WATCHTOWER;
            }
        }
    }
    
    /**
     * Selects a special structure type.
     * 
     * @param rng Random number generator
     * @return Special structure type
     */
    private StructureType selectSpecialStructure(Random rng) {
        StructureType[] specialTypes = {
            StructureType.TEMPLE,
            StructureType.GUILD_HALL,
            StructureType.ARMORY,
            StructureType.ENCHANTED_LIBRARY
        };
        return specialTypes[rng.nextInt(specialTypes.length)];
    }
    
    /**
     * Finds a suitable location for a new structure near clan center.
     * 
     * @param centerX Clan center X
     * @param centerY Clan center Y
     * @param structures Existing structures
     * @param roads Existing roads
     * @param biomes World biomes
     * @param elevation World elevation
     * @param worldWidth World width
     * @param worldHeight World height
     * @param rng Random number generator
     * @return [x, y] location or null if no suitable location
     */
    private int[] findSuitableLocation(
        double centerX,
        double centerY,
        List<Structure> structures,
        List<RoadTile> roads,
        Biome[][] biomes,
        double[][] elevation,
        int worldWidth,
        int worldHeight,
        Random rng
    ) {
        // Try 100 random locations within 30 tiles of clan center
        for (int attempt = 0; attempt < 100; attempt++) {
            int x = (int) centerX + rng.nextInt(61) - 30; // -30 to +30
            int y = (int) centerY + rng.nextInt(61) - 30;
            
            // Check bounds
            if (x < 0 || x >= worldWidth || y < 0 || y >= worldHeight) {
                continue;
            }
            
            // Check placement rules
            Map<String, RoadTile> roadMap = roads.stream()
                .collect(Collectors.toMap(
                    r -> r.getX() + "," + r.getY(),
                    r -> r,
                    (r1, r2) -> r1
                ));
            if (placementRules.canPlaceStructure(x, y, StructureType.HOUSE, org.adventure.structure.EntranceSide.SOUTH, structures, roadMap)) {
                return new int[]{x, y};
            }
        }
        
        return null; // No suitable location found
    }
    
    /**
     * Processes NPC clan diplomacy (alliances, relationship changes).
     * 
     * @param clan The clan making diplomatic decisions
     * @param allClans List of all clans
     * @param currentTick Current game tick
     */
    private void processNPCDiplomacy(Clan clan, List<Clan> allClans, long currentTick) {
        for (Clan otherClan : allClans) {
            if (otherClan.getId().equals(clan.getId())) {
                continue;
            }
            
            RelationshipRecord relationship = clan.getRelationships().get(otherClan.getId());
            if (relationship == null) {
                continue;
            }
            
            int reputation = (int) relationship.getReputation();
            
            // Check for alliance proposal
            if (reputation > ALLIANCE_RELATIONSHIP_THRESHOLD) {
                // Check for mutual enemies
                boolean hasMutualEnemy = allClans.stream()
                    .anyMatch(thirdClan -> {
                        RelationshipRecord ourRelation = clan.getRelationships().get(thirdClan.getId());
                        RelationshipRecord theirRelation = otherClan.getRelationships().get(thirdClan.getId());
                        return ourRelation != null && theirRelation != null
                            && ourRelation.getReputation() < ALLIANCE_ENEMY_THRESHOLD
                            && theirRelation.getReputation() < ALLIANCE_ENEMY_THRESHOLD;
                    });
                
                if (hasMutualEnemy && relationship.getAllianceStrength() <= 30) {
                    // Propose alliance (boost reputation and alignment for alliance)
                    RelationshipRecord newRelationship = new RelationshipRecord(
                        otherClan.getId(),
                        75, // reputation
                        0.0, // influence
                        50.0, // alignment (high alignment = allied)
                        0.0, // raceAffinity
                        currentTick
                    );
                    
                    clan.updateRelationship(newRelationship);
                    
                    // Reciprocate
                    RelationshipRecord reciprocalRelationship = new RelationshipRecord(
                        clan.getId(),
                        75, // reputation
                        0.0, // influence
                        50.0, // alignment
                        0.0, // raceAffinity
                        currentTick
                    );
                    
                    otherClan.updateRelationship(reciprocalRelationship);
                    
                    // TODO: Create "Alliance" event for story system
                }
            }
            
            // Passive relationship decay/growth
            if (currentTick % 1000 == 0) {
                // Relationships drift toward neutral over time
                int newReputation = reputation;
                if (reputation > 0) {
                    newReputation = Math.max(0, reputation - 1);
                } else if (reputation < 0) {
                    newReputation = Math.min(0, reputation + 1);
                }
                
                if (newReputation != reputation) {
                    // Preserve alignment (alliance status) during drift
                    RelationshipRecord updatedRelationship = new RelationshipRecord(
                        otherClan.getId(),
                        newReputation,
                        relationship.getInfluence(),
                        relationship.getAlignment(),
                        relationship.getRaceAffinity(),
                        currentTick
                    );
                    
                    clan.updateRelationship(updatedRelationship);
                }
            }
        }
    }
    
    /**
     * Processes NPC clan trade (establish trade routes, generate income).
     * 
     * @param clan The clan engaging in trade
     * @param allClans List of all clans
     * @param structures List of all structures
     * @param currentTick Current game tick
     */
    private void processNPCTrade(Clan clan, List<Clan> allClans, List<Structure> structures, long currentTick) {
        for (Clan otherClan : allClans) {
            if (otherClan.getId().equals(clan.getId())) {
                continue;
            }
            
            RelationshipRecord relationship = clan.getRelationships().get(otherClan.getId());
            if (relationship == null || relationship.getReputation() < TRADE_RELATIONSHIP_THRESHOLD) {
                continue; // Need neutral or positive relationship for trade
            }
            
            // Check distance between clan centers
            double distance = Math.sqrt(
                Math.pow(clan.getCenterX() - otherClan.getCenterX(), 2) +
                Math.pow(clan.getCenterY() - otherClan.getCenterY(), 2)
            );
            
            if (distance > TRADE_DISTANCE_THRESHOLD) {
                continue; // Too far for trade
            }
            
            // Generate trade income (every 100 ticks)
            if (currentTick % 100 == 0) {
                Clan updatedClan = clan.deposit(TRADE_BONUS_PER_100_TICKS);
                
                // Update clan in list
                int clanIndex = allClans.indexOf(clan);
                if (clanIndex >= 0) {
                    allClans.set(clanIndex, updatedClan);
                }
                
                // Slowly improve relationship through trade
                if (currentTick % 1000 == 0) {
                    int newReputation = (int) Math.min(100, relationship.getReputation() + 5);
                    RelationshipRecord updatedRelationship = new RelationshipRecord(
                        otherClan.getId(),
                        newReputation,
                        relationship.getInfluence(),
                        relationship.getAlignment(),
                        relationship.getRaceAffinity(),
                        currentTick
                    );
                    
                    updatedClan.updateRelationship(updatedRelationship);
                }
            }
        }
    }
    
    /**
     * Processes NPC clan warfare (attacks on rival structures).
     * 
     * @param clan The clan considering war
     * @param allClans List of all clans
     * @param structures List of all structures
     * @param currentTick Current game tick
     */
    private void processNPCWarfare(Clan clan, List<Clan> allClans, List<Structure> structures, long currentTick) {
        for (Clan otherClan : allClans) {
            if (otherClan.getId().equals(clan.getId())) {
                continue;
            }
            
            RelationshipRecord relationship = clan.getRelationships().get(otherClan.getId());
            if (relationship == null || relationship.getReputation() >= WAR_RELATIONSHIP_THRESHOLD) {
                continue; // Not hostile enough
            }
            
            // Check war cooldown
            long lastWar = lastWarTick.getOrDefault(clan.getId() + "_" + otherClan.getId(), 0L);
            if (currentTick - lastWar < WAR_COOLDOWN_TICKS) {
                continue; // Too soon since last attack
            }
            
            // Calculate military strength (simple: count military structures)
            long ourMilitary = structures.stream()
                .filter(s -> s.getOwnerId().equals(clan.getId()))
                .filter(s -> s.getType() == StructureType.BARRACKS || s.getType() == StructureType.WATCHTOWER)
                .count();
            
            long theirMilitary = structures.stream()
                .filter(s -> s.getOwnerId().equals(otherClan.getId()))
                .filter(s -> s.getType() == StructureType.BARRACKS || s.getType() == StructureType.WATCHTOWER)
                .count();
            
            // Only attack if we're significantly stronger
            if (ourMilitary < (theirMilitary + 1) * WAR_STRENGTH_MULTIPLIER) {
                continue;
            }
            
            // Find enemy structures to attack
            List<Structure> enemyStructures = structures.stream()
                .filter(s -> s.getOwnerId().equals(otherClan.getId()))
                .collect(Collectors.toList());
            
            if (enemyStructures.isEmpty()) {
                continue;
            }
            
            // Attack random enemy structure
            Random rng = new Random(currentTick ^ clan.getId().hashCode());
            Structure target = enemyStructures.get(rng.nextInt(enemyStructures.size()));
            
            // Deal 50-70% damage
            double damage = 50.0 + rng.nextDouble() * 20.0;
            target.takeDamage(damage, (int) currentTick);
            
            // Update war timestamp
            lastWarTick.put(clan.getId() + "_" + otherClan.getId(), currentTick);
            
            // Worsen relationship
            int newReputation = (int) Math.max(-100, relationship.getReputation() - 20);
            RelationshipRecord updatedRelationship = new RelationshipRecord(
                otherClan.getId(),
                newReputation,
                relationship.getInfluence(),
                -50.0, // alignment (negative = hostile, breaks alliance)
                relationship.getRaceAffinity(),
                currentTick
            );
            
            clan.updateRelationship(updatedRelationship);
            
            // TODO: Create "War" event for story system
        }
    }
    
    /**
     * Checks if clan should split due to large size.
     * NPC clans split at 50+ members, player clans never auto-split.
     * 
     * @param clan The clan to check
     * @param npcs List of all NPCs
     * @param structures List of all structures
     * @param allClans List of all clans
     * @param currentTick Current game tick
     */
    private void checkForSplit(
        Clan clan,
        List<NamedNPC> npcs,
        List<Structure> structures,
        List<Clan> allClans,
        long currentTick
    ) {
        // Count clan members
        List<NamedNPC> members = npcs.stream()
            .filter(npc -> npc.getClanId().equals(clan.getId()))
            .collect(Collectors.toList());
        
        if (members.size() <= SPLIT_SIZE_THRESHOLD) {
            return; // Not large enough to split
        }
        
        // Check for multiple settlements
        List<Structure> clanStructures = structures.stream()
            .filter(s -> s.getOwnerId().equals(clan.getId()))
            .collect(Collectors.toList());
        
        if (clanStructures.size() < 10) {
            return; // Need at least 10 structures to split
        }
        
        // Split clan (60/40 split)
        int splitPoint = (int) (members.size() * 0.6);
        List<NamedNPC> clan1Members = members.subList(0, splitPoint);
        List<NamedNPC> clan2Members = members.subList(splitPoint, members.size());
        
        // Create new clan
        String newClanId = "clan_split_" + clan.getId() + "_" + currentTick;
        Clan newClan = new Clan.Builder()
            .id(newClanId)
            .name(clan.getName() + " (Split)")
            .type(clan.getType())
            .members(clan2Members.stream().map(NamedNPC::getId).collect(Collectors.toList()))
            .treasury(clan.getTreasury() * 0.4) // 40% of treasury
            .centerX(clan.getCenterX() + 10) // Offset center
            .centerY(clan.getCenterY() + 10)
            .relationships(new HashMap<>(clan.getRelationships()))
            .foundingTick(currentTick)
            .lastActiveTick(currentTick)
            .build();
        
        allClans.add(newClan);
        
        // Update original clan
        Clan updatedClan = new Clan.Builder(clan)
            .members(clan1Members.stream().map(NamedNPC::getId).collect(Collectors.toList()))
            .treasury(clan.getTreasury() * 0.6) // Keep 60% of treasury
            .build();
        
        int clanIndex = allClans.indexOf(clan);
        if (clanIndex >= 0) {
            allClans.set(clanIndex, updatedClan);
        }
        
        // Update NPC clan IDs
        // TODO: Update clan2Members to new clan ID
        // This would require NamedNPC.setClanId() method or rebuilding NPC list
        // For now, NPCs will need to be updated externally after clan split
        
        // TODO: Create "Clan Split" event for story system
    }
}
