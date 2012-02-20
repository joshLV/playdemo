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
    public static final String MIDDLE = "middle";
    public static final String LARGE = "large";
    public static final String TINY = "tiny";

    private static final String IMAGE_ROOT = "/nfs/images"; //缩略图根目录
    private static final Pattern targetImagePattern = Pattern.compile("^([^_]+)_([a-z0-9]+).(jpg|png|gif|jpeg)$");

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
        String imageSizeType = "";
        if (imageName.contains(TINY)) {
            width = 60;
            height = 36;
            imageSizeType = TINY;
        } else if (imageName.contains(SMALL)) {
            width = 75;
            height = 45;
            imageSizeType = SMALL;
        } else if (imageName.contains(MIDDLE)) {
            width = 150;
            height = 90;
            imageSizeType = MIDDLE;
        } else if (imageName.contains(LARGE)) {
            width = 300;
            height = 180;
            imageSizeType = LARGE;
        } else {
            notFound();
        }

        Matcher matcher = targetImagePattern.matcher(imageName);

        if (!matcher.matches()) {
            notFound();
        }

        String targetImagePath = IMAGE_ROOT + File.separator + "p" + File.separator + firstDir + File.separator + secondDir + File.separator + thirdDir + File.separator + imageName;
        String originImagePath = IMAGE_ROOT + File.separator + "o" + File.separator + firstDir + File.separator + secondDir + File.separator + thirdDir + File.separator + matcher.group(1) + "." + matcher.group(3);


        System.out.println("targetImagePath:" + targetImagePath);
        System.out.println("originImagePath:" + originImagePath);

        File targetImage = new File(targetImagePath);

        if (!targetImage.exists()) {
            File originImage = new File(originImagePath);
            if (!originImage.exists()) {
                //访问的原始文件不存在时直接返回默认图片的相应规格的图片
                originImage = new File(IMAGE_ROOT + "/o/1/1/1/default.png");
                targetImage = new File(IMAGE_ROOT + "/p/1/1/1/default_" + imageSizeType + ".png");
                if (targetImage.exists()){
                    renderBinary(targetImage);
                }
            }
            //创建缩略图
            play.libs.Images.resize(originImage, targetImage, width, height);
        }
        renderBinary(targetImage);
    }

}