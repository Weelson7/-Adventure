# Objects, Crafting & Legacy Effects

## Overview
Items, crafting mechanics, magical effects, and legacy/story systems.

## Core Concepts
- Extensive object categories (weapons, tools, armor, etc.)
- Crafting proficiency system
- Magic spells/capabilities, evolving objects
- Durability, restoration, physical processes
- Stories created at worldgen, known by characters or in storybooks

## Data Structures & Relationships
- Object: Category, stats, durability, magic, legacy
- Crafting: Recipe, proficiency
- Story: Linked to objects/structures

## Generation & Initialization
- Objects generated, found, stolen, crafted
- Legacy effects assigned by story or event

## Interactions & Edge Cases
- Object modification, enchantment, evolution
- Durability and restoration
- Edge cases: multiple stories, legacy transfer, object loss

## Expansion & Modularity
- Add new object categories, magic types
- Modular crafting and legacy system

## Magic System

- Spells are constructed from rune sequences:
  - 4 runes for spell type (e.g., attack, heal, shield, summon)
  - 8 runes for elements (e.g., fire, water, earth, air, light, dark, arcane, nature)
  - 4 runes for spell modifiers (e.g., range, duration, power, area)
- Each spell has a mana cost and stability rating
- Higher casting stat reduces chance of spell backlash (negative effect)
- Spell creation and casting are modular, allowing for future expansion of runes and effects

### Magic & Character Stats Integration (Concrete)

**Mana Pool:**
- Each character has a `manaPool` with `current` and `max` values (defined in `docs/data_models.md`).
- `maxMana` is a derived stat computed at character creation and on level-up:
  - Formula (suggested default): `maxMana = baseMana + (castingStat * manaPerStat)`
  - Example: `baseMana = 10`, `manaPerStat = 2`, Intelligence = 14 → `maxMana = 10 + (14*2) = 38`
- Mana regenerates per tick (configurable rate, e.g., 1 mana per 10 ticks) or via consumables (potions, rest).

**Casting Stat:**
- Default primary casting stat: **Intelligence**. This stat influences spell power, accuracy, and backlash probability.
- Secondary stats can provide modifiers (e.g., Perception for targeting, Charisma for summoning).

**Spell Costs:**
- Each spell has a base `manaCost` defined by its rune complexity:
  - Simple spells (1-2 runes): 5-10 mana
  - Medium spells (3-4 runes): 15-30 mana
  - Complex spells (5+ runes): 40-80 mana
- Cost modifiers: spell modifiers (range, area, power) multiply base cost by a factor (e.g., +50% per modifier rune).

**Backlash Mechanics:**
- Backlash probability formula (suggested):
  - `P_backlash = max(0, (spellComplexity - stability) / castingStat)`
  - Where `spellComplexity` = sum of rune difficulty values, `stability` = spell's inherent stability rating (0-100), `castingStat` = character's Intelligence (or other).
  - Example: complexity=60, stability=40, Intelligence=20 → `P_backlash = (60-40)/20 = 1.0` (100% backlash if P>1, clamp to 0..1 for probability).
- Backlash effects (chosen randomly on failure): mana drain, temporary stat debuff, spell fizzle, minor damage to caster, or unintended side effect.

**Spell Learning & Mastery:**
- Characters learn spells via study, experience, or magical events.
- Repeated casting of a spell increases mastery, which improves `stability` for that spell (e.g., +1 stability per 10 successful casts, soft cap at +20).
- Mastery also reduces `manaCost` slightly (e.g., -5% per mastery tier, up to -20%).

Notes:
- Link to `docs/characters_stats_traits_skills.md` for stat definitions and progression.
- See `docs/design_decisions.md` for canonical casting stat and mana regen rates.

## Object Categories & Hierarchies
- Weapons: swords, axes, bows, spears, staves, daggers, legendary artifacts, focus (for magic)
- Tools: hammers, pickaxes, shovels, lockpicks, alchemy sets
- Armor: helmets, chestplates, shields, boots, magical armors
- Jewelry: rings, necklaces, amulets, enchanted items
- Furniture: beds, tables, chairs, storage
- Potions: healing, buffing, poison, transformation
- Books: storybooks, manuals, spellbooks, maps
- Food: meat, bread, fruit, vegetables, magical foods
- Materials: wood, stone, metal, gems, rare magical substances
- Artifacts: legendary, magical, historical, unique items
- Containers: bags, chests, barrels, magical containers
- Decorations: paintings, statues, banners
- Vehicles: carts, boats, mounts, flying machines
- Instruments: musical, measuring, magical devices
- Keys, scrolls, and other special items
- Hierarchies allow for subtypes, rarity tiers, and specialization

