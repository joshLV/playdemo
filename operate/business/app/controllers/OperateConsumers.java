package controllers;

import models.consumer.User;
import models.consumer.UserCondition;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 用户管理控制器.
 * <p/>
 * User: sujie
 * Date: 5/15/12
 * Time: 4:12 PM
 */
@With(OperateRbac.class)
@ActiveNavigation("consumers_index")
public class OperateConsumers extends Controller {

    private static final int PAGE_SIZE = 20;

    public static void index(UserCondition condition) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        if (condition == null) {
            condition = new UserCondition();
        }
        JPAExtPaginator<User> users = User.findByCondition(condition, pageNumber,
                PAGE_SIZE);


        render(users, condition);
    }

    public static void show(long id) {
        User user = User.findById(id);
        render(user);
    }


    public static void freeze(long id) {
        User.freeze(id);
        index(null);
    }

    public static void unfreeze(long id) {
        User.unfreeze(id);
        index(null);
    }
}