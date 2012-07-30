/**
 * 
 */
package functional;

import models.cms.VoteQuestion;
import models.consumer.Address;
import models.consumer.User;
import models.consumer.UserVote;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.uhuila.common.constants.DeletedStatus;

import controllers.modules.website.cas.Security;

import play.mvc.Http;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;

/**
 * @author wangjia
 * @date 2012-7-30 上午9:10:40 
 */
public class AddressesTest extends FunctionalTest {

	
	
	  @Before
	    @SuppressWarnings("unchecked")
	    public void setup() {
	        Fixtures.delete(Address.class);
	        Fixtures.delete(User.class);
	        Fixtures.loadModels("fixture/user.yml");
	        Fixtures.loadModels("fixture/addresses.yml");
	        
	        Long userId = (Long) Fixtures.idCache.get("models.consumer.User-user");
			User user = User.findById(userId);

			// 设置测试登录的用户名
			Security.setLoginUserForTest(user.loginName);
	    }
	  
	  @After
		public void tearDown() {
			// 清除登录Mock
			Security.cleanLoginUserForTest();
		}
	  
	  @Test
	    public void testIndex() {
	        Http.Response response = GET("/orders/addresses");
	        assertStatus(200, response); 
	    	assertContentMatch("收货地址", response);
	    }
	  
	  @Test
	    public void testList() {
	        Http.Response response = GET("/orders/addresses/list");
	        assertStatus(200, response); 
	    	assertContentMatch("编辑", response);
	    }
	  
	  @Test
	    public void testShow() {
		  	Long addressId = (Long) Fixtures.idCache.get("models.consumer.Address-test3");
	        Http.Response response = GET("/orders/addresses/{"+addressId+"}");
	        assertStatus(200, response); 
	    	assertContentMatch("收货地址", response);
	    }
	  
	  @Test
	    public void testShowDefault(){
	        Http.Response response = GET("/orders/addresses/default");
	        assertStatus(200, response); 
	    	assertContentMatch("收货地址", response);
	    }
	  
	  
	  @Test
	    public void testUpdateDefault(){
		  	Long addressId = (Long) Fixtures.idCache.get("models.consumer.Address-test3");	 
		  	Address address=Address.findById(addressId);
		  	address.city="徐汇区";
		  	
//		  	/friendsLinks/{id}  	    	
//	        long id = (Long) Fixtures.idCache.get("models.cms.FriendsLink-Link1");
//	        String params = "friendsLinks.linkName=changed&friendsLinks.link=www.changed.com";
//	        Http.Response response =  PUT("/friendsLinks/"+id,"application/x-www-form-urlencoded",params);
		        
	        Http.Response response = PUT("/orders/addresses/{"+addressId+"}/default","application/x-www-form-urlencoded",address.city);
	        assertStatus(200, response); 
	        addressId = (Long) Fixtures.idCache.get("models.consumer.Address-test3");	 
		  	address=Address.findById(addressId);
		  	assertEquals("徐汇区",address.city);	        
	    }
	  
	  @Test
	    public void testDelete(){		  
		  Long addressId = (Long) Fixtures.idCache.get("models.consumer.Address-test3");		  
		  Response response = DELETE("/orders/addresses/" + addressId);
		  assertStatus(200, response);	
		  Address addressDeleted = Address.findById(addressId);
		  assertNull(addressDeleted);
	  }
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
	  
}
