package unit;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import models.order.Order;
import models.order.OrderStatus;
import models.order.OrdersCondition;
import models.resale.Resaler;
import models.sales.Area;
import models.sales.Brand;
import models.sales.Category;
import models.sales.Goods;
import models.sales.GoodsLevelPrice;
import models.sales.Shop;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import play.modules.paginate.JPAExtPaginator;
import play.test.Fixtures;
import play.test.UnitTest;

public class OrderUnitTest extends UnitTest {
	@Before
	@SuppressWarnings("unchecked")
	public void setup() {
		Fixtures.delete(Shop.class);
		Fixtures.delete(Goods.class);
		Fixtures.delete(Category.class);
		Fixtures.delete(Brand.class);
		Fixtures.delete(Area.class);
		Fixtures.delete(Resaler.class);
		Fixtures.delete(GoodsLevelPrice.class);
		Fixtures.delete(models.order.OrderItems.class);
		Fixtures.delete(models.order.Order.class);
		Fixtures.loadModels("fixture/areas_unit.yml");
		Fixtures.loadModels("fixture/categories_unit.yml");
		Fixtures.loadModels("fixture/brands_unit.yml");
		Fixtures.loadModels("fixture/shops_unit.yml");
		Fixtures.loadModels("fixture/goods_unit.yml");
		Fixtures.loadModels("fixture/level_price.yml");
		Fixtures.loadModels("fixture/resaler.yml");
		Fixtures.loadModels("fixture/orders.yml", "fixture/orderItems.yml");
		Long resalerId = (Long) Fixtures.idCache.get("models.resale.Resaler-Resaler_1");
		Resaler resaler =Resaler.findById(resalerId);
		Long id = (Long)play.test.Fixtures.idCache.get("models.order.Order-order1");
		Order order = models.order.Order.findById(id);
		order.setUser(resaler.getId(), models.accounts.AccountType.RESALER);

		id = (Long)play.test.Fixtures.idCache.get("models.order.Order-order2");
		order = models.order.Order.findById(id);
		order.setUser(resaler.getId(), models.accounts.AccountType.RESALER);

		id = (Long)play.test.Fixtures.idCache.get("models.order.Order-order3");
		order = models.order.Order.findById(id);
		order.setUser(resaler.getId(), models.accounts.AccountType.RESALER);

		id = (Long)play.test.Fixtures.idCache.get("models.order.Order-order4");
		order = models.order.Order.findById(id);
		order.setUser(resaler.getId(), models.accounts.AccountType.RESALER);
		
		id = (Long)play.test.Fixtures.idCache.get("models.order.Order-order5");
		order = models.order.Order.findById(id);
		order.setUser(resaler.getId(), models.accounts.AccountType.RESALER);
		
		id = (Long)play.test.Fixtures.idCache.get("models.order.Order-order6");
		order = models.order.Order.findById(id);
		order.setUser(resaler.getId(), models.accounts.AccountType.RESALER);
	}

	/**
	 * 测试订单列表
	 */
	@Test
	public void testOrder() {
		Long resalerId = (Long) Fixtures.idCache.get("models.resale.Resaler-Resaler_1");
		Resaler resaler =Resaler.findById(resalerId);

		OrdersCondition condition = new OrdersCondition();
		condition.createdAtBegin = new Date();
		condition.createdAtEnd = new Date();
		int pageNumber = 1;
		int pageSize = 15;

		JPAExtPaginator<Order> list = Order.findResalerOrders(condition, resaler, pageNumber, pageSize);
		assertEquals(0, list.size());

		condition = new OrdersCondition();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			condition.createdAtBegin = sdf.parse("2012-03-01");
			condition.createdAtEnd = new Date();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		condition.status = OrderStatus.PAID;
		condition.goodsName = "哈根达斯";
		list = Order.findResalerOrders(condition, resaler, pageNumber, pageSize);
		assertEquals(1, list.size());

	}

	@Test
	public void testGetThisMonthTotal() {
		Long resalerId = (Long) Fixtures.idCache.get("models.resale.Resaler-Resaler_1");
		Resaler resaler =Resaler.findById(resalerId);
		Order.getThisMonthTotal(resaler);
		Map totalMap = Order.getTotalMap();
		assertNotNull(totalMap);

	}

}
