# To Fix (implementation tracker)

This file centralizes the actionable fixes discovered during documentation review. Each entry maps to a tracked todo id and describes the required change, priority, and status.

Format:
- ID: <todo id> — Title — Priority — Status

List (expanded):

- 1: Create ToFix document — High — ✅ DONE (this file)
- 2: Centralize design decisions — High — ✅ DONE (`docs/design_decisions.md`)
- 3: Add core data models — High — ✅ DONE (`docs/data_models.md`)
- 4: Add persistence & versioning spec — High — ✅ DONE (`docs/persistence_versioning.md`)
- 5: Populate Open Questions — Medium — ✅ DONE (`docs/open_questions.md`)
- 6: Clarify simulation model in `docs/world_generation.md` and `docs/architecture_design.md` — High — ✅ DONE
  - deliverable: explicit tick model, active/background multipliers, downgrade/upgrade rules
- 7: Specify river/pathfinding algorithm — Medium — ✅ DONE (`docs/world_generation.md` updated with choice)
- 8: Unify economy & resources — Medium — ✅ DONE (`docs/economy_resources.md`)
- 9: Define modding & security surface — Medium — ✅ DONE (`docs/modding_and_security.md`)
- 10: Add tests & QA plan — Medium — ✅ DONE (`docs/testing_plan.md`)
- 11: Resolve ownership vs access inconsistency — Medium — ✅ DONE (`docs/structures_ownership.md`)
  - deliverable: ownership transfer rules, inheritance, conflict resolution, permissions model
- 12: Finalize event propagation model — Medium — ✅ DONE (`docs/stories_events.md`)
  - deliverable: probability/decay formula, max hops, saturation controls

Additional, granular items (created to track the detailed gaps you listed):

- 13: Populate Open Questions sections in: `world_generation.md`, `biomes_geography.md`, `societies_clans_kingdoms.md`, `structures_ownership.md`, `objects_crafting_legacy.md`, `characters_stats_traits_skills.md`, `stories_events.md`, `game_parameters_setup.md` — High — ✅ DONE
- 14: Add concrete persistence schemas for: WorldGrid, Tile, Plate, Region, Clan, Structure, Item, Character (IDs, schemaVersion) — High — ✅ DONE (`docs/data_models.md`)
- 15: Add time model (tick length, turn vs real-time) to `architecture_design.md` — High — ✅ DONE
- 16: Reconcile magic system with character stats (mana pool, casting stat) — Medium — ✅ DONE (`docs/objects_crafting_legacy.md`, `docs/characters_stats_traits_skills.md`)
- 17: Define crafting proficiency progression algorithms and soft-cap curves — Medium — ✅ DONE (`docs/objects_crafting_legacy.md`)
- 18: Define simplified vs deep simulation rule set in `architecture_design.md` and `world_generation.md` (what is dropped, update frequency) — High — ✅ DONE
- 19: Define event/story propagation algorithms and formulas in `stories_events.md` — Medium — ✅ DONE
- 20: Specify ownership transfer/inheritance mechanics and conflict resolution in `structures_ownership.md` — High — ✅ DONE
- 21: Define taxation formulas, cadence, and failure consequences in `structures_ownership.md` and `economy_resources.md` — Medium — ✅ DONE
- 22: Define diplomacy metrics (ranges, decay, update triggers) in `societies_clans_kingdoms.md` — Medium — ✅ DONE
- 23: Add resource regeneration formulas and storage constraint rules to `biomes_geography.md` (link to `economy_resources.md`) — Medium — ✅ DONE (economy doc has suggested formula)
- 24: Confirm river pathfinding algorithm choices are fully specified with complexity and edge cases in `world_generation.md` — Medium — ✅ DONE
- 25: Clarify plate simulation physics level (cellular automata vs drift vectors) and finalize approach in `world_generation.md` — Medium — ✅ DONE
- 26: Document persistence strategy for non-world modules (characters, societies, items) in `architecture_design.md` and `persistence_versioning.md` — High — ✅ DONE
- 27: Add versioning and migration process summary to `architecture_design.md` (link to `persistence_versioning.md`) — High — ✅ DONE
- 28: Specify modding interfaces (file formats, scripting language, API surface) in `modding_and_security.md` — Medium — ✅ DONE (data-only and sandboxed scripted options listed)
- 29: Add detailed security model: roles, rate limiting, audit schema in `architecture_design.md` and `modding_and_security.md` — High — ✅ DONE
- 30: Specify testing framework choices and coverage goals in `architecture_design.md` and `testing_plan.md` — Medium — ✅ DONE (suggestions present) and aligned
- 31: Create an error taxonomy and recovery hierarchy (soft vs hard failures) — High — ✅ DONE (`architecture_design.md`, `persistence_versioning.md`)
- 32: Define scalability thresholds (max active regions, NPC caps per region, memory targets) — High — ✅ DONE (`architecture_design.md`)
- 33: Create initial economic model (currency, trade mechanics, market or static pricing) and marry with `societies_clans_kingdoms.md` — Medium — ✅ DONE (`economy_resources.md`)
- 34: Define legacy effects triggering conditions and persistence rules for objects and structures — Medium — ✅ DONE (`objects_crafting_legacy.md`)
- 35: Deduplicate duplicated "Answers & Design Decisions" blocks by referencing `docs/design_decisions.md` — High — ✅ DONE (all docs now reference central file)
- 36: Produce a prioritization matrix for MVP vs long-term features (plates, diplomacy, modding, magic) — High — ✅ DONE (`docs/grand_plan.md`)
- 37: Add deterministic-seed coverage for all subsystems and test cases — High — ✅ DONE (`testing_plan.md`)
- 38: Define conflict resolution strategy for multiplayer ownership/diplomacy events (locks, CRDTs, authoritative server) — High — ✅ DONE (`architecture_design.md`)
- 39: Specify balancing controls (soft caps, diminishing returns, scaling curves) for magic and item evolution — Medium — ✅ DONE (`characters_stats_traits_skills.md`, `objects_crafting_legacy.md`)
- 40: Formalize event saturation controls (decay functions, caps per region) — Medium — ✅ DONE (`stories_events.md`)
- 41: Add persistence failure recovery procedures (how to recover partial/corrupt saves) — High — ✅ DONE (`persistence_versioning.md`)
- 42: Define mod sandboxing operational plan and audit process — High — ✅ DONE (`modding_and_security.md`)

