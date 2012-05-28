package controllers;

import play.Logger;
import play.Play;
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
public class Images extends Controller {

    public static final String SMALL = "small";
    public static final String MIDDLE = "middle";
    public static final String LARGE = "large";
    public static final String TINY = "tiny";
    public static final String LOGO = "logo";
    
    public static final String SLIDE = "slide";

    private static final Pattern targetImagePattern = Pattern.compile("^([^_]+)_([a-z0-9]+).(jpg|png|gif|jpeg)$");
    private static final String IMAGE_ROOT_ORIGINAL = play.Play
            .configuration.getProperty("image.root.original",
                    "/nfs/images/o"); //原始图根目录
    private static final String IMAGE_ROOT_GENERATED = play.Play
            .configuration.getProperty("image.root.generated",
                    "/nfs/images/p"); //缩略图根目录
    public static String ROOT_PATH = Play.configuration.getProperty("upload.imagepath", "");

    public static void showOriginalImage(String path1, String path2, String path3, String path4) {
        final String path = "/" + path1 + "/" + path2 + "/" + path3 + "/" + path4;
        System.out.println("path:" + path);
        System.out.println("ROOT_PATH:" + ROOT_PATH);
        String targetImagePath = ROOT_PATH + path;

        File targetImage = new File(targetImagePath);
        if (!targetImage.exists()) {
            Logger.warn(targetImagePath + " Not Found!");
            error(404, path + "图片文件不存在！");
        }
        renderBinary(targetImage);
    }

    /**
     * 根据图片路径显示指定规格的缩略图.
     *
     * @param firstDir
     * @param secondDir
     * @param thirdDir
     * @param imageName
     */
    public static void showImage(long firstDir, long secondDir, long thirdDir, String imageName) {
        int width = 0;
        int height = 0;
        String imageSizeType = "";
        if (imageName.contains(TINY)) {
            width = 60;
            height = 46;
            imageSizeType = TINY;
        } else if (imageName.contains(SMALL)) {
            width = 150;
            height = 110;
            imageSizeType = SMALL;
        } else if (imageName.contains(MIDDLE)) {
            width = 170;
            height = 130;
            imageSizeType = MIDDLE;
        } else if (imageName.contains(LARGE)) {
            width = 340;
            height = 260;
            imageSizeType = LARGE;
        } else if (imageName.contains(LOGO)) {
            width = 300;
            height = 180;
            imageSizeType = LOGO;
        } else if (imageName.contains(SLIDE)) {
            width = 478;
            height = 218;
            imageSizeType = SLIDE;
        } else {
            notFound();
        }

        Matcher matcher = targetImagePattern.matcher(imageName);

        if (!matcher.matches()) {
            notFound();
        }

        String targetImageParent = IMAGE_ROOT_GENERATED + File.separator + firstDir + File.separator + secondDir + File.separator + thirdDir;

        if (!(new File(targetImageParent).isDirectory())) {
            new File(targetImageParent).mkdirs();
        }

        String targetImagePath = targetImageParent + File.separator + imageName;
        String originImagePath = IMAGE_ROOT_ORIGINAL + File.separator + firstDir + File.separator + secondDir + File.separator + thirdDir + File.separator + matcher.group(1) + "." + matcher.group(3);


        File targetImage = new File(targetImagePath);

        if (!targetImage.exists()) {
            File originImage = new File(originImagePath);
            if (!originImage.exists()) {
                //访问的原始文件不存在时直接返回默认图片的相应规格的图片
                originImage = new File(IMAGE_ROOT_ORIGINAL + "/1/1/1/default.png");
                targetImage = new File(IMAGE_ROOT_GENERATED + "/1/1/1/default_" + imageSizeType + ".png");
                if (targetImage.exists()) {
                    renderBinary(targetImage);
                }
            }
            //创建缩略图
            play.libs.Images.resize(originImage, targetImage, width, height);
        }
        renderBinary(targetImage);
    }

}