package unit;

import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.cms.FriendsLink;
import models.cms.LinkStatus;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.ModelPaginator;
import play.test.UnitTest;

/**
 * User: Juno
 * Date: 12-7-26
 * Time: 上午9:53
 */
public class FriendsLinkUnitTest extends UnitTest {
    FriendsLink friendsLink;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        friendsLink = FactoryBoy.create(FriendsLink.class);
    }

    @Test
    public void testGetPage() {
        FactoryBoy.batchCreate(5, FriendsLink.class,
                new SequenceCallback<FriendsLink>() {
                    @Override
                    public void sequence(FriendsLink target, int seq) {
                        target.content = "conten_" + seq;

                    }

                });
        ModelPaginator page = FriendsLink.getPage(1, 15);
        assertEquals(6, page.size());
    }

    @Test
    public void testUpdate() {
        assertEquals(LinkStatus.OPEN, friendsLink.status);

        friendsLink.linkName = "link01";
        friendsLink.link = "www.uhuila.com";
        friendsLink.content = "This is uhuila link";
        friendsLink.userName = "uhuila";
        friendsLink.status = LinkStatus.FORBID;
        friendsLink.save();
        //内容更新
        FriendsLink.update(friendsLink.id, friendsLink);
        friendsLink.refresh();
        friendsLink.status = LinkStatus.FORBID;
        assertEquals("link01", friendsLink.linkName);
        assertEquals("www.uhuila.com", friendsLink.link);
        assertEquals("uhuila", friendsLink.userName);
        assertEquals("This is uhuila link", friendsLink.content);
    }

    @Test
    public void testIsExist() {
        String validLink = "baidu";
        assertEquals("", FriendsLink.isExisted(friendsLink.id, validLink));

        friendsLink.link = "www.baidu.com";
        friendsLink.save();

        final FriendsLink f1 = FactoryBoy.create(FriendsLink.class, "SAVE");
        assertEquals("注意该URL已经存在！", FriendsLink.isExisted(f1.id, f1.link));

        friendsLink.link = "www.uhuila.com";
        friendsLink.status = LinkStatus.FORBID;
        friendsLink.save();
        final FriendsLink f2 = FactoryBoy.create(FriendsLink.class, "FORBID");

        assertEquals("注意该URL已经禁止！", FriendsLink.isExisted(f2.id, f2.link));

    }
}