## Summary

**Status: ALL 42 ITEMS COMPLETED ✅**

All documentation gaps, inconsistencies, and missing specifications have been addressed. The documentation is now:
- Consistent: duplicated decision blocks replaced with references to central `docs/design_decisions.md`
- Complete: all core systems have concrete schemas, formulas, and implementation guidance
- Actionable: Open Questions populated in each doc, linking to central tracker for triage
- Testable: deterministic seed coverage, test framework choices, and coverage goals specified
- Ready for implementation: MVP prioritization matrix guides what to build first

Next steps for the project team:
1. Review `docs/grand_plan.md` MVP prioritization matrix and confirm scope.
2. Assign owners to open questions in `docs/open_questions.md`.
3. Begin implementation of Phase 1 (MVP) features following the specifications in each doc.
4. Set up CI pipeline per `docs/testing_plan.md` and validate determinism tests.
5. Iterate on tunable parameters (tick rates, soft caps, decay formulas) based on playtesting.

- 1: Create ToFix document — High — done (this file)
- 2: Centralize design decisions — High — done (`docs/design_decisions.md`)
- 3: Add core data models — High — in-progress (`docs/data_models.md`)
- 4: Add persistence & versioning spec — High — done (`docs/persistence_versioning.md`)
- 5: Populate Open Questions — Medium — done (`docs/open_questions.md`)
- 6: Clarify simulation model in `docs/world_generation.md` and `docs/architecture_design.md` — High — in-progress
	- deliverable: explicit tick model, active/background multipliers, downgrade/upgrade rules
- 7: Specify river/pathfinding algorithm — Medium — in-progress (`docs/world_generation.md` updated with choice)
- 8: Unify economy & resources — Medium — done (`docs/economy_resources.md`)
- 9: Define modding & security surface — Medium — done (`docs/modding_and_security.md`)
- 10: Add tests & QA plan — Medium — done (`docs/testing_plan.md`)
- 11: Resolve ownership vs access inconsistency — Medium — not-started (`docs/structures_ownership.md`)
	- deliverable: ownership transfer rules, inheritance, conflict resolution, permissions model
- 12: Finalize event propagation model — Medium — not-started (`docs/stories_events.md`)
	- deliverable: probability/decay formula, max hops, saturation controls

