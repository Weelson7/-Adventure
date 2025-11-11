# Modding and Security

This file defines the initial modding surface and security recommendations.

Modding surface
- Data-only mods: JSON/YAML files that add/alter biomes, features, item prototypes. Preferred for MVP.
- Scripted mods: sandboxed scripting (Lua or WebAssembly) for deterministic gameplay hooks. Only enable after a robust sandboxing plan.

File formats & API
- Use JSON schema for data mods; validate on load.
- Expose generation hooks with clear inputs/outputs and RNG seed passed in so mods remain deterministic when needed.

Sandboxing & security
- Never run untrusted code with host process privileges.
- For scripted mods, prefer WebAssembly (WASM) with a deterministic runtime and limited host bindings (no network, limited filesystem access).
- Rate-limit and resource-limit mod execution (CPU, memory, wall time). Log mod actions and enable safe-mode rollbacks.

Permissions & roles
- Role types: admin, moderator, developer, player.
- Use capability-based permissions for file uploads and mod activation.

Notes
- Start with data-only mods for community involvement; revisit scripted mods after building migration, backup, and audit tooling.

### Mod Sandboxing Operational Plan (checklist & audit process)

**Pre-Deployment Checklist (for enabling scripted mods):**
1. ✅ Implement WASM runtime with resource limits (CPU time, memory, no network access).
2. ✅ Define whitelist of host-callable functions (e.g., getTileData, logMessage).
3. ✅ Build mod signature verification system (cryptographic signing by trusted authors).
4. ✅ Create mod approval pipeline: automated scans + manual review for risky operations.
5. ✅ Deploy isolated test environment for mod validation before production.
6. ✅ Document mod API surface and security constraints in developer guides.

**Operational Audit Process:**
1. All mod uploads logged with: uploaderID, timestamp, mod hash, approval status.
2. Automated static analysis: scan mod bytecode for forbidden operations (filesystem writes, network calls).
3. Manual review (for high-privilege mods): security team reviews source and approves/rejects.
4. Staging deployment: run mod in isolated sandbox on test world for 24 hours; monitor for crashes, exploits.
5. Production approval: requires sign-off from 2 reviewers + automated checks passing.
6. Post-deployment monitoring: track mod execution time, resource usage, error rates. Auto-disable if thresholds exceeded.

**Rollback & Incident Response:**
- If mod causes crashes or exploits, immediately disable (server-side kill switch).
- Rollback to last-known-good save if mod corrupted world state.
- Investigate using audit logs; ban uploaderID if malicious intent confirmed.
- Notify community and publish incident report (transparency).

**Safe-Mode for Players:**
- Players can opt into "safe mode" that disables all scripted mods and uses only vanilla game logic.
- Servers can enforce safe-mode globally until mod system matures.

Notes:
- This plan assumes scripted mods are post-MVP feature. For MVP, restrict to data-only mods (JSON/YAML config files).
- Link to `docs/design_decisions.md` for mod enablement policy and risk assessment.

