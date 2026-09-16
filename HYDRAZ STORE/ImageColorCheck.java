import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageColorCheck {
    public static void main(String[] args) throws Exception {
        File file = new File("src/main/resources/web/img/boxpvp-common-key.png");
        BufferedImage img = ImageIO.read(file);
        
        System.out.println("Dimensions: " + img.getWidth() + "x" + img.getHeight());
        System.out.println("Top Left: " + new Color(img.getRGB(0,0), true));
        System.out.println("Top Right: " + new Color(img.getRGB(img.getWidth()-1,0), true));
        System.out.println("Bottom Left: " + new Color(img.getRGB(0,img.getHeight()-1), true));
        System.out.println("Center: " + new Color(img.getRGB(img.getWidth()/2,img.getHeight()/2), true));
        
        // Let's print a small line of pixels from 0 to 50 on X axis to see if there's a gradient
        System.out.print("Gradient check (y=0): ");
        for(int i=0; i<50; i+=5) {
            Color c = new Color(img.getRGB(i, 0), true);
            System.out.print("["+c.getRed()+","+c.getGreen()+","+c.getBlue()+","+c.getAlpha()+"] ");
        }
        System.out.println();
    }
}
