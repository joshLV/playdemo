package unit;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

import org.junit.Ignore;
import org.junit.Test;

import play.libs.Images;
import play.test.UnitTest;

/**
 * @author likang
 */
@Ignore
public class PlayImageTest extends UnitTest{
    @Test
    public void testResize() throws IOException{
        String originalPath = "/Users/likang/Downloads/youjia.jpg";
        String targetPath = "/Users/likang/Downloads/youjia_1.jpg";
        String targetBPath = "/Users/likang/Downloads/youjia_2.jpg";

        File original = new File(originalPath);
        File target = new File(targetPath);
        Images.resize(original, target, 340, 260, true);

        resize(new File(originalPath), new File(targetBPath), 340, 260, true);

    }



    /**
     * Resize an image
     * @param originalImage The image file
     * @param to The destination file
     * @param w The new width (or -1 to proportionally resize) or the maxWidth if keepRatio is true
     * @param h The new height (or -1 to proportionally resize) or the maxHeight if keepRatio is true
     * @param keepRatio : if true, resize will keep the original image ratio and use w and h as max dimensions
     */
    private static void resize(File originalImage, File to, int w, int h, boolean keepRatio) {
        try {
            BufferedImage source = ImageIO.read(originalImage);
            int owidth = source.getWidth();
            int oheight = source.getHeight();
            double ratio = (double) owidth / oheight;

            int maxWidth = w;
            int maxHeight = h;

            if (w < 0 && h < 0) {
                w = owidth;
                h = oheight;
            }
            if (w < 0 && h > 0) {
                w = (int) (h * ratio);
            }
            if (w > 0 && h < 0) {
                h = (int) (w / ratio);
            }

            if(keepRatio) {
                h = (int) (w / ratio);
                if(h > maxHeight) {
                    h = maxHeight;
                    w = (int) (h * ratio);
                }
                if(w > maxWidth) {
                    w = maxWidth;
                    h = (int) (w / ratio);
                }
            }

            String mimeType = "image/jpeg";
            if (to.getName().endsWith(".png")) {
                mimeType = "image/png";
            }
            if (to.getName().endsWith(".gif")) {
                mimeType = "image/gif";
            }

            // out
            BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Image srcSized = source.getScaledInstance(w, h, Image.SCALE_DEFAULT);
            Graphics graphics = dest.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, w, h);
            graphics.drawImage(srcSized, 0, 0, null);
            ImageWriter writer = ImageIO.getImageWritersByMIMEType(mimeType).next();
            ImageWriteParam params = writer.getDefaultWriteParam();
            FileImageOutputStream toFs = new FileImageOutputStream(to);
            writer.setOutput(toFs);
            IIOImage image = new IIOImage(dest, null, null);
            writer.write(null, image, params);
            toFs.flush();
            toFs.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
