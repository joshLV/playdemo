package function;

import factory.FactoryBoy;
import org.junit.Test;
import play.mvc.Before;
import play.test.FunctionalTest;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 9/25/12
 * Time: 2:58 PM
 */
public class DDApiUtilTest extends FunctionalTest{
    @Before
    public void setup() {
        FactoryBoy.lazyDelete();
    }

    @Test
    public void tesSyncSellCount() {
        
    }

}
