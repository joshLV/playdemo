package factory.cms;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.cms.CmsQuestion;
import util.DateHelper;

/**
 * User: wangjia
 * Date: 12-10-30
 * Time: 下午4:58
 */
public class CmsQuestionFactory extends ModelFactory<CmsQuestion> {
    @Override
    public CmsQuestion define() {
        CmsQuestion cq = new CmsQuestion();
        cq.content = "满百送电影票活动，是不是拍一张这个面值一百的就可以了？还是这个只算80块？";
        cq.cookieId = "61edf4ff-2867-433e-9ced-dd9442ed10c3";
        cq.createdAt = DateHelper.t("2012-06-18 07:58:56");
        cq.operateUserId = 3l;
        cq.repliedAt = DateHelper.t("2012-07-03T10:24:43");
        cq.reply = "亲，满百送电影票需要购物金额到100元哦~如果只买1张100的券算80哟！";
        cq.visible = true;
        cq.userName = "用户";
        cq.goodsName = "限时促-[多商区]有家川菜现金券-免预约不限时100";
        return cq;
    }
}
