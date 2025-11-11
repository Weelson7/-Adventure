# Game.java Documentation

**Package:** `org.adventure`  
**Type:** CLI Application (Main Entry Point)  
**Status:** Prototype — MVP Phase 1  
**Last Updated:** November 11, 2025

---

## Overview

`Game.java` is the **primary CLI entry point** for the !Adventure game prototype. It provides a text-based interface for generating procedural worlds, viewing them as ASCII maps, sampling elevation data interactively, and exporting world data to JSON format.

This class serves as a **demonstration harness** for the world generation subsystem and will evolve into a full multiplayer server/client architecture in later MVP phases.

---

## Purpose & Responsibilities

### Primary Functions
1. **World Generation CLI** — Accept command-line parameters to configure world size, seed, and output
2. **ASCII Visualization** — Render generated elevation maps as ASCII art for quick visual inspection
3. **Interactive Sampling** — Provide a REPL-like interface to query elevation at specific coordinates
4. **JSON Export** — Write generated world chunks to JSON files for persistence validation

### Design Goals
- **Simplicity:** Minimal dependencies, easy to run standalone
- **Determinism:** Same seed produces same output (validates `WorldGen` determinism)
- **Extensibility:** Foundation for future server/client architecture

---

## Class Structure

### Main Method Signature
```java
public static void main(String[] args) throws IOException
```

**Parameters:**
- `args` — Command-line arguments (see CLI Options below)

**Throws:**
- `IOException` — If JSON output file cannot be written

**Flow:**
1. Parse CLI arguments (width, height, seed, flags)
2. Create `WorldGen` instance with specified dimensions
3. Generate world with seed
4. Print ASCII representation to stdout
5. Optionally write JSON to file
6. Optionally enter interactive sampling mode

---

## CLI Options

### Flags

| Flag | Argument | Default | Description |
|------|----------|---------|-------------|
| `--width` | `N` | `40` | World width in tiles (columns) |
| `--height` | `N` | `20` | World height in tiles (rows) |
| `--seed` | `S` | `System.currentTimeMillis()` | Random seed for deterministic generation |
| `--interactive` | — | `false` | Enter interactive sampling mode after generation |
| `--out` | `file` | `null` | Write chunk JSON to specified file |
| `--help` / `-h` | — | — | Print usage and exit |

### Usage Examples

**Basic world generation:**
```bash
java -cp target\adventure-0.1.0-SNAPSHOT.jar org.adventure.Game
```

**Custom dimensions and seed:**
```bash
java -cp target\adventure-0.1.0-SNAPSHOT.jar org.adventure.Game --width 128 --height 64 --seed 42
```

**Export to JSON:**
```bash
java -cp target\adventure-0.1.0-SNAPSHOT.jar org.adventure.Game --width 60 --height 30 --seed 12345 --out world.json
```

**Interactive sampling mode:**
```bash
java -cp target\adventure-0.1.0-SNAPSHOT.jar org.adventure.Game --interactive
```

**Using Maven exec plugin:**
```bash
mvn exec:java -Dexec.args="--width 80 --height 40 --seed 999"
```

---

## Methods

### `printAscii(WorldGen wg)`

**Purpose:** Render the generated world as ASCII art to stdout.

**Parameters:**
- `wg` — The `WorldGen` instance containing elevation data

**Logic:**
1. Iterate through all tiles (row-by-row)
2. Map elevation values to ASCII characters via `mapChar(e)`
3. Build string row-by-row and print

**Output Format:**
- Each tile is represented by a single character
- Characters are chosen based on elevation thresholds

---

### `mapChar(double e)`

**Purpose:** Map elevation value to an ASCII character for visualization.

**Parameters:**
- `e` — Elevation value (normalized 0.0 to 1.0)

**Returns:** `char` — ASCII character representing terrain type

**Mapping Table:**

| Elevation Range | Character | Terrain Type |
|----------------|-----------|--------------|
| `< 0.2` | `~` | Water (ocean/lake) |
| `0.2 - 0.4` | `,` | Beach/shore |
| `0.4 - 0.7` | `"` | Grass/plains |
| `0.7 - 0.9` | `^` | Hills |
| `≥ 0.9` | `M` | Mountains |

**Example Output:**
```
~~~~~~~~~~,,,"""""""^^^^MMMM^^^^"""",,,~~~~~~~~
~~~,,,,,,""""""""^^^^^^MMMMM^^^""""""",,,,~~~~~
```

**Design Notes:**
- Character choices optimize readability in terminal/console
- Thresholds are tuned for visual balance (can be adjusted)
- Future: support color ANSI codes for enhanced visualization

---

### `runInteractive(WorldGen wg)`

