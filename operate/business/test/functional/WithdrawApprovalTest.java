package functional;

import factory.FactoryBoy;
import org.junit.Before;
import play.test.FunctionalTest;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 12/3/12
 * Time: 2:42 PM
 */
public class WithdrawApprovalTest extends FunctionalTest {
    @Before
    public void setup() {
        FactoryBoy.deleteAll();

    }
}
    