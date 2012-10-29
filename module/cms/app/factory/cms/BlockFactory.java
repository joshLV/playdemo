package factory.cms;

import util.DateHelper;

import com.uhuila.common.constants.DeletedStatus;

import models.cms.Block;
import models.cms.BlockType;
import factory.FactoryBoy;
import factory.ModelFactory;

public class BlockFactory extends ModelFactory<Block> {

	@Override
	public Block define() {
		Block block = new Block();
		block.deleted = DeletedStatus.UN_DELETED;
		block.displayOrder = FactoryBoy.sequence(Block.class);
		block.expireAt = DateHelper.afterDays(3);
		block.effectiveAt = DateHelper.beforeDays(1);
		block.imageUrl = "http://img0.uhcdn.com/p/3/" + block.displayOrder + "/3/block.jpg";
		block.link = "http://www.yibaiquan.com/p/1234";
		block.title = "block";
		block.type = BlockType.WEBSITE_SLIDE;
		block.setContent("Hello, world!");
		return block;
	}

}
