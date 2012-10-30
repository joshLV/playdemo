package models.website;

import models.consumer.UserWebIdentification;
import models.consumer.UserWebIdentificationData;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.jobs.OnApplicationStart;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;

@OnApplicationStart(async = true)
public class UserWebIdentificationConsumer extends RabbitMQConsumer<UserWebIdentificationData> {

	@Override
	protected void consume(UserWebIdentificationData uwiMsg) {
		//开启事务管理
        JPAPlugin.startTx(false);

		UserWebIdentification wui = UserWebIdentification.findOne(uwiMsg.cookieId);
		if (wui == null) {
			wui = uwiMsg.toUserWebIdentification();
			Logger.info("尝试保存UserWebIdentification（cookie:" + uwiMsg.cookieId + ")");
			wui.save(); //考虑做一下如果有ID，调用更新操作，否则调用新增操作.
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
		return UserWebIdentificationData.class;
	}

	@Override
	protected String queue() {
		return UserWebIdentification.MQ_KEY;
	}


}
