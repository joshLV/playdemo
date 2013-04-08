package controllers;

import models.sales.Brand;
import models.sales.InventoryStock;
import models.sales.InventoryStockItem;
import models.sales.InventoryStockItemCondition;
import models.sales.Sku;
import models.sales.StockActionType;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.beans.Transient;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * 库存管理
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
    public static void index(InventoryStockItemCondition condition) {
        int pageNumber = getPage();
        if (condition == null) {
            condition = new InventoryStockItemCondition();
        }
        JPAExtPaginator<InventoryStockItem> stockItemList = InventoryStockItem.findByCondition(condition, pageNumber,
                PAGE_SIZE);
        stockItemList.setBoundaryControlsEnabled(true);
        Long id = OperateRbac.currentUser().id;
        List<Brand> brandList = Brand.findByOrder(null, id);
        List<Sku> skuList = Sku.findShiHuiUnDeleted();
        render(stockItemList, skuList, brandList, pageNumber, condition);
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
    public static void createStockOut(@Valid InventoryStockItem stockItem) {
        checkStockOutCount(stockItem);
        if (Validation.hasErrors()) {
            setInitParams();
            render("InventoryStocks/stockOut.html");
        }
        InventoryStock stock = stockItem.stock;
        Sku sku = Sku.findById(stockItem.sku.id);
        stock.supplier = sku.supplier;
        stock.createdBy = OperateRbac.currentUser().userName;
        stock.actionType = StockActionType.OUT;
        stock.create();
        stockItem.stock = stock;
        Long changeCount = stockItem.changeCount;
        stockItem.changeCount = 0 - changeCount;
        stockItem.createdAt = new Date();
        stockItem.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED;
        stockItem.save();
        List<InventoryStockItem> stockInItemList = InventoryStockItem.find("sku=? and remainCount>0 order by createdAt", stockItem.sku).fetch();
        Long totalStockOutCount = 0 - stockItem.changeCount;
        for (InventoryStockItem item : stockInItemList) {
            item.remainCount -= totalStockOutCount;
            if (item.remainCount < 0) {
                totalStockOutCount = -item.remainCount;
                item.remainCount = 0l;
                item.save();
            } else {
                item.save();
                break;
            }
        }
        index(null);
    }


    @ActiveNavigation("inventory_stockIn")
    public static void createStockIn(@Valid InventoryStockItem stockItem) {
        if (Validation.hasErrors()) {
            setInitParams();
            render("InventoryStocks/stockIn.html");
        }
        InventoryStock stock = stockItem.stock;
        Sku sku = Sku.findById(stockItem.sku.id);
        stock.supplier = sku.supplier;
        stock.createdBy = OperateRbac.currentUser().userName;
        stock.actionType = StockActionType.IN;
        stock.create();
        stockItem.stock = stock;
        stockItem.remainCount = stockItem.changeCount;
        stockItem.createdAt = new Date();
        stockItem.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED;
        stockItem.save();
        index(null);
    }

    private static void setInitParams() {
        List<Sku> skuList = Sku.findShiHuiUnDeleted();
        renderArgs.put("skuList", skuList);
        renderArgs.put("createdBy", OperateRbac.currentUser().userName);
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
        Sku sku = Sku.findById(id);
        renderJSON(sku.getRemainCount());
    }


    private static void checkStockOutCount(InventoryStockItem stockItem) {
        Long stockItemRemainCount = stockItem.sku.getRemainCount();
        if (stockItemRemainCount == null || stockItem.changeCount < 0 || stockItemRemainCount < stockItem.changeCount) {
            Validation.addError("stockItem.changeCount", "validation.moreThanStockCount");
        } else if (stockItem.changeCount == 0) {
            Validation.addError("stockItem.changeCount", "validation.moreThanZero");
        }
    }


}
