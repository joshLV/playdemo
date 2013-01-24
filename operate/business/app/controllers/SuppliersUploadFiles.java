package controllers;

import com.uhuila.common.util.FileUploadUtil;
import com.uhuila.common.util.PathUtil;
import models.supplier.Supplier;
import models.supplier.SupplierContractImage;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO.
 * <p/>
 * User: wangjia
 * Date: 13-1-24
 * Time: 上午10:56
 */

@With(OperateRbac.class)
@ActiveNavigation("suppliers_upload_contracts")
public class SuppliersUploadFiles extends Controller {
    public static final String IMAGE_MIDDLE = "234x178";
    public static String ROOT_PATH = Play.configuration.getProperty("upload.imagepath", "");
    public static String FILE_TYPES = Play.configuration.getProperty("newsImg.fileTypes", "");
    public static long MAX_SIZE = Long.parseLong(Play.configuration.getProperty("upload.size", String.valueOf(1024 * 1024)));

    /**
     * 上传文件
     *
     * @param imgFile
     */
    public static void uploadImages(File imgFile, Long supplierId) {
        //文件保存目录路径
        if (imgFile == null) {
            getError("请选择文件。");
        }
        //检查目录
        File uploadDir = new File(ROOT_PATH);
        if (!uploadDir.isDirectory()) {
            getError("上传目录不存在。");
        }

        //检查目录写权限
        if (!uploadDir.canWrite()) {
            getError("上传目录没有写权限。");
        }
        //检查文件大小
        if (imgFile.length() > MAX_SIZE) {
            getError("上传文件大小超过限制。");
        }
        //检查扩展名
        //定义允许上传的文件扩展名
        String[] fileTypes = FILE_TYPES.trim().split(",");
        String fileExt = imgFile.getName().substring(imgFile.getName().lastIndexOf(".") + 1).toLowerCase();
        if (!Arrays.<String>asList(fileTypes).contains(fileExt)) {
            getError("上传文件扩展名仅限于：" + StringUtils.join(fileTypes) + "。");
        }
        //上传文件
        try {
            String targetFilePath = FileUploadUtil.storeImage(imgFile, supplierId, ROOT_PATH);
            Map<String, Object> map = new HashMap<>();
            map.put("error", 0);
            String path = targetFilePath.substring(ROOT_PATH.length(), targetFilePath.length());
            if (path == null) {
                getError("上传失败，服务器忙，请稍后再试。");
            }
            Supplier supplier = Supplier.findById(supplierId);
            new SupplierContractImage(supplier, path).save();
            path = PathUtil.signImgPath(path);
            map.put("url", "/contract/p" + path);
            renderJSON(map);
        } catch (Exception e) {
            System.out.print(e.fillInStackTrace());
            getError("上传失败，服务器忙，请稍候再试。");
        }
    }

    private static void getError(String message) {
        Map map = new HashMap();
        map.put("error", 1);
        map.put("message", message);
        renderJSON(map);
    }
}
