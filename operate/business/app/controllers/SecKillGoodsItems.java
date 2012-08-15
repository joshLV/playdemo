package controllers;

import models.sales.SecKillGoodsItem;
import operate.rbac.annotations.ActiveNavigation;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

/**
 * <p/>
 * User: yanjy
 * Date: 12-8-15
 * Time: 上午11:18
 */
@With(OperateRbac.class)
@ActiveNavigation("seckill_goods_add")
public class SecKillGoodsItems extends Controller {

    /**
     * 展示添加商品页面
     */
    public static void index(Long seckillId) {
        models.sales.SecKillGoods secKillGoods = models.sales.SecKillGoods.findById(seckillId);
        render(secKillGoods);
    }

    public static void add(Long seckillId) {
        render(seckillId);
    }

    public static void create(@Valid SecKillGoodsItem secKillGoodsItem) {
        if (Validation.hasErrors()) {
            render("SecKillGoodsItems/add.html");
        }

        models.sales.SecKillGoods goods = models.sales.SecKillGoods.findById(secKillGoodsItem.secKillGoods.id);
        secKillGoodsItem.secKillGoods = goods;

        secKillGoodsItem.save();

        render("SecKillGoods/index.html");
    }

    public static void edit(Long id) {

        SecKillGoodsItem secKillGoodsItem
                = SecKillGoodsItem.findById(id);

        render(secKillGoodsItem);
    }

    public static void update(Long id, @Valid SecKillGoodsItem secKillGoodsItem) {
        System.out.println(">>>>>>>>>>>>" + secKillGoodsItem.secKillEndAt);
        checkExpireAt(secKillGoodsItem);

        if (Validation.hasErrors()) {
            render("SecKillGoodsItems/edit.html", secKillGoodsItem, id);
        }

        SecKillGoodsItem.update(id, secKillGoodsItem);

        render("SecKillGoods/index.html");
    }

    private static void checkExpireAt(SecKillGoodsItem goods) {
        if (goods.secKillBeginAt != null && goods.secKillEndAt != null && goods.secKillEndAt.before(goods.secKillBeginAt)) {
            Validation.addError("SecKillGoodsItem.secKillAt", "validation.beforeThanEffectiveAt");
        }

    }
}
