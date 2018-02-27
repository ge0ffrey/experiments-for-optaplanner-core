package be.ge0ffrey.mysandbox.multithreadedsolving;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class FailedMultithreadExperiment {

    public static final int THREAD_COUNT = 4;

    public static final int MOVE_COUNT = 100_000;

    public static void main(String[] args) {
        System.out.println("Bootstrapping...");
        long start = System.currentTimeMillis();
        FailedMultithreadExperiment.Parent parent  = new FailedMultithreadExperiment.Parent();
        List<Thread> threadList = new ArrayList<>(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; i++) {
            FailedMultithreadExperiment.Child child = new FailedMultithreadExperiment.Child(parent, i);
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
        System.out.printf("\nDuration: %,.3f moves per second\n", MOVE_COUNT * 1000.0 / (System.currentTimeMillis() - start));
    }

    static class Parent implements Runnable {

        private BlockingQueue<String> moveQueue = new ArrayBlockingQueue<>(THREAD_COUNT * 3); // TOD FIXME
        private BlockingQueue<String> responseQueue = new ArrayBlockingQueue<>(THREAD_COUNT * 3);

        private Random random = new Random(37);

        public Parent() {
        }

        public void run() {
            for (int i = 0; i < MOVE_COUNT; i++) {
                int move = random.nextInt(1000);
                try {
                    moveQueue.put(Integer.toString(move));
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Parent thread interrupted.", e);
                }
            }
            for (int i = 0; i < THREAD_COUNT; i++) {
                moveQueue.add("stop");
            }
            for (int i = 0; i < MOVE_COUNT; i++) {
                int score;
                try {
                    score = Integer.parseInt(responseQueue.take());
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Parent thread interrupted.", e);
                }
                System.out.print(score);
            }
        }
    }

    static class Child implements Runnable {

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
                    moveString = parent.moveQueue.take();
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Child thread (" + index + ") interrupted.", e);
                }
                if (moveString.equals("stop")) {
                    return;
                }
                int move = Integer.parseInt(moveString);
                int response = Calculator.calculateScore(random, move) % 10;
                try {
                    parent.responseQueue.put(Integer.toString(response));
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Child thread (" + index + ") interrupted.", e);
                }
            }
        }

    }

}
