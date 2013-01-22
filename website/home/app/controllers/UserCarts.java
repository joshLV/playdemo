package controllers;
import cache.CacheCallBack;
import cache.CacheHelper;
import controllers.modules.website.cas.SecureCAS;
import controllers.modules.website.cas.annotations.SkipCAS;
import models.consumer.User;
import models.order.Cart;
import play.data.binding.As;
import play.modules.paginate.ValuePaginator;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;

import java.util.List;

/**
 * 购物车控制器.
 * <p/>
 * User: sujie
 * Date: 5/10/12
 * Time: 4:43 PM
 */
@With({SecureCAS.class, WebsiteInjector.class})
@SkipCAS
public class UserCarts extends Controller {
    private static final int TOP_LIMIT = 5;

    /**
     * 在顶部展示所有购物车内容
     */
    public static void tops() {

        final User user = SecureCAS.getUser();
        Http.Cookie cookie = request.cookies.get("identity");
        final String cookieValue = cookie == null ? null : cookie.value;

        List<Cart> cartList = Cart.findAll(user, cookieValue);  /* CacheHelper.getCache(Cart.getCartCacheKey(user, cookieValue), new CacheCallBack<List<Cart>>() {
            @Override
            public List<Cart> loadData() {
                return Cart.findAll(user, cookieValue);
            }
        }); */
        int count = 0;
        for (Cart cart : cartList) {
            count += cart.number;
        }
        if (cartList.size() <= TOP_LIMIT) {
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
            return;
        }

        if (goodsIds == null || goodsIds.size() == 0) {
            error(500, "no goods specified");
            return;
        }

        Cart.delete(user, cookieValue, goodsIds);

        ok();
    }
}