# Societies: Clans & Kingdoms

## Overview
Social structures, relationships, and diplomacy among clans and kingdoms.

## Core Concepts
- Clans form kingdoms or remain independent
- No min/max clan size; clans merge or are destroyed
- Secret relationships, hidden agendas, diplomacy system
- Loyalty based on wealth, military, relationships

## Data Structures & Relationships
- Clan: Members, assets, relationships
- Kingdom: Allied clans, ruler/vassal structure
- Diplomacy: Alliances, treaties

## Generation & Initialization
- Clans/kingdoms generated at worldgen
- Relationships initialized by affinity or leader statements

## Interactions & Edge Cases
- Diplomacy affects alliances, crises
- Secret relationships trigger events
- Edge cases: clan destruction, merging, crisis resolution

## Expansion & Modularity
- Add new diplomacy options, crisis types
- Modular society system

## Clan/Kingdom Governance
- Leadership structures: monarchy, council, democracy, tribal chiefdom
- Succession rules: hereditary, elective, merit-based, conquest
- Internal politics: factions, power struggles, decision-making processes

## Clan/Kingdom Formation & Dissolution
- Merging: alliances, marriage, conquest, shared goals
- Splitting: internal conflict, ideological differences, betrayal
- Destruction: loss of all members, defeat in war, absorption by another group
- Triggers: events, player actions, diplomatic crises

## Diplomacy System Details
- Diplomatic actions: trade, war, alliances, vassalage, treaties, espionage
- Negotiation mechanics: reputation, influence, alignment, race affinity
- Diplomatic crises: betrayal, embargo, succession disputes

### Diplomacy Metrics (concrete ranges and update rules)

Each society-to-society relationship is tracked via a `RelationshipRecord` (see `docs/data_models.md`):

**Numeric Ranges:**
- **Reputation**: -100 (hostile) to +100 (trusted ally). Affects trade willingness, alliance stability.
- **Influence**: 0 (no influence) to 100 (dominant). Represents political/economic leverage.
- **Alignment**: -100 (opposing values) to +100 (shared values). Affects cooperation willingness.
- **Race Affinity**: -50 (racial tension) to +50 (racial kinship). Modifies base interaction outcomes.

**Decay Rates (per tick or event-driven):**
- Reputation decays slowly toward neutral (0) if no interactions: `Δreputation = -sign(reputation) * 0.01 per 100 ticks` (tunable).
- Influence decays if not maintained: `Δinfluence = -0.05 per 100 ticks` unless trade/tribute active.
- Alignment shifts via events or policy changes; base decay is minimal (-0.001 per tick).
- Race affinity is mostly static but can shift via integration events.

**Update Triggers:**
- **Periodic updates**: every 100 ticks (configurable) apply decay and recompute derived metrics (e.g., alliance strength).
- **Event-driven updates**: trade agreements, wars, betrayals, gifts, diplomatic missions trigger immediate metric changes.
  - Example: successful trade mission → `Δreputation = +5`, `Δinfluence = +2`
  - Example: betrayal event → `Δreputation = -30`, trust flag set to false for N ticks

**Derived Metrics:**
- **Alliance Strength** = `(reputation + alignment) / 2`. Must be > 30 to form alliances.
- **War Likelihood** = `max(0, (-reputation - 20) / 50)`. Increases with negative reputation.

Notes:
- Link to `docs/design_decisions.md` for canonical decay rates and thresholds.
- See `docs/open_questions.md` for unresolved tuning (e.g., optimal decay rates for large vs small worlds).


## Hidden Agendas & Secret Relationships
- Formation: personal ambitions, secret pacts, rivalries
- Tracking: hidden metrics, event logs, NPC/player knowledge
- Revelation: special events, investigations, rumors
- Impact: triggers crises, alters loyalty, changes diplomatic landscape

## Loyalty & Affinity Mechanics
- Loyalty calculated from wealth, military power, relationships, shared history
- Affinity influenced by race, alignment, reputation, past interactions
- Metrics change over time and through events, affecting stability and diplomacy

## Societal Events & Crises
- Examples: civil war, succession crisis, betrayal, famine, invasion, rebellion
- Triggers: low loyalty, hidden agendas, external threats, player actions
- Resolution: negotiation, combat, diplomacy, player intervention

## Player Interaction
- Players can join, lead, or influence clans/kingdoms
- Actions: diplomacy, coups, crisis management, resource allocation
- Player-driven events: founding new clans, forging alliances, instigating crises

## Assets & Resource Management
- Clans/kingdoms manage assets: territory, structures, resources, currency
- Economic systems: taxation, trade, production, consumption
- Resource scarcity or abundance affects stability and growth

## Modularity & Extensibility
- Guidelines for adding new society types, diplomatic options, crisis events
- Support for modding/custom society definitions via config or scripting

## Open Questions

- Diplomacy metrics: define numeric ranges for reputation/influence, decay rates, and update triggers (periodic vs event-driven).
- Economy integration: how tightly should clan/kingdom treasuries tie to regional resource production vs tax income? Define formulas and cadence.
- Max/min clan size and governance implications: should there be hard limits or soft scaling penalties for very large/small societies?
- Secret relationships & revelations: who can discover secrets, what are reveal mechanics, and how are secrets persisted/revoked?
- Simultaneous diplomacy actions: define conflict resolution (transaction ordering, deterministic tiebreakers) for actions in the same tick.

Log unresolved items to `docs/open_questions.md` for owner assignment and prioritization.

## Design Decisions

For canonical design decisions, see **`docs/design_decisions.md`**. Key decisions for societies/diplomacy:

- Secret relationships revealed via special events.
- Diplomacy uses reputation, influence, alignment, and race affinity (see Diplomacy Metrics section above for ranges and decay rates).
- Societies manage treasuries, taxation, and trade income (integrated with economic model).

Refer to `docs/design_decisions.md` for authoritative policies on societies and economy.