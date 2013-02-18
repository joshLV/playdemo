package controllers;

import com.uhuila.common.util.FileUploadUtil;
import com.uhuila.common.util.PathUtil;
import models.supplier.Supplier;
import models.supplier.SupplierContract;
import models.supplier.SupplierContractImage;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.mvc.Controller;
import play.mvc.With;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

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
    public static String ROOT_PATH = Play.configuration.getProperty("upload.contractpath", "");
    public static String FILE_TYPES = Play.configuration.getProperty("newsImg.fileTypes", "");
    public static long MAX_SIZE = Long.parseLong(Play.configuration.getProperty("upload.size", String.valueOf(1024 * 1024)));

    private static final String EXT_IMAGE_ROOT = "p";

    public static final String BASE_URL = Play.configuration.getProperty("uri.operate_business");

    /**
     * 编辑商户合同时 上传文件   编辑商户合同时
     *
     * @param imgFile
     */
    public static void uploadImages(File imgFile, Long supplierId, Long contractId) {
        //文件保存目录路径
        System.out.println("uploadSupplierContractImages");
        if (imgFile == null) {
            getError("请选择文件。");
        }
        System.out.println("selectedFileSuc");
        //检查目录
        File uploadDir = new File(ROOT_PATH);

        if (!uploadDir.isDirectory()) {
            getError("上传目录不存在。");
        }
        System.out.println("UploadDirectoryExist");

        //检查目录写权限
        if (!uploadDir.canWrite()) {
            getError("上传目录没有写权限。");
        }
        System.out.println("WithWriteRight");

        //检查文件大小
//        if (imgFile.length() > MAX_SIZE) {
//            getError("上传文件大小超过限制。");
//        }

        //检查扩展名
        //定义允许上传的文件扩展名
        String[] fileTypes = FILE_TYPES.trim().split(",");
        String fileExt = imgFile.getName().substring(imgFile.getName().lastIndexOf(".") + 1).toLowerCase();
        if (!Arrays.<String>asList(fileTypes).contains(fileExt)) {
            getError("上传文件扩展名仅限于：" + StringUtils.join(fileTypes) + "。");
        }
        System.out.println("UploadFileExtensionNameSuc");

        //上传文件
        try {
            System.out.println("StarttoUploadFile");
            String targetFilePath = storeImage(imgFile, supplierId, contractId, true, ROOT_PATH);
//            String targetFilePath = FileUploadUtil.storeImage(imgFile, ROOT_PATH);
            Map<String, Object> map = new HashMap<>();
            map.put("error", 0);
            System.out.println("StarttoUploadFileAfterPutError");

            String path = targetFilePath.substring(ROOT_PATH.length(), targetFilePath.length());
            if (path == null) {
                getError("上传失败，服务器忙，请稍后再试。");
            }
            System.out.println("StarttoUploadFileSuc");

            Supplier supplier = Supplier.findById(supplierId);
            SupplierContract contract = SupplierContract.findById(contractId);

            BufferedImage buff = ImageIO.read(imgFile);
            String size = String.valueOf(buff.getWidth()) + "x" + String.valueOf(buff.getHeight());
            new SupplierContractImage(supplier, contract, imgFile.getName(), path, size).save();

            path = PathUtil.signImgPath(path);
            System.out.println(path + "===path>>");

            map.put("url", BASE_URL + "/contract/p" + path);
            System.out.println(BASE_URL + "/contract/p" + path + "===>url");
            renderJSON(map);
        } catch (Exception e) {
            getError("上传失败，服务器忙，请稍候再试。");
        }
    }

    public static void uploadOldImages(String imgFilePath, Long supplierId, Long contractId) {
        //文件保存目录路径
        File imgFile = new File(imgFilePath, "");
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
//        if (imgFile.length() > MAX_SIZE) {
//            getError("上传文件大小超过限制。");
//        }

        //检查扩展名
        //定义允许上传的文件扩展名
        String[] fileTypes = FILE_TYPES.trim().split(",");
        String fileExt = imgFile.getName().substring(imgFile.getName().lastIndexOf(".") + 1).toLowerCase();
        if (!Arrays.<String>asList(fileTypes).contains(fileExt)) {
            getError("上传文件扩展名仅限于：" + StringUtils.join(fileTypes) + "。");
        }

        //上传文件
        try {
            String targetFilePath = storeImage(imgFile, supplierId, contractId, true, ROOT_PATH);
//            String targetFilePath = FileUploadUtil.storeImage(imgFile, ROOT_PATH);
            Map<String, Object> map = new HashMap<>();
            map.put("error", 0);

            String path = targetFilePath.substring(ROOT_PATH.length(), targetFilePath.length());
            if (path == null) {
                getError("上传失败，服务器忙，请稍后再试。");
            }

            Supplier supplier = Supplier.findById(supplierId);
            SupplierContract contract = SupplierContract.findById(contractId);

            BufferedImage buff = ImageIO.read(imgFile);
            String size = String.valueOf(buff.getWidth()) + "x" + String.valueOf(buff.getHeight());
            new SupplierContractImage(supplier, contract, imgFile.getName(), path, size).save();

            path = PathUtil.signImgPath(path);

            map.put("url", BASE_URL + "/contract/p" + path);
            return;
//            renderJSON(map);
        } catch (Exception e) {
            getError("上传失败，服务器忙，请稍候再试。");
        }
    }


    public static void getError(String message) {
        Map map = new HashMap();
        map.put("error", 1);
        map.put("message", message);
        renderJSON(map);
    }

    public static String storeImage(File imgFile, long supplierId, long contractId, boolean needNewName,
                                    String rootPath) throws IOException {

        //取得文件
        FileInputStream in = new FileInputStream(imgFile);
        // 将该数据流写入到指定文件中
//        String storePath = rootPath + PathUtil.getPathById(supplierId);

        String storePath = rootPath + "/" + String.valueOf(supplierId) + "/" + String.valueOf(contractId) + "/";

        File targetPath = new File(storePath);
        if (!(targetPath.isDirectory())) {
            targetPath.mkdirs();
        }
        String extension = imgFile.getName().substring(imgFile.getName().lastIndexOf("."), imgFile.getName().length());
        String targetFileName = needNewName ? generateUniqueId() + extension : imgFile.getName();

        String targetFilePath = storePath + targetFileName;
        if (new File(targetFilePath).exists()) {
            deleteExtImages(storePath, EXT_IMAGE_ROOT);
            new File(targetFilePath).delete();
        }

        FileOutputStream out = new FileOutputStream(targetFilePath);
        byte[] buffer = new byte[1024];
        int bytes_read;
        while ((bytes_read = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytes_read);
        }
        in.close();
        out.close();
        return targetFilePath;
    }

    public static String deleteExtImages(String storePath, String extImagePath) {
        if (storePath == null || "".equals(storePath)) {
            return null;
        }
        String rootPath = storePath.endsWith(File.separator) ? storePath.substring(0, storePath.length() - 1) : storePath;
        rootPath = storePath.startsWith(File.separator) ? rootPath.substring(1) : rootPath;
        String[] rootDirs = rootPath.split(File.separator);
        String extPath = extImagePath.startsWith(File.separator) ? extImagePath.substring(1) : extImagePath;
        if (rootDirs.length >= 3) {
            rootDirs[rootDirs.length - 4] = extPath;
        }
        String path = "";
        for (String dir : rootDirs) {
            path += File.separator + dir;
        }
        File directory = new File(path);
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                file.delete();
            }
        }
        return path;
    }

    /**
     * 生成唯一编号.
     *
     * @return 唯一编号
     */
    public static long generateUniqueId() {
        int random = new Random().nextInt() % 100;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmssSSS");
        String now = sdf.format(new Date());
        return Long.parseLong(now + Math.abs(random));
    }

}
