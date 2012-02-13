package controllers;

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

    public static final String SMALL = "small";
    public static final CharSequence MIDDLE = "middle";
    public static final CharSequence LARGE = "large";
    public static final String TINY = "tiny";

    private static final String IMAGE_ROOT = "/nfs/images"; //缩略图根目录
    private static final Pattern toImagePat = Pattern.compile("^([^_]+)_([a-z0-9]+).(jpg|png|gif|jpeg)$");

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
        if (imageName.contains(TINY)) {
            width = 60;
            height = 36;
        } else if (imageName.contains(SMALL)) {
            width = 75;
            height = 45;
        } else if (imageName.contains(MIDDLE)) {
            width = 150;
            height = 90;
        } else if (imageName.contains(LARGE)) {
            width = 300;
            height = 180;
        } else {
            notFound();
        }

        Matcher matcher = toImagePat.matcher(imageName);

        if (!matcher.matches()) {
            notFound();
        }

        String toImagePath = IMAGE_ROOT + File.separator + "p" + File.separator + firstDir + File.separator + secondDir + File.separator + thirdDir + File.separator + imageName;

        String originImagePath = IMAGE_ROOT + File.separator + "o" + File.separator + firstDir + File.separator + secondDir + File.separator + thirdDir + File.separator + matcher.group(1)+"."+matcher.group(3);


        File toFile = new File(toImagePath);

        System.out.println("============"+originImagePath + " " + toImagePath + " " + width + " " + height);

        if (!toFile.exists()) {
            //创建缩略图
            File originImage = new File(originImagePath);
            play.libs.Images.resize(originImage, toFile, width, height);
        }
        renderBinary(toFile);
    }

}