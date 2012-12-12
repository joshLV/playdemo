package controllers;

import models.sales.Goods;
import models.sales.GoodsCondition;
import models.sales.GoodsSchedule;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;


/**
 * <p/>
 * User: yanjy
 * Date: 12-12-10
 * Time: 上午11:03
 */
@With(OperateRbac.class)
@ActiveNavigation("goods_schedule_index")
public class GoodsSchedules extends Controller {
    public static int PAGE_SIZE = 15;

    public static void index(GoodsCondition condition) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        if (condition == null) {
            condition = new GoodsCondition();
        }
        JPAExtPaginator<GoodsSchedule> goodsPage = GoodsSchedule.findByCondition(condition, pageNumber,
                PAGE_SIZE);
        goodsPage.setBoundaryControlsEnabled(true);

        render(goodsPage, condition);
    }

    @ActiveNavigation("goods_schedule_add")
    public static void add() {
        render();
    }

    @ActiveNavigation("goods_schedule_add")
    public static void create(@Valid GoodsSchedule goodsSchedule) {
        checkItems(goodsSchedule, "add.html");
        goodsSchedule.createdAt = new Date();
        goodsSchedule.save();
        index(null);
    }

    public static void update(Long id, @Valid GoodsSchedule goodsSchedule) {
        checkItems(goodsSchedule, "edit.html");
        GoodsSchedule.update(id, goodsSchedule);
        index(null);
    }

    public static void edit(Long id) {
        GoodsSchedule goodsSchedule = GoodsSchedule.findById(id);
        String goodsName = "商品名：" + goodsSchedule.goods.shortName;
        render(goodsSchedule, goodsName, id);
    }

    /**
     * 取得商品名字
     *
     * @param id
     */
    public static void getName(Long id) {
        Goods goods = Goods.findById(id);
        if (goods != null) {
            renderText(goods.shortName);
        }
    }

    private static void checkItems(GoodsSchedule goodsSchedule, String page) {
        String goodsName = "";
        if (goodsSchedule.goods == null) {
            Validation.addError("goodsSchedule.goods.id", "validation.required");
        } else {
            goodsName = "商品名：" + goodsSchedule.goods.shortName;
        }
        if (goodsSchedule.effectiveAt != null && goodsSchedule.expireAt != null && goodsSchedule.expireAt.before(goodsSchedule.effectiveAt)) {
            Validation.addError("goodsSchedule.expireAt", "validation.beforeThanEffectiveAt");
        }
        if (Validation.hasErrors()) {
            render("GoodsSchedules/" + page, goodsName);
        }
    }

    /**
     * 删除指定商品
     *
     * @param id 商品ID
     */
    public static void delete(Long id) {
        GoodsSchedule goods = GoodsSchedule.findById(id);
        goods.delete();
        index(null);
    }

}
