package org.adventure;

import org.adventure.world.WorldGen;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WorldGenTest {

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
}