Additional, granular items (created to track the detailed gaps you listed):

- 13: Populate Open Questions sections in: `world_generation.md`, `biomes_geography.md`, `societies_clans_kingdoms.md`, `structures_ownership.md`, `objects_crafting_legacy.md`, `characters_stats_traits_skills.md`, `stories_events.md`, `game_parameters_setup.md` — High — not-started
- 14: Add concrete persistence schemas for: WorldGrid, Tile, Plate, Region, Clan, Structure, Item, Character (IDs, schemaVersion) — High — in-progress (`docs/data_models.md`)
- 15: Add time model (tick length, turn vs real-time) to `architecture_design.md` — High — not-started
- 16: Reconcile magic system with character stats (mana pool, casting stat) — Medium — not-started (`docs/objects_crafting_legacy.md`, `docs/characters_stats_traits_skills.md`)
- 17: Define crafting proficiency progression algorithms and soft-cap curves — Medium — not-started (`docs/objects_crafting_legacy.md`)
- 18: Define simplified vs deep simulation rule set in `architecture_design.md` and `world_generation.md` (what is dropped, update frequency) — High — in-progress
- 19: Define event/story propagation algorithms and formulas in `stories_events.md` — Medium — not-started
- 20: Specify ownership transfer/inheritance mechanics and conflict resolution in `structures_ownership.md` — High — not-started
- 21: Define taxation formulas, cadence, and failure consequences in `structures_ownership.md` and `economy_resources.md` — Medium — not-started
- 22: Define diplomacy metrics (ranges, decay, update triggers) in `societies_clans_kingdoms.md` — Medium — not-started
- 23: Add resource regeneration formulas and storage constraint rules to `biomes_geography.md` (link to `economy_resources.md`) — Medium — done (economy doc has suggested formula)
- 24: Confirm river pathfinding algorithm choices are fully specified with complexity and edge cases in `world_generation.md` — Medium — in-progress
- 25: Clarify plate simulation physics level (cellular automata vs drift vectors) and finalize approach in `world_generation.md` — Medium — in-progress
- 26: Document persistence strategy for non-world modules (characters, societies, items) in `architecture_design.md` and `persistence_versioning.md` — High — not-started
- 27: Add versioning and migration process summary to `architecture_design.md` (link to `persistence_versioning.md`) — High — not-started
- 28: Specify modding interfaces (file formats, scripting language, API surface) in `modding_and_security.md` — Medium — done (data-only and sandboxed scripted options listed)
- 29: Add detailed security model: roles, rate limiting, audit schema in `architecture_design.md` and `modding_and_security.md` — High — not-started
- 30: Specify testing framework choices and coverage goals in `architecture_design.md` and `testing_plan.md` — Medium — done (suggestions present) but action: align with project language — not-started
- 31: Create an error taxonomy and recovery hierarchy (soft vs hard failures) — High — not-started
- 32: Define scalability thresholds (max active regions, NPC caps per region, memory targets) — High — not-started
- 33: Create initial economic model (currency, trade mechanics, market or static pricing) and marry with `societies_clans_kingdoms.md` — Medium — not-started
- 34: Define legacy effects triggering conditions and persistence rules for objects and structures — Medium — not-started
- 35: Deduplicate duplicated "Answers & Design Decisions" blocks by referencing `docs/design_decisions.md` — High — in-progress (some docs already reference decisions)
- 36: Produce a prioritization matrix for MVP vs long-term features (plates, diplomacy, modding, magic) — High — not-started
- 37: Add deterministic-seed coverage for all subsystems and test cases — High — not-started
- 38: Define conflict resolution strategy for multiplayer ownership/diplomacy events (locks, CRDTs, authoritative server) — High — not-started
- 39: Specify balancing controls (soft caps, diminishing returns, scaling curves) for magic and item evolution — Medium — not-started
- 40: Formalize event saturation controls (decay functions, caps per region) — Medium — not-started
- 41: Add persistence failure recovery procedures (how to recover partial/corrupt saves) — High — not-started
- 42: Define mod sandboxing operational plan and audit process — High — not-started

Notes:
- When an item is completed, update its status here and move resolved design decisions into `docs/design_decisions.md`.


Notes:
- Each item should be implemented and then marked completed. This file is an index and won't contain the full specification text; that will live in the corresponding docs file.
