package controllers.real;

import controllers.OperateRbac;
import models.order.ExpressCompany;
import models.order.OuterOrderPartner;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 快递公司管理
 * User: wangjia
 * Date: 13-3-13
 * Time: 上午10:56
 */
@With(OperateRbac.class)
@ActiveNavigation("express_company_index")
public class ExpressCompanies extends Controller {
    public static void index() {
        int pageNumber = getPage();

//        JPAExtPaginator<ExpressCompany> stockItemList = InventoryStockItem.findByCondition(condition, pageNumber,
//                PAGE_SIZE);
//        stockItemList.setBoundaryControlsEnabled(true);
//        Long id = OperateRbac.currentUser().id;
//        List<Brand> brandList = Brand.findByOrder(null, id);
//        List<Sku> skuList = Sku.findUnDeleted();
//        render(stockItemList, skuList, brandList, pageNumber, condition);

    }

    @ActiveNavigation("express_company_add")
    public static void add() {
//        setInitParams();
        render();
    }

    @ActiveNavigation("express_company_add")
    public static void create(@Valid ExpressCompany express) {
        if (Validation.hasErrors()) {
//            savePageParams(sku);
            render("Skus/add.html");
        }
//        sku.create();
//        index(null);
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