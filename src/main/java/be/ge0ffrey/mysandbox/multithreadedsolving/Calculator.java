package be.ge0ffrey.mysandbox.multithreadedsolving;

import java.util.Random;

public class Calculator {

    // TODO try 3_000, 30_000 and 300_000
    private static final int LOOP_SIZE_MIN = 10_000;
    private static final int LOOP_SIZE_MAX = 30_000;
    private static final int LOOP_SIZE_DIFF = LOOP_SIZE_MAX - LOOP_SIZE_MIN;

    public static int calculateScore(Random random, int move) {
        int loopSize = LOOP_SIZE_MIN + (int) (random.nextDouble() * LOOP_SIZE_DIFF);
        int score = move;
        for (int i = 0; i < loopSize; i++) {
            score += 100_000;
            score %= 1_000_000_000;
        }
        return score % 100_000;
    }

    // Used to fine tweak the LOOP_SIZE
    public static void main(String[] args) {
        Random random = new Random();
        long TIME_IN_MS = 20_000;
        long end = System.currentTimeMillis() + TIME_IN_MS;
        long calculationCount = 0;
        long blackHole = 0;
        while (System.currentTimeMillis() < end) {
            blackHole += calculateScore(random, (int) calculationCount);
            calculationCount++;
        }
        long calcCountPerSecond = calculationCount / (TIME_IN_MS / 1000);
        System.out.println("  Black hole: " + blackHole);
        System.out.println("Time run (" + (TIME_IN_MS / 1000) + "), calculation count (" + calculationCount
                + "), speed (" + calcCountPerSecond + "/sec).");
    }

}
