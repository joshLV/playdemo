/**
 * 
 */
package functional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.accounts.Account;
import models.consumer.User;
import models.order.Cart;
import models.order.Order;
import models.order.OrderItems;
import models.sales.Brand;
import models.sales.Category;
import models.sales.Goods;
import models.sales.Shop;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.modules.paginate.ValuePaginator;
import play.mvc.Http;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;
import controllers.modules.website.cas.SecureCAS;
import controllers.modules.website.cas.Security;

/**
 * @author wangjia
 * @date 2012-7-31 上午10:42:21 
 */
public class CartsTest  extends FunctionalTest {

	@Before
	@SuppressWarnings("unchecked")
	public void setup() {
		Fixtures.delete(User.class);
		Fixtures.delete(Cart.class);
	
		Fixtures.delete(Category.class);
		Fixtures.delete(Shop.class);
		Fixtures.delete(Brand.class);
		Fixtures.delete(OrderItems.class);
		Fixtures.delete(Goods.class);
		Fixtures.delete(Order.class);
		Fixtures.delete(Account.class);
		
		Fixtures.loadModels("fixture/user.yml");
		Fixtures.loadModels("fixture/categories_unit.yml");
		Fixtures.loadModels("fixture/shops_unit.yml");
		Fixtures.loadModels("fixture/brands_unit.yml");
		Fixtures.loadModels("fixture/goods.yml");
		Fixtures.loadModels("fixture/goods_unit.yml");
		Fixtures.loadModels("fixture/orders.yml");
		Fixtures.loadModels("fixture/orderItems.yml");
		Fixtures.loadModels("fixture/cart.yml");
		
		
		

		
	}

	@After
	public void tearDown() {
		// 清除登录Mock
		Security.cleanLoginUserForTest();
	}
	
	
	@Test
	public void testIndexIsBuyFlag() {
		auth();
		Response response = GET("/carts");
		//System.out.println("aaa>>>"+response.out.toString());
		assertStatus(200, response); 
		assertContentMatch("一百券 - 购物车", response);
	}
	
	@Test
	public void testOrderGoodsNull() {
		auth();
		Map<String, String> orderParams = new HashMap<>();	
		long goodsId = (long) Fixtures.idCache.get("models.sales.Goods-goods2");
		long cartId = (long) Fixtures.idCache.get("models.order.Cart-cart1");
		Cart cart=Cart.findById(cartId);
		orderParams.put("goodsId",String.valueOf(999));
		orderParams.put("increment", "1");		
		Response response = POST("/carts", orderParams);
		assertContentMatch("no such goods", response);
		
	}
	
	@Test
	public void testOrderUserNull() {
		Map<String, String> orderParams = new HashMap<>();	
		long goodsId = (long) Fixtures.idCache.get("models.sales.Goods-Goods_002");	
		
		long cartId = (long) Fixtures.idCache.get("models.order.Cart-cart1");
		Cart cart=Cart.findById(cartId);
	    cart.number=2;
	    cart.save();    
		orderParams.put("goodsId",String.valueOf(goodsId));
		orderParams.put("increment", "1");		
		Response response = POST("/carts", orderParams);
		assertContentMatch("can not identity current user", response);
		
		
	}
	

	
	@Test
	public void testTopsCartListSizeMoreThanFive() {
		auth();	
		Response response = GET("/carts/tops");
		assertStatus(200, response);
		 
	}
	
	
	@Test
	public void testDeleteUserNull() {
		long goodsId = (long) Fixtures.idCache.get("models.sales.Goods-Goods_002");	
		Response response = DELETE("/carts/" + goodsId);
		assertStatus(500, response);
		assertContentMatch("can not identity current user", response);
	}
	
	@Test
	public void testDeleteGoodsIdValid() {
		auth();
		String goodsId=null;
		Response response = DELETE("/carts/"+goodsId) ;
		assertStatus(500, response);
		assertContentMatch("no goods specified", response);
		
	}
	
	private long auth() {
		
		Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
		User user = User.findById(userId);
		// 设置测试登录的用户名
		Security.setLoginUserForTest(user.loginName);
		return userId;
	}
	
	
}
