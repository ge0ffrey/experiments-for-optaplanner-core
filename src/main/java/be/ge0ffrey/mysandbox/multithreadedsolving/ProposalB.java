package be.ge0ffrey.mysandbox.multithreadedsolving;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ProposalB {

    private static final int PULSE_FREQUENCY = 10_000;

    public static void main(String[] args) {
        new ProposalB().runAndReturnSpeed();
    }

    private final int threadCount;
    private final int timeInMs;
    private Calculator calculator;

    private ProposalB() {
        this(4, 20_000, new Calculator());
    }

    public ProposalB(int threadCount, int timeInMs, Calculator calculator) {
        this.threadCount = threadCount;
        this.timeInMs = timeInMs;
        this.calculator = calculator;
    }

    public long runAndReturnSpeed() {
        System.out.printf("Proposal B: %,d ms, %d threads, %d - %d loop size.\n",
                timeInMs, threadCount, calculator.getLoopSizeMin(), calculator.getLoopSizeMax());
        long start = System.currentTimeMillis();
        List<Child> childList = new ArrayList<>(threadCount);
        Parent parent  = new Parent(childList);
        List<Thread> threadList = new ArrayList<>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            Child child = new Child(parent, i, calculator);
            childList.add(child);
            Thread thread = new Thread(child);
            threadList.add(thread);
            thread.start();
        }
        int moveIndex = parent.runUntilMoveIndex();
        for (Thread thread : threadList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new IllegalStateException("Join interrupted.", e);
            }
        }
        long duration = System.currentTimeMillis() - start;
        long speed = moveIndex * 1000 / duration;
        System.out.printf("  Duration (%,d ms), speed (%,d/second).\n", duration, speed);
        return speed;
    }

    class Parent {

        public final int bufferSize = threadCount * 3;

        private Random random = new Random(37);

        private List<Child> childList;

        public Parent(List<Child> childList) {
            this.childList = childList;
        }

        public int runUntilMoveIndex() {
            long start = System.currentTimeMillis();
            int moveIndex = 0;
            for (int i = 0; i < bufferSize; i++) {
                int move = random.nextInt(1000);
                Child child = childList.get(moveIndex % childList.size());
                child.moveQueue.add(Integer.toString(move));
                moveIndex++;
            }
            StringBuilder trackRecord = new StringBuilder(10_000);
            while (true) {
                Child child = childList.get(moveIndex % childList.size());
                int score;
                try {
                    score = Integer.parseInt(child.responseQueue.take());
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Parent thread interrupted.", e);
                }
                if (moveIndex % PULSE_FREQUENCY == 0) {
                    trackRecord.append(score);
                    if (System.currentTimeMillis() >= start + timeInMs) {
                        // Winner winner chicken dinner
                        break;
                    }
                }
                int move = random.nextInt(1000);
                child.moveQueue.add(Integer.toString(move));
                moveIndex++;
            }
            for (int i = 0; i < threadCount; i++) {
                Child child = childList.get(i % childList.size());
                try {
                    child.moveQueue.put("stop");
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Parent thread interrupted.", e);
                }
            }
            System.out.println("  Track record: " + trackRecord);
            return moveIndex;
        }
    }

    class Child implements Runnable {

        private BlockingQueue<String> moveQueue = new ArrayBlockingQueue<>(3);
        private BlockingQueue<String> responseQueue = new ArrayBlockingQueue<>(3);

        private Parent parent;
        private int index;
        private Calculator calculator;
        private Random random;

        public Child(Parent parent, int index, Calculator calculator) {
            this.parent = parent;
            this.index = index;
            this.calculator = calculator;
            random = new Random(index);
        }

        public void run() {
            while (true) {
                String moveString;
                try {
                    moveString = moveQueue.take();
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Child thread (" + index + ") interrupted.", e);
                }
                if (moveString.equals("stop")) {
                    return;
                }
                int move = Integer.parseInt(moveString);
                int response = calculator.calculateScore(random, move) % 10;
                try {
                    responseQueue.put(Integer.toString(response));
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Child thread (" + index + ") interrupted.", e);
                }
            }
        }

    }

}
