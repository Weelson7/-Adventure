# Open Questions

Track unresolved design questions here. Assign an owner and priority when possible.

1. Simulation depth: What exact data is dropped in simplified region updates? (owner: TBD) — High
2. Magic & stats: Should magic zones directly alter base stats or only provide status effects? (owner: design) — Medium
3. Crafting progression: Formula for proficiency XP and soft caps — Medium
4. Economy: Currency model, trade mechanics, and taxation cadence — High
5. Modding: Which scripting options are supported (data-only vs code plugins)? Sandbox policy — Medium
6. Ownership conflicts: How to resolve simultaneous ownership claims and rollbacks — High
7. Max active regions: Upper bound for simultaneous active regions for performance planning — Medium
8. Event saturation algorithm: concrete decay formula for story probability — Medium
9. Persistence format: JSON vs protobuf for runtime state — Low (depends on performance testing)

Notes:
- Move resolved items into `docs/design_decisions.md` and mark them closed here.
