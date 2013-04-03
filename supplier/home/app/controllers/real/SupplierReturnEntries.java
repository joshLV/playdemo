package controllers.real;

import controllers.SupplierRbac;
import controllers.supplier.SupplierInjector;
import models.admin.SupplierUser;
import models.order.OrderItems;
import models.order.RealGoodsReturnEntry;
import models.order.RealGoodsReturnEntryCondition;
import models.order.RealGoodsReturnStatus;
import models.sales.Goods;
import models.sales.InventoryStock;
import models.sales.InventoryStockItem;
import models.sales.StockActionType;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * 商户退货单管理.
 * <p/>
 * User: sujie
 * Date: 4/2/13
 * Time: 3:12 PM
 */
@With({SupplierRbac.class, SupplierInjector.class})
public class SupplierReturnEntries extends Controller {

    private static final int PAGE_SIZE = 20;
    /**
     * 查看待处理的商户的实物退货单.
     */
    public static void index(RealGoodsReturnEntryCondition condition) {
        SupplierUser supplierUser = SupplierRbac.currentUser();
        Long supplierId = supplierUser.supplier.id;
        if (condition == null) {
            condition = new RealGoodsReturnEntryCondition(supplierId, RealGoodsReturnStatus.HANDLING);
        }
        condition.supplierId = supplierId;

        final int page = getPage();
        JPAExtPaginator<RealGoodsReturnEntry> entryPage = RealGoodsReturnEntry.getPage(condition, page, PAGE_SIZE);

        List<models.sales.Goods> goodsList = models.sales.Goods.findDistinctShortNameBySupplierId(supplierId);

        render(entryPage, condition, goodsList);
    }

    /**
     * 仓库管理员确认收到货.
     *
     * @param id
     */
    public static void received(Long id) {
        //1、修改退货单状态.
        RealGoodsReturnEntry entry = RealGoodsReturnEntry.findById(id);
        entry.status = RealGoodsReturnStatus.RETURNED;
        entry.save();
        //3、退款
        OrderItems.handleRefund(entry.orderItems, entry.returnedCount);

        index(null);
    }

    private static int getPage() {
        String page = request.params.get("page");
        if (StringUtils.isNotEmpty(page) && (page.contains("?x-http-method-override=PUT") || page.contains("x-http-method-override=PUT"))) {
            page = page.replace("x-http-method-override=PUT", "").replace("?", "");
        }
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        return pageNumber;
    }

}
