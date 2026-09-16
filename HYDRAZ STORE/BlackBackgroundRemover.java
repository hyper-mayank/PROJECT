import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

public class BlackBackgroundRemover {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: java BlackBackgroundRemover <directory>");
            return;
        }
        
        File folder = new File(args[0]);
        File[] files = folder.listFiles((dir, name) -> name.startsWith("test_") && name.endsWith(".png"));
        
        if (files == null || files.length == 0) {
            System.out.println("No test files found.");
            return;
        }

        for (File file : files) {
            System.out.println("Processing " + file.getName());
            BufferedImage img = ImageIO.read(file);
            int width = img.getWidth();
            int height = img.getHeight();
            BufferedImage outImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            
            boolean[][] visited = new boolean[width][height];
            Queue<int[]> queue = new LinkedList<>();
            
            // Start from borders
            for (int x = 0; x < width; x++) {
                queue.add(new int[]{x, 0});
                queue.add(new int[]{x, height - 1});
            }
            for (int y = 0; y < height; y++) {
                queue.add(new int[]{0, y});
                queue.add(new int[]{width - 1, y});
            }
            
            // Flood fill
            while (!queue.isEmpty()) {
                int[] p = queue.poll();
                int x = p[0], y = p[1];
                
                if (x < 0 || x >= width || y < 0 || y >= height) continue;
                if (visited[x][y]) continue;
                
                int pixel = img.getRGB(x, y);
                Color c = new Color(pixel, true);
                
                // If it's very dark (almost black)
                if (c.getRed() < 40 && c.getGreen() < 40 && c.getBlue() < 40) {
                    visited[x][y] = true;
                    queue.add(new int[]{x-1, y});
                    queue.add(new int[]{x+1, y});
                    queue.add(new int[]{x, y-1});
                    queue.add(new int[]{x, y+1});
                }
            }
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = img.getRGB(x, y);
                    if (visited[x][y]) {
                        outImg.setRGB(x, y, 0x00000000); // Fully transparent
                    } else {
                        // Blend black edges to transparent for anti-aliasing
                        Color c = new Color(pixel, true);
                        if (c.getRed() < 80 && c.getGreen() < 80 && c.getBlue() < 80) {
                            // Dark edge pixel, reduce alpha based on brightness
                            int brightness = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
                            int alpha = (int)((brightness / 80.0) * 255);
                            int newPixel = (alpha << 24) | (pixel & 0x00FFFFFF);
                            outImg.setRGB(x, y, newPixel);
                        } else {
                            outImg.setRGB(x, y, pixel);
                        }
                    }
                }
            }
            
            ImageIO.write(outImg, "png", file);
            System.out.println("Saved " + file.getName());
        }
    }
}
