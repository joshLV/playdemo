package controllers.real;

import cache.CacheCallBack;
import cache.CacheHelper;
import controllers.OperateRbac;
import models.order.ExpressCompany;
import models.order.OuterOrderPartner;
import models.sales.Goods;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;
import java.util.Map;

/**
 * 快递公司管理
 * User: wangjia
 * Date: 13-3-13
 * Time: 上午10:56
 */
@With(OperateRbac.class)
@ActiveNavigation("express_company_index")
public class ExpressCompanies extends Controller {
    public static int PAGE_SIZE = 15;

    @ActiveNavigation("express_company_index")
    public static void index() {
        int pageNumber = getPage();
        List<ExpressCompany> expressList = ExpressCompany.findAll();
        render(expressList);
    }

    @ActiveNavigation("express_company_add")
    public static void add() {
        render();
    }

    @ActiveNavigation("express_company_add")
    public static void create(@Valid ExpressCompany express) {
        if (Validation.hasErrors()) {
            render("real/ExpressCompanies/add.html");
        }
        express.create();
        index();
    }

    public static void edit(Long id) {
        ExpressCompany express = ExpressCompany.findById(id);
        render(express);
    }

    public static void update(Long id, @Valid ExpressCompany express) {
        if (Validation.hasErrors()) {
            render("real/ExpressCompanies/edit.html", express, id);
        }
        ExpressCompany.update(id, express);
        index();
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