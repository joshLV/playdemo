package controllers;

import models.sales.Category;
import models.sales.CategorySerializer;
import play.mvc.Controller;

import java.util.List;
import play.mvc.With;
import navigation.annotations.ActiveNavigation;
import controllers.supplier.cas.SecureCAS;

/**
 * 商品分类的控制器.
 * <p/>
 * User: sujie
 * Date: 3/12/12
 * Time: 10:42 AM
 */
@With({SecureCAS.class, MenuInjector.class})
@ActiveNavigation("goods_index")
public class Categories extends Controller {

    public static void showSubs(Long id) {
        List<Category> categories = Category.findByParent(id);

        renderJSON(categories, new CategorySerializer());
    }

}
