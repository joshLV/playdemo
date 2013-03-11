package controllers;

import models.sales.*;
import models.supplier.Supplier;
import models.supplier.SupplierCategory;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.db.jpa.JPA;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import javax.persistence.Query;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO.
 * <p/>
 * User: wangjia
 * Date: 13-3-4
 * Time: 下午5:53
 */
@With(OperateRbac.class)
@ActiveNavigation("inventory_add")
public class InventoryStocks extends Controller {
    public static int PAGE_SIZE = 15;

    @ActiveNavigation("inventory_details")
    public static void index() {
        int pageNumber = getPage();
        render();
    }

    @ActiveNavigation("inventory_stockIn")
    public static void stockIn() {
        setInitParams();
        render();
    }

    @ActiveNavigation("inventory_stockOut")
    public static void stockOut() {
        setInitParams();
        render();
    }

    @ActiveNavigation("inventory_stockOut")
    public static void createStockOut(@Valid InventoryStock stock) {
        checkStockOutCountAndPrice(stock);
        checkStockOutCount(stock);
        if (Validation.hasErrors()) {
            renderArgs.put("stock.id_supplierName", stock.supplier.id);
            renderArgs.put("stock.brand.id", stock.brand.id);
            renderArgs.put("stock.sku.id", stock.sku.id);
            savePageParams(stock);
            render("InventoryStocks/stockOut.html");
        }
        stock.createdBy = OperateRbac.currentUser().userName;
        stock.actionType = StockActionType.OUT;
        List<InventoryStockItem> stockInItemList = InventoryStockItem.find("sku=? and remainCount>0 order by createdAt", stock.sku).fetch();
        Long totalStockOutCount = stock.stockOutCount;
        for (InventoryStockItem item : stockInItemList) {
            item.remainCount -= totalStockOutCount;
            if (item.remainCount < 0) {
                totalStockOutCount = -item.remainCount;
                item.remainCount = 0l;
            }
            item.save();
        }
        InventoryStockItem stockItem = new InventoryStockItem(stock);
        stockItem.create();
        stock.inventoryStockItemList = new LinkedList<>();
        stock.inventoryStockItemList.add(stockItem);
        stock.createdBy = OperateRbac.currentUser().loginName;
        stock.create();
        stock.save();

        index();
    }


    @ActiveNavigation("inventory_stockIn")
    public static void createStockIn(@Valid InventoryStock stock) {
        checkStockInCountAndPrice(stock);
        if (Validation.hasErrors()) {
            savePageParams(stock);
            render("InventoryStocks/stockIn.html");
        }
        stock.createdBy = OperateRbac.currentUser().userName;
        stock.actionType = StockActionType.IN;

        InventoryStockItem stockItem = new InventoryStockItem(stock);
        stockItem.create();
        stock.inventoryStockItemList = new LinkedList<>();
        stock.inventoryStockItemList.add(stockItem);
        stock.createdBy = OperateRbac.currentUser().loginName;

        stock.create();
        stock.save();
        index();
    }

    private static void setInitParams() {
        List<Supplier> supplierList = Supplier.findUnDeleted();
        List<Brand> brandList = null;
        renderArgs.put("createdBy", OperateRbac.currentUser().userName);
        renderArgs.put("supplierList", supplierList);
        renderArgs.put("brandList", brandList);
    }

    private static void savePageParams(InventoryStock stock) {
        setInitParams();
        if (stock.supplier != null) {
            Long id = OperateRbac.currentUser().id;
            List<Brand> brandList = Brand.findByOrder(new Supplier(stock.supplier.id), id);
            renderArgs.put("brandList", brandList);
        }
    }

    private static int getPage() {
        String page = request.params.get("page");
        if (StringUtils.isNotEmpty(page) && (page.contains("?x-http-method-override=PUT") || page.contains("x-http-method-override=PUT"))) {
            page = page.replace("x-http-method-override=PUT", "").replace("?", "");
        }
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        return pageNumber;
    }

    public static void stockSku(Long brandId) {
        List<Sku> skuList = Sku.findByBrand(brandId);
        for (Sku sku : skuList) {
            sku.supplier = null;
            sku.brand = null;
        }
        renderJSON(skuList);
    }

    public static void stockBrands(Long id) {
        //品牌列表
        Supplier supplier = Supplier.findById(id);

        Long loginUserId = OperateRbac.currentUser().id;
        List<Brand> brandList = Brand.findByOrder(supplier, loginUserId);
        for (Brand brand : brandList) {
            brand.supplier = null;
        }
        renderJSON(brandList);
    }

    public static void stockSkuRemainCount(Long id) {
        Query query = JPA.em().createQuery("SELECT SUM(st.remainCount) FROM InventoryStockItem st where st.sku.id= :skuId and st.deleted!= :deleted");
        query.setParameter("skuId", id);
        query.setParameter("deleted", com.uhuila.common.constants.DeletedStatus.DELETED);

        Long stockItemRemainCount = (Long) query.getSingleResult();
        renderJSON(stockItemRemainCount);
    }

    private static void checkStockOutCount(InventoryStock stock) {
        Query query = JPA.em().createQuery("SELECT SUM(st.remainCount) FROM InventoryStockItem st where st.sku.id= :skuId and st.deleted!= :deleted");
        query.setParameter("skuId", stock.sku.id);
        query.setParameter("deleted", com.uhuila.common.constants.DeletedStatus.DELETED);
        Long stockItemRemainCount = (Long) query.getSingleResult();
        if (stock.stockOutCount == null) {
            Validation.addError("stock.stockOutCount", "validation.required");
        } else if (stock == null || stockItemRemainCount == null || stock.stockOutCount < 0 || stockItemRemainCount < stock.stockOutCount) {
            Validation.addError("stock.stockOutCount", "validation.moreThanStockCount");
        } else if (stock.stockOutCount == 0) {
            Validation.addError("stock.stockOutCount", "validation.moreThanZero");
        }
    }

    private static void checkStockOutCountAndPrice(InventoryStock stock) {
        if (stock.stockOutCount == null) {
            Validation.addError("stock.stockOutCount", "validation.required");
        }
        if (stock.salePrice == null) {
            Validation.addError("stock.salePrice", "validation.required");
        }
    }

    private static void checkStockInCountAndPrice(InventoryStock stock) {
        if (stock.stockInCount == null) {
            Validation.addError("stock.stockInCount", "validation.required");
        }
        if (stock.originalPrice == null) {
            Validation.addError("stock.originalPrice", "validation.required");
        }
    }


}
