# Testing & QA Plan

Goals
- Ensure generation determinism, detect regressions, and validate core invariants (no lakes in oceans, rivers flow downhill, schema migrations succeed).

Framework
- Use the project's language-native test framework (unit tests + integration tests). Keep generation tests lightweight and deterministic by seeding RNG.

### Testing Framework & Coverage Goals (concrete choices)

**Framework Choice:**
- **Java**: JUnit 5 + Mockito for unit tests, TestContainers for integration tests requiring databases or services.
- **Python**: pytest + unittest.mock for unit tests, pytest-docker for integration.
- **JavaScript/TypeScript**: Jest or Mocha + Chai for unit tests, Supertest for API integration tests.
- (Adapt based on actual project language; document choice in `docs/architecture_design.md` once finalized.)

**Coverage Goals:**
- **Unit tests**: 70% line coverage for core modules (world generation, region simulation, persistence, character/society management).
- **Critical modules**: 85%+ coverage for persistence, migration scripts, security, and conflict resolution logic.
- **Integration tests**: cover key workflows (world generation → region activation → character interaction → save/load cycle).

**Deterministic-Seed Coverage:**
- Every procedural generation system (plates, rivers, features, stories, NPCs) must accept a seed parameter.
- Test: run generation twice with same seed and assert output checksums match (validates determinism).
- Add regression tests for known edge cases (e.g., plateau river resolution, closed-basin lakes).

**Test Data:**
- Provide sample seeds and small worlds (128x128, 4 altitude layers) for fast test execution.
- Store golden checksums for known seeds to detect regressions.

**CI Integration:**
- Run unit tests on every PR (fast, <5 min).
- Run integration tests and map diff validation nightly (slower, ~30 min).
- Fail builds if coverage drops below target thresholds.

Notes:
- Update this section once project language is finalized.
- Link to `docs/architecture_design.md` for CI pipeline configuration and deployment strategy.

## Deterministic-Seed Coverage & Testing

**Goal**: Ensure all procedural generation and simulation systems honor deterministic seeds so worlds and behaviors are reproducible.

**Requirements:**
- Every system that uses randomness (world generation, NPC spawning, event triggers, loot drops, combat rolls) must accept a `seed` parameter or use a seeded RNG instance.
- Seeded RNG must be passed explicitly; avoid global unseeded random calls (e.g., `Math.random()` in JS, `random.random()` in Python without seeding).

**Test Coverage:**
1. **World Generation**: Run worldgen twice with same seed and parameters. Assert that:
   - Plate boundaries match (checksum of plate map).
   - Elevation, temperature, moisture maps are identical (tile-by-tile comparison or checksum).
   - River paths are identical.
   - Regional feature placements match (volcano/magic zone coordinates).

2. **Region Simulation**: Activate a region, run N ticks, save state. Reload and re-run from same tick with same seed. Assert:
   - NPC positions, stats, inventories match.
   - Event queue is identical.
   - Resource node states match.

3. **Event/Story Propagation**: Trigger an event with a seed, let it propagate. Re-run with same seed and assert propagation paths and probabilities match.

4. **Combat & Randomized Mechanics**: Run a simulated combat encounter with seeded RNG. Re-run and assert damage rolls, hit/miss outcomes, and loot drops are identical.

**Implementation Notes:**
- Use a seedable PRNG library (e.g., `java.util.Random(seed)`, `numpy.random.default_rng(seed)`, `seedrandom` for JS).
- Store seed used for each test case; log seed on failure for reproducibility.
- Add regression tests for known edge cases discovered during playtesting (e.g., plateau river bugs, closed-basin lake generation).

**Continuous Monitoring:**
- Add a daily CI job that runs determinism validation tests on a set of golden seeds.
- If checksums diverge, flag as regression and block merges until fixed.

Notes:
- Link to `docs/design_decisions.md` for seed management policy and RNG library choices.
- See `docs/world_generation.md` for seed usage in plate/river/feature generation.



Suggested tests
- Generation determinism: run world generation with same seed and parameters twice and compare checksums of essential outputs (plate map hashes, elevation map checksums).
- River pathfinding: unit tests for edge cases (plateaus, closed basins), and regression tests ensuring no uphill segments.
- Persistence migrations: migration scripts tested against sample old-version payloads.

CI
- Run core tests on each PR; run heavier integration tests nightly.

Test data
- Provide minimal sample seeds and small worlds (e.g., 128x128) that are fast to generate.

Notes
- Add visual diff tooling for map outputs (optional) to make debugging easier for artists/designers.
