import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

public class ImageTransparency {
    public static void main(String[] args) throws Exception {
        String dir = "src/main/resources/web/img";
        File folder = new File(dir);
        File[] files = folder.listFiles((dir1, name) -> name.startsWith("boxpvp-") && name.endsWith(".png"));
        
        if (files == null) {
            System.out.println("No files found.");
            return;
        }

        for (File file : files) {
            System.out.println("Processing " + file.getName());
            BufferedImage img = ImageIO.read(file);
            BufferedImage outImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
            
            int bgColor = img.getRGB(0, 0); 
            Color bg = new Color(bgColor, true);
            
            // We use a flood fill algorithm from the corners to remove the background
            boolean[][] visited = new boolean[img.getWidth()][img.getHeight()];
            Queue<int[]> queue = new LinkedList<>();
            
            // Start flood fill from corners
            queue.add(new int[]{0, 0});
            queue.add(new int[]{img.getWidth()-1, 0});
            queue.add(new int[]{0, img.getHeight()-1});
            queue.add(new int[]{img.getWidth()-1, img.getHeight()-1});
            
            while (!queue.isEmpty()) {
                int[] p = queue.poll();
                int x = p[0], y = p[1];
                if (x < 0 || x >= img.getWidth() || y < 0 || y >= img.getHeight()) continue;
                if (visited[x][y]) continue;
                
                Color c = new Color(img.getRGB(x, y), true);
                int rDiff = Math.abs(c.getRed() - bg.getRed());
                int gDiff = Math.abs(c.getGreen() - bg.getGreen());
                int bDiff = Math.abs(c.getBlue() - bg.getBlue());
                
                if (rDiff < 30 && gDiff < 30 && bDiff < 30) {
                    visited[x][y] = true;
                    // Add neighbors
                    queue.add(new int[]{x-1, y});
                    queue.add(new int[]{x+1, y});
                    queue.add(new int[]{x, y-1});
                    queue.add(new int[]{x, y+1});
                }
            }
            
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int pixel = img.getRGB(x, y);
                    if (visited[x][y]) {
                        outImg.setRGB(x, y, 0x00FFFFFF); // Transparent
                    } else {
                        outImg.setRGB(x, y, pixel);
                    }
                }
            }
            
            ImageIO.write(outImg, "png", file);
            System.out.println("Saved " + file.getName());
        }
    }
}
