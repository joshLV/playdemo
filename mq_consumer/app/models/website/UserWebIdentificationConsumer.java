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
        JPA.em().flush();  // 避免出现再次插件时wui为空，但还是出现cookieId索引不唯一.
		UserWebIdentification wui = UserWebIdentification.findOne(uwiMsg.cookieId);
		if (wui == null) {
			wui = uwiMsg.toUserWebIdentification();
	        boolean rollBack = false;
			Logger.info("尝试保存UserWebIdentification（cookie:" + uwiMsg.cookieId + ")");
			try {
			    if (wui.id != null && wui.id > 0l) {
			        UserWebIdentification wui2 = UserWebIdentification.findById(wui.id);
			        if (wui.user != null) {
			            wui2.user = wui.user;
			        }
			        wui2.cartCount = wui.cartCount;
			        wui2.orderCount = wui.cartCount;
			        wui2.payAmount = wui.payAmount;
			        wui2.registerCount = wui.registerCount;
			        wui2.save();
			    } else {
			        // new wui.
			        wui.save();
			    }
	            JPA.em().flush();
			} catch (Exception e) {
	            rollBack = true;
	            Logger.info("update UserWebIdentification failed, will roll back now.", e);
			    return;
			} finally {
	            JPAPlugin.closeTx(rollBack);
	        }
		} else {
			Logger.info("msg83841341:UserWebIdentification（cookie:" + uwiMsg.cookieId + ")已经被其它进程保存");
			JPAPlugin.closeTx(true);
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
