package unit.extension;

import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import unit.extension.sample.NotsupportedInvocation;
import unit.extension.sample.SampleContext;
import unit.extension.sample.SampleInvocation;
import util.extension.ExtensionInvoker;
import util.extension.ExtensionResult;

/**
 * User: tanglq
 * Date: 13-3-4
 * Time: 下午10:27
 */
public class ExtensionInvokerTest extends UnitTest {

    @Before
    public void setUp() throws Exception {
        ExtensionInvoker.initExtensions();
    }

    @Test
    public void testFooContext() {
        SampleContext fooContext = new SampleContext();
        fooContext.id = 1l;
        fooContext.type = "foo";

        ExtensionResult result = ExtensionInvoker.run(SampleInvocation.class, fooContext);
        assertEquals(0, result.code);
        assertEquals(100, fooContext.result);
    }

    @Test
    public void testBarContext() {
        SampleContext barContext = new SampleContext();
        barContext.id = 1l;
        barContext.type = "bar";

        ExtensionResult result = ExtensionInvoker.run(SampleInvocation.class, barContext);
        assertEquals(1, result.code);
        assertEquals(-100, barContext.result);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testNotsupportInvocation() throws Exception {
        SampleContext barContext = new SampleContext();
        barContext.id = 1l;
        barContext.type = "bar";

        ExtensionInvoker.run(NotsupportedInvocation.class, barContext);
    }
}
