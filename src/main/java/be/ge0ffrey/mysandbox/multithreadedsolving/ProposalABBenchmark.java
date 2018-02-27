package be.ge0ffrey.mysandbox.multithreadedsolving;

public class ProposalABBenchmark {

    private static final int TIME_IN_MS = 120_000;
    private static final int[] THREAD_COUNTS = {1, 2, 4, 8, 16, 18};
    private static final int[][] LOOP_SIZES = {
            {3_000, 3_000},
            {30_000, 30_000},
            {300_000, 300_000},
            {30_000, 60_000},
            {30_000, 300_000},
            {30_000, 3_000_000}};

    public static void main(String[] args) {
        StringBuilder resultA = new StringBuilder("threadCount,loopSizeMin,loopSizeMax,speed");
        StringBuilder resultB = new StringBuilder("threadCount,loopSizeMin,loopSizeMax,speed");
        for (int threadCount : THREAD_COUNTS) {
            for (int[] loopSize : LOOP_SIZES) {
                Calculator calculator = new Calculator(loopSize[0], loopSize[1]);
                long speedA = new ProposalA(threadCount, TIME_IN_MS, calculator).runAndReturnSpeed();
                resultA.append(threadCount).append(",").append(loopSize[0]).append(",").append(loopSize[1]).append(",").append(speedA);
                long speedB = new ProposalB(threadCount, TIME_IN_MS, calculator).runAndReturnSpeed();
                resultB.append(threadCount).append(",").append(loopSize[0]).append(",").append(loopSize[1]).append(",").append(speedB);
            }
        }
        System.out.println("");
        System.out.println("Result A");
        System.out.println("========");
        System.out.println(resultA);
        System.out.println("Result B");
        System.out.println("========");
        System.out.println(resultB);
    }

}
