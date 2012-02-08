package controllers;

import play.libs.Images;
import play.mvc.Controller;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 图片的控制.
 * User: sujie
 * Date: 2/8/12
 * Time: 11:42 AM
 */
public class ImageController extends Controller {

    private static final String IMAGE_ROOT = "/home/sujie"; //缩略图根目录
    public static final String SMALL = "small";
    public static final CharSequence MIDDLE = "middle";
    public static final CharSequence LARGE = "large";
    public static final Pattern pat = Pattern.compile("^([^_]+)_([a-z0-9]+).(jpg|png|gif|jpeg)$");

    /**
     * 根据图片路径显示指定规格的缩略图.
     *
     * @param firstDir
     * @param secondDir
     * @param thirdDir
     * @param imageName
     */
    public static void showImage(int firstDir, int secondDir, int thirdDir, String imageName) {


        int width = 0;
        int height = 0;
        if (imageName.contains(SMALL)) {
            width = 100;
            height = 100;
        } else if (imageName.contains(MIDDLE)) {
            width = 200;
            height = 200;
        } else if (imageName.contains(LARGE)) {
            width = 400;
            height = 400;
        } else {
            notFound();
        }

        Matcher matcher = pat.matcher(imageName);

        if (!matcher.matches()) {
            notFound();
        }
        String originImageName = matcher.group(0);

        String toImagePath = IMAGE_ROOT + File.separator + firstDir + File.separator + secondDir + File.separator + thirdDir + File.separator + imageName;

        String originImagePath = IMAGE_ROOT + File.separator + firstDir + File.separator + secondDir + File.separator + thirdDir + File.separator + originImageName + imageName.substring(imageName.indexOf("."));


        File toFile = new File(toImagePath);
        if (!toFile.exists()) {
            //创建缩略图
            Images.resize(new File(originImagePath), toFile, width, height);
        }
        renderBinary(toFile);
    }

}