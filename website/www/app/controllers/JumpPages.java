package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.cms.Block;
import models.cms.BlockClickTrack;
import models.cms.BlockType;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;
import controllers.modules.website.cas.SecureCAS;
import controllers.modules.website.cas.annotations.SkipCAS;

@With({SecureCAS.class, WebsiteInjector.class})
@SkipCAS
public class JumpPages extends Controller {

	public static void jump360buy(Long id) {
		List<Block> blocks = null;
		if (id == null) {
			blocks = Block.findByType(BlockType.JUMP_TO_OUTER, new Date());
		} else {
			blocks = new ArrayList<>();
			Block block = Block.findById(id);
			if (block == null) {
				notFound();
			}
			blocks.add(block);
		}
		Map<Long, models.sales.Goods> goodsMap = new HashMap<>();
		for (Block block : blocks) {
			Long goodsId = Long.parseLong(block.title);
			models.sales.Goods goods = models.sales.Goods.findById(goodsId);
			if (goods == null) {
				Logger.error("外链跳转Block指定了一个非法的GoodsId(value=" + block.title + "), 请到后台检查.");
				redirect("http://www.yibaiquan.com");
			} else {
				goodsMap.put(block.id, goods);
			}
		}
		render(blocks, goodsMap);
	}
	
	public static void doJump(Long id) {
		Block block = Block.findById(id);
		if (block == null) {
			notFound();
		}
		BlockClickTrack click = new BlockClickTrack();
		click.block = block;
		click.cookieId = WebsiteInjector.getWebIdentificationCookieId();
		click.ip = request.remoteAddress;
		if (SecureCAS.getUser() != null) {
			click.user_id = SecureCAS.getUser().id;
		}
		click.createdAt = new Date();
		click.save();
		redirect(block.link);
	}
}
