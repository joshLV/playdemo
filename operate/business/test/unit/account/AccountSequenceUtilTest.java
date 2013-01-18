package unit.account;

import factory.FactoryBoy;
import org.junit.Ignore;
import org.junit.Test;
import play.test.FunctionalTest;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 1/11/13
 * Time: 10:21 AM
 */
@Ignore
public class AccountSequenceUtilTest extends FunctionalTest {
    @org.junit.Before
    public void setup() {
        FactoryBoy.deleteAll();
    }

    @Test
    public void testEmpty() throws Exception {
        // TOOD
    }
}
    