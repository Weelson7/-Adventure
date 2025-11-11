# Characters: Stats, Traits, Skills

## Overview
Playable and NPC races, stat mechanics, traits, and skill systems.

## Core Concepts
- Every sapient race playable; bestiary required
- Stats: soft caps, harder to increase at higher values
- Traits: lifelong, cannot be suppressed/hidden
- Skills: earned, can be forgotten for retraining, more skills means slower skill upgrade rate.

## Data Structures & Relationships
- Character: Race, stats, traits, skills, assets, carried objects.
- Bestiary: Catalog of races/species

## Generation & Initialization
- Characters generated at worldgen or by player/NPC action
- Stats, traits, skills assigned by race, experience, actions

## Stat Definitions & Progression
- Core stats: Strength, Vigor, Intelligence, Charisma, Agility, Endurance, Perception, Luck
- Stats affect combat, crafting, social, and magic actions
- Progression via experience, training, events, and trait effects
- Soft caps: higher values require more effort to increase
- Temporary modifiers: injuries, buffs, debuffs, equipment

### Stat Soft-Cap Function (concrete formula)

To prevent runaway stat growth, use a logistic soft-cap function for XP-to-stat conversion:

**Formula:**
```
newStatValue = currentStat + Δstat
where Δstat = baseGain / (1 + (currentStat / softCapThreshold)^2)
```

**Parameters (tunable):**
- `baseGain`: XP or training points earned (e.g., 10 points from training session)
- `softCapThreshold`: stat value where diminishing returns kick in (default: 50 for human baseline, 100 for legendary)
- Example: currentStat=30, baseGain=10, threshold=50 → Δstat = 10 / (1 + (30/50)^2) = 10 / 1.36 ≈ 7.35

**Hard Cap:**
- Absolute maximum stat value: 200 (prevents overflow and ensures balance).
- Reaching hard cap requires exponential XP investment and is intended for endgame legendary characters only.

**Trait Modifiers:**
- Traits like "Fast Learner" multiply `baseGain` by a factor (e.g., 1.2x).
- Traits like "Cursed" can reduce `baseGain` or shift `softCapThreshold` lower.

**Integration with Magic System:**
- `derivedStats.maxMana` computed as: `maxMana = baseMana + (Intelligence * manaPerStatPoint)` where `baseMana=10`, `manaPerStatPoint=2` (see `docs/objects_crafting_legacy.md`).
- Mana regeneration: `manaRegen = 1 + floor(Intelligence / 10)` per tick.

Notes:
- Link to `docs/design_decisions.md` for canonical soft-cap thresholds and trait multipliers.
- See `docs/objects_crafting_legacy.md` for spell casting mechanics and backlash formulas.


## Trait System
- Example traits: Fast Learner, Robust, Agile, Cursed, Blessed, Night Vision, Resilient, Clumsy
- Traits can be hereditary, random, or earned through actions/events
- Trait mutation possible via special events or magical effects
- Traits affect skill upgrade rate, stat progression, and unique abilities

## Skill System
- Skill categories: Combat (sword, archery), Crafting (smithing, alchemy), Magic (rune casting, enchanting), Social (persuasion, leadership), Survival (tracking, foraging)
- Skills acquired by meeting conditions, training, or experience
- Skills can be forgotten for retraining; more skills slow upgrade rate
- Skill synergy: advanced skills unlocked by combining basic skills
- Skill trees for specialization and progression

## Bestiary Details
- Races/species have base stats, predispositions, affinities, and unique abilities
- Examples: Elves (agile, night vision), Dwarves (robust, mining bonus), Orcs (strength, intimidation), Dragons (flight, fire breath)
- Bestiary catalogs all playable and NPC races/species

## Character Creation & Customization
- Players choose race/species, starting stats, traits, and skills
- Customization options: appearance, background, starting equipment
- Progression paths: specialization in combat, magic, crafting, or social roles

## Interactions & Edge Cases
- Stat/skill loss from injuries, curses, or events
- Trait mutation or stacking from magical effects or lineage
- Effects of death, resurrection, or transformation on stats/traits/skills
- Handling stat overflow, trait suppression (rare/special cases)

## Assets & Inventory
- Carried objects and assets affect stats, skills, and traits
- Equipment grants bonuses, abilities, or temporary modifiers
- Inventory management: weight, slots, quick access

## Modularity & Extensibility
- Guidelines for adding new stats, traits, skills, and races
- Support for modding/custom character definitions via config or scripting

## Open Questions

- Stat soft-cap function: specify exact curve (suggested: logistic or power-law) and parameters for core stats so progression is deterministic and testable.
- Magic integration: confirm `derivedStats` computation rules (e.g., maxMana = f(intelligence, traitModifiers)) and whether mana regenerates per tick or via consumables.
- Casting stat selection: finalize which stat(s) influence casting (default: Intelligence) and any secondary stats (e.g., Perception for aiming spells).
- Trait persistence & mutation: rules for trait inheritance, mutation rates, and trait removal mechanics (rare events vs controlled retraining).
- Skill retraining cost model: specify XP/tax or time costs to forget and retrain skills; define soft caps on total skill points.
- Serialization: which character fields are required in persisted schemas (minimal vs full serialization) for region-streaming performance.

Record unresolved questions in `docs/open_questions.md` and assign owners.

## Design Decisions

For canonical design decisions, see **`docs/design_decisions.md`**. Key decisions for characters/stats/traits/skills:

- Traits affect skill upgrade rate, not direct acquisition (e.g., Fast Learner boosts stat gain and skill upgrades).
- Stat progression uses logistic soft-cap function to prevent runaway growth.
- Magic integration: derivedStats.maxMana computed from Intelligence; mana regenerates per tick.

Refer to `docs/design_decisions.md` for cross-cutting rules and balancing philosophy.
