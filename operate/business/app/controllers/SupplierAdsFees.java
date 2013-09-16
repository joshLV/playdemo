package controllers;

import com.uhuila.common.constants.DeletedStatus;
import models.sales.Sku;
import models.supplier.ReceivedType;
import models.supplier.Supplier;
import models.supplier.SupplierAdsFee;
import models.supplier.SupplierAdsFeesCondition;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.modules.paginate.ModelPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;

/**
 * User: yan
 * Date: 13-8-5
 * Time: 下午4:55
 */
@With(OperateRbac.class)
@ActiveNavigation("ads_fee_index")
public class SupplierAdsFees extends Controller {
    public static int PAGE_SIZE = 15;

    /**
     * 广告费管理一览
     */
    public static void index(SupplierAdsFeesCondition condition) {
        if (condition == null) {
            condition = new SupplierAdsFeesCondition();
        }
        int pageNumber = getPage();
        List<Supplier> supplierList = Supplier.findUnDeleted();
        JPAExtPaginator<SupplierAdsFee> adsFeeList = SupplierAdsFee.getPage(condition, pageNumber, PAGE_SIZE);
        render(adsFeeList, supplierList);
    }

    @ActiveNavigation("ads_fee_add")
    public static void add() {
        List<Supplier> supplierList = Supplier.findUnDeleted();
        render(supplierList);
    }

    /**
     * 创建广告费
     */
    public static void create(@Valid SupplierAdsFee ads) {
        if (Validation.hasErrors()) {
            render("SupplierAdsFees/add.html", ads);
        }
        ads.createdAt = new Date();
        ads.deleted = DeletedStatus.UN_DELETED;
        ads.save();
        index(null);
    }

    public static void edit(Long id) {
        SupplierAdsFee ads = SupplierAdsFee.findById(id);
        List<Supplier> supplierList = Supplier.findUnDeleted();
        render(ads, supplierList);
    }

    /**
     * 修改信息
     */
    public static void update(Long id, @Valid SupplierAdsFee ads) {
        if (Validation.hasErrors()) {
            render("SupplierAdsFees/edit.html", ads);
        }

        SupplierAdsFee updAdsFee = SupplierAdsFee.findById(id);
        updAdsFee.receivedAt = ads.receivedAt;
        updAdsFee.adsFee = ads.adsFee;
        updAdsFee.receivedType = ads.receivedType;
        updAdsFee.updatedAt = new Date();
        updAdsFee.remark = ads.remark;
        updAdsFee.updatedBy = OperateRbac.currentUser().userName;
        updAdsFee.save();

        index(null);
    }

    public static void delete(Long id) {
        SupplierAdsFee supplierAdsFee = SupplierAdsFee.findById(id);
        if (supplierAdsFee == null) {
            return;
        }
        supplierAdsFee.delete();
        index(null);
    }


    private static int getPage() {
        String page = request.params.get("page");
        if (StringUtils.isNotEmpty(page) && (page.contains("?x-http-method-override=PUT") || page.contains("x-http-method-override=PUT"))) {
            page = page.replace("x-http-method-override=PUT", "").replace("?", "");
        }
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }
}
