package models.taobao;

import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.ItemSkuAddRequest;
import com.taobao.api.request.ItemSkuDeleteRequest;
import com.taobao.api.request.ItemSkuUpdateRequest;
import com.taobao.api.response.ItemSkuAddResponse;
import com.taobao.api.response.ItemSkuDeleteResponse;
import com.taobao.api.response.ItemSkuUpdateResponse;
import com.uhuila.common.constants.DeletedStatus;
import models.RabbitMQConsumerWithTx;
import models.accounts.AccountType;
import models.ktv.*;
import models.oauth.OAuthToken;
import models.oauth.WebSite;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.sales.ResalerProduct;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.jobs.OnApplicationStart;

import javax.persistence.Query;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: yan
 * Date: 13-5-6
 * Time: 下午3:18
 */
@OnApplicationStart(async = true)
public class KtvSkuConsumer extends RabbitMQConsumerWithTx<KtvSkuMessage> {
    @Override
    public void consumeWithTx(KtvSkuMessage message) {
        //根据价格策略更新sku
        if (message.ktvProductGoodsId == null && message.scheduledId != null) {
            KtvTaobaoUtil.updateTaobaoSkuByPriceSchedule(message.scheduledId);
        } else {
            //根据ktv产品更新sku
            KtvProductGoods ktvProductGoods = KtvProductGoods.findById(message.ktvProductGoodsId);
            if (ktvProductGoods != null) {
                KtvTaobaoUtil.updateTaobaoSkuByProductGoods(ktvProductGoods);
            }
        }
    }

    @Override
    protected Class getMessageType() {
        return KtvSkuMessage.class;
    }

    @Override
    protected String queue() {
        return KtvSkuMessageUtil.QUEUE_NAME;
    }
}
