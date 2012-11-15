package factory.cms;

import com.uhuila.common.constants.DeletedStatus;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.cms.FriendsLink;
import models.cms.LinkStatus;


/**
 * <p/>
 * User: yanjy
 * Date: 12-11-15
 * Time: 上午10:16
 */
public class FriendsLinkFactory extends ModelFactory<FriendsLink> {
    @Override
    public FriendsLink define() {
        FriendsLink friendsLink = new FriendsLink();
        friendsLink.linkName = "link02";
        friendsLink.content = "This is yibaiquan link";
        friendsLink.userName = "Jim";
        friendsLink.link = "www.yibaiquan.com";
        friendsLink.status = LinkStatus.OPEN;
        friendsLink.deleted = DeletedStatus.UN_DELETED;
        return friendsLink;
    }

    @Factory(name = "SAVE")
    public FriendsLink defineSAVE(FriendsLink friendsLink) {
        friendsLink.status = LinkStatus.SAVE;
        friendsLink.deleted = DeletedStatus.UN_DELETED;
        friendsLink.linkName = "link04";
        friendsLink.link = "www.baidu.com";
        return friendsLink;
    }

    @Factory(name = "FORBID")
    public FriendsLink defineFORBID(FriendsLink friendsLink) {
        friendsLink.deleted = DeletedStatus.UN_DELETED;
        friendsLink.linkName = "link05";
        friendsLink.link = "www.uhuila.com";
        friendsLink.status = LinkStatus.FORBID;
        return friendsLink;
    }

}
