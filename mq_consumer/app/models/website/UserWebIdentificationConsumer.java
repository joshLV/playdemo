package models.website;

import models.consumer.UserWebIdentification;
import play.Logger;
import play.cache.Cache;
import play.jobs.OnApplicationStart;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;

@OnApplicationStart(async = true)
public class UserWebIdentificationConsumer extends RabbitMQConsumer<String> {

	@Override
	protected void consume(String cookieValue) {
		System.out.println("处理消息：cookievalue=" + cookieValue);
		UserWebIdentification wui = UserWebIdentification.findOne(cookieValue);
		if (wui == null) {
			Logger.info("尝试保存UserWebIdentification（cookie:" + cookieValue + ")");
			wui = (UserWebIdentification) Cache.get(UserWebIdentification.MQ_KEY + cookieValue);
			if (wui != null) {
				wui.save(); //考虑做一下如果有ID，调用更新操作，否则调用新增操作.
				Cache.delete(UserWebIdentification.MQ_KEY + cookieValue);
			} else {
				Logger.error("UserWebIdentification cookvalue:" + cookieValue + " 没有在缓存中找到，请检查。");
			}
		} else {
			Logger.info("UserWebIdentification（cookie:" + cookieValue + ")已经被其它对象保存");
		}
	}

	@Override
	protected Class getMessageType() {
		return String.class;
	}

	@Override
	protected String queue() {
		return UserWebIdentification.MQ_KEY;
	}


}
