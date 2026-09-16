import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

public class FixImages {
    static void process(String input, String output) throws Exception {
        System.out.println("Processing: " + input);
        BufferedImage img = ImageIO.read(new File(input));
        int width = img.getWidth(), height = img.getHeight();
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        boolean[][] isBg = new boolean[width][height];
        Queue<int[]> queue = new LinkedList<>();
        for (int x = 0; x < width; x++) { queue.add(new int[]{x,0}); queue.add(new int[]{x,height-1}); }
        for (int y = 0; y < height; y++) { queue.add(new int[]{0,y}); queue.add(new int[]{width-1,y}); }
        while (!queue.isEmpty()) {
            int[] p = queue.poll(); int x=p[0],y=p[1];
            if (x<0||x>=width||y<0||y>=height||isBg[x][y]) continue;
            Color c = new Color(img.getRGB(x,y),true);
            if (c.getRed()<50 && c.getGreen()<50 && c.getBlue()<50) {
                isBg[x][y]=true;
                queue.add(new int[]{x-1,y}); queue.add(new int[]{x+1,y});
                queue.add(new int[]{x,y-1}); queue.add(new int[]{x,y+1});
            }
        }
        for (int y=0;y<height;y++) for (int x=0;x<width;x++) {
            int px = img.getRGB(x,y);
            if (isBg[x][y]) { out.setRGB(x,y,0); continue; }
            Color c = new Color(px,true);
            if (c.getRed()<100&&c.getGreen()<100&&c.getBlue()<100) {
                int b=(c.getRed()+c.getGreen()+c.getBlue())/3;
                int a=(int)((b/100.0)*255);
                out.setRGB(x,y,(a<<24)|(px&0x00FFFFFF));
            } else { out.setRGB(x,y,px); }
        }
        ImageIO.write(out,"png",new File(output));
        System.out.println("Saved: " + output);
    }
    public static void main(String[] args) throws Exception {
        String art = "C:\\Users\\Admin\\.gemini\\antigravity\\brain\\83bc9b8d-9638-461e-878c-512e085f2e94\\";
        String img = "src\\main\\resources\\web\\img\\";
        process(art+"fixed_inferno_rank_1783274994850.png", img+"boxpvp-inferno-rank.png");
        process(art+"fixed_dragon_key_1783275004335.png",   img+"boxpvp-dragon-key.png");
        System.out.println("Done!");
    }
}
