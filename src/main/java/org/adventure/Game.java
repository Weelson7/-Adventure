package org.adventure;

import org.adventure.world.WorldGen;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class Game {
    // Simple ASCII world viewer prototype
    public static void main(String[] args) throws IOException {
        int width = 40;
        int height = 20;
        long seed = System.currentTimeMillis();
        boolean interactive = false;
        File out = null;

        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if ("--width".equals(a) && i + 1 < args.length) {
                width = Integer.parseInt(args[++i]);
            } else if ("--height".equals(a) && i + 1 < args.length) {
                height = Integer.parseInt(args[++i]);
            } else if ("--seed".equals(a) && i + 1 < args.length) {
                seed = Long.parseLong(args[++i]);
            } else if ("--interactive".equals(a)) {
                interactive = true;
            } else if ("--out".equals(a) && i + 1 < args.length) {
                out = new File(args[++i]);
            } else if ("--help".equals(a) || "-h".equals(a)) {
                printUsageAndExit();
            }
        }

        System.out.printf(Locale.ROOT, "Generating world %dx%d seed=%d\n", width, height, seed);
        WorldGen wg = new WorldGen(width, height);
        wg.generate(seed);

        printAscii(wg);

        if (out != null) {
            System.out.println("Writing chunk JSON to " + out);
            wg.writeChunkJson(out);
        }

        if (interactive) {
            runInteractive(wg);
        }
    }

    private static void printAscii(WorldGen wg) {
        // map elevation to characters
        for (int y = 0; y < wg.getHeight(); y++) {
            StringBuilder sb = new StringBuilder();
            for (int x = 0; x < wg.getWidth(); x++) {
                double e = wg.getElevation(x, y);
                char c = mapChar(e);
                sb.append(c);
            }
            System.out.println(sb.toString());
        }
    }

    private static char mapChar(double e) {
        if (e < 0.2) return '~';
        if (e < 0.4) return ','; // beach
        if (e < 0.7) return '"'; // grass
        if (e < 0.9) return '^'; // hills
        return 'M'; // mountain
    }

    private static void runInteractive(WorldGen wg) {
        System.out.println("Interactive mode. Type 'x y' to sample elevation, or 'quit' to exit.");
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) break;
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) break;
            String[] parts = line.split("\\s+");
            try {
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                if (x < 0 || x >= wg.getWidth() || y < 0 || y >= wg.getHeight()) {
                    System.out.println("Out of bounds");
                } else {
                    double e = wg.getElevation(x, y);
                    System.out.printf(Locale.ROOT, "elevation=%.4f\n", e);
                }
            } catch (Exception ex) {
                System.out.println("Invalid input. Use: x y");
            }
        }
        scanner.close();
        System.out.println("Bye");
    }

    private static void printUsageAndExit() {
        System.out.println("Usage: java -cp target/adventure-0.1.0-SNAPSHOT.jar org.adventure.Game [--width N] [--height N] [--seed S] [--out file] [--interactive]");
        System.exit(0);
    }
}
