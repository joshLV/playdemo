package controllers;

import models.sales.Category;
import models.sales.CategorySerializer;
import play.mvc.Controller;

import java.util.List;

/**
 * 商品分类的控制器.
 * <p/>
 * User: sujie
 * Date: 3/12/12
 * Time: 10:42 AM
 */
public class Categories extends Controller {

    public static void showSubs(Long id) {
        List<Category> categories = Category.findByParent(id);

        renderJSON(categories, new CategorySerializer());
    }

}