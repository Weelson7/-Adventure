# WorldGenTest.java Documentation

**Package:** `org.adventure`  
**Type:** JUnit 5 Test Class  
**Test Target:** `org.adventure.world.WorldGen`  
**Status:** MVP Phase 1 — Active  
**Last Updated:** November 11, 2025

---

## Overview

`WorldGenTest.java` is a **JUnit 5 test suite** that validates the deterministic behavior of the `WorldGen` class. It ensures that world generation produces consistent, reproducible results across runs — a critical requirement for multiplayer synchronization, save/load integrity, and regression testing.

This test class implements the **deterministic-seed coverage** strategy outlined in `docs/testing_plan.md`.

---

## Purpose & Responsibilities

### Primary Functions
1. **Determinism Validation** — Verify same seed produces identical worlds (checksum comparison)
2. **Persistence Smoke Test** — Validate JSON serialization doesn't crash (basic sanity check)
3. **Regression Detection** — Detect unintentional changes to generation algorithm

### Design Goals
- **Fast Execution:** Use small world sizes (128×128) for quick CI runs
- **Clarity:** Test names clearly describe what is being validated
- **Isolation:** Each test is independent (no shared state between tests)
- **Coverage:** Exercise core generation path (MVP Phase 1 baseline)

---

## Test Cases

### `deterministicGenerationProducesSameChecksum()`

**Purpose:** Validate that `WorldGen.generate()` produces identical results for the same seed.

**Test Strategy:**
1. Create two independent `WorldGen` instances with same dimensions
2. Generate worlds using same seed
3. Compute checksums for both worlds
4. Assert checksums are identical

**Code:**
```java
@Test
public void deterministicGenerationProducesSameChecksum() throws Exception {
    int w = 128, h = 128;
    long seed = 123456789L;

    WorldGen g1 = new WorldGen(w, h);
    g1.generate(seed);
    String c1 = g1.checksum();

    WorldGen g2 = new WorldGen(w, h);
    g2.generate(seed);
    String c2 = g2.checksum();

    assertEquals(c1, c2, "Checksums must match for the same seed");

    // write to a temp file to validate persistence path (sanity)
    File tmp = new File(System.getProperty("java.io.tmpdir"), "chunk_test.json");
    g1.writeChunkJson(tmp);
    tmp.deleteOnExit();
}
```

**Test Data:**
- **Dimensions:** 128×128 tiles (small enough for fast execution, large enough to catch issues)
- **Seed:** `123456789L` (arbitrary large value, good bit distribution)

**Assertions:**
- **Primary:** `assertEquals(c1, c2, ...)` — Checksums must match
- **Secondary:** `writeChunkJson()` completes without exception (persistence smoke test)

**Failure Modes:**
- **Non-Deterministic RNG:** If `RandomUtil.valueNoise()` uses unseeded random, checksums differ
- **Floating-Point Instability:** If computation order changes (e.g., parallelization), rounding errors accumulate
- **Algorithm Change:** If noise formula changes, checksums diverge (expected, update test)

**Expected Behavior:**
- ✅ **Pass:** Checksums are identical (40-60 character Base64 string)
- ❌ **Fail:** Checksums differ → generation is non-deterministic (bug)

**Performance:**
- **Execution Time:** ~100-200 ms (two 128×128 generations + checksums)
- **CI Impact:** Negligible (runs in <1 second, no external dependencies)

---

## Test Coverage

### What Is Tested
- ✅ Core generation path (`WorldGen.generate()`)
- ✅ Checksum computation (`WorldGen.checksum()`)
- ✅ JSON serialization (`WorldGen.writeChunkJson()`)
- ✅ Determinism across independent instances

### What Is NOT Tested (Future Enhancements)
- ❌ **Elevation Range Validation:** Values are in [0.0, 1.0] (add bounds test)
- ❌ **Different Seeds:** Verify different seeds produce different checksums (uniqueness test)
- ❌ **Large Worlds:** Test 512×512, 1024×1024 performance (benchmark test)
- ❌ **Edge Cases:** Zero dimensions, negative dimensions, max dimensions (validation test)
- ❌ **Persistence Round-Trip:** Write JSON → read back → verify data integrity (integration test)
- ❌ **Concurrency:** Parallel generation produces correct results (stress test)

---

## Test Data & Fixtures

### Fixed Test Parameters
```java
int w = 128, h = 128;          // Small world for fast execution
long seed = 123456789L;        // Arbitrary seed with good bit distribution
```

**Rationale:**
- 128×128 = 16,384 tiles — large enough to catch spatial correlation issues
- Small enough to execute in <100 ms (CI-friendly)
- Seed `123456789L` has good bit distribution (avoids low-entropy seeds like `1L`)

### Temporary Files
```java
File tmp = new File(System.getProperty("java.io.tmpdir"), "chunk_test.json");
tmp.deleteOnExit();
```

**Design Notes:**
- Uses system temp directory (portable across OSes)
- `deleteOnExit()` ensures cleanup (even if test fails)
- Future: use JUnit `@TempDir` annotation for automatic cleanup

