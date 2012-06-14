package controllers;

import models.cms.Promotion;
import models.cms.PromotionCondition;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.Scope;
import play.mvc.With;

/**
 * @author likang
 */
@With(OperateRbac.class)
@ActiveNavigation("promotion_list")
public class Promotions extends Controller{
    private static final int PAGE_SIZE = 20;

    @ActiveNavigation("promotion_list")
    public static void index(PromotionCondition condition){
        String page = request.params.get("page");
        int pageNumber =  StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        if(condition == null){
            condition = new PromotionCondition();
        }

        JPAExtPaginator<Promotion> promotionPage = Promotion.findByCondition(condition,
                pageNumber, PAGE_SIZE);

        render(promotionPage, condition);
    }

    public static void create(@Valid Promotion promotion){
        if (Validation.hasErrors()){
            Validation.keep();
            add(promotion);
        }
        promotion.save();
        index(null);
    }

    @ActiveNavigation("promotion_list")
    public static void add(Promotion promotion){
        render(promotion);
    }

    @ActiveNavigation("promotion_list")
    public static void detail(long id){
        Promotion promotion = Promotion.findById(id);
        render(promotion);
    }
}
