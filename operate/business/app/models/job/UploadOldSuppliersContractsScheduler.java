package models.job;

import com.uhuila.common.util.PathUtil;
import controllers.SuppliersUploadFiles;
import models.supplier.Supplier;
import models.supplier.SupplierContract;
import models.supplier.SupplierContractImage;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 定时自动上传旧的合同
 * <p/>
 * User: wangjia
 * Date: 13-2-7
 * Time: 上午11:37
 */


@On("0 00 16 18 2 ? 2013")
public class UploadOldSuppliersContractsScheduler extends Job {
    public static String FILE_TYPES = Play.configuration.getProperty("newsImg.fileTypes", "");
    public static String ROOT_PATH = Play.configuration.getProperty("upload.contractpath", "");
    public static final String BASE_URL = Play.configuration.getProperty("uri.operate_business");
    public static final String SOURCE_URL = Play.configuration.getProperty("uri.old_suppliers_contracts");

    @Override
    public void doJob() throws Exception {
        String sourceUrl = SOURCE_URL;
        walkDirectory(sourceUrl);
    }

    public static void walkDirectory(String path) {
        File root = new File(path);
        String[] subNote = root.list();
        for (String supplierIdDirName : subNote) {
            File root2 = new File(path + "/" + supplierIdDirName);

            File[] list = root2.listFiles();
            Supplier supplier = Supplier.findById(Long.valueOf(supplierIdDirName));
            SupplierContract existedContract = SupplierContract.find("supplierId =?", Long.valueOf(supplierIdDirName)).first();
            if (existedContract == null) {
                SupplierContract contract = new SupplierContract(supplier);
                SupplierContract newContract = new SupplierContract(supplier);
                newContract.create();
                newContract.save();
                for (File f : list) {
                    Long supplierId = Long.valueOf(supplierIdDirName);
                    String imgFilePath = f.getAbsoluteFile().toString();
                    Long contractId = newContract.id;
                    uploadOldImages(imgFilePath, supplierId, contractId);

                }
            }
        }
    }


    public static void uploadOldImages(String imgFilePath, Long supplierId, Long contractId) {
        //upload image
        //文件保存目录路径
        File imgFile = new File(imgFilePath, "");
        if (imgFile == null) {
            SuppliersUploadFiles.getError("请选择文件。");
        }

        //检查目录
        File uploadDir = new File(ROOT_PATH);

        if (!uploadDir.isDirectory()) {
            SuppliersUploadFiles.getError("上传目录不存在。");
        }

        //检查目录写权限
        if (!uploadDir.canWrite()) {
            SuppliersUploadFiles.getError("上传目录没有写权限。");
        }

        //检查扩展名
        //定义允许上传的文件扩展名
        String[] fileTypes = FILE_TYPES.trim().split(",");
        String fileExt = imgFile.getName().substring(imgFile.getName().lastIndexOf(".") + 1).toLowerCase();
        if (!Arrays.<String>asList(fileTypes).contains(fileExt)) {
            SuppliersUploadFiles.getError("上传文件扩展名仅限于：" + StringUtils.join(fileTypes) + "。");
        }

        //上传文件
        try {
            String targetFilePath = SuppliersUploadFiles.storeImage(imgFile, supplierId, contractId, true, ROOT_PATH);
            Map<String, Object> map = new HashMap<>();
            map.put("error", 0);

            String storedPath = targetFilePath.substring(ROOT_PATH.length(), targetFilePath.length());
            if (storedPath == null) {
                SuppliersUploadFiles.getError("上传失败，服务器忙，请稍后再试。");
            }

            Supplier supplier = Supplier.findById(supplierId);
            SupplierContract contract = SupplierContract.findById(contractId);

            BufferedImage buff = ImageIO.read(imgFile);
            String size = String.valueOf(buff.getWidth()) + "x" + String.valueOf(buff.getHeight());
            new SupplierContractImage(supplier, contract, imgFile.getName(), storedPath, size).save();

            storedPath = PathUtil.signImgPath(storedPath);

            map.put("url", BASE_URL + "/contract/p" + storedPath);
        } catch (Exception e) {
            SuppliersUploadFiles.getError("上传失败，服务器忙，请稍候再试。");
        }
    }
}
