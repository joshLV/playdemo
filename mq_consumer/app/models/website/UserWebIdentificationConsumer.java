package models.website;

import models.consumer.UserWebIdentification;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.order.OuterOrderStatus;
import play.Logger;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.jobs.OnApplicationStart;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;

@OnApplicationStart(async = true)
public class UserWebIdentificationConsumer extends RabbitMQConsumer<String> {

	@Override
	protected void consume(String cookieValue) {
		  //开启事务管理
        JPAPlugin.startTx(false);

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

        boolean rollBack = false;
        try {
        	System.out.println("commit....");
            JPA.em().flush();
        } catch (RuntimeException e) {
            rollBack = true;
            Logger.info("update UserWebIdentification failed, will roll back", e);
            //不抛异常 不让mq重试本job
        } finally {
            JPAPlugin.closeTx(rollBack);
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
