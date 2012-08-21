package controllers;

import models.sales.SecKillGoodsCondition;
import models.sales.SecKillGoodsItem;
import models.sales.SecKillGoodsStatus;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.binding.As;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

/**
 * <p/>
 * User: yanjy
 * Date: 12-8-15
 * Time: 上午11:18
 */
@With(OperateRbac.class)
@ActiveNavigation("seckill_goods_index")
public class SecKillGoodsItems extends Controller {
    public static int PAGE_SIZE = 15;

    /**
     * 展示添加秒杀活动页面
     */
    @ActiveNavigation("seckill_goods_index")
    public static void index(Long seckillId, SecKillGoodsCondition condition) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        if (condition == null) {
            condition = new SecKillGoodsCondition();

        }

        JPAExtPaginator<SecKillGoodsItem> secKillGoodsItems = SecKillGoodsItem.findByCondition(condition, seckillId, pageNumber,
                PAGE_SIZE);
        secKillGoodsItems.setBoundaryControlsEnabled(true);
        models.sales.SecKillGoods secKillGoods = models.sales.SecKillGoods.findById(seckillId);
        render(secKillGoodsItems, secKillGoods, seckillId, condition);
    }

    @ActiveNavigation("seckill_goods_add")
    public static void add(Long seckillId) {
        models.sales.SecKillGoods secKillGoods = models.sales.SecKillGoods.findById(seckillId);
        render(seckillId, secKillGoods);
    }

    public static void create(Long seckillId, @Valid SecKillGoodsItem secKillGoodsItem) {
        if (Validation.hasErrors()) {
            models.sales.SecKillGoods secKillGoods = models.sales.SecKillGoods.findById(seckillId);
            render("SecKillGoodsItems/add.html", secKillGoodsItem, secKillGoods, seckillId);
        }

        models.sales.SecKillGoods goods = models.sales.SecKillGoods.findById(seckillId);
        secKillGoodsItem.secKillGoods = goods;

        secKillGoodsItem.save();

        index(seckillId, null);
    }

    public static void edit(Long seckillId, Long id) {
        SecKillGoodsItem secKillGoodsItem
                = SecKillGoodsItem.findById(id);
        models.sales.SecKillGoods secKillGoods = models.sales.SecKillGoods.findById(seckillId);
        render(secKillGoodsItem, seckillId, secKillGoods);
    }

    public static void update(Long id, Long seckillId, @Valid SecKillGoodsItem secKillGoodsItem) {
        checkExpireAt(secKillGoodsItem);
        if (Validation.hasErrors()) {
            models.sales.SecKillGoods secKillGoods = models.sales.SecKillGoods.findById(seckillId);
            render("SecKillGoodsItems/edit.html", secKillGoodsItem, secKillGoods, id, seckillId);
        }

        SecKillGoodsItem.update(id, secKillGoodsItem);

        index(seckillId, null);
    }

    private static void checkExpireAt(SecKillGoodsItem goods) {

        if (goods.secKillBeginAt != null && goods.secKillEndAt != null && goods.secKillEndAt.before(goods.secKillBeginAt)) {

            Validation.addError("secKillGoodsItem.secKillEndAt", "validation.beforeThanSecKillBeginAt");
        }

    }

    /**
     * 下架.
     *
     * @param id 秒杀活动ID
     * @param id 秒杀活动子ID
     */
    public static void offSale(Long seckillId, Long id) {
        SecKillGoodsItem.updateStatus(SecKillGoodsStatus.OFFSALE, id);
        index(seckillId, null);
    }

    /**
     * 上架.
     *
     * @param id 秒杀活动ID
     * @param id 秒杀活动子ID
     */
    public static void onSale(Long seckillId, Long id) {
        SecKillGoodsItem.updateStatus(SecKillGoodsStatus.ONSALE, id);
        index(seckillId, null);
    }

    /**
     * 删除指定商品
     *
     * @param id 商品ID
     */
    public static void delete(Long seckillId, Long id) {
        SecKillGoodsItem goods = SecKillGoodsItem.findById(id);
        goods.delete();
        index(seckillId, null);
    }
}