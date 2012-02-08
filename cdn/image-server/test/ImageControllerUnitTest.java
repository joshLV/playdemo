import org.junit.Test;
import play.libs.Images;
import play.test.UnitTest;

import java.io.File;

/**
 * .
 * User: sujie
 * Date: 2/8/12
 * Time: 1:54 PM
 */
public class ImageControllerUnitTest extends UnitTest{

    @Test
    public void testShowImage() throws  Exception{
        File toFile = new File("/home/sujie/origin_100x100.jpg");
        File fromFile = new File("/home/sujie/origin.jpg");
        Images.resize(fromFile,toFile,100,100);
    }
    
}
