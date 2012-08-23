package functional;

import models.sales.SecKillGoodsItem;
import org.junit.Ignore;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import factory.FactoryBoy;
/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-8-22
 * Time: 下午3:03
 * To change this template use File | Settings | File Templates.
 */
public class SecKillOrdersTest  extends FunctionalTest {
    @Ignore
    @Test
    public void testIndex() {

        SecKillGoodsItem item = FactoryBoy.create(SecKillGoodsItem.class);
        Http.Response response = GET("/seckill-orders");


    }

}
