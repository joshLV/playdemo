package controllers;

import models.sales.Brand;
import models.sales.Sku;
import models.sales.SkuCondition;
import models.supplier.Supplier;
import models.supplier.SupplierCategory;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 13-2-26
 * Time: 下午6:01
 */
@With(OperateRbac.class)
@ActiveNavigation("sku_index")
public class SkuManage extends Controller {
    public static int PAGE_SIZE = 15;

    @ActiveNavigation("sku_index")
    public static void index(SkuCondition condition) {
        int pageNumber = getPage();
        if (condition == null) {
            condition = new SkuCondition();
        }
        JPAExtPaginator<Sku> skuList = Sku.findByCondition(condition, pageNumber,
                PAGE_SIZE);
        skuList.setBoundaryControlsEnabled(true);
        List<Supplier> supplierList = Supplier.findUnDeleted();
        Long id = OperateRbac.currentUser().id;
        List<Brand> brandList = Brand.findByOrder(null, id);
        render(skuList, brandList, supplierList, pageNumber, condition);
    }

    private static int getPage() {
        String page = request.params.get("page");
        if (StringUtils.isNotEmpty(page) && (page.contains("?x-http-method-override=PUT") || page.contains("x-http-method-override=PUT"))) {
            page = page.replace("x-http-method-override=PUT", "").replace("?", "");
        }
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        return pageNumber;
    }

    @ActiveNavigation("sku_add")
    public static void add() {
        setInitParams();
        render();
    }

    @ActiveNavigation("sku_add")
    public static void create(@Valid Sku sku) {
        if (Validation.hasErrors()) {
            savePageParams(sku);
            render("SkuManage/add.html");
        }
        sku.create();
        index(null);
    }

    /**
     * 页面有错误信息的时候，保留页面信息
     *
     * @param sku
     */
    private static void savePageParams(Sku sku) {
        setInitParams();
        if (sku.supplierId != null) {
            Long id = OperateRbac.currentUser().id;
            List<Brand> brandList = Brand.findByOrder(new Supplier(sku.supplierId), id);
            renderArgs.put("brandList", brandList);
        }
    }

    private static void setInitParams() {
        List<Supplier> supplierList = Supplier.findUnDeleted();
        List<SupplierCategory> supplierCategoryList = SupplierCategory.findAll();
        renderArgs.put("supplierList", supplierList);
        renderArgs.put("supplierCategoryList", supplierCategoryList);
    }

    public static void edit(Long id) {
        Sku sku = Sku.findById(id);
        setInitParams();
        render(sku);
    }

    public static void update(Long id, @Valid Sku sku) {
        if (Validation.hasErrors()) {
            savePageParams(sku);
            render("SkuManage/edit.html", sku);
        }
        Sku.update(id, sku);
        index(null);
    }

    public static void delete(Long id) {
        Sku.delete(id);
        index(null);
    }
}
