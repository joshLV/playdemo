package unit;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.constants.PlatformType;
import play.test.Fixtures;
import play.test.UnitTest;
import org.junit.Before;
import org.junit.Test;
import models.cms.FriendsLink;
import play.modules.paginate.ModelPaginator;

/**
 * Created with IntelliJ IDEA.
 * User: Juno
 * Date: 12-7-26
 * Time: 上午9:53
 * To change this template use File | Settings | File Templates.
 */
public class FriendsLinkUnitTest extends UnitTest {

    @Before
    public void setup(){
        Fixtures.delete(FriendsLink.class);
        Fixtures.loadModels("fixture/FriendsLink.yml");
    }

    @Test
    public void testGetPage(){
        ModelPaginator page = FriendsLink.getPage(1, 15);
        assertEquals(3, page.size());
    }

    @Test
    public void testUpdate(){
        long id1 = (Long) Fixtures.idCache.get("models.cms.FriendsLink-Link1");
        long id2 = (Long) Fixtures.idCache.get("models.cms.FriendsLink-Link2");
        FriendsLink link2 = FriendsLink.findById(id2);
        // 将Link2 的内容更新到 link1
        FriendsLink.update(id1,link2);

        FriendsLink updatedLink1 = FriendsLink.findById(id1);
        assertEquals("link02",updatedLink1.linkName);
        assertEquals("www.yibaiquan.com",updatedLink1.link);
        assertEquals("Jim",updatedLink1.userName);
        assertEquals("This is yibaiquan link",updatedLink1.content);
    }

    @Test
    public void testIsExist(){

        long id1 = (Long) Fixtures.idCache.get("models.cms.FriendsLink-Link1");
        long id3 = (Long) Fixtures.idCache.get("models.cms.FriendsLink-Link3");
        String validLink = "baidu";
        assertEquals("", FriendsLink.isExisted(id1,validLink));
        String existLink = "www.baidu.com";
        assertEquals("注意该URL已经存在！",FriendsLink.isExisted(id3,existLink));
        String forbidLink = "www.uhuila.com";
        assertEquals("注意该URL已经禁止！",FriendsLink.isExisted(id1,forbidLink));

    }
}