## Crafting System Details
- Crafting stations: forges, alchemy labs, enchanting tables, workbenches
- Required tools and environmental effects (e.g., temperature, magic field)
- Recipe discovery via experimentation, books, NPCs, or events
- Crafting proficiency progression: specialization unlocks advanced recipes
- Failure outcomes: wasted materials, flawed items, accidental magical effects

### Crafting Proficiency Progression (Concrete)

**Proficiency Levels:**
- Novice (0-99 XP), Apprentice (100-299), Journeyman (300-599), Expert (600-999), Master (1000+)
- Each tier unlocks new recipes and reduces failure rates.

**XP Gain Formula:**
- Base XP per craft: `ΔXP = baseXP * recipeRarity * qualityBonus * toolBonus`
  - `baseXP`: default 10 for simple recipes, 20 for medium, 50 for complex
  - `recipeRarity`: 1.0 for common, 1.5 for uncommon, 2.0 for rare, 3.0 for legendary
  - `qualityBonus`: 1.0 for flawed/failed, 1.2 for standard, 1.5 for high quality, 2.0 for masterwork
  - `toolBonus`: 1.0 for basic tools, 1.2 for fine tools, 1.5 for masterwork tools

**Soft Caps & Diminishing Returns:**
- XP gain decreases for recipes below current tier: crafting Novice recipes at Journeyman tier gives only 50% XP.
- Progression curve uses logarithmic scaling: XP required for next tier = `currentTier * 100 * (1 + log(currentTier+1))`
- Example: Novice→Apprentice = 100 XP, Apprentice→Journeyman = ~200 XP, Journeyman→Expert = ~330 XP.

**Specialization:**
- Characters can specialize in crafting categories (smithing, alchemy, enchanting, etc.). Specialization grants +20% XP in that category and unlocks exclusive recipes.
- Max specializations: 2 per character (to encourage trade and cooperation).

**Failure & Experimentation:**
- Failure chance = `max(0, (recipeDifficulty - proficiencyLevel) / 10)`, clamped to 0..1.
- Experimentation (no recipe): higher failure chance but grants discovery XP and chance to unlock new recipes.
- Failures award partial XP (e.g., 25% of success XP) to encourage learning from mistakes.

**Example XP Calculation:**
- Craft a rare sword (baseXP=20, rarity=2.0, quality=high 1.5, tool=fine 1.2): `ΔXP = 20 * 2.0 * 1.5 * 1.2 = 72 XP`

Notes:
- Link to `docs/design_decisions.md` for canonical proficiency tier thresholds and specialization limits.
- See `docs/characters_stats_traits_skills.md` for how traits (e.g., Fast Learner) modify XP gain rates.

## Magic System Expansion
- Example rune combinations: fire+attack+area = fireball; heal+water+range = healing rain
- Spell learning via study, experience, or magical events
- Spell memorization and mastery: frequent use increases reliability
- Enchanting and imbuing objects with spells, dispelling magic from items
- Spell backlash: random negative effects if stability is low

## Object Evolution & Legacy
- Objects evolve through usage, exposure to magic, or story events
- Legacy transfer: inheritance, theft, destruction, or magical rituals
- Legendary items gain unique powers and story-driven effects
- Evolution tracked by usage stats, event triggers, and magical influence

### Legacy Effects: Triggering Conditions & Persistence (concrete rules)

