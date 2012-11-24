package unit;

import models.resale.Resaler;
import models.resale.ResalerCondition;
import models.resale.ResalerCreditable;
import models.resale.ResalerLevel;
import models.resale.ResalerStatus;

import org.junit.Test;

import play.modules.paginate.JPAExtPaginator;
import play.test.UnitTest;
import factory.FactoryBoy;

public class ResalerUnitTest extends UnitTest {

    Resaler resaler;
    
	@org.junit.Before
	public void setup() {
	    FactoryBoy.deleteAll();

	    resaler = FactoryBoy.create(Resaler.class);
	}

	@Test
	public void testIndex() {
		ResalerCondition condition = new ResalerCondition();
		condition.loginName="ang";  // need match 'dangdang'
		condition.status=ResalerStatus.APPROVED;
		int pageNumber=1;
		int pageSize=10;
		JPAExtPaginator<Resaler> list =  Resaler.findByCondition(condition, pageNumber, pageSize);
		assertEquals(1,list.size());
	}


    @Test
    public void testIndexUnApproved() {
        ResalerCondition condition = new ResalerCondition();
        condition.loginName="ang";  // need match 'dangdang'
        condition.status=ResalerStatus.UNAPPROVED;
        int pageNumber=1;
        int pageSize=10;
        JPAExtPaginator<Resaler> list =  Resaler.findByCondition(condition, pageNumber, pageSize);
        assertEquals(0,list.size());
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
		String remark ="";
		Resaler.update(resaler.id, ResalerStatus.APPROVED,ResalerLevel.NORMAL, remark, ResalerCreditable.NO, null);
		Resaler r = Resaler.findById(resaler.id);
		assertEquals(ResalerStatus.APPROVED, r.status );
		assertEquals(ResalerLevel.NORMAL, r.level );

		remark ="该分销商信用不够！";
		Resaler.update(resaler.id, ResalerStatus.UNAPPROVED,ResalerLevel.NORMAL, remark, ResalerCreditable.NO, null);
		r = Resaler.findById(resaler.id);
		assertEquals(ResalerStatus.UNAPPROVED, r.status );
		assertEquals(remark, r.remark );
	}

	@Test
	public void testFreeze() {
		Resaler.freeze(resaler.id);
		Resaler r = Resaler.findById(resaler.id);
		assertEquals(ResalerStatus.FREEZE, r.status);
	}

	@Test
	public void testUnfreeze() {
		Resaler.unfreeze(resaler.id);
		Resaler r = Resaler.findById(resaler.id);
		assertEquals(ResalerStatus.APPROVED, r.status);
	}
}
