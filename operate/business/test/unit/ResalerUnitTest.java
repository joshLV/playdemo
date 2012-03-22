package unit;

import java.util.HashMap;
import java.util.Map;

import models.resale.Resaler;
import models.resale.ResalerCondition;
import models.resale.ResalerStatus;

import org.junit.Test;

import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.UnitTest;

public class ResalerUnitTest extends UnitTest {

	@org.junit.Before
	public void setup() {
		Fixtures.delete(Resaler.class);
		Fixtures.loadModels("fixture/resaler.yml");
	}
	
	@Test
	public void testIndex() {
		ResalerCondition condition = new ResalerCondition();
		condition.loginName="y";
		int pageNumber=1;
		int pageSize=10;
		JPAExtPaginator<Resaler> list =  Resaler.findByCondition(condition, pageNumber, pageSize);
		assertEquals(2,list.size());
	}
	
	
	
	@Test
	public void testCondition() {
		ResalerCondition condition = new ResalerCondition();
		condition.loginName="y";
		condition.status=ResalerStatus.APPROVED;
		String sql = condition.getFitter();
		assertEquals("1=1 and r.loginName like :loginName and r.status like :status",sql);
		assertNotNull(condition.getParamMap());
	}
	
	/**
	 * 审核分销商
	 */
	@Test
	public void updateStatus() {
		Long id = (Long) Fixtures.idCache.get("models.resale.Resaler-resaler_2");
		String remark ="该分销商信用不够！";
		Resaler.update(id, ResalerStatus.APPROVED, remark);
		Resaler resaler = Resaler.findById(id);
		assertEquals(ResalerStatus.APPROVED,resaler.status );
	}
}
