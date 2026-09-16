import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

public class ProcessLogo {
    public static void main(String[] args) throws Exception {
        File inputFile = new File("C:\\Users\\Admin\\.gemini\\antigravity\\brain\\83bc9b8d-9638-461e-878c-512e085f2e94\\.user_uploaded\\media_1786732517585.jpg");
        File outputFile = new File("src\\main\\resources\\web\\img\\logo.png");

        if (!inputFile.exists()) {
            System.out.println("Error: Input file does not exist: " + inputFile.getAbsolutePath());
            return;
        }

        BufferedImage img = ImageIO.read(inputFile);
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage outImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Flood fill from borders to identify outer dark background
        boolean[][] isBg = new boolean[width][height];
        Queue<int[]> queue = new LinkedList<>();

        for (int x = 0; x < width; x++) {
            queue.add(new int[]{x, 0});
            queue.add(new int[]{x, height - 1});
        }
        for (int y = 0; y < height; y++) {
            queue.add(new int[]{0, y});
            queue.add(new int[]{width - 1, y});
        }

        int threshold = 35; // Pure/near black border threshold

        while (!queue.isEmpty()) {
            int[] p = queue.poll();
            int x = p[0], y = p[1];
            if (x < 0 || x >= width || y < 0 || y >= height || isBg[x][y]) continue;

            int rgb = img.getRGB(x, y);
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;

            if (r < threshold && g < threshold && b < threshold) {
                isBg[x][y] = true;
                queue.add(new int[]{x - 1, y});
                queue.add(new int[]{x + 1, y});
                queue.add(new int[]{x, y - 1});
                queue.add(new int[]{x, y + 1});
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = img.getRGB(x, y);
                if (isBg[x][y]) {
                    outImg.setRGB(x, y, 0x00000000); // Transparent
                } else {
                    Color c = new Color(pixel, true);
                    // Smooth transition on outer boundary
                    if (c.getRed() < 55 && c.getGreen() < 55 && c.getBlue() < 55) {
                        int brightness = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
                        int alpha = (int)((brightness / 55.0) * 255);
                        outImg.setRGB(x, y, (alpha << 24) | (pixel & 0x00FFFFFF));
                    } else {
                        outImg.setRGB(x, y, pixel);
                    }
                }
            }
        }

        ImageIO.write(outImg, "png", outputFile);
        System.out.println("Successfully generated transparent logo: " + outputFile.getAbsolutePath());
    }
}
