package controllers;

import com.uhuila.common.util.PathUtil;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import play.Logger;
import play.Play;
import play.mvc.Controller;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 图片的控制.
 * User: sujie
 * Date: 2/8/12
 * Time: 11:42 AM
 */
public class Images extends Controller {
    private static final String IMAGE_ROOT_ORIGINAL = play.Play .configuration.getProperty("image.root.original", "/nfs/images/o"); //原始图根目录
    private static final String IMAGE_ROOT_GENERATED = play.Play .configuration.getProperty("image.root.generated", "/nfs/images/p"); //缩略图根目录
    public static String ROOT_PATH = Play.configuration.getProperty("upload.imagepath", "");

    private static final Pattern imageNamePattern = Pattern.compile("([a-z0-9]{8})_([^_]+)(_.+)*\\.((?i)(jpg|jpeg|png|gif))$");
    private static final Pattern sizePattern = Pattern.compile(".+_([0-9]+)x([0-9]+)(_.+)*\\.((?i)(jpg|jpeg|png|gif))$");
    private static final Pattern waterPattern = Pattern.compile(".+_nw(_.+)*\\.((?i)(jpg|jpeg|png|gif))$");


    public static void showOriginalImage(String path1, String path2, String path3, String path4) {
        String targetImagePath = joinPath(ROOT_PATH, path1, path2, path3, path4);
        System.out.println("path:" + targetImagePath);
        System.out.println("ROOT_PATH:" + ROOT_PATH);

        File targetImage = new File(targetImagePath);
        if (!targetImage.exists()) {
            Logger.warn(targetImagePath + " Not Found!");
            error(404, "图片文件不存在！");
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
    public static void showImage(String firstDir, String secondDir, String thirdDir, String imageName) {
        Matcher imageNameMatcher = imageNamePattern.matcher(imageName);
        if (!imageNameMatcher.matches()) {
            Logger.error("image not found: %s", imageName);
            notFound();
        }

        String sign = imageNameMatcher.group(1);
        String fileRawName = imageNameMatcher.group(2);
        String fileFixName = imageNameMatcher.group(3);
        String fileExtension = imageNameMatcher.group(4);
        fileFixName = fileFixName == null ? "" : fileFixName;
        //验证签名
        if(!PathUtil.imgSign(fileRawName + fileFixName + "." + fileExtension).equals(sign)){
            Logger.error("image sign failed: " + imageName);
            notFound();
        }


        File originImage = new File(joinPath(IMAGE_ROOT_ORIGINAL, firstDir, secondDir, thirdDir), fileRawName  + "." + fileExtension);

        //访问的原始文件不存在时直接返回默认图片的相应规格的图片
        if (!originImage.exists()) {
            originImage = new File(Play.applicationPath,joinPath("public", "images", "default.png"));
            firstDir = secondDir = thirdDir = "1";
            imageName = "359e9dab_default_nw.png";
            imageNameMatcher = imageNamePattern.matcher(imageName);
            imageNameMatcher.matches();
            fileRawName = imageNameMatcher.group(2);
            fileExtension = imageNameMatcher.group(4);
        }

        StringBuilder targetFileName = new StringBuilder(fileRawName);

        File targetParent = new File(joinPath(IMAGE_ROOT_GENERATED, firstDir, secondDir, thirdDir));
        //检查目标目录
        if (!targetParent.exists()) {
            if(!targetParent.mkdirs()){
                Logger.error("can not mkdir on %s", targetParent.getPath());
                error("can not mkdir on " + targetParent.getPath());
            }
        }

        //检查是否指定了目标大小
        Matcher sizeMatcher = sizePattern.matcher(imageName);
        int width = 0, height = 0;
        boolean resize = false, noWatermark = false;
        if(sizeMatcher.matches()){
            resize = true;
            width = Integer.parseInt(sizeMatcher.group(1));
            height = Integer.parseInt(sizeMatcher.group(2));
            targetFileName.append("_").append(width).append("x").append(height);
        }

        //检查是否指定了不需要水印
        if(waterPattern.matcher(imageName).matches()){
            noWatermark = true;
            targetFileName.append("_nw");
        }

        targetFileName.append(".").append(fileExtension);

        File targetImage = new File(targetParent, targetFileName.toString());

        if (!targetImage.exists()) {
            //创建缩略图和水印
            try {
                Thumbnails.Builder<File> imageBuilder = Thumbnails.of(originImage).outputQuality(0.99f);

                //缩放
                if(resize){ imageBuilder.size(width, height); }
                else { imageBuilder.scale(1.0D); }

                //水印
                if(!noWatermark){
                    BufferedImage img = imageBuilder.asBufferedImage();
                    //水印大小为原图片大小的1/5
                    int waterWidth = img.getWidth()/5;
                    int waterHeight = img.getHeight()/5;
                    Thumbnails.Builder<File> waterBuilder = Thumbnails.of(
                            new File(Play.applicationPath, joinPath("public", "images", "watermark.png")))
                            .outputQuality(0.99f)
                            .size(waterWidth, waterHeight);
                    imageBuilder.watermark(Positions.BOTTOM_RIGHT, waterBuilder.asBufferedImage(), 0.5f);
                }

                imageBuilder.toFile(targetImage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        renderBinary(targetImage);
    }

    private static String joinPath(String... nodes){
        StringBuilder path = new StringBuilder();
        for(String node: nodes){
            path.append(node).append(File.separator);
        }
        if(nodes.length > 0){
            return path.substring(0, path.length() - File.separator.length());
        }else {
            return "";
        }
    }

}