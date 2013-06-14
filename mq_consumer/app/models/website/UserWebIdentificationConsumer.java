package models.website;

import models.consumer.UserWebIdentification;
import models.consumer.UserWebIdentificationData;
import models.mq.RabbitMQConsumerWithTx;
import play.Logger;
import play.jobs.OnApplicationStart;

/**
 * 已通过QueueId保证只处理一次QueueMessage.
 */
@OnApplicationStart(async = true)
public class UserWebIdentificationConsumer extends RabbitMQConsumerWithTx<UserWebIdentificationData> {

	@Override
	public void consumeWithTx(UserWebIdentificationData uwiMsg) {
		UserWebIdentification wui = UserWebIdentification.findOne(uwiMsg.cookieId);
		if (wui == null) {
			wui = uwiMsg.toUserWebIdentification();
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
			} catch (Exception e) {
	            Logger.info("update UserWebIdentification failed, will roll back now.", e);
	        }
		} else {
			Logger.info("msg83841341:UserWebIdentification（cookie:" + uwiMsg.cookieId + ")已经被其它进程保存");
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
