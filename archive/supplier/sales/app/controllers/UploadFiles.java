package controllers;

import com.uhuila.common.util.FileUploadUtil;
import com.uhuila.common.util.PathUtil;
import models.sales.Goods;
import navigation.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@With(SupplierRbac.class)
@ActiveNavigation("goods_index")
public class UploadFiles extends Controller {

    public static String ROOT_PATH = Play.configuration.getProperty("upload.imagepath", "");
    public static String FILE_TYPES = Play.configuration.getProperty("newsImg.fileTypes", "");
    public static long MAX_SIZE = Long.parseLong(Play.configuration.getProperty("upload.size", String.valueOf(1024 * 1024)));

    /**
     * 上传文件
     *
     * @param imgFile
     */
    public static void uploadImage(File imgFile) {
        //文件保存目录路径
        if (imgFile == null) {
            getError(1, "请选择文件。");
        }
        //检查目录
        File uploadDir = new File(ROOT_PATH);
        if (!uploadDir.isDirectory()) {
            getError(2, "上传目录不存在。");
        }

        //检查目录写权限
        if (!uploadDir.canWrite()) {
            getError(3, "上传目录没有写权限。");
        }

        //检查文件大小
        if (imgFile.length() > MAX_SIZE) {
            getError(4, "上传文件大小超过限制。");
        }

        //检查扩展名
        //定义允许上传的文件扩展名
        String[] fileTypes = FILE_TYPES.trim().split(",");
        String fileExt = imgFile.getName().substring(imgFile.getName().lastIndexOf(".") + 1).toLowerCase();
        if (!Arrays.<String>asList(fileTypes).contains(fileExt)) {
            getError(5, "上传文件扩展名仅限于：" + StringUtils.join(fileTypes) + "。");
        }

        //上传文件
        try {
            String targetFilePath = FileUploadUtil.storeImage(imgFile, ROOT_PATH);

            Map map = new HashMap();
            map.put("error", 0);

            String path = targetFilePath.substring(ROOT_PATH.length(), targetFilePath.length());
            //不加水印
            path = PathUtil.addImgPathMark(path, "nw");
            if(path == null){
                getError(6, "上传失败，服务器忙，请稍后再试。");
            }
            path = PathUtil.signImgPath(path);
            map.put("url", "http://" + Goods.IMAGE_SERVER + "/p" + path);

            renderJSON(map);
        } catch (Exception e) {
            getError(7, "上传失败，服务器忙，请稍候再试。");
        }
    }

    private static void getError(int errorCode, String message) {
        Map map = new HashMap();
        map.put("error", errorCode);
        map.put("message", message);
        renderJSON(map);
    }
}
