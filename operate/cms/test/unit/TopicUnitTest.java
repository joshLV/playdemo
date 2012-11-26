package unit;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.constants.PlatformType;
import models.cms.Topic;
import models.cms.TopicType;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.ModelPaginator;
import play.test.UnitTest;
import factory.FactoryBoy;

/**
 * 公告单元测试.
 * <p/>
 * User: sujie
 * Date: 4/25/12
 * Time: 3:48 PM
 */
public class TopicUnitTest extends UnitTest {
    Topic t;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        FactoryBoy.deleteAll();
        t = FactoryBoy.create(Topic.class);

    }

    @Test
    public void testGetContent() {
        Topic topic = new Topic();
        topic.setContent("  ");
        assertEquals("", topic.getContent());
        topic.setContent("<br />");
        assertEquals("", topic.getContent());
    }

    @Test
    public void testGetPage() {
        ModelPaginator page = Topic.getPage(1, 15, null, null);
        assertEquals(1, page.size());
        page = Topic.getPage(1, 15, PlatformType.UHUILA, null);
        assertEquals(1, page.size());
        page = Topic.getPage(1, 15, PlatformType.UHUILA, TopicType.NEWS);
        assertEquals(0, page.size());
    }

    @Test
    public void testDelete() {
        Topic.delete(t.id);
        Topic deletedTopic = Topic.findById(t.id);
        assertNotNull(deletedTopic);
        assertEquals(DeletedStatus.DELETED, deletedTopic.deleted);
    }

}
