package unit.account;

import factory.FactoryBoy;
import play.test.FunctionalTest;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 1/11/13
 * Time: 10:21 AM
 */
public class AccountSequenceUtilTest extends FunctionalTest {
    @org.junit.Before
    public void setup() {
        FactoryBoy.deleteAll();
    }

}
    