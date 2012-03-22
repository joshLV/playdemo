package function;

import java.util.HashMap;
import java.util.Map;

import models.resale.Resaler;
import models.resale.ResalerStatus;

import org.junit.Before;
import org.junit.Test;

import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;

public class ResalerTest extends FunctionalTest {


	@org.junit.Before
	public void setup() {
		Fixtures.delete(Resaler.class);
		Fixtures.loadModels("fixture/resaler.yml");
	}

	/**
	 * 查看分销商信息
	 */
	@Test
	public void testIndex() {
		Response response = GET("/resalers");
		assertStatus(302, response);
	}
	
	/**
	 * 查看分销商详细信息
	 */
	@Test
	public void testDetails() {
		Long id = (Long) Fixtures.idCache.get("models.resale.Resaler-resaler_1");

		Response response = GET("/resalers/" + id + "/view");
		assertStatus(302, response);
	}

}
