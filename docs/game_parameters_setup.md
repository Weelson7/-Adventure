# Game Parameters & World Setup

## Overview
Configurable parameters for world creation and game setup.

## Core Concepts
- Size, lethality, species pack, resource abundance
- Presets for world creation
- No preview; debug mode available
- Modular game design

## Data Structures & Relationships
- GameConfig: Parameters for worldgen
- Preset: Predefined configuration

## Generation & Initialization
- Parameters set before worldgen, fixed after
- Debug mode for testing

## Interactions & Edge Cases
- Parameter impact on world features
- Edge cases: resource scarcity, parameter conflicts

## Expansion & Modularity
- Add new parameters, presets
- Modular setup system

## Parameter Definitions
- World size: grid dimensions, altitude layers
- Lethality: affects hazard frequency, NPC aggression, event danger
- Species pack: selection of playable/NPC races, bestiary scope
- Resource abundance: initial and regeneration rates for resources
- Climate: temperature, precipitation, seasonal variation
- NPC density: population per region, migration rates
- Event frequency: rate of world events, crises, and story triggers
- Magic level: availability and power of magic, magical anomalies
- Tech level: available technologies, crafting complexity
- Starting resources: initial assets for players/clans

## Preset System
- Example presets: Classic Fantasy, High Lethality, Resource Rich, Magic Overload, Peaceful Exploration
- Presets can be created, saved, and selected by users
- Presets include recommended values and descriptions

## Canonical Defaults & Presets

The following presets provide example values that map to `docs/specs_summary.md` canonical defaults. These are suggested starting presets — server operators can override per-deployment.

- Global canonical defaults (operator-configurable):
	- `tickLengthSeconds`: 1
	- `activeTickRateMultiplier`: 1.0
	- `backgroundTickRateMultiplier`: 1/60
	- `maxActiveRegionsPerServer`: 100 (initial estimate — requires profiling)
	- `taxRate`: 0.05
	- `gracePeriodDays`: 14
	- `seizureThresholdDays`: 21

- Preset: Classic Fantasy (recommended for first playtests)
	- worldSize: medium (512x512)
	- altitudeLayers: 4
	- lethality: medium
	- resourceAbundance: normal
	- magicLevel: normal
	- npcDensity: medium
	- eventFrequency: normal

- Preset: High Lethality
	- worldSize: medium
	- lethality: high
	- resourceAbundance: normal
	- npcDensity: medium-high
	- eventFrequency: high

- Preset: Resource Rich
	- resourceAbundance: high
	- npcDensity: low
	- eventFrequency: low

- Preset: Magic Overload
	- magicLevel: high
	- numberOfMagicZones: increased
	- resourceAbundance: normal

- Preset: Peaceful Exploration
	- lethality: low
	- npcDensity: low
	- eventFrequency: low
	- resourceAbundance: normal

Notes:
- Expose only safe parameters to players (e.g., worldSize, preset choice, starting resources). Keep operator-sensitive parameters (tick rates, maxActiveRegions, server caps) as operator-only settings or server-side overrides.
- Update UI to present preset descriptions and warn when a chosen preset may conflict with server caps.

## Parameter Validation & Conflict Resolution
- Automatic checks for invalid or conflicting parameter combinations
- Warnings and suggestions for corrections
- User prompts for critical conflicts

## Debug Mode Features
- Real-time parameter tweaking and world inspection
- Event logging and simulation speed controls
- Ability to spawn resources, NPCs, and events for testing
- Restrictions to prevent debug mode abuse in multiplayer

## Impact Analysis
- Visualization of parameter impact before worldgen (e.g., resource maps, hazard overlays)
- Feedback on expected gameplay balance and world features

## Edge Case Handling
- Handling extreme values (e.g., zero resources, max lethality)
- Recovery from failed or aborted world generation
- Fallback to safe defaults if generation fails

## Modularity & Extensibility
- Guidelines for adding new parameters and presets
- Support for modding/custom parameter sets via config files or scripting

## Open Questions
## Open Questions

- Exposed parameters: which internal simulation parameters should be exposed to users via presets (tickRate, backgroundUpdateRatio, maxActiveRegions) and which should remain server/operator-only?
- Preset defaults: define canonical default values for key presets (Classic Fantasy, High Lethality, Resource Rich) and publish them as examples.
- Parameter validation thresholds: define safe ranges and failure behavior (clamp, warn, or block) for extreme parameter combinations.
- Multiplayer safety: which parameters could harm multiplayer stability if user-chosen (e.g., extremely high NPC density) and therefore need server-side caps?
- Preview & visualization: should the editor provide a quick preview map for presets (low-res) and what resources/time budget do previews have?

Record unresolved choices in `docs/open_questions.md` for triage.
## Design Decisions

For canonical design decisions, see **`docs/design_decisions.md`**. Key decisions for game parameters:

- Custom parameter sets supported (default and user-defined presets).
- Resource abundance balanced by world size, biome distribution, population/resource consumption rates, and periodic checks.
- Parameter validation enforced to prevent invalid/conflicting configurations.

Refer to `docs/design_decisions.md` for authoritative parameter ranges and defaults.