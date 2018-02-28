package be.ge0ffrey.mysandbox.multithreadedsolving;

public class ProposalABBenchmark {

    private static final int TIME_IN_MS = 120_000;
    private static final int[] THREAD_COUNTS = {1, 2, 4, 8, 16, 32, 64};
    private static final int[][] LOOP_SIZES = {
            {3_000, 3_000},
            {30_000, 30_000},
            {300_000, 300_000},
            {30_000, 60_000},
            {30_000, 300_000},
            {30_000, 3_000_000}};

    public static void main(String[] args) {
        StringBuilder result = new StringBuilder("threadCount,loopSizeMin,loopSizeMax,speedA, speed B\n");
        for (int threadCount : THREAD_COUNTS) {
            for (int[] loopSize : LOOP_SIZES) {
                Calculator calculator = new Calculator(loopSize[0], loopSize[1]);
                long speedA = new ProposalA(threadCount, TIME_IN_MS, calculator).runAndReturnSpeed();
                long speedB = new ProposalB(threadCount, TIME_IN_MS, calculator).runAndReturnSpeed();
                result.append(threadCount).append(",").append(loopSize[0]).append(",").append(loopSize[1]).append(",").append(speedA).append(",").append(speedB).append("\n");
            }
        }
        System.out.println("");
        System.out.println("Result");
        System.out.println("======");
        System.out.println(result);
    }

}
