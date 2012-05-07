package controllers;

import static play.Logger.warn;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import models.admin.SupplierRole;
import models.admin.SupplierUser;
import models.sms.SMSUtil;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;
import com.uhuila.common.util.FileUploadUtil;
import com.uhuila.common.util.RandomNumberUtil;

/**
 * 商户管理的控制器.
 * <p/>
 * User: sujie
 * Date: 3/20/12
 * Time: 3:13 PM
 */
@With(OperateRbac.class)
@ActiveNavigation("suppliers_index")
public class Suppliers extends Controller {
    private static final String ADMIN_ROLE = "admin";
    private static final String SALES_ROLE = "sales";

    public static void index() {
        List<Supplier> suppliers = Supplier.findUnDeleted();
        render(suppliers);
    }

    @ActiveNavigation("suppliers_add")
    public static void add() {
        render();
    }

    /**
     * 创建商户，同时创建商户的一个管理员.
     *
     * @param supplier
     * @param image
     * @param admin
     */
    @ActiveNavigation("suppliers_add")
    public static void create(@Valid Supplier supplier, File image, @Valid SupplierUser admin) {
        checkImage(image);
        initAdmin(admin);

        checkItems(supplier);
        //随机产生6位数字密码
        String password = RandomNumberUtil.generateSerialNumber(6);
        admin.encryptedPassword = password;
        admin.confirmPassword = password;

        if (Validation.hasErrors()) {
            render("Suppliers/add.html");
        }
        supplier.loginName = admin.loginName;
        supplier.create();
        try {
            supplier.logo = uploadImagePath(image, supplier.id);
            supplier.save();
        } catch (IOException e) {
            error("supplier.image_upload_failed");
        }
        admin.create(supplier.id);

        //发送密码给商户管理员手机
        String comment = Play.configuration.getProperty("message.comment");
        SMSUtil.send(comment.replace("password", password), admin.mobile, "0000");
        
        index();
    }

    /**
     * 验证手机和电话
     *
     * @param supplier
     */
    private static void checkItems(Supplier supplier) {
        if (StringUtils.isEmpty(supplier.mobile) && StringUtils.isEmpty(supplier.phone)) {
            Validation.addError("supplier.mobile", "validation.lessOne");
        }
    }

    private static void initAdmin(SupplierUser admin) {
        admin.roles = new ArrayList<>();
        admin.roles.add(SupplierRole.findByKey(ADMIN_ROLE));
        admin.roles.add(SupplierRole.findByKey(SALES_ROLE));
    }

    private static void checkImage(File image) {
        if (image == null) {
            return;
        }
        //检查目录
        File uploadDir = new File(UploadFiles.ROOT_PATH);
        if (!uploadDir.isDirectory()) {
            Validation.addError("supplier.image", "validation.write");
        }

        //检查目录写权限
        if (!uploadDir.canWrite()) {
            Validation.addError("supplier.image", "validation.write");
        }

        if (image.length() > UploadFiles.MAX_SIZE) {
            Validation.addError("supplier.image", "validation.maxFileSize");
        }

        //检查扩展名
        //定义允许上传的文件扩展名
        String[] fileTypes = UploadFiles.FILE_TYPES.trim().split(",");
        String fileExt = image.getName().substring(image.getName().lastIndexOf(".") + 1).toLowerCase();
        if (!Arrays.<String>asList(fileTypes).contains(fileExt)) {
            Validation.addError("supplier.image", "validation.invalidType", StringUtils.join(fileTypes, ','));
        }
    }

    /**
     * 上传图片
     *
     * @param uploadImageFile
     * @param supplierId
     */
    private static String uploadImagePath(File uploadImageFile, Long supplierId) throws IOException {
        if (uploadImageFile == null || uploadImageFile.getName() == null) {
            return "";
        }
        //取得文件存储路径

        String absolutePath = FileUploadUtil.storeImage(uploadImageFile, supplierId, UploadFiles.ROOT_PATH);
        return absolutePath.substring(UploadFiles.ROOT_PATH.length(), absolutePath.length());
    }

    /**
     * 编辑门店页面展示
     *
     * @param id 门店标识
     */
    public static void edit(long id) {
        Supplier supplier = Supplier.findById(id);
        SupplierUser admin = SupplierUser.findAdmin(id, supplier.loginName);
        render(supplier, admin, id);
    }

    public static void update(Long id, @Valid Supplier supplier, File image, @Valid SupplierUser admin, Long adminId) {
        checkItems(supplier);
        if (Validation.hasErrors()) {
            for (String key : validation.errorsMap().keySet()) {
                warn("validation.errorsMap().get(" + key + "):" + validation.errorsMap().get(key));
            }

            render("Suppliers/edit.html", id, admin);
        }

        Supplier.update(id, supplier);
        if (adminId == null) {
            admin.create(id);
        } else {
            SupplierUser.update(adminId, admin);
        }
        index();
    }

    public static void freeze(long id) {
        Supplier.freeze(id);
        index();
    }

    public static void unfreeze(long id) {
        Supplier.unfreeze(id);
        index();
    }

    public static void delete(long id) {
        Supplier.delete(id);
        index();
    }
}
