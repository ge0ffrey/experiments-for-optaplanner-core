package be.ge0ffrey.mysandbox.multithreadedsolving;

public class Calculator {

    private static final int LOOP_SIZE = 30_000;

    public static int calculateScore(int move) {
        int score = move;
        for (int i = 0; i < LOOP_SIZE; i++) {
            score += 100_000;
            score %= 1_000_000_000;
        }
        return score % 100_000;
    }

    // Used to fine tweak the LOOP_SIZE
    public static void main(String[] args) {
        long TIME_IN_MS = 20_000;
        long end = System.currentTimeMillis() + TIME_IN_MS;
        long calculationCount = 0;
        long blackHole = 0;
        while (System.currentTimeMillis() < end) {
            blackHole += calculateScore((int) calculationCount);
            calculationCount++;
        }
        long calcCountPerSecond = calculationCount / (TIME_IN_MS / 1000);
        System.out.println("  Black hole: " + blackHole);
        System.out.println("Time run (" + (TIME_IN_MS / 1000) + "), calculation count (" + calculationCount
                + "), speed (" + calcCountPerSecond + "/sec).");
    }

}
