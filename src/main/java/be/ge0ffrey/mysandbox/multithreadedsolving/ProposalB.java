package be.ge0ffrey.mysandbox.multithreadedsolving;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ProposalB {

    public static final int THREAD_COUNT = 4;

    public static final int TIME_IN_MS = 20_000;
    public static final int PULSE_FREQUENCY = 10_000;

    public static void main(String[] args) {
        System.out.println("Proposal B\n");
        System.out.printf("Running for %,d ms...\n", TIME_IN_MS);
        long start = System.currentTimeMillis();
        List<Child> childList = new ArrayList<>(THREAD_COUNT);
        Parent parent  = new Parent(childList);
        List<Thread> threadList = new ArrayList<>(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; i++) {
            Child child = new Child(parent, i);
            childList.add(child);
            Thread thread = new Thread(child);
            threadList.add(thread);
            thread.start();
        }
        parent.run();
        for (Thread thread : threadList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new IllegalStateException("Join interrupted.", e);
            }
        }
        System.out.printf("Duration (%,d ms).\n", (System.currentTimeMillis() - start));
    }

    static class Parent implements Runnable {

        public static final int BUFFER_SIZE = THREAD_COUNT * 3;

        private Random random = new Random(37);

        private List<Child> childList;

        public Parent(List<Child> childList) {
            this.childList = childList;
        }

        public void run() {
            long start = System.currentTimeMillis();
            int moveIndex = 0;
            for (int i = 0; i < BUFFER_SIZE; i++) {
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
                    if (System.currentTimeMillis() >= start + TIME_IN_MS) {
                        // Winner winner chicken dinner
                        break;
                    }
                }
                int move = random.nextInt(1000);
                child.moveQueue.add(Integer.toString(move));
                moveIndex++;
            }
            for (int i = 0; i < THREAD_COUNT; i++) {
                Child child = childList.get(i % childList.size());
                child.moveQueue.add("stop");
            }
            long duration = System.currentTimeMillis() - start;
            System.out.println("Track record: " + trackRecord);
            System.out.printf("Duration (%,d ms), speed (%,d/second).\n", duration, moveIndex * 1000 / duration);
        }
    }

    static class Child implements Runnable {

        private BlockingQueue<String> moveQueue = new ArrayBlockingQueue<>(3);
        private BlockingQueue<String> responseQueue = new ArrayBlockingQueue<>(3);

        private Parent parent;
        private int index;
        private Random random;

        public Child(Parent parent, int index) {
            this.parent = parent;
            this.index = index;
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
                int response = Calculator.calculateScore(random, move) % 10;
                try {
                    responseQueue.put(Integer.toString(response));
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Child thread (" + index + ") interrupted.", e);
                }
            }
        }

    }

}
