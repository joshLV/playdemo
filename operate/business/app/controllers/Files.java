package controllers;

import java.io.File;
import play.Play;
import play.mvc.Controller;

//import navigation.annotations.ActiveNavigation;

/**
 * 图片文件的访问控制.
 * <p/>
 * User: sujie
 * Date: 3/13/12
 * Time: 2:46 PM
 */
//@With(OperateRbac.class)
//@ActiveNavigation("goods_index")
public class Files extends Controller {
    public static String ROOT_PATH = Play.configuration.getProperty("upload.imagepath", "");

    public static void getImage(String path1, String path2, String path3, String path4) {
        String targetImagePath = ROOT_PATH + "/" + path1 + "/" + path2 + "/" + path3 + "/" + path4;

        File targetImage = new File(targetImagePath);
        renderBinary(targetImage);
    }
}
