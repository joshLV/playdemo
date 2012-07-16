package models.cms;

import com.uhuila.common.constants.DeletedStatus;
import play.data.validation.*;
import play.db.jpa.Model;
import play.modules.paginate.ModelPaginator;
import play.modules.view_ext.annotation.Mobile;
import cache.CacheCallBack;
import cache.CacheHelper;

import javax.persistence.*;
import java.util.List;

/**
 * TODO.
 * <p/>
 * User: yanjy
 * Date: 12-7-13
 * Time: 上午11:29
 */
@Entity
@Table(name = "friends_link")
public class FriendsLink extends Model {

    private static final long serialVersionUID = 80131405113012L;

    /**
     * 链接名称
     */
    @Column(name = "link_name")
    @Required
    @MinSize(1)
    @MaxSize(100)
    public String linkName;

    /**
     * 链接
     */
    @Required
    public String link;

    @Column(name = "user_name")
    public String userName;
    @Email
    public String email;

    @Mobile
    public String mobile;

    @Match(value = "[1-9][0-9]{4,}")
    public String qq;

    @Enumerated(EnumType.STRING)
    public DeletedStatus deleted;

    public Integer displayOrder;
    public static final String CACHEKEY = "FRIENDS_LINK";

    @Override
    public void _save() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        super._save();
    }

    @Override
    public void _delete() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        super._delete();
    }

    public static ModelPaginator getPage(int pageNumber, int pageSize) {
        ModelPaginator friendsList = new ModelPaginator<>(FriendsLink.class, "deleted = ?",
                DeletedStatus.UN_DELETED).orderBy("displayOrder desc");
        friendsList.setPageNumber(pageNumber);
        friendsList.setPageSize(pageSize);
        return friendsList;
    }

    public static void update(Long id, FriendsLink friendsLinks) {
        FriendsLink friendsLink = FriendsLink.findById(id);
        friendsLink.email = friendsLinks.email;
        friendsLink.displayOrder = friendsLinks.displayOrder;
        friendsLink.link = friendsLinks.link;
        friendsLink.linkName = friendsLinks.linkName;
        friendsLink.mobile = friendsLinks.mobile;
        friendsLink.qq = friendsLinks.qq;
        friendsLink.save();
    }

    public static List<FriendsLink> findAllByDeleted() {
        List<FriendsLink> friendsLinks = FriendsLink.find("deleted=? order by displayOrder desc", DeletedStatus.UN_DELETED).fetch();
        return friendsLinks;
    }
}
