package function;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.sales.Goods;
import models.sales.GoodsStatus;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.uhuila.common.constants.DeletedStatus;

import play.Play;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;

public class GoodsTest extends FunctionalTest {

	@org.junit.Before
	public void setup() {
		Fixtures.delete(Goods.class);
		Fixtures.loadModels("fixture/goods.yml");
	}

	/**
	 * 查看商品信息
	 */
	@Test
	public void testDetails() {
		Long goodsId = (Long) Fixtures.idCache.get("models.sales" +
				".Goods-Goods1");

		Http.Response response = GET("/goods/" + goodsId+"/view");
		assertIsOk(response);
		assertContentType("text/html", response);
		assertCharset(Play.defaultWebEncoding, response);
	}

	/**
	 * 修改商品信息
	 */
	@Test
	public void testEdit() {
		Long goodsId = (Long) Fixtures.idCache.get("models.sales" +
				".Goods-Goods2");
		Http.Response response = GET("/goods/" + goodsId+"/edit");
		assertIsOk(response);
		assertContentType("text/html", response);
		assertCharset(Play.defaultWebEncoding, response);

		Map<String, String> goodsParams = new HashMap<String,String>();
		goodsParams.put("goods.name", "test");
		goodsParams.put("id", "1");
		response = POST("/goods/"+goodsId+"/update", goodsParams);
		assertStatus(302,response);
		Goods goods = Goods.findById(goodsId);
		Assert.assertEquals(goods.name,"test");  
	}

	/**
	 * 添加商品信息
	 */
	@Test
	public void testCreate() {
		Map<String, String> goodsParams = new HashMap<String,String>();
		goodsParams.put("goods.name", "laiyifen1");
		goodsParams.put("goods.no", "20000000");
		goodsParams.put("goods.companyId", "0");
		goodsParams.put("goods.status", GoodsStatus.ONSALE.toString());
		goodsParams.put("goods.prompt", "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
		goodsParams.put("goods.details", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		goodsParams.put("goods.imagePath", "/opt/3.jpg");
		goodsParams.put("goods.deleted",DeletedStatus.DELETED.toString());
		goodsParams.put("goods.createdBy", "yanjy");
		goodsParams.put("radios", "1");
		goodsParams.put("status", "1");
		goodsParams.put("checkoption", "1");
		Response response = POST("/goods", goodsParams);
		response.setContentTypeIfNotSet("text/html; charset=GBK");
		assertStatus(200,response);
		List<Goods> list = Goods.findAll();
		Assert.assertNotNull(list);  
	}

	/**
	 * 删除商品信息
	 */
	@Test
	public void testDelete() {
		Long goodsId = (Long) Fixtures.idCache.get("models.sales" +
				".Goods-Goods2");
		Response response = DELETE("/goods/{id}/delete?checkoption[]="+goodsId);
		assertStatus(302,response);
		Goods goods = Goods.findById(goodsId);
		Assert.assertEquals(goods.deleted,DeletedStatus.DELETED);  
	}

	/**
	 * 修改商品上下架
	 */
	@Test
	public void updateStatus() {
		Long goodsId = (Long) Fixtures.idCache.get("models.sales" +
				".Goods-Goods1");
		Map<String, String> goodsParams = new HashMap<String,String>();

		goodsParams.put("status", GoodsStatus.ONSALE.toString());
		goodsParams.put("checkoption[]", goodsId.toString());
		Response response = POST("/updatestatus", goodsParams);
		assertStatus(302,response);
		Goods goods = Goods.findById(goodsId);
		Assert.assertEquals(goods.status,GoodsStatus.ONSALE);  
	}
}