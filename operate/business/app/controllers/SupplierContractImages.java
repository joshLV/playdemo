package controllers;

import com.uhuila.common.util.PathUtil;
import models.supplier.Supplier;
import operate.rbac.ContextedPermission;
import play.Logger;
import play.Play;
import play.mvc.Before;
import play.mvc.Controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import play.mvc.With;


/**
 * 商户合同图片的控制
 * <p/>
 * User: wangjia
 * Date: 13-1-24
 * Time: 下午1:48
 */
@With(OperateRbac.class)
public class SupplierContractImages extends Controller {
    private static final String IMAGE_ROOT_ORIGINAL = play.Play.configuration.getProperty("image.root.original", "/nfs/images/contract/o"); //原始图根目录
    private static final String IMAGE_ROOT_GENERATED = play.Play.configuration.getProperty("image.root.generated", "/nfs/images/contract/p"); //缩略图根目录
    public static String ROOT_PATH = Play.configuration.getProperty("upload.contractpath", "");

    private static final Pattern imageNamePattern = Pattern.compile("([a-z0-9]{8})_([^_]+)(_.+)*\\.((?i)(jpg|jpeg|png|gif))$");
    private static final Pattern sizePattern = Pattern.compile(".+_([0-9]+)x([0-9]+)(_.+)*\\.((?i)(jpg|jpeg|png|gif))$");
    private static final Pattern waterPattern = Pattern.compile(".+_nw(_.+)*\\.((?i)(jpg|jpeg|png|gif))$");


    public static void showOriginalImage(String path1, String path2, String path3, String path4) {
        String targetImagePath = joinPath(ROOT_PATH, path1, path2, path3, path4);
//        System.out.println("path:" + targetImagePath);
//        System.out.println("ROOT_PATH:" + ROOT_PATH);

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
     * @param imageName
     */
    public static void showImage(String firstDir, String secondDir, String imageName) {
        Boolean hasManagerViewContractPermission = ContextedPermission.hasPermission("MANAGER_VIEW_SUPPLIER_CONTRACT");
        Boolean hasViewContractPermission = ContextedPermission.hasPermission("VIEW_SUPPLIER_CONTRACT");
        Long operatorId = OperateRbac.currentUser().id;
        if (hasManagerViewContractPermission != null && !hasManagerViewContractPermission) {
            List<Supplier> suppliers = Supplier.find("salesId=?", operatorId).fetch();
            List<Long> supplierIds = new ArrayList<>();
            for (Supplier s : suppliers) {
                supplierIds.add(s.id);
            }
            if (!supplierIds.contains(operatorId)) {
                renderText("你没有权限查看此图片");
            }
        }

        Matcher imageNameMatcher = imageNamePattern.matcher(imageName);
        if (!imageNameMatcher.matches()) {
            Logger.error("image not found: %s", imageName);
            notFound();
        }

        String sign = imageNameMatcher.group(1);                //图片签名
        String fileRawName = imageNameMatcher.group(2);         //纯文件名
        String fileFixName = imageNameMatcher.group(3);         //文件名中的附加后缀
        String fileExtension = imageNameMatcher.group(4);       //文件扩展名
        fileFixName = fileFixName == null ? "" : fileFixName;

        //验证签名
        if (!PathUtil.imgSign(fileRawName + fileFixName + "." + fileExtension).equals(sign)) {
            Logger.error("image sign failed: " + imageName);
            notFound();
        }

        File originImage = new File(joinPath(IMAGE_ROOT_ORIGINAL, firstDir, secondDir), fileRawName + "." + fileExtension);

        //访问的原始文件不存在时，读取默认图片作为图片源，同时修改生成文件的目录为/1/1/1/
        boolean originImageExist = true;
        StringBuilder defaultTargetImageName = new StringBuilder("default");
        if (!originImage.exists()) {
            originImage = new File(Play.applicationPath, joinPath("public", "images", "default.png"));

            originImageExist = false;
            firstDir = secondDir = "1";
        }

        File targetParent = new File(joinPath(IMAGE_ROOT_GENERATED, firstDir, secondDir));
        //检查目标目录
        if (!targetParent.exists()) {
            if (!targetParent.mkdirs()) {
                Logger.error("can not mkdir on %s", targetParent.getPath());
                error("can not mkdir on " + targetParent.getPath());
            }
        }
        //检查是否指定了目标大小，如果指定了大小，那么生成的默认图片的文件名也要更改
        Matcher sizeMatcher = sizePattern.matcher(imageName);
        int width = 0, height = 0;
        boolean resize = false, noWatermark = false;
        if (sizeMatcher.matches()) {
            resize = true;
            width = Integer.parseInt(sizeMatcher.group(1));
            height = Integer.parseInt(sizeMatcher.group(2));

            defaultTargetImageName.append("_").append(width).append("x").append(height);
        }

        //检查是否指定了不需要水印，默认图片无需加水印
        //if(waterPattern.matcher(imageName).matches() || !originImageExist){
        noWatermark = true;
        //}

        defaultTargetImageName.append(".png");
        if (!originImageExist) {
            imageName = defaultTargetImageName.toString();
        }

        File targetImage = new File(targetParent, imageName);

        if (!targetImage.exists()) {
            //创建缩略图和水印
            try {
                Thumbnails.Builder<File> imageBuilder = Thumbnails.of(originImage).outputQuality(0.99f);

                //缩放
                if (resize) {
                    imageBuilder.size(width, height);
                    imageBuilder.keepAspectRatio(false);
                } else {
                    imageBuilder.scale(1.0D);
                }

                //水印
                if (!noWatermark) {
                    BufferedImage img = imageBuilder.asBufferedImage();
                    //水印大小为原图片大小的1/5
                    int waterWidth = img.getWidth() / 5;
                    int waterHeight = img.getHeight() / 5;
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

    private static String joinPath(String... nodes) {
        StringBuilder path = new StringBuilder();
        for (String node : nodes) {
            path.append(node).append(File.separator);
        }
        if (nodes.length > 0) {
            return path.substring(0, path.length() - File.separator.length());
        } else {
            return "";
        }
    }
}
