package unit;

import java.util.Date;
import java.util.List;

import models.order.OrderStatus;
import models.order.Orders;
import models.sales.Area;
import models.sales.Goods;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.UnitTest;


public class OrderUnitTest extends UnitTest {
    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        Fixtures.delete(models.order.Orders.class);
        Fixtures.loadModels("fixture/orders.yml","fixture/user.yml");
    }

    /**
     * 测试订单列表
     */
	@Test
	public void testOrder(){
		Orders orders=new Orders();
		orders.createdAtBegin=new Date();
		orders.createdAtEnd=new Date();
		orders.status=OrderStatus.UNPAID;
		orders.deliveryType=1;
		orders.payMethod="1";
		String compnayId ="1";
		List<Orders> list = Orders.query(orders,compnayId);
		Assert.assertEquals(0,list.size());  

	}

    /**
     * 测试券列表
     */
    @Test
    public void testQueryCoupons(){
        List<Orders> list = Orders.queryCoupons();
        Assert.assertEquals(1,list.size());

    }
}
