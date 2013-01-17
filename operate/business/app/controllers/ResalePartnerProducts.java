package controllers;

import models.admin.OperateUser;
import models.resale.ResalerProduct;
import models.resale.ResalerProductJournal;
import models.sales.*;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 * Date: 13-1-8
 */
@With(OperateRbac.class)
@ActiveNavigation("resale_partner_product")
public class ResalePartnerProducts extends Controller {
    public static int PAGE_SIZE = 20;

    @ActiveNavigation("resale_partner_product")
    public static void index(GoodsCondition condition) {
        int pageNumber = getPage();
        if (condition == null) {
            condition = new GoodsCondition();
        }

        JPAExtPaginator<models.sales.Goods> goodsPage = models.sales.Goods.findByCondition(condition, pageNumber,
                PAGE_SIZE);
        goodsPage.setBoundaryControlsEnabled(true);

        List<Supplier> supplierList = Supplier.findUnDeleted();
        Map<String, List<ResalerProduct> > partnerProducts = new HashMap<>();
        for(models.sales.Goods goods : goodsPage) {
            List<ResalerProduct> products = ResalerProduct.find("byGoods", goods).fetch();
            for (ResalerProduct product : products) {
                List<ResalerProduct> p = partnerProducts.get(goods.id + product.partner.toString());
                if (p == null) {
                    p = new ArrayList<>();
                    partnerProducts.put(goods.id + product.partner.toString(), p);
                }
                System.out.println(goods.id + product.partner.toString() + ":" + product.goods.shortName + ":" + product.goods.code);
                p.add(product);
            }
        }

        render(goodsPage, condition, supplierList, partnerProducts);
    }

    private static int getPage() {
        String page = request.params.get("page");
        return StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
    }

    @ActiveNavigation("resale_partner_product")
    public static void journal(Long productId) {
        ResalerProduct product = ResalerProduct.findById(productId);
        List<ResalerProductJournal> journals = ResalerProductJournal.find("byProduct", product).fetch();
        for(ResalerProductJournal journal : journals) {
            journal.operator = ((OperateUser)OperateUser.findById(journal.operatorId)).userName;
        }
        render(journals);
    }
    @ActiveNavigation("resale_partner_product")
    public static void journalJson(Long journalId) {
        ResalerProductJournal journal = ResalerProductJournal.findById(journalId);
        if (journal == null) {
            notFound();
        }
        render(journal);
    }

}
