package controllers.real;

import com.uhuila.common.constants.DeletedStatus;
import controllers.OperateRbac;
import models.order.PurchaseItem;
import models.order.PurchaseOrder;
import models.order.Vendor;
import models.sales.InventoryStock;
import models.sales.InventoryStockItem;
import models.sales.Sku;
import models.sales.StockActionType;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 采购合同管理
 * <p/>
 * User: wangjia
 * Date: 13-3-29
 * Time: 下午2:53
 */
@With(OperateRbac.class)
@ActiveNavigation("purchase_orders_index")
public class PurchaseOrders extends Controller {

    @ActiveNavigation("purchase_orders_index")
    public static void index(String keyword) {
        int page = getPage();
        List<PurchaseOrder> purchaseOrderList = PurchaseOrder.findByCondition(keyword);
        for (PurchaseOrder purchaseOrder : purchaseOrderList) {
            purchaseOrder.totalPrice = BigDecimal.ONE.setScale(2);
            for (PurchaseItem item : purchaseOrder.purchaseItems) {
                purchaseOrder.totalPrice = purchaseOrder.totalPrice.add(item.price.multiply(BigDecimal.valueOf(item.count)).setScale(2, BigDecimal.ROUND_HALF_UP));
            }
        }
        render(purchaseOrderList, page, keyword);
    }

    private static int getPage() {
        String page = request.params.get("page");
        if (StringUtils.isNotEmpty(page) && (page.contains("?x-http-method-override=PUT") || page.contains("x-http-method-override=PUT"))) {
            page = page.replace("x-http-method-override=PUT", "").replace("?", "");
        }
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        return pageNumber;
    }

    @ActiveNavigation("purchase_orders_add")
    public static void add() {
        List<Vendor> vendorList = Vendor.findUnDeleted();
        List<Sku> skuList = Sku.findShiHuiUnDeleted();
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.create();
        purchaseOrder.deleted = DeletedStatus.DELETED;
        purchaseOrder.save();
        Long purchaseOrderId = purchaseOrder.id;
        List<PurchaseItem> purchaseItemList =
                PurchaseItem.find("purchaseOrder=? and deleted = ? ", purchaseOrder, DeletedStatus.UN_DELETED).fetch();
        renderArgs.put("createdBy", OperateRbac.currentUser().userName);
        render(vendorList, skuList, purchaseOrderId, purchaseItemList);
    }

    @ActiveNavigation("vendors_add")
    public static void create(@Valid PurchaseOrder purchaseOrder, Long purchaseOrderId) {
        if (purchaseOrder.vendor == null || purchaseOrder.vendor.id == null) {
            Validation.addError("purchaseOrder.vendor.id", "validation.selected");
        }
        if (Validation.hasErrors()) {
            List<PurchaseItem> purchaseItemList =
                    PurchaseItem.find("purchaseOrder.id=? and deleted = ? ", purchaseOrderId, DeletedStatus.UN_DELETED).fetch();
            List<Sku> skuList = Sku.findShiHuiUnDeleted();
            List<Vendor> vendorList = Vendor.findUnDeleted();

            render("real/PurchaseOrders/add.html", purchaseItemList, skuList, vendorList, purchaseOrder, purchaseOrderId);
        }
        PurchaseOrder existedPurchaseOrder = PurchaseOrder.findById(purchaseOrderId);
        existedPurchaseOrder.vendor = purchaseOrder.vendor;
        existedPurchaseOrder.invoiceType = purchaseOrder.invoiceType;
        existedPurchaseOrder.paymentType = purchaseOrder.paymentType;
        existedPurchaseOrder.signedAt = purchaseOrder.signedAt;
        existedPurchaseOrder.createdBy = OperateRbac.currentUser().userName;
        existedPurchaseOrder.deleted = DeletedStatus.UN_DELETED;
        existedPurchaseOrder.save();
        index(null);
    }