**Purpose:** Provide an interactive REPL for sampling world elevation data.

**Parameters:**
- `wg` — The `WorldGen` instance to query

**User Commands:**
- `x y` — Sample elevation at coordinates (x, y)
- `quit` / `exit` — Exit interactive mode

**Flow:**
1. Display prompt (`>`)
2. Read user input line
3. Parse coordinates (space-separated integers)
4. Validate bounds (0 ≤ x < width, 0 ≤ y < height)
5. Print elevation value formatted to 4 decimal places
6. Repeat until quit command

**Example Session:**
```
Interactive mode. Type 'x y' to sample elevation, or 'quit' to exit.
> 10 5
elevation=0.6543
> 50 25
elevation=0.2103
> quit
Bye
```

**Error Handling:**
- Out-of-bounds coordinates → "Out of bounds" message
- Invalid input format → "Invalid input. Use: x y" message

---

### `printUsageAndExit()`

**Purpose:** Print usage information and exit program.

**Behavior:**
- Prints command-line syntax and options to stdout
- Calls `System.exit(0)` (terminates JVM)

**Output:**
```
Usage: java -cp target/adventure-0.1.0-SNAPSHOT.jar org.adventure.Game [--width N] [--height N] [--seed S] [--out file] [--interactive]
```

---

## Dependencies

### Internal
- `org.adventure.world.WorldGen` — Core world generation engine

### External (JDK)
- `java.io.File` — File I/O for JSON export
- `java.io.IOException` — Exception handling for file operations
- `java.util.Locale` — Locale-independent formatting (POSIX compliance)
- `java.util.Scanner` — Interactive input reading

---

## Testing Strategy

### Unit Tests
- **Argument Parsing:** Validate all CLI flags are parsed correctly (not yet implemented)
- **Character Mapping:** Test `mapChar()` boundary conditions (0.0, 1.0, thresholds)
- **Interactive Input:** Mock `System.in` to test REPL commands

### Integration Tests
- **End-to-End CLI:** Run with various argument combinations, validate output
- **Determinism Validation:** Run twice with same seed, compare stdout (should match)
- **JSON Export:** Generate world, write JSON, validate file exists and is valid JSON

### Manual Testing
```bash
# Test default parameters
mvn exec:java

# Test custom seed determinism (run twice, compare)
mvn exec:java -Dexec.args="--seed 42" > out1.txt
mvn exec:java -Dexec.args="--seed 42" > out2.txt
diff out1.txt out2.txt  # Should be identical

# Test JSON export
mvn exec:java -Dexec.args="--seed 999 --out test.json"
# Validate test.json is valid JSON and contains expected fields
```

---

## Known Issues & Limitations

### Current Limitations
1. **No Color Support:** ASCII output is monochrome (consider ANSI color codes)
2. **Fixed Character Set:** Terrain characters are hardcoded (future: configurable themes)
3. **No Error Recovery:** Invalid args crash program (add graceful error handling)
4. **Single-Threaded:** Generation blocks until complete (future: async progress indicator)

### Future Enhancements (Post-MVP)
- Add `--load <file>` flag to reload saved worlds
- Support `--format <ascii|json|png>` for multiple output formats
- Add progress bar for large world generation (>512x512)
- Implement `--compare <file1> <file2>` to diff two worlds
- Add `--benchmark` mode to profile generation performance

---

## Related Files

### Source Files
- `org.adventure.world.WorldGen` — World generation engine (doc: [WorldGen.md](world/WorldGen.md))
- `org.adventure.world.RandomUtil` — Deterministic RNG utilities (doc: [RandomUtil.md](world/RandomUtil.md))

### Test Files
- `org.adventure.WorldGenTest` — Determinism validation tests (doc: [../../test/java/org/adventure/WorldGenTest.md](../../../test/java/org/adventure/WorldGenTest.md))

### Documentation
- [BUILD.md](../../../../../BUILD.md) — Build guide with CLI examples
- [docs/world_generation.md](../../../../../docs/world_generation.md) — World generation design
- [docs/testing_plan.md](../../../../../docs/testing_plan.md) — Determinism test strategy

---

## Version History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 0.1.0 | 2025-11-11 | AI Assistant | Initial prototype with ASCII viewer and interactive mode |

---

## Notes

- This class uses `Locale.ROOT` for consistent number formatting across locales
- Scanner is closed in `runInteractive()` to prevent resource leaks
- JSON export delegates to `WorldGen.writeChunkJson()` (persistence responsibility separation)
- Future: migrate to a proper CLI framework (e.g., picocli) for robust argument parsing

---

**Status:** ✅ Functional — Ready for MVP Phase 1 prototyping  
**Next Steps:** Add unit tests for CLI argument parsing and character mapping
