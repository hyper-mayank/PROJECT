import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class RadialMask {
    public static void main(String[] args) throws Exception {
        String dir = "src/main/resources/web/img";
        File folder = new File(dir);
        File[] files = folder.listFiles((dir1, name) -> name.startsWith("boxpvp-") && name.endsWith(".png"));
        
        if (files == null) {
            System.out.println("No files found.");
            return;
        }

        // Inner radius (fully opaque) and outer radius (fully transparent)
        double innerRadius = 350.0;
        double outerRadius = 450.0;

        for (File file : files) {
            System.out.println("Applying mask to " + file.getName());
            BufferedImage img = ImageIO.read(file);
            int width = img.getWidth();
            int height = img.getHeight();
            BufferedImage outImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            
            double cx = width / 2.0;
            double cy = height / 2.0;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = img.getRGB(x, y);
                    
                    double dist = Math.sqrt(Math.pow(x - cx, 2) + Math.pow(y - cy, 2));
                    int alpha = 255;
                    
                    if (dist > outerRadius) {
                        alpha = 0;
                    } else if (dist > innerRadius) {
                        double fraction = 1.0 - ((dist - innerRadius) / (outerRadius - innerRadius));
                        alpha = (int) (255 * fraction);
                    }
                    
                    // Multiply existing alpha (if any) with our new mask alpha
                    int existingAlpha = (pixel >> 24) & 0xff;
                    int finalAlpha = (existingAlpha * alpha) / 255;
                    
                    // Preserve RGB, just update Alpha
                    int newPixel = (finalAlpha << 24) | (pixel & 0x00FFFFFF);
                    outImg.setRGB(x, y, newPixel);
                }
            }
            
            ImageIO.write(outImg, "png", file);
        }
        System.out.println("All images masked successfully.");
    }
}
