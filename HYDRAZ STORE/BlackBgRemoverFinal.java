import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Map;
import java.util.HashMap;

public class BlackBgRemoverFinal {
    public static void main(String[] args) throws Exception {
        String artifactsDir = "C:\\Users\\Admin\\.gemini\\antigravity\\brain\\83bc9b8d-9638-461e-878c-512e085f2e94";
        String outputDir = "src\\main\\resources\\web\\img";

        Map<String, String> fileMap = new HashMap<>();
        fileMap.put("test_baron_rank_1783246597841.png",   "boxpvp-baron-rank.png");
        fileMap.put("test_inferno_rank_1783272593528.png", "boxpvp-inferno-rank.png");
        fileMap.put("test_dragon_rank_1783272623723.png",  "boxpvp-dragon-rank.png");
        fileMap.put("test_common_key_1783246607850.png",   "boxpvp-common-key.png");
        fileMap.put("test_phoenix_key_1783272634994.png",  "boxpvp-phoenix-key.png");
        fileMap.put("test_dragon_key_1783272646017.png",   "boxpvp-dragon-key.png");
        fileMap.put("test_omega_key_1783272656310.png",    "boxpvp-omega-key.png");
        fileMap.put("test_samurai_key_1783272667433.png",  "boxpvp-samurai-key.png");

        for (Map.Entry<String, String> entry : fileMap.entrySet()) {
            File inputFile = new File(artifactsDir, entry.getKey());
            File outputFile = new File(outputDir, entry.getValue());

            if (!inputFile.exists()) {
                System.out.println("SKIP (not found): " + entry.getKey());
                continue;
            }

            System.out.println("Processing " + entry.getKey() + " -> " + entry.getValue());
            BufferedImage img = ImageIO.read(inputFile);
            int width = img.getWidth();
            int height = img.getHeight();
            BufferedImage outImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            // Flood fill from all 4 borders to mark background pixels
            boolean[][] isBg = new boolean[width][height];
            Queue<int[]> queue = new LinkedList<>();
            for (int x = 0; x < width; x++) { queue.add(new int[]{x, 0}); queue.add(new int[]{x, height - 1}); }
            for (int y = 0; y < height; y++) { queue.add(new int[]{0, y}); queue.add(new int[]{width - 1, y}); }

            while (!queue.isEmpty()) {
                int[] p = queue.poll();
                int x = p[0], y = p[1];
                if (x < 0 || x >= width || y < 0 || y >= height || isBg[x][y]) continue;
                Color c = new Color(img.getRGB(x, y), true);
                // Mark as background if very dark (black background)
                if (c.getRed() < 40 && c.getGreen() < 40 && c.getBlue() < 40) {
                    isBg[x][y] = true;
                    queue.add(new int[]{x - 1, y}); queue.add(new int[]{x + 1, y});
                    queue.add(new int[]{x, y - 1}); queue.add(new int[]{x, y + 1});
                }
            }

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = img.getRGB(x, y);
                    if (isBg[x][y]) {
                        outImg.setRGB(x, y, 0x00000000); // fully transparent
                    } else {
                        Color c = new Color(pixel, true);
                        // Smooth dark edges
                        if (c.getRed() < 80 && c.getGreen() < 80 && c.getBlue() < 80) {
                            int brightness = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
                            int alpha = (int)((brightness / 80.0) * 255);
                            outImg.setRGB(x, y, (alpha << 24) | (pixel & 0x00FFFFFF));
                        } else {
                            outImg.setRGB(x, y, pixel);
                        }
                    }
                }
            }

            ImageIO.write(outImg, "png", outputFile);
            System.out.println("  Saved -> " + outputFile.getName());
        }

        System.out.println("All done!");
    }
}
