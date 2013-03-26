package models.sina;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import models.order.ECoupon;
import models.sales.ResalerProduct;
import models.sales.ResalerProductJournal;
import play.Logger;
import play.jobs.OnApplicationStart;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;

import java.util.HashMap;
import java.util.Map;

/**
 * User: yan
 * Date: 13-3-26
 * Time: 下午1:50
 */
@OnApplicationStart(async = true)
public class SinaVouchersConsumer extends RabbitMQConsumer<Long> {
    @Override
    protected void consume(Long couponId) {
        ECoupon coupon = ECoupon.findById(couponId);
        if (coupon == null) {
            return;
        }
        if (coupon.synced) {
            Logger.info("this coupon have been synced! coupon : %s", coupon.eCouponSn);
            return;
        }
        SinaVoucherResponse response = createVouchers(coupon);
        //生成卡券信息
        if (response.isOk()) {
            coupon.partnerCouponId = response.content.getAsJsonObject().get("id").getAsString();
            coupon.synced = true;
            coupon.save();
        }

    }

    /**
     * 创建卡券
     *
     * @param coupon
     * @return
     */
    private SinaVoucherResponse createVouchers(ECoupon coupon) {
        Map<String, String> requestParams = new HashMap<>();
        ResalerProduct resalerProduct = ResalerProduct.findById(coupon.orderItems.outerGoodsNo);
        ResalerProductJournal journal = ResalerProductJournal.find("product = ? order by createdAt desc", resalerProduct).first();
        Map<String, String> journalMap = new Gson().fromJson(journal.jsonData,new TypeToken<Map<String, String>>(){}.getType());
        String type = journalMap.get("type");
        requestParams.put("type_template_id", coupon.orderItems.outerGoodsNo);
        requestParams.put("belong_coop_id", type);
        if ("2".equals(type)) {//会员卡
            requestParams.put("user_name", "");
            requestParams.put("user_level", "");
            requestParams.put("point_balance", "");
        }
        requestParams.put("coop_id", coupon.eCouponSn);
        requestParams.put("code", coupon.eCouponSn);
        requestParams.put("uid", coupon.order.getUser().openId);
        return SinaVoucherUtil.uploadVoucher(new Gson().toJson(requestParams));
    }

    @Override
    protected Class getMessageType() {
        return Long.class;
    }

    @Override
    protected String queue() {
        return SinaVouchersMessageUtil.QUEUE_NAME;
    }
}
