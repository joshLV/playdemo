package factory.cms;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.constants.PlatformType;
import factory.ModelFactory;
import models.cms.Topic;

import java.util.Date;

/**
 * @author likang
 *         Date: 12-11-15
 */
public class TopicFactory extends ModelFactory<Topic>{
    @Override
    public Topic define() {
        Topic topic = new Topic();
        topic.deleted = DeletedStatus.UN_DELETED;
        topic.type = TopicType.NOTICE;
        topic.platformType = PlatformType.UHUILA;
        topic.effectiveAt = new Date();
        topic.expireAt = new Date(System.currentTimeMillis() + 60*1000*60*24*30);
        topic.displayOrder = 100;
        topic.setContent("测试内容如下");
        topic.title = "测试公告开始啦";
        return topic;
    }
}