    public static void edit(Long id) {
        PurchaseOrder purchaseOrder = PurchaseOrder.findById(id);
        List<Vendor> vendorList = Vendor.findUnDeleted();
        List<PurchaseItem> purchaseItemList =
                PurchaseItem.find("purchaseOrder.id=? and deleted = ?", id, DeletedStatus.UN_DELETED).fetch();
        List<Sku> skuList = Sku.findShiHuiUnDeleted();
        Long purchaseOrderId = purchaseOrder.id;
        renderArgs.put("createdBy", OperateRbac.currentUser().userName);
        render(purchaseOrder, vendorList, purchaseItemList, skuList, purchaseOrderId);
    }

    public static void update(Long id, PurchaseOrder purchaseOrder) {


        PurchaseOrder updatePurchaseOrder = PurchaseOrder.findById(id);
        if (updatePurchaseOrder == null) {
            return;
        }
        if (purchaseOrder.vendor == null || purchaseOrder.vendor.id == null) {
            Validation.addError("purchaseOrder.vendor.id", "validation.selected");
        }
        if (Validation.hasErrors()) {
            List<PurchaseItem> purchaseItemList =
                    PurchaseItem.find("purchaseOrder.id=? and deleted = ? ", updatePurchaseOrder.id, DeletedStatus.UN_DELETED).fetch();
            List<Sku> skuList = Sku.findShiHuiUnDeleted();
            List<Vendor> vendorList = Vendor.findUnDeleted();
            Long purchaseOrderId = updatePurchaseOrder.id;
            render("real/PurchaseOrders/add.html", purchaseItemList, skuList, vendorList, purchaseOrder, purchaseOrderId);
        }
        updatePurchaseOrder.vendor = purchaseOrder.vendor;
        updatePurchaseOrder.invoiceType = purchaseOrder.invoiceType;
        updatePurchaseOrder.paymentType = purchaseOrder.paymentType;
        updatePurchaseOrder.signedAt = purchaseOrder.signedAt;
        updatePurchaseOrder.updatedBy = OperateRbac.currentUser().userName;
        updatePurchaseOrder.updatedAt = new Date();
        updatePurchaseOrder.deleted = DeletedStatus.UN_DELETED;
        updatePurchaseOrder.save();
        index(null);

    }


    public static void updateItem(Long purchaseOrderId, @Valid PurchaseItem item, PurchaseOrder purchaseOrder) {
        if (item.sku == null || item.sku.id == null) {
            Validation.addError("item.sku.id", "validation.selected");
        }
        if (Validation.hasErrors()) {
            List<PurchaseItem> purchaseItemList =
                    PurchaseItem.find("purchaseOrder.id=? and deleted = ? ", purchaseOrderId, DeletedStatus.UN_DELETED).fetch();
            List<Sku> skuList = Sku.findShiHuiUnDeleted();
            List<Vendor> vendorList = Vendor.findUnDeleted();
            renderArgs.put("createdBy", OperateRbac.currentUser().userName);
            render("real/PurchaseOrders/add.html", item, purchaseOrderId, purchaseItemList, skuList, vendorList);
        }
        PurchaseOrder currentPurchaseOrder = PurchaseOrder.findById(purchaseOrderId);
        if (purchaseOrder.vendor != null) {
            currentPurchaseOrder.vendor = purchaseOrder.vendor;
        }
        if (purchaseOrder.invoiceType != null) {
            currentPurchaseOrder.invoiceType = purchaseOrder.invoiceType;
        }
        if (purchaseOrder.paymentType != null) {
            currentPurchaseOrder.paymentType = purchaseOrder.paymentType;
        }
        if (purchaseOrder.signedAt != null) {
            currentPurchaseOrder.signedAt = purchaseOrder.signedAt;
        }
        currentPurchaseOrder.save();
        item.purchaseOrder = currentPurchaseOrder;

        item.sku.save();


        item.deleted = DeletedStatus.UN_DELETED;
        item.save();
        List<PurchaseItem> purchaseItemList =
                PurchaseItem.find("purchaseOrder.id=? and deleted = ? ", purchaseOrderId, DeletedStatus.UN_DELETED).fetch();
        List<Sku> skuList = Sku.findShiHuiUnDeleted();

        List<Vendor> vendorList = Vendor.findUnDeleted();
        purchaseOrder = PurchaseOrder.findById(purchaseOrderId);
        renderArgs.put("createdBy", OperateRbac.currentUser().userName);
        render("real/PurchaseOrders/add.html", purchaseItemList, skuList, vendorList, purchaseOrderId, purchaseOrder);
    }

