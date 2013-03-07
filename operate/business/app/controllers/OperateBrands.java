package controllers;

import com.google.gson.Gson;
import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.FileUploadUtil;
import models.sales.Brand;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.ModelPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static play.Logger.warn;

/**
 * 品牌管理.
 * <p/>
 * User: sujie
 * Date: 4/17/12
 * Time: 11:11 AM
 */

@With(OperateRbac.class)
@ActiveNavigation("brands_index")
public class OperateBrands extends Controller {

    public static int PAGE_SIZE = 15;

    /**
     * 获取券号列表页.
     */
    public static void index(Long supplierId, Long brandId) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        ModelPaginator brandPage = Brand.getBrandPage(pageNumber, PAGE_SIZE, supplierId, brandId);
        Long id = OperateRbac.currentUser().id;
        List<Brand> brandList = Brand.findByOrder(null, id);
        List<Supplier> supplierList = Supplier.findUnDeleted();
        render(brandPage, supplierList, brandList);
    }

    /**
     * 添加品牌
     */
    @ActiveNavigation("brands_add")
    public static void add() {
        List<Supplier> supplierList = Supplier.findUnDeleted();
        Brand brand = new Brand();
        render(supplierList, brand);
    }

    @ActiveNavigation("brands_add")
    public static void create(@Valid Brand brand, @Required File logoImage, @Required File siteDisplayImage) {
        checkImageFile(logoImage);
        checkImageFile(siteDisplayImage);
        if (Validation.hasErrors()) {
            List<Supplier> supplierList = Supplier.findUnDeleted();
            for (String key : validation.errorsMap().keySet()) {
                warn("create:      validation.errorsMap().get(" + key + "):" + validation.errorsMap().get(key));
            }
            render("OperateBrands/add.html", supplierList);
        }
        try {
            brand.logo = uploadFile(logoImage, null);
            brand.siteDisplayImage = uploadFile(siteDisplayImage, null);
        } catch (IOException e) {
            e.printStackTrace();
            error(500, "brand.image_upload_failed");
        }
        brand.deleted = DeletedStatus.UN_DELETED;
        brand.create();

        index(null, null);
    }

    /**
     * 上传图片
     *
     * @param uploadImageFile
     */
    private static String uploadFile(File uploadImageFile, String oldImageFile) throws IOException {
        if (uploadImageFile == null || uploadImageFile.getName() == null) {
            return "";
        }
        //取得文件存储路径
        String absolutePath = FileUploadUtil.storeImage(uploadImageFile, OperateUploadFiles.ROOT_PATH);
        if (oldImageFile != null && !"".equals(oldImageFile)) {
            File oldImage = new File(OperateUploadFiles.ROOT_PATH + oldImageFile);
            oldImage.delete();
        }
        return absolutePath.substring(OperateUploadFiles.ROOT_PATH.length(), absolutePath.length());
    }


    private static void checkImageFile(File logo) {
        if (logo == null) {
            return;
        }
        //检查目录
        File uploadDir = new File(OperateUploadFiles.ROOT_PATH);
        if (!uploadDir.isDirectory()) {
            Validation.addError("brand.logo", "validation.write");
        }

        //检查目录写权限
        if (!uploadDir.canWrite()) {
            Validation.addError("brand.logo", "validation.write");
        }

        if (logo.length() > OperateUploadFiles.MAX_SIZE) {
            Validation.addError("brand.logo", "validation.maxFileSize");
        }

        //检查扩展名
        //定义允许上传的文件扩展名
        String[] fileTypes = OperateUploadFiles.FILE_TYPES.trim().split(",");
        String fileExt = logo.getName().substring(logo.getName().lastIndexOf(".") + 1).toLowerCase();
        if (!Arrays.<String>asList(fileTypes).contains(fileExt)) {
            Validation.addError("brand.logo", "validation.invalidType", StringUtils.join(fileTypes, ','));
        }
    }

    public static void edit(Long id) {
        Brand brand = Brand.findById(id);
        List<Supplier> supplierList = Supplier.findUnDeleted();
        renderArgs.put("imageLogoPath", brand.getShowLogo());
        renderArgs.put("siteDisplayMiddleImage", brand.getSiteDisplayMiddleImage());
        render(brand, supplierList, id);
    }

    public static void update(Long id, File siteDisplayImage, File logoImage, @Valid Brand brand) {
        checkImageFile(logoImage);
        checkImageFile(siteDisplayImage);
        if (Validation.hasErrors()) {
            renderArgs.put("imageLogoPath", brand.getShowLogo());
            for (String key : validation.errorsMap().keySet()) {
                warn("update: validation.errorsMap().get(" + key + "):" + validation.errorsMap().get(key));
//                System.out.println("update: validation.errorsMap().get(" + key + "):" + validation.errorsMap().get(key));;
            }
            render("OperateBrands/edit.html", brand, id);
        }

        try {
            Brand oldBrand = Brand.findById(id);
            String oldImagePath = oldBrand == null ? null : oldBrand.logo;
            String image = uploadFile(logoImage, oldImagePath);
            if (StringUtils.isNotEmpty(image)) {
                brand.logo = image;
            }

            String oldSitePath = oldBrand == null ? null : oldBrand.siteDisplayImage;
            String nowImage = uploadFile(siteDisplayImage, oldSitePath);

            if (StringUtils.isNotEmpty(nowImage)) {
                brand.siteDisplayImage = nowImage;
            }
        } catch (IOException e) {
            error(e);
        }
        Brand.update(id, brand);

        index(null, null);
    }

    public static void delete(Long id) {
        Brand brand = Brand.findById(id);
        if (brand != null) {
            brand.deleted = DeletedStatus.DELETED;
            brand.save();
        }
        index(null, null);
    }


    public static void goodsBrands(Long id) {
        //品牌列表
        Supplier supplier = Supplier.findById(id);

        Long loginUserId = OperateRbac.currentUser().id;
        List<Brand> brandList = Brand.findByOrder(supplier, loginUserId);

        String skuBrand = request.params.get("skuBrand");
        if (StringUtils.isNotBlank(skuBrand)) {
            render("OperateBrands/skuBrands.html", brandList);
        }
        String stockBrand = request.params.get("stockBrand");
        if (StringUtils.isNotBlank(stockBrand)) {
//            renderJSON(brandList);
            System.out.println(brandList.size() + "===brandList.size()>>");
            render("OperateBrands/stockBrands.html", brandList);
        }
        render(brandList);
    }
}