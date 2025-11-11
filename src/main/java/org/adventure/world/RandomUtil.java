package org.adventure.world;

import java.util.Random;

public final class RandomUtil {
    private RandomUtil() {}

    // Simple coordinate-based deterministic value noise using seed mixing
    public static double valueNoise(long seed, int x, int y) {
        long mix = seed ^ (((long)x << 32) | (y & 0xffffffffL));
        Random r = new Random(mix);
        return r.nextDouble();
    }
}
