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
public class KtvSkuConsumer extends RabbitMQConsumerWithTx<Long> {
    @Override
    public void consumeWithTx(Long scheduledId) {
        KtvTaobaoUtil.updateTaobaoSkuByPriceSchedule(scheduledId);
    }

    @Override
    protected Class getMessageType() {
        return Long.class;
    }

    @Override
    protected String queue() {
        return KtvSkuMessageUtil.QUEUE_NAME;
    }
}
