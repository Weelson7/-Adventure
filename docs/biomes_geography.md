# Biomes & Geography

## Overview
Defines the types, distribution, and mechanics of biomes and geographical features in the world.

## Core Concepts

- Biomes strictly separated by tile boundaries, with progressive transitions (no abrupt change from extreme cold to warm)
- Each biome has unique resources, hazards, and environmental effects
- Micro-biomes (e.g., oasis, volcanic vent, magical grove) exist as regional features

## Data Structures & Relationships

- Biome: Type, resources, hazards, environmental effects, native species
- Tile: Assigned biome, transition data
- RegionalFeature: Micro-biomes, rare features

## Generation & Initialization

- Biome assigned based on elevation, temperature, water proximity, and plate boundaries
- Micro-biomes and rare features placed as special regional features

## Interactions & Edge Cases

- Biome transitions managed at tile boundaries, with gradual changes in resource/hazard distribution
- Resource/hazard distribution affects gameplay and character stats
- Edge cases: abrupt transitions, overlapping features, biome isolation

## Expansion & Modularity

- Add new biomes, resources, hazards, and environmental effects
- Modular biome system for future expansion and custom biomes

## Open Questions

- Should biomes ever directly modify base character stats (beyond explicit features like magic zones)? Current docs say no; confirm and document exceptions.
- Resource regeneration tuning: confirm regenRate and Rmax per-resource defaults and how they scale with biome area and health (see `docs/economy_resources.md`).
- Micro-biome placement rules: what is the exact rarity model and placement constraints for oasis, magical groves, and vents?
- Seasonal & climate-change mechanics: how frequently do biome-level transformations occur and what triggers them (cumulative damage, global climate drift, catastrophic events)?
- Storage & node constraints: maximum per-node storage, drop rates, and overflow behavior for resources collected by players/NPCs.

Add unresolved or implementation questions to `docs/open_questions.md`.


## Design Decisions

For canonical design decisions, see **`docs/design_decisions.md`**. Key decisions for biomes/geography:

- Biomes do not affect character stats, abilities, or movement speed (except via blessings/curses or explicit features).
- Resource abundance and hazard frequency balanced by biome rarity, altitude, and dynamic regeneration rates.
- Biome borders managed with gradual transitions, transitional micro-biomes, and event-driven anomalies.

Refer to `docs/design_decisions.md` and `docs/economy_resources.md` for resource/regeneration formulas.# Main Biome List

- Forest: Wood, herbs, wildlife; hazards: wild animals, dense vegetation
- Desert: Sand, minerals, rare plants; hazards: heat, dehydration, sandstorms
- Tundra: Ice, rare minerals, cold-resistant plants; hazards: cold, blizzards
- Grassland: Grains, grazing animals; hazards: wildfires, predators
- Savanna: Grass, large animals; hazards: drought, predators
- Taiga: Coniferous wood, fur animals; hazards: cold, wolves
- Jungle: Exotic plants, rare animals; hazards: disease, predators, humidity
- Swamp: Herbs, amphibians; hazards: disease, quicksand, insects
- Steppe: Grasses, migratory animals; hazards: wind, predators
- Ocean: Fish, salt, pearls; hazards: storms, drowning
- Lake: Freshwater fish, reeds; hazards: flooding, cold
- Mountain: Stone, ores, rare herbs; hazards: avalanches, altitude sickness
- Hills: Stone, small animals; hazards: landslides
- Plains: Crops, grazing animals; hazards: wildfires
- Volcanic: Obsidian, rare minerals; hazards: eruptions, toxic gases
- Magical: Magical plants, artifacts; hazards: magical anomalies
- Subterranean: Gems, fungi, underground creatures; hazards: cave-ins, darkness
- Urban: Trade goods, crafted items; hazards: crime, pollution

## Biome Generation Algorithms
- Use layered noise functions and tectonic plate simulation to generate organic biome shapes and distributions
- Biome adjacency rules prevent unrealistic transitions (e.g., desert next to tundra)
- Biome size and clustering influenced by world parameters and plate boundaries

## Environmental Effects
- Weather systems (rain, snow, storms) affect biomes dynamically
- Seasonal changes alter resource availability, hazards, and native species behavior
- Global climate system influences biome health and transformation

## Resource Regeneration & Depletion
- Resources renew over time based on biome type and health
- Overexploitation by players/NPCs can deplete resources, triggering scarcity events
- Recovery rates depend on environmental effects and player actions

## Dynamic Biome Changes
- Biomes can transform due to events (e.g., forest fire, magical corruption, desertification)
- Triggers include player actions, global events, or natural disasters
- Consequences: altered resources, hazards, and native species

## Micro-biome Mechanics
- Micro-biomes placed based on local conditions and rarity
- Unique features, resources, and events tied to micro-biomes (e.g., magical groves, volcanic vents)
- Micro-biomes can act as transitional zones or special encounter areas

## Native Species & Ecosystem Simulation
- Populations managed via food chains, migration, and breeding cycles
- Extinction events possible from overhunting, disasters, or biome transformation
- Ecosystem health affects resource regeneration and hazard frequency

## Biome-Specific Events
- Volcanic eruptions, magical storms, plague outbreaks, forest fires, droughts
- Events can alter biome state, trigger quests, or impact player strategies

## Player Interaction with Biomes
- Players can exploit, protect, or alter biomes (e.g., reforestation, mining, magical rituals)
- Long-term consequences for biome health and resource availability
- Reputation and rewards for sustainable or destructive actions

## Modularity & Customization
- Guidelines for adding new biomes, resources, hazards, and events
- Support for modding and custom biome definitions via configuration files or scripting
