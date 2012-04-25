package unit;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.constants.PlatformType;
import models.cms.Topic;
import models.cms.TopicType;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.ModelPaginator;
import play.test.Fixtures;
import play.test.UnitTest;

/**
 * 公告单元测试.
 * <p/>
 * User: sujie
 * Date: 4/25/12
 * Time: 3:48 PM
 */
public class TopicUnitTest extends UnitTest {
    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        Fixtures.delete(Topic.class);
        Fixtures.loadModels("fixture/topics.yml");
    }

    @Test
    public void testDelete() {
        long id = (Long) Fixtures.idCache.get("models.cms.Topic-Test");

        Topic.delete(id);

        Topic deletedTopic = Topic.findById(id);

        assertNotNull(deletedTopic);
        assertEquals(DeletedStatus.DELETED, deletedTopic.deleted);
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

}
