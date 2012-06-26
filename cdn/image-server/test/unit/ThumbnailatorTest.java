package unit;

import net.coobird.thumbnailator.Thumbnails;
import org.junit.Ignore;
import org.junit.Test;
import play.test.UnitTest;

import java.io.File;
import java.io.IOException;

/**
 * @author likang
 */
@Ignore
public class ThumbnailatorTest extends UnitTest{
    @Test
    public void testThumbnail() throws IOException{
        String originalPath = "/Users/likang/Downloads/image_6.jpg";
        String targetPath = "/Users/likang/Downloads/image_6_thumb.jpg";

        Thumbnails.of(new File(originalPath))
                .size(600, 200)
                .outputQuality(0.99f)
                .toFile(new File(targetPath));
    }
}
