package controllers;

import com.uhuila.common.util.FileUploadUtil;
import controllers.modules.cas.SecureCAS;
import models.admin.SupplierUser;
import models.supplier.Supplier;
import navigation.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 商户管理的控制器.
 * <p/>
 * User: sujie
 * Date: 3/20/12
 * Time: 3:13 PM
 */
@With({SecureCAS.class, MenuInjector.class})
@ActiveNavigation("suppliers_index")
public class Suppliers extends Controller {
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
    public static void create(@Valid Supplier supplier, File image, @Valid SupplierUser admin) {
        checkImage(image);
        admin.loginName = "admin";
        if (Validation.hasErrors()) {
            Validation.keep();
            add();
        }
        supplier.create();
        try {
            uploadImagePath(image, supplier);
            supplier.save();
        } catch (IOException e) {
            error("supplier.image_upload_failed");
        }
        admin.create();

        index();
    }

    private static void checkImage(File image) {
        if (image != null) {
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
    }

    private static void uploadImagePath(File image, Supplier supplier) throws IOException {
        if (image == null || image.getName() == null) {
            return;
        }
        //取得文件存储路径

        String absolutePath = FileUploadUtil.storeImage(image, supplier.id, UploadFiles.ROOT_PATH);
        supplier.logo = absolutePath.substring(UploadFiles.ROOT_PATH.length(), absolutePath.length());
    }

    /**
     * 编辑门店页面展示
     *
     * @param id 门店标识
     */
    public static void edit(long id, Supplier supplier) {
        if (supplier == null || supplier.id == null) {
            supplier = Supplier.findById(id);
        }
        render(supplier);
    }

    public static void update(@Valid Supplier supplier, File image) {
        if (validation.hasErrors()) {
            Validation.keep();
            edit(supplier.id, supplier);
        }
        try {
            uploadImagePath(image, supplier);
        } catch (IOException e) {
            error("supplier.image_upload_failed");
        }
        Supplier.update(supplier);
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
        ok();
    }
}
