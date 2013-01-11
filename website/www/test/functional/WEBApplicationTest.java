package functional;

import factory.FactoryBoy;
import models.cms.Topic;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.Map;

/**
 * User: tanglq
 * Date: 13-1-11
 * Time: 下午8:35
 */
public class WEBApplicationTest extends FunctionalTest {
    Topic topic;
    @Before
    public void setUp() throws Exception {
        FactoryBoy.deleteAll();
        topic = FactoryBoy.create(Topic.class);
    }

    @Test
    public void testAbout() throws Exception {
        Http.Response response = GET(Router.reverse("WEBApplication.about").url);
        assertIsOk(response);
    }

    @Test
    public void testHelp() throws Exception {
        Http.Response response = GET(Router.reverse("WEBApplication.help").url);
        assertIsOk(response);
    }

    @Test
    public void testRebate() throws Exception {
        Http.Response response = GET(Router.reverse("WEBApplication.rebate").url);
        assertIsOk(response);
    }

    @Test
    public void testContact() throws Exception {
        Http.Response response = GET(Router.reverse("WEBApplication.contact").url);
        assertIsOk(response);
    }

    @Test
    public void testService() throws Exception {
        Http.Response response = GET(Router.reverse("WEBApplication.service").url);
        assertIsOk(response);
    }

    @Test
    public void testRule() throws Exception {
        Http.Response response = GET(Router.reverse("WEBApplication.rule").url);
        assertIsOk(response);
    }

    @Test
    public void testLink() throws Exception {
        Http.Response response = GET(Router.reverse("WEBApplication.link").url);
        assertIsOk(response);
    }

    @Test
    public void testTopic() throws Exception {
        Map<String, Object> urlParams = new HashMap<>();
        urlParams.put("id", topic.id);
        Http.Response response = GET(Router.reverse("WEBApplication.topic", urlParams).url);
        assertIsOk(response);
        assertEquals(topic.id, ((Topic)renderArgs("topic")).id);
    }

    @Test
    public void testList() throws Exception {
        Http.Response response = GET(Router.reverse("WEBApplication.list").url);
        assertIsOk(response);
    }
}