    public static void deletePurchaseItem(Long itemId, Long purchaseOrderId) {
        PurchaseItem item = PurchaseItem.findById(itemId);
        if (item != null) {
            item.deleted = DeletedStatus.DELETED;
            item.save();
        }
        List<PurchaseItem> purchaseItemList =
                PurchaseItem.find("purchaseOrder.id=? and deleted = ?", purchaseOrderId, DeletedStatus.UN_DELETED).fetch();
        List<Sku> skuList = Sku.findShiHuiUnDeleted();
        List<Vendor> vendorList = Vendor.findUnDeleted();
        PurchaseOrder purchaseOrder = PurchaseOrder.findById(purchaseOrderId);
        renderArgs.put("createdBy", OperateRbac.currentUser().userName);
        render("real/PurchaseOrders/add.html", purchaseItemList, purchaseOrderId, skuList, vendorList, purchaseOrder);
    }

    public static void delete(Long id) {
        PurchaseOrder purchaseOrder = PurchaseOrder.findById(id);
        if (purchaseOrder == null) {
            return;
        }
        purchaseOrder.deleted = DeletedStatus.DELETED;
        purchaseOrder.save();
        index(null);
    }

    public static void export(long id) {
        PurchaseOrder purchaseOrder = PurchaseOrder.findById(id);
        List<PurchaseItem> purchaseItemList =
                PurchaseItem.find("purchaseOrder.id=? and deleted = ? ", id, DeletedStatus.UN_DELETED).fetch();
        purchaseOrder.totalPrice = new BigDecimal(0);
        for (PurchaseItem item : purchaseItemList) {
            item.discount = item.price.divide(item.sku.marketPrice).setScale(2, BigDecimal.ROUND_HALF_UP);
            item.totalPrice = item.price.multiply(BigDecimal.valueOf(item.count)).setScale(2, BigDecimal.ROUND_HALF_UP);
            purchaseOrder.totalPrice = purchaseOrder.totalPrice.add(new BigDecimal(item.totalPrice.toString()));
        }
//        purchaseOrder.totalPrice.add(item.totalPrice);
        render(purchaseOrder, purchaseItemList);
    }

    public static void createStockIn(Long id) {
        PurchaseOrder purchaseOrder = PurchaseOrder.findById(id);
        if (purchaseOrder == null) {
            return;
        }
        for (PurchaseItem item : purchaseOrder.purchaseItems) {
            InventoryStock stock = new InventoryStock();
            Sku sku = Sku.findById(item.sku.id);
            stock.supplier = sku.supplier;
            stock.createdBy = OperateRbac.currentUser().userName;
            stock.actionType = StockActionType.IN;
            stock.remark = "采购合同编号:" + purchaseOrder.serialNo;
            stock.create();
            InventoryStockItem stockItem = new InventoryStockItem(stock);
            stockItem.sku = item.sku;
            stockItem.remainCount = item.count;
            stockItem.changeCount = item.count;
            stockItem.price = item.price;
            stockItem.purchaseItem = item;
            stockItem.save();
        }
        purchaseOrder.stockIn = true;
        purchaseOrder.save();
        index(null);
    }
}
