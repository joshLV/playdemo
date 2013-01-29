package controllers;

import controllers.modules.website.cas.SecureCAS;
import controllers.modules.website.cas.annotations.SkipCAS;
import models.consumer.User;
import models.consumer.UserWebIdentification;
import models.order.Cart;
import models.order.Order;
import models.order.OrderItems;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.data.binding.As;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车控制器，提供http接口对购物车进行增删该查
 *
 * @author likang
 */
@With({SecureCAS.class, WebsiteInjector.class})
@SkipCAS
public class Carts extends Controller {
    public static final int TOP_LIMIT = 5;

    /**
     * 购物车主界面
     */
    public static void index() {
        User user = SecureCAS.getUser();
        Http.Cookie cookie = request.cookies.get("identity");
        String cookieValue = cookie == null ? null : cookie.value;
        List<Cart> carts = Cart.findAll(user, cookieValue);
        //显示最近浏览过的商品
        cookie = request.cookies.get("saw_goods_ids");
        String sawGoodsIds = cookie == null ? "" : cookie.value;
        List<Long> goodsIds = new ArrayList<>();

        for (String goodsId : sawGoodsIds.split(",")) {
            if (StringUtils.isNotEmpty(goodsId) && goodsIds.size() < 5) {
                goodsIds.add(new Long(goodsId));
            }
        }
        List<models.sales.Goods> sawGoodsList = goodsIds.size() > 0 ? models.sales.Goods.findInIdList(goodsIds) : new ArrayList<models.sales.Goods>();
        //登陆的场合，判断该会员是否已经购买过此限购商品
        if (user != null) {
            //该用户曾经购买该商品的数量
            Long boughtNumber = 0l;

            for (Cart cart : carts) {
                boughtNumber = OrderItems.itemsNumber(user, cart.goods.id);
                boolean canNotBuy = Order.checkLimitNumber(cart.goods.id, boughtNumber, (int) cart.number);

                if (canNotBuy) {
                    renderArgs.put("limit_goodsId", cart.goods.id);
                }
            }
        }

        render(user, carts, sawGoodsList);
    }

    /**
     * 加入或修改购物车列表
     *
     * @param goodsId   商品ID
     * @param increment 购物车中商品数增量，
     *                  若购物车中无此商品，则新建条目
     *                  若购物车中有此商品，且商品数量加增量小于等于0，视为无效
     */
    public static void order(long goodsId, int increment) {
        User user = SecureCAS.getUser();
        Http.Cookie cookie = request.cookies.get("identity");
        String cookieValue = cookie == null ? null : cookie.value;
        models.sales.Goods goods = models.sales.Goods.findById(goodsId);
        if (goods == null) {
            error(500, "no such goods: " + goodsId);
        }

        if (user == null && cookie == null) {
            error(500, "can not identity current user");
        }

        Cart.order(user, cookieValue, goods, increment);

        List<Cart> carts = Cart.findAll(user, cookieValue);
        BigDecimal amount = BigDecimal.ZERO;
        int count = 0;
        for (Cart cart : carts) {
            amount = amount.add(cart.goods.salePrice.multiply(new BigDecimal(cart.number)));
            count += cart.number;
        }
        if (Play.mode != Play.Mode.DEV && increment > 0 && WebsiteInjector.getUserWebIdentification() != null) {
            UserWebIdentification uwi = UserWebIdentification.findOne(WebsiteInjector.getUserWebIdentification().cookieId);
            if (uwi == null) {
                uwi = WebsiteInjector.getUserWebIdentification();
                uwi.save();
            }
            if (uwi.cartCount == null) {
                uwi.cartCount = 0;
            }
            uwi.cartCount += increment;
            uwi.save();
        }
        renderJSON("{\"count\":" + count + ", \"amount\":\"" + amount + "\"}");
    }

    /**
     * 在顶部展示所有购物车内容,最多显示5条购物车记录.
     */
    public static void tops() {
        final User user = SecureCAS.getUser();
        Http.Cookie cookie = request.cookies.get("identity");
        final String cookieValue = cookie == null ? null : cookie.value;
        List<Cart> cartList = Cart.findAll(user, cookieValue); /* CacheHelper.getCache(Cart.getCartCacheKey(user, cookieValue), new CacheCallBack<List<Cart>>() {
            @Override
            public List<Cart> loadData() {
                return Cart.findAll(user, cookieValue);
            }
        }); */


        int count = 0;
        for (Cart cart : cartList) {
            count += cart.number;
        }


        if (cartList.size() <= 5) {
            renderArgs.put("carts", cartList);
        } else {

            ValuePaginator<Cart> carts = new ValuePaginator<>(cartList);
            carts.setPageNumber(1);
            carts.setPageSize(TOP_LIMIT);


            renderArgs.put("carts", carts);
        }

        renderArgs.put("count", count);
        render();
    }

    /**
     * 从购物车中删除指定商品列表
     *
     * @param goodsIds 商品列表
     */
    public static void delete(@As(",") List<Long> goodsIds) {
        User user = SecureCAS.getUser();
        Http.Cookie cookie = request.cookies.get("identity");
        String cookieValue = cookie == null ? null : cookie.value;

        if (user == null && cookie == null) {
            error(500, "can not identity current user");
        }

        if (goodsIds == null || goodsIds.size() == 0) {
            error(500, "no goods specified");
        }

        Cart.delete(user, cookieValue, goodsIds);

        ok();
    }
}