---

## Integration with CI/CD

### GitHub Actions Workflow
This test runs automatically on every PR via `.github/workflows/ci.yml`:

```yaml
- name: Build and test with Maven
  run: mvn -B -U clean test
```

**CI Behavior:**
- Runs on push to `main` and all pull requests
- Fails PR if test fails (blocks merge)
- Cached Maven dependencies for speed

### Local Execution
```bash
# Run all tests
mvn test

# Run only this test class
mvn test -Dtest=WorldGenTest

# Run with verbose output
mvn test -Dtest=WorldGenTest -X
```

---

## Determinism Testing Strategy

### Golden Seed Registry (Future Enhancement)

**Concept:** Store known-good checksums for specific seeds to detect regressions.

**Implementation:**
```java
private static final Map<Long, String> GOLDEN_CHECKSUMS = Map.of(
    123456789L, "expectedChecksumHere",
    999L, "anotherExpectedChecksum",
    42L, "yetAnotherChecksum"
);

@Test
public void regressionTestAgainstGoldenSeeds() {
    for (Map.Entry<Long, String> entry : GOLDEN_CHECKSUMS.entrySet()) {
        WorldGen wg = new WorldGen(128, 128);
        wg.generate(entry.getKey());
        assertEquals(entry.getValue(), wg.checksum(),
            "Checksum changed for seed " + entry.getKey());
    }
}
```

**Benefits:**
- Detect unintentional algorithm changes
- Protect against regression bugs
- Document expected output for specific seeds

**Maintenance:**
- Update golden checksums when algorithm intentionally changes
- Store checksums in version control (track changes over time)

---

## Known Issues & Limitations

### Current Limitations
1. **No Bounds Validation:** Doesn't test that elevation values are in [0.0, 1.0]
2. **Single Seed Test:** Only tests one seed (should test multiple for confidence)
3. **No Uniqueness Check:** Doesn't verify different seeds produce different checksums
4. **No Performance Benchmark:** No assertion on execution time (should be <10 sec for 512×512)
5. **Incomplete Persistence Test:** Writes JSON but doesn't read it back

### Future Enhancements
- Add `@ParameterizedTest` with multiple seeds
- Add bounds validation for elevation array
- Add performance benchmark with `@Timeout` annotation
- Add round-trip persistence test (write → read → compare)
- Add concurrency test (parallel generation)

---

## Related Tests (To Be Added)

### Phase 1.1 Tests
- `BiomeAssignmentTest` — Validate biome logic
- `RiverPathfindingTest` — Validate rivers flow downhill
- `FeaturePlacementTest` — Validate volcano/magic zone placement

### Phase 1.8 Tests
- `PersistenceRoundTripTest` — Write/read/compare cycle
- `MigrationTest` — Test schema v1 → v2 migration
- `BackupRestoreTest` — Test backup rotation and restore

---

## Dependencies

### Test Framework
- **JUnit 5 (Jupiter)** — `org.junit.jupiter:junit-jupiter:5.9.3`
  - `@Test` annotation
  - `Assertions.assertEquals()` static import

### Test Target
- **WorldGen** — `org.adventure.world.WorldGen` (production class under test)

### JDK Libraries
- `java.io.File` — Temporary file creation

---

## Version History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 0.1.0 | 2025-11-11 | AI Assistant | Initial determinism test |

---

## Test Execution Examples

### Success Output
```
[INFO] Running org.adventure.WorldGenTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.263 s - in org.adventure.WorldGenTest
[INFO] BUILD SUCCESS
```

### Failure Output (Example)
```
[ERROR] Failures:
[ERROR]   WorldGenTest.deterministicGenerationProducesSameChecksum:21 
    Checksums must match for the same seed
    Expected: dGVzdENoZWNrc3VtMQ==
    Actual:   dGVzdENoZWNrc3VtMg==
[INFO] BUILD FAILURE
```

---

## Best Practices

### When to Update This Test
1. **Algorithm Changes:** If noise formula or layer weights change, regenerate expected checksums
2. **Bug Fixes:** If fixing a determinism bug, add regression test with specific seed
3. **Performance Changes:** If optimizing generation, ensure checksums remain unchanged

### How to Debug Failures
1. **Print Checksums:** Log actual checksums to console
2. **Visual Inspection:** Use `Game.java` CLI to render worlds with failing seeds
3. **Diff Elevation Arrays:** Compare elevation values tile-by-tile to find divergence point
4. **Bisect Changes:** Use git bisect to find commit that broke determinism

---

## Notes

- Test uses JUnit 5 (Jupiter) — ensure IDE/Maven supports JUnit 5
- Temporary file cleanup relies on JVM shutdown (use `@TempDir` for more robust cleanup)
- Checksum comparison is case-sensitive (Base64 encoding)
- Future: consider adding visual diff output (render both worlds as ASCII and compare)

---

**Status:** ✅ Active — Validates determinism for MVP Phase 1  
**Next Steps:** Add golden seed registry and bounds validation tests
