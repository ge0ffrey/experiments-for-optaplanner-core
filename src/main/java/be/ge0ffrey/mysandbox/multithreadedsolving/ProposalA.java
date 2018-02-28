package be.ge0ffrey.mysandbox.multithreadedsolving;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ProposalA {

    private static final int BUFFER_MULTIPLICATION = 30;
    private static final int PULSE_FREQUENCY = 10_000;

    public static void main(String[] args) {
        new ProposalA().runAndReturnSpeed();
    }

    private final int threadCount;
    private final int timeInMs;
    private Calculator calculator;

    private ProposalA() {
        this(4, 20_000, new Calculator());
    }

    public ProposalA(int threadCount, int timeInMs, Calculator calculator) {
        this.threadCount = threadCount;
        this.timeInMs = timeInMs;
        this.calculator = calculator;
    }

    public long runAndReturnSpeed() {
        System.out.printf("Proposal A: %,d ms, %d threads, %d - %d loop size.\n",
                timeInMs, threadCount, calculator.getLoopSizeMin(), calculator.getLoopSizeMax());
        long start = System.currentTimeMillis();
        Parent parent  = new Parent();
        List<Thread> threadList = new ArrayList<>(threadCount);
        for (int i = 0; i < threadCount; i++) {

            Child child = new Child(parent, i, calculator);
            Thread thread = new Thread(child);
            threadList.add(thread);
            thread.start();
        }
        long moveIndex = parent.runUntilMoveIndex();
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

        public final int bufferSize = threadCount * BUFFER_MULTIPLICATION;

        private BlockingQueue<Wrapper> moveQueue = new ArrayBlockingQueue<>(bufferSize);
        private WrapperQueue responseQueue = new WrapperQueue(bufferSize);

        private Random random = new Random(37);

        public Parent() {
        }

        public long runUntilMoveIndex() {
            long start = System.currentTimeMillis();
            long moveIndex = 0;
            for (int i = 0; i < bufferSize; i++) {
                int move = random.nextInt(1000);
                moveQueue.add(new Wrapper(moveIndex, Integer.toString(move)));
                moveIndex++;
            }
            StringBuilder trackRecord = new StringBuilder(10_000);
            while (true) {
                Wrapper wrapper;
                try {
                    wrapper = responseQueue.take();
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Parent thread interrupted.", e);
                }
                int score = Integer.parseInt(wrapper.move);
                if (moveIndex % PULSE_FREQUENCY == 0) {
                    trackRecord.append(score);
                    if (System.currentTimeMillis() >= start + timeInMs) {
                        // Winner winner chicken dinner
                        break;
                    }
                }
                int move = random.nextInt(1000);
                moveQueue.add(new Wrapper(moveIndex, Integer.toString(move)));
                moveIndex++;
            }
            for (int i = 0; i < threadCount; i++) {
                try {
                    moveQueue.put(new Wrapper(-1, "stop"));
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Parent thread interrupted.", e);
                }
            }
            System.out.println("  Track record: " + trackRecord);
            return moveIndex;
        }
    }

    class Child implements Runnable {

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
                Wrapper wrapper;
                try {
                    wrapper = parent.moveQueue.take();
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Child thread (" + index + ") interrupted.", e);
                }
                if (wrapper.move.equals("stop")) {
                    return;
                }
                int move = Integer.parseInt(wrapper.move);
                int response = calculator.calculateScore(random, move) % 10;
                try {
                    parent.responseQueue.put(new Wrapper(wrapper.moveIndex, Integer.toString(response)));
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Child thread (" + index + ") interrupted.", e);
                }
            }
        }

    }

    static class Wrapper {
        long moveIndex;
        String move;

        public Wrapper(long moveIndex, String move) {
            this.moveIndex = moveIndex;
            this.move = move;
        }
    }

    static class WrapperQueue {

        private BlockingQueue<Wrapper> innerQueue;
        private Map<Long, Wrapper> backlog;

        private long searchMoveIndex = -1;

        public WrapperQueue(int capacity) {
            innerQueue = new ArrayBlockingQueue<>(capacity);
            backlog = new HashMap<>(capacity);
        }

        public void put(Wrapper wrapper) throws InterruptedException {
            innerQueue.put(wrapper);
        }

        public Wrapper take() throws InterruptedException {
            searchMoveIndex++;
            if (!backlog.isEmpty()) {
                Wrapper wrapper = backlog.remove(searchMoveIndex);
                if (wrapper != null) {
                    return wrapper;
                }
            }
            while (true) {
                Wrapper wrapper = innerQueue.take();
                if (wrapper.moveIndex == searchMoveIndex) {
                    return wrapper;
                } else {
                    backlog.put(wrapper.moveIndex, wrapper);
                }
            }
        }
    }

}
