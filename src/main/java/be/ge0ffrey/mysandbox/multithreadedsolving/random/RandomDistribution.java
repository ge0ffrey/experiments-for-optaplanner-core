package be.ge0ffrey.mysandbox.multithreadedsolving.random;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

public class RandomDistribution {

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 1000;
    private static final int IMAGE_SIZE = WIDTH * HEIGHT;
    private static final int GENERATION_SIZE = IMAGE_SIZE * 1;

    private static final int THREAD_COUNT = 32;

    private static int[][] countRoster;
    private static int maximumCount = 0;

    public static void main(String[] args) {
        generateSingleThreadedRandomDistribution();
        drawImage("Single threaded random distribution");
        generateMultiThreadedRandomDistribution();
        drawImage("Multithreaded random distribution");
    }

    private static void generateSingleThreadedRandomDistribution() {
        countRoster = new int[WIDTH][HEIGHT];
        Random random = new Random();
        for (int i = 0; i < GENERATION_SIZE; i++) {
            int randomInt = random.nextInt(IMAGE_SIZE);
            int count = countRoster[randomInt / WIDTH][randomInt % WIDTH];
            count++;
            if (count > maximumCount) {
                maximumCount = count;
            }
            countRoster[randomInt / WIDTH][randomInt % WIDTH] = count;
        }
    }

    private static void generateMultiThreadedRandomDistribution() {
        countRoster = new int[WIDTH][HEIGHT];
        Random globalRandom = new Random();
        Random[] randoms = new Random[THREAD_COUNT];
        for (int i = 0; i < randoms.length; i++) {
            randoms[i] = new Random(globalRandom.nextLong());
        }
        for (int i = 0; i < GENERATION_SIZE; i++) {
            Random random = randoms[i % THREAD_COUNT];
            int randomInt = random.nextInt(IMAGE_SIZE);
            int count = countRoster[randomInt / WIDTH][randomInt % WIDTH];
            count++;
            if (count > maximumCount) {
                maximumCount = count;
            }
            countRoster[randomInt / WIDTH][randomInt % WIDTH] = count;
        }
    }

    private static void drawImage(String title) {
        Color[] colors = new Color[256];
        for (int i = 0; i < colors.length; i++) {
            colors[255 - i] = new Color(i, i, i);
        }

        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.setColor(Color.RED);
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                int count = countRoster[i][j];
                g.setColor(colors[255 * count / maximumCount]);
                g.fillRect(i, j, 1, 1);
            }
        }
        JFrame frame = new JFrame(title);
        frame.setContentPane(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}
