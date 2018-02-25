package be.ge0ffrey.mysandbox;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MultithreadExperiment {

    public static final int THREAD_COUNT = 4;

    public static final int MOVE_COUNT = 100_000;

    public static void main(String[] args) {
        System.out.println("Bootstrapping...");
        long start = System.currentTimeMillis();
        MultithreadExperiment.Parent parent  = new MultithreadExperiment.Parent();
        List<Thread> threadList = new ArrayList<>(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; i++) {
            MultithreadExperiment.Child child = new MultithreadExperiment.Child(parent, i);
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
        System.out.printf("\nDuration: %,d ms\n", System.currentTimeMillis() - start);
    }

    static class Parent implements Runnable {

        private BlockingQueue<String> moveQueue = new ArrayBlockingQueue<>(MOVE_COUNT);
        private BlockingQueue<String> responseQueue = new ArrayBlockingQueue<>(MOVE_COUNT);

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
                int response = calculateScore(move);
                try {
                    parent.responseQueue.put(Integer.toString(response));
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Child thread (" + index + ") interrupted.", e);
                }
            }
        }

        public int calculateScore(int move) {
            int score = (random.nextInt(100) + move) % 10;
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new IllegalStateException("Child thread (" + index + ") interrupted.", e);
            }
            return score;
        }

    }

}