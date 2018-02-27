package be.ge0ffrey.mysandbox.multithreadedsolving;

import java.util.Random;

public class Calculator {

    private final int loopSizeMin;
    private final int loopSizeMax;
    private final int loopSizeDiff;

    public Calculator() {
        this(10_000, 30_000);
    }

    public Calculator(int loopSizeMin, int loopSizeMax) {
        this.loopSizeMin = loopSizeMin;
        this.loopSizeMax = loopSizeMax;
        loopSizeDiff = loopSizeMax - loopSizeMin;
    }

    public int getLoopSizeMin() {
        return loopSizeMin;
    }

    public int getLoopSizeMax() {
        return loopSizeMax;
    }

    public int calculateScore(Random random, int move) {
        // The pow 10 is to have 90% end up near loopSizeMin
        int loopSize = loopSizeMin + (int) (Math.pow(random.nextDouble(), 10) * loopSizeDiff);
        int score = move;
        for (int i = 0; i < loopSize; i++) {
            score += 100_000;
            score %= 1_000_000_000;
        }
        return score % 100_000;
    }

    // Used to fine tweak the LOOP_SIZE
    public static void main(String[] args) {
        Calculator calculator = new Calculator();
        Random random = new Random();
        long TIME_IN_MS = 20_000;
        long end = System.currentTimeMillis() + TIME_IN_MS;
        long calculationCount = 0;
        long blackHole = 0;
        while (System.currentTimeMillis() < end) {
            blackHole += calculator.calculateScore(random, (int) calculationCount);
            calculationCount++;
        }
        long calcCountPerSecond = calculationCount / (TIME_IN_MS / 1000);
        System.out.println("  Black hole: " + blackHole);
        System.out.println("Time run (" + (TIME_IN_MS / 1000) + "), calculation count (" + calculationCount
                + "), speed (" + calcCountPerSecond + "/sec).");
    }

}
