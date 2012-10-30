package functional;

import java.util.List;

import models.cms.Block;
import models.cms.BlockType;
import models.sales.Goods;

import org.junit.Before;
import org.junit.Test;

import play.mvc.Http.Response;
import play.test.FunctionalTest;
import factory.FactoryBoy;
import factory.callback.BuildCallback;

public class JumpPagesTest extends FunctionalTest {

	private Block block;
	private Goods goods;

	@Before
	public void setUp() {
		FactoryBoy.deleteAll();
		
		goods = FactoryBoy.create(Goods.class);
		block = FactoryBoy.create(Block.class, new BuildCallback<Block>() {
			@Override
			public void build(Block b) {
				b.type = BlockType.JUMP_TO_OUTER;
				b.title = goods.id.toString();
				b.setContent("品味浪漫意式美食，85元抢购巴贝拉100元代金券");
			}
		});
		FactoryBoy.batchCreate(2, Block.class, new BuildCallback<Block>() {
			@Override
			public void build(Block b) {
				Goods g = FactoryBoy.create(Goods.class);
				b.title = g.id.toString();
				b.type = BlockType.JUMP_TO_OUTER;
			}
		});
	}
	
	/*
	 * 
	 * 
GET     /jump/360buy                            JumpPages.jump360buy
GET     /jump/360buy/{id}                       JumpPages.jump360buy
GET     /jump/to/{id}                           JumpPages.doJump
	 */
	@Test
	public void 测试列出所有产品的跳转页面() {
		Response response = GET("/jump/360buy");
        assertIsOk(response);
        assertContentType("text/html", response);
        
        List<Block> blocks = (List<Block>)renderArgs("blocks");
        assertNotNull(blocks);
        assertEquals(3, blocks.size());
        assertContentMatch("品味浪漫意式美食，85元抢购巴贝拉100元代金券", response);
	}
	
	@Test
	public void 测试列出指定产品的跳转页面() {
		Response response = GET("/jump/360buy/" + block.id);
        assertIsOk(response);
        assertContentType("text/html", response);
        
        List<Block> blocks = (List<Block>)renderArgs("blocks");
        assertNotNull(blocks);
        assertEquals(1, blocks.size());
        assertContentMatch("品味浪漫意式美食，85元抢购巴贝拉100元代金券", response);
	}
	
	@Test
	public void 测试非法目标Block时返回404() throws Exception {
		Block.deleteAll();
		Response response = GET("/jump/360buy/9999");
        assertStatus(404, response);
	}

	@Test
	public void 当指定非法GoodsId时返回500() throws Exception {
		FactoryBoy.create(Block.class, new BuildCallback<Block>() {
			@Override
			public void build(Block b) {
				b.type = BlockType.JUMP_TO_OUTER;
				b.title = "9999";
			}
		});

		Response response = GET("/jump/360buy");
        assertStatus(302, response);
	}
	
	@Test 
	public void 测试执行跳转时记录跳转数() {
		assertEquals(0, block.totalClickedCount());
		assertEquals(0, block.todayClickedCount());
		Response response = GET("/jump/to/" + block.id);
        assertStatus(302, response);

        Block b = Block.findById(block.id);
		assertEquals(1, b.totalClickedCount());
		assertEquals(1, b.todayClickedCount());
	}
	
}
