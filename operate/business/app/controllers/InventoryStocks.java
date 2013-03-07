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

    @ActiveNavigation("inventory_add")
    public static void add() {
        setInitParams();
        render();
    }

    @ActiveNavigation("inventory_add")
    public static void create(@Valid InventoryStock stock) {
        System.out.println(stock.supplier.id + "===stock.supplier.id>>");
        System.out.println(stock.remark + "===stock.remark>>");
        System.out.println(stock.saler + "===stock.saler>>");
        System.out.println(stock.storekeeper + "===stock.storekeeper>>");
        System.out.println(stock.actionType + "===stock.actionType>>");
        System.out.println(stock.effectiveAt + "===stock.effectiveAt>>");
        System.out.println(stock.expireAt + "===stock.expireAt>>");
        System.out.println(stock.stockInCount + "===stock.stockInCount>>");
        System.out.println(stock.originalPrice + "===stock.originalPrice>>");
        System.out.println(stock.sku.id + "===stock.sku.id>>");
        System.out.println(stock.sku + "===stock.sku>>");
        if (Validation.hasErrors()) {
            System.out.println(validation.errorsMap() + "===validation.errorsMap()>>");
            savePageParams(stock);
            render("InventoryStocks/add.html");
        }
        stock.createdBy = OperateRbac.currentUser().userName;

        InventoryStockItem stockItem = new InventoryStockItem(stock);
        stockItem.save();
        stockItem.create();
        System.out.println(stockItem + "===stockItem>>");
//        stockItem.save();
        stock.inventoryStockItemList = new LinkedList<>();
        stock.inventoryStockItemList.add(stockItem);
        stock.createdBy = OperateRbac.currentUser().loginName;
        stock.actionType = StockActionType.IN;
        System.out.println(stock.sku + "===111stock.sku>>");
        stock.save();
        stock.create();
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
        System.out.println(id + "===id>>");
        Query query = JPA.em().createQuery("SELECT SUM(st.remainCount) FROM InventoryStockItem st where st.sku.id= :skuId and st.deleted!= :deleted");
        query.setParameter("skuId", id);
        query.setParameter("deleted", com.uhuila.common.constants.DeletedStatus.DELETED);

        Long stockItemRemainCount = (Long) query.getSingleResult();
        System.out.println(stockItemRemainCount + "===stockItemRemainCount>>");
        renderJSON(stockItemRemainCount);

    }

}
