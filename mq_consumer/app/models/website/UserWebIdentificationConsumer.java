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
public class UserWebIdentificationConsumer extends RabbitMQConsumer<UserWebIdentification> {

	@Override
	protected void consume(UserWebIdentification uwiMsg) {
		  //开启事务管理
        JPAPlugin.startTx(false);

		UserWebIdentification wui = UserWebIdentification.findOne(uwiMsg.cookieId);
		if (wui == null) {
			Logger.info("尝试保存UserWebIdentification（cookie:" + uwiMsg.cookieId + ")");
			uwiMsg.save(); //考虑做一下如果有ID，调用更新操作，否则调用新增操作.
		} else {
			Logger.info("msg83841341:UserWebIdentification（cookie:" + uwiMsg.cookieId + ")已经被其它进程保存");
		}

        boolean rollBack = false;
        try {
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
		return UserWebIdentification.class;
	}

	@Override
	protected String queue() {
		return UserWebIdentification.MQ_KEY;
	}


}
