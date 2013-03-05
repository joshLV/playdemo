package unit.yabo.extension;

import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import unit.yabo.extension.sample.SampleContext;
import yabo.extension.base.ExtensionHandler;
import yabo.extension.base.ExtensionResult;

/**
 * User: tanglq
 * Date: 13-3-4
 * Time: 下午10:27
 */
public class ExtensionHandlerTest extends UnitTest {

    @Before
    public void setUp() throws Exception {
        ExtensionHandler.initExtensions();
    }

    @Test
    public void testFooContext() {
        SampleContext fooContext = new SampleContext();
        fooContext.id = 1l;
        fooContext.type = "foo";

        ExtensionResult result = ExtensionHandler.run("Sample", fooContext);
        assertEquals(0, result.code);
        assertEquals(100, fooContext.result);
    }

    @Test
    public void testBarContext() {
        SampleContext barContext = new SampleContext();
        barContext.id = 1l;
        barContext.type = "bar";

        ExtensionResult result = ExtensionHandler.run("Sample", barContext);
        assertEquals(1, result.code);
        assertEquals(-100, barContext.result);
    }
}
