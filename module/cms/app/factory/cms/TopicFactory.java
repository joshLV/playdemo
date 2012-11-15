package factory.cms;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.constants.PlatformType;
import factory.ModelFactory;
import models.cms.Topic;
import models.cms.TopicType;
import util.DateHelper;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-11-15
 * Time: 上午10:29
 * To change this template use File | Settings | File Templates.
 */
public class TopicFactory extends ModelFactory<Topic> {
    @Override
    public Topic define() {
        Topic t = new Topic();
        t.title = "测试公告开始啦";
        t.effectiveAt = DateHelper.t("2012-3-21 00:00:00");
        t.expireAt = DateHelper.t("2013-3-21 00:00:00");
        t.displayOrder = 100;
        t.platformType = PlatformType.UHUILA;
        t.type = TopicType.NOTICE;
        t.deleted = DeletedStatus.UN_DELETED;
        return t;
    }
}
