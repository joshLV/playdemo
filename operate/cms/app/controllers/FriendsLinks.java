package controllers;

import com.uhuila.common.constants.DeletedStatus;
import models.cms.FriendsLink;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.ModelPaginator;
import play.mvc.Controller;
import play.mvc.With;

/**
 * <p/>
 * User: yanjy
 * Date: 12-7-13
 * Time: 上午11:05
 */

@With(OperateRbac.class)
@ActiveNavigation("friends_index")
public class FriendsLinks extends Controller {
    private static final int PAGE_SIZE = 15;

    public static void index() {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        ModelPaginator friendsLinkList = FriendsLink.getPage(pageNumber, PAGE_SIZE);

        render(friendsLinkList);
    }

    @ActiveNavigation("friends_add")
    public static void add() {
        render();
    }

    public static void create(@Valid FriendsLink friendsLinks) {
        if (Validation.hasErrors()) {
            render("FriendsLinks/add.html", friendsLinks);
        }
        friendsLinks.deleted = DeletedStatus.UN_DELETED;
        friendsLinks.save();
        index();
    }

    public static void edit(Long id) {
        FriendsLink friendsLinks = FriendsLink.findById(id);
        render(friendsLinks);
    }

    public static void update(Long id, @Valid FriendsLink friendsLinks) {
        if (Validation.hasErrors()) {
            render("FriendsLinks/edit.html", friendsLinks);
        }
        FriendsLink.update(id, friendsLinks);
        index();
    }

    public static void delete(Long id) {
        FriendsLink friendsLink = FriendsLink.findById(id);
        friendsLink.deleted = DeletedStatus.DELETED;
        friendsLink.save();
        index();
    }
}
