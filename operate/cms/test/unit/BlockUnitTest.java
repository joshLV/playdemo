package unit;

import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import models.cms.Block;
import models.cms.BlockType;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.ModelPaginator;
import play.test.UnitTest;

/**
 * 公告单元测试.
 * <p/>
 * User: sujie
 * Date: 4/25/12
 * Time: 3:48 PM
 */
public class BlockUnitTest extends UnitTest {
    Block block;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        FactoryBoy.deleteAll();
        block = FactoryBoy.create(Block.class);
    }

    @Test
    public void testGetContent() {
        Block block = new Block();
        block.setContent("  ");
        assertEquals("", block.getContent());

        block.setContent("<br />");
        assertEquals("", block.getContent());
    }

    @Test
    public void testGetPage() {
        ModelPaginator page = Block.getPage(1, 15, null);
        assertEquals(1, page.size());
        page = Block.getPage(1, 15, null);
        assertEquals(1, page.size());
        page = Block.getPage(1, 15, BlockType.DAILY_SPECIAL);
        assertEquals(0, page.size());
    }

    @Test
    public void testDelete() {
        Block.delete(block.id);

        Block deletedBlock = Block.findById(block.id);

        assertNotNull(deletedBlock);
        assertEquals(DeletedStatus.DELETED, deletedBlock.deleted);
    }

}