**Triggering Conditions:**
- **Story linkage**: objects tied to stories (via `historyReferenceId`) automatically gain legacy potential. Legacy effects trigger when:
  - Story reaches key milestones (e.g., hero's sword used in decisive battle)
  - Object changes ownership via significant event (theft, inheritance, conquest)
  - Object exposed to rare regional features (magic zones, ancient ruins)
- **Usage thresholds**: objects gain evolution points via usage. Example: weapon gains +1 evolution point per 100 successful hits. At thresholds (e.g., 500, 1000, 2000 usage), roll for legacy effect.
- **Event-driven**: specific world events (e.g., volcanic eruption, magical storm) can imbue nearby objects with legacy effects.

**Legacy Effect Types:**
- **Stat bonuses**: +damage, +durability, +enchantment slots
- **Special abilities**: unique powers (e.g., "Dragonslayer: +50% damage vs dragons")
- **Curses**: negative effects that balance power (e.g., "Bloodthirsty: wielder gains aggression debuff")
- **Story triggers**: possessing the item unlocks quests or alters NPC reactions

**Persistence Rules:**
- Objects with `historyReferenceId` always store full legacy state (evolution points, active effects, trigger history).
- Objects without story linkage store legacy state only if evolutionPoints > threshold (default 100) to save storage.
- Legacy state includes: `evolutionPoints`, `legacyEffects: list`, `triggerHistory: list of event IDs`, `lastUpdatedTick`.

**Balancing (soft caps):**
- Evolution points capped at 10,000 (legendary tier). Further usage provides diminishing XP.
- Legacy effects stack but with diminishing returns: first effect full strength, second effect 75% strength, third 50%, etc.
- Curses automatically applied if power exceeds balance threshold (e.g., >3 major bonuses triggers a curse roll).

Notes:
- Link to `docs/design_decisions.md` for canonical evolution thresholds and balancing curves.
- See `docs/stories_events.md` for how story milestones trigger legacy effects.


## Durability & Restoration
- Objects degrade with use, damage, or magical backlash
- Repair requires materials, skill checks, and sometimes special conditions
- Restoration can improve or alter object stats, unlock hidden abilities

## Story Integration
- Stories linked to objects/structures via worldgen, events, or discovery
- Discovering a story can unlock new abilities, legacy effects, or quests
- NPCs and books can reveal object histories and trigger legacy events

## Edge Case Handling
- Multiple stories linked to one object: prioritize by relevance, allow layered effects
- Object loss: destroyed items may leave behind remnants or trigger events
- Duplication: unique items flagged to prevent exploits, legacy effects tied to instance

## Modularity & Extensibility
- Guidelines for adding new magic types, crafting recipes, object categories, and legacy systems
- Support for modding/custom object definitions via config files or scripting

## Open Questions

- Magic <> Stats linkage: define whether magic uses a dedicated mana pool per-character or a shared resource (stamina-like). Which stat is the primary casting stat (suggested default: Intelligence) and how does it scale derived `maxMana`? See `docs/data_models.md` for derivedStats fields.
- Casting/backlash formula: formalize P_backlash = g(stability, castingStat, spellComplexity). What is the scalar range and examples for balancing (e.g., stability 0..100, castingStat 0..100).
- Crafting proficiency progression: exact XP curves, per-tier soft caps, and XP sources (recipes crafted, successful quality, experimentation). Provide example formula (e.g., ΔXP = base * skillMultiplier * rarityFactor).
- Recipe discovery vs unlocking: rules for experimentation, recipe books, and NPC teaching. How do discovery chances scale with tools and environment?
- Legacy persistence: should evolving objects always store evolution state, or only if tied to stories? If storage is limited, what minimum evolution data must be kept to reproduce behavior after reload?
- Enchantment storage & serialization: how are complex enchantments represented in `Item.properties` (structured JSON, proto)? Define schema or link to `docs/persistence_versioning.md` for choices.
- Balancing controls: define soft caps and diminishing returns for item evolution to avoid runaway power growth. Reference `docs/design_decisions.md` for balancing approach.

Add unresolved items to `docs/open_questions.md` and assign owners/priorities.

## Design Decisions

For canonical design decisions, see **`docs/design_decisions.md`**. Key decisions for objects/crafting/legacy:

- Objects only track history if tied to stories (to save storage).
- Evolving objects balanced like characters (soft caps, diminishing returns).
- Magic uses mana pools; castingStat (default Intelligence) determines power and backlash chance.
- Crafting proficiency uses XP curves with soft caps and specialization limits.

Refer to `docs/design_decisions.md` and doc-specific sections above for full details.
