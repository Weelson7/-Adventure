# Open Questions (triaged)

Track unresolved design questions here. Each item below has a suggested owner, status, and link to the canonical spec when resolved.

1. Simulation depth: What exact data is dropped in simplified region updates? — High
	- Owner: `architecture`
	- Status: Resolved (see `docs/specs_summary.md#background-summarization-contract` for the canonical contract).

2. Magic & stats: Should magic zones directly alter base stats or only provide status effects? — Medium
	- Owner: `design`
	- Status: Resolved — default: magic zones produce explicit feature-based modifiers (status effects) only; they do not directly alter base stats except via explicit feature rules. See `docs/design_decisions.md` and `docs/specs_summary.md#magic-and-stats`.

3. Crafting progression: Formula for proficiency XP and soft caps — Medium
	- Owner: `objects`
	- Status: Resolved — formula and tiers captured in `docs/objects_crafting_legacy.md`; canonical thresholds and tuning values are in `docs/specs_summary.md#crafting-progression`.

4. Economy: Currency model, trade mechanics, and taxation cadence — High
	- Owner: `economy`
	- Status: Partially resolved — static currency model and tax cadence (weekly) documented in `docs/economy_resources.md`; canonical defaults (taxRate, gracePeriod, seizureThreshold) are proposed in `docs/specs_summary.md` and should be reviewed by `economy` owners.

5. Modding: Which scripting options are supported (data-only vs code plugins)? Sandbox policy — Medium
	- Owner: `modding`
	- Status: Resolved for MVP — data-only mods preferred; WASM sandboxing plan and operational checklist in `docs/modding_and_security.md`. Numeric resource caps are proposed in `docs/specs_summary.md`.

6. Ownership conflicts: How to resolve simultaneous ownership claims and rollbacks — High
	- Owner: `structures`
	- Status: Resolved — authoritative server ordering + deterministic tiebreaker documented in `docs/structures_ownership.md`. Contested expiry default added to `docs/specs_summary.md`.

7. Max active regions: Upper bound for simultaneous active regions for performance planning — Medium
	- Owner: `ops`
	- Status: Partially resolved — initial targets in `docs/architecture_design.md` (default 100). Marked in `docs/specs_summary.md` as initial estimates that require profiling.

8. Event saturation algorithm: concrete decay formula for story probability — Medium
	- Owner: `stories`
	- Status: Resolved — default decay: exponential with k=0.8 (see `docs/specs_summary.md#event-propagation`); alternative functions may be experimented with under feature flags.

9. Persistence format: JSON vs protobuf for runtime state — Low
	- Owner: `persistence`
	- Status: Partially resolved — recommendation: use JSON for initial builds and protobuf for performance-sensitive builds; migration guidance and `schemaVersion` policy are in `docs/persistence_versioning.md` and `docs/specs_summary.md`.

Notes:
- When a decision is finalized, copy the short decision into `docs/design_decisions.md` and update the status here to `Resolved`.
- If you want me to assign concrete people (GitHub handles) instead of module owners, I can update the file accordingly.
