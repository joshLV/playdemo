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

	/**
	 * 跳转到360buy的跳转页.
	 */
	public static void jump360buy(String id) {
		List<Block> blocks = null;
		if (id == null) {
			blocks = Block.findByType(BlockType.JUMP_TO_OUTER, new Date());
		} else {
			String[] ids = id.split("-");
			blocks = new ArrayList<>();
			for (String sId : ids) {
				Block block = Block.findById(Long.parseLong(sId));
				if (block != null) {
					blocks.add(block);
				}
			}
			if (blocks.size() == 0) {
				notFound();
			}
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
	
	/**
	 * 执行对CMS Block对应url的跳转，同时记录点击数。
	 * @param id
	 */
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
