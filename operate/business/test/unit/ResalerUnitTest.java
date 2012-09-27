package unit;

import models.resale.ResalerCreditable;
import models.resale.*;

import org.junit.Test;

import play.modules.paginate.JPAExtPaginator;
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
		condition.status=ResalerStatus.UNAPPROVED;
		int pageNumber=1;
		int pageSize=10;
		JPAExtPaginator<Resaler> list =  Resaler.findByCondition(condition, pageNumber, pageSize);
		assertEquals(1,list.size());
	}



	@Test
	public void testCondition() {
		ResalerCondition condition = new ResalerCondition();
		condition.loginName="y";
		condition.status=ResalerStatus.APPROVED;
		String sql = condition.getFitter();
		assertEquals("1=1 and r.loginName like :loginName and r.status = :status",sql);
		assertNotNull(condition.getParamMap());
	}

	/**
	 * 审核分销商
	 */
	@Test
	public void updateStatus() {
		Long id = (Long) Fixtures.idCache.get("models.resale.Resaler-resaler_2");
		String remark ="";
		Resaler.update(id, ResalerStatus.APPROVED,ResalerLevel.NORMAL, remark, ResalerCreditable.NO);
		Resaler resaler = Resaler.findById(id);
		assertEquals(ResalerStatus.APPROVED,resaler.status );
		assertEquals(ResalerLevel.NORMAL,resaler.level );

		remark ="该分销商信用不够！";
		Resaler.update(id, ResalerStatus.UNAPPROVED,ResalerLevel.NORMAL, remark, ResalerCreditable.NO);
		resaler = Resaler.findById(id);
		assertEquals(ResalerStatus.UNAPPROVED,resaler.status );
		assertEquals(remark,resaler.remark );
	}

	@Test
	public void testFreeze() {
		Long id = (Long) Fixtures.idCache.get("models.resale.Resaler-resaler_2");
		Resaler.freeze(id);
		Resaler resaler = Resaler.findById(id);
		assertEquals(ResalerStatus.FREEZE, resaler.status);
	}

	@Test
	public void testUnfreeze() {
		Long id = (Long) Fixtures.idCache.get("models.resale.Resaler-resaler_3");
		Resaler.unfreeze(id);
		Resaler resaler = Resaler.findById(id);
		assertEquals(ResalerStatus.APPROVED, resaler.status);
	}
}
